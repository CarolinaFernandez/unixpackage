#!/bin/bash

# More: http://www.cs.rug.nl/~jurjen/DebPackaging/dh_make_and_dpkg_buildpackage.html
# Other: http://people.debian.org/~jaldhar/make_package1.html
# Other: http://www.webupd8.org/2010/01/how-to-create-deb-package-ubuntu-debian.html
# Other: http://www.debian.org/doc/manuals/maint-guide/dreq.en.html
# Other: http://www.debian.org/doc/manuals/maint-guide/dother.en.html


# *NOTE*
# [1] Name "packagename_x.y" after your package and version
# [2] Name "packagename" after your package too and put all the necessary code inside


########################################
# METADATA LOCATED UNDER DEBIAN/
#

default_source_path=`ls -l --time-style="long-iso" | egrep '^d' | awk '{print $8}' | grep -v "create_deb*" | head -2 | tail -1`

source_path=""
name=""
email=""
license=""
package_name=""
package_version=""
package_class=""
debian_files_location=""
create_gpg_key=false
sign_package=false

# Default: interactive. May be disabled via parameters
interactive=true
# Required parameters for simple/manual executions
# In advanced mode, pre-configured debian files are retrieved from another location
required_arguments_dh=('$package_name' '$package_version' '$package_class' '$name' '$email' '$license' '$source_path')
required_arguments_sign=('$name' '$email')

native_package=""
gpg_key_exists=""

########################################

## Obtains root folder
#path=${PWD##/*/}
base_path=${PWD}
# Get path where the script is running
root_script=$(cd $(dirname $0); pwd -P; cd $base_path)
root_package=$(readlink -e $root_script/../../packages/deb)
path_to_script=$root_script

current_date=$(date +"%Y-%m-%d_%H-%M-%S")
# Output and package locations (not yet existing)
path_to_package=$root_script/${source_path}_${package_version}
output_path=$(readlink -m $root_script/../../../unix_package_output__${current_date})

# Helps installing dependencies
dpkg_dependencies=('dh-make' 'lintian')

# Aux functions
function breakline()
{
    i=$1
    while [[ $i -gt 0 ]]; do
        i=$((i-1))
        echo ""
    done
}

function install_dependencies()
{
  # Install this package prior to sign any package
  if [[ $sign_package == true ]]; then
    dpkg_dependencies=('${dpkg_dependencies[@]}' 'rng-tools')
    #apt-get install -y rng-tools
  fi
  for dpkg_dependency in ${dpkg_dependencies[@]}; do
    dependency_exists=$(dpkg -l | grep "$dpkg_dependency")
    # If package does not exist OR it is not properly installed, do install it
    if [[ -z $dependency_exists || $dependency_exists != *ii* ]]; then
      apt-get install -y $dpkg_dependency
    fi
  done
}

function parse_arguments()
{
  path_to_script=$(dirname $0)
  # Not every argument contains data (sometimes, one is enough to parse)
  while [[ $# > 0 ]]; do
    key="$1"
    shift

    case $key in
    -y|--yes)
        interactive=false
        ;;
    -s|--source)
        source_path="$1"
        shift
        ;;
    -d|--debianfiles)
        # Only one path allowed
        debian_files_location="$1"
        shift
        ;;
    -n|--name)
        # Read first argument w/o setting spaces on it
        name="$1"
        shift
        while [[ $1 != -* ]]; do
          name="$name $1"
          shift
        done
        ;;
    -e|--email)
        email="$1"
        shift
        ;;
    -l|--license)
        license="$1"
        shift
        ;;
    -C|--class)
        package_class="$1"
        shift
        ;;
    -p|--packagename)
        package_name="$1"
        shift
        ;;
    -v|--version)
        package_version="$1"
        shift
        ;;
    -S|--sign)
        sign_package=true
        create_gpg_key=true
        ;;
    *)
        ;;
    esac
  done
}


# Validate input to script
function validate_parameters()
{
  # No file location for debian means it is simple/manual mode
  # and thus no further information is needed
  if [[ -z $debian_files_location ]]; then
    for required_argument_dh in ${required_arguments_dh[@]}; do
      if [[ -z $(eval echo $required_argument_dh) ]]; then
        error "E: Argument '$required_argument_dh' is required for dh-make"
      fi
    done
  fi
  # When signing the package, though, name and e-mail are required
  if [[ $sign_package == true ]]; then
    for required_argument_sign in ${required_arguments_sign[@]}; do
      if [[ -z $(eval echo $required_argument_sign) ]]; then
        error "E: Argument '$required_argument_sign' is required for signing the package"
      fi
    done
  fi
}

# Exit on failure
function error()
{
    echo $1
    clean_debianized || echo "Could not remove temporary files for 'debian'ization"
    exit
}

function clean_debianized()
{
  breakline 1
  echo "> Cleaning the house..."
  if [[ $interactive == true ]]; then
    while true; do
      read -p "> Do you want to remove temporary files created for the 'debian'ization?: " yn
      case $yn in
        [Yy]* ) do_clean_debianized
                break;;
        [Nn]* ) break;;
        * ) echo "Please answer 'Y' or 'N'.";;
      esac
    done
  else
    do_clean_debianized
  fi
  breakline 1
}

function do_clean_debianized()
{
  if [[ -d $root_script/create_deb ]]; then
    rm -r $root_script/create_deb;
  fi
  if [[ -d $path_to_package/debian ]]; then
    rm -r $path_to_package/debian;
  fi
  if [[ -f $path_to_package.orig.tar.gz ]]; then
    rm $path_to_package.orig.tar.gz;
  fi
  if [[ -f $path_to_package*.dsc ]]; then
    rm $path_to_package*.dsc;
  fi
  if [[ -f build-stamp ]]; then
    rm build-stamp;
  fi
}

function create_sources_folder()
{
  # If a source for debian files is passed, take into account for the directory
#  if [[ ! -z $debian_files_location ]]; then
#    path_to_package=$root_script/unix_package__${current_date}
#  fi
  if [[ ! -d $path_to_package ]]; then
    mkdir -p $path_to_package
  fi
}

function copy_samples()
{
  if [[ ! -d $root_script/create_deb/debian ]]; then
    mkdir -p $root_script/create_deb/debian
  fi
  cp -Rp $root_package/* $root_script/create_deb/debian/ || error "Could not copy sample files into $root_script/create_deb/debian/";
}

function do_copy_samples()
{
  if [[ -d $root_script/create_deb/debian ]]; then
    rm -r $root_script/create_deb/debian || error "Could not remove automatically generated files under $root_script/create_deb/debian/";
  fi;
  copy_samples;
}

function copy_edit_samples()
{
  if [[ $interactive == true ]]; then
    while true; do
      read -p "> Do you want to edit the medatada (press 'y') or to use sample files (press 'n')?: " meta
      case $meta in
        [Yy]* ) nautilus $root_script/create_deb/debian || error "Could not open debian/ folder. Try to get there by yourself";
                while true; do
                  read -p "> Get some time to modify the metadata (files under $root_script/create_deb/debian) and press 'y' when finished: " mod;
                  case $mod in
                            [Yy]* ) break;;
                            * ) echo "Please answer 'Y' when finished.";;
                        esac;
                    done;
                    break;;
            [Nn]* ) do_copy_samples;
                    break;;
            * ) echo "Please answer 'Y' or 'N'.";;
        esac
    done
  else
    if [[ ! -z $debian_files_location ]]; then
      do_copy_user_samples
    else
      do_copy_samples
      # Symlink so as to dh_install finds the source at fallback
      ln -s  $root_script/$source_path $root_script/create_deb/debian/$source_path || error "Could not link the $source_path code from debian/"
    fi
  fi
}

function do_copy_user_samples()
{
  if [[ -d $debian_files_location ]]; then
    # Remove pre-generated files under the script folder
    if [[ -d $path_to_package/debian ]]; then
      rm -r $path_to_package/debian/*
    else
      mkdir -p $path_to_package/debian
    fi
    # And use the user samples for it
    cp -Rp $debian_files_location/* $path_to_package/debian/ || error "Could not copy user's sample files into $path_to_package/debian/"
  fi
}

function move_to_output()
{
  if [[ ! -d $output_path ]]; then
    mkdir -p $output_path
  fi
  mv $root_script/*.deb $output_path/
  mv $root_script/*.dsc $output_path/
  mv $root_script/*.changes $output_path/
  echo ">> The output is located under $output_path/"
}

function perform_dh_make()
{
  echo "> Generating ${source_path}_${package_version}.tar.gz..."
  tar --ignore-failed-read -pczf ${source_path}_${package_version}.tar.gz ${source_path}_${package_version} --exclude='create_deb' || echo "Could not create ${source_path}_${package_version}.tar.gz"

  # Export DEBFULLNAME environment variable to set the maintainer name
  export DEBFULLNAME=$name
  # Use "yes" to run non-interactively
  cd ${source_path}_${package_version}
  # Use native (-n) to avoid having a revision appended to the version
  if [[ $package_version != *"-"* ]]; then
    native_package="-n"
  fi
  dh_make_params="--yes $native_package -$package_class -c $license -e $email -p ${source_path}_${package_version} -f $path_to_package.tar.gz"

  /usr/bin/dh_make --yes $dh_make_params || error "Could not dh_make in $source_path with ${source_path}_${package_version}.tar.gz"
  rm $path_to_package.tar.gz || error "Could not delete ${source_path}_${package_version}.tar.gz"
}

breakline 1
echo "  ********** DEBIAN GENERATOR: START **********"
cd $path_to_script

breakline 1
echo "> Fetching parameters..."
parse_arguments $@

breakline 1
echo "> Validating parameters..."
validate_parameters

breakline 1
echo "> Installing dependencies..."
install_dependencies

breakline 1
echo "> Creating sources..."
create_sources_folder

breakline 1
if [[ $interactive == true ]]; then 
  echo "*NOTE* If you want to SIGN this package please see that the name and e-mail you use are already part of one of your GPG signatures. If this were not the case you will have the chance to generate it later."
fi

create_dir=$PWD

breakline 2
# Generating .tar.gz file (for typical installations)
if [[ -z $debian_files_location ]]; then
  perform_dh_make
fi

breakline 2
echo "> Cleaning & copying sample files under debian/ ..."
#copy_samples
#cd  $root_script/create_deb/debian || error "Could not remove non-relevant files under $root_script/create_deb/debian"
#ls | rm -r `awk '{ if ($i != "changelog" && $i != "control" && $i != "copyright" && $i != "install" && $i != "README.Debian" && $i != "README.source" && $i != "rules") { print $i;} }'` || error "Could not remove non-relevant files under $root_script/create_deb/debian"
#cd $root_script/create_deb || error "Could not remove non-relevant files under $root_script/create_deb/debian/"
copy_edit_samples

breakline 2
echo "> Checking keys for package signing..."
if [[ $interactive == true ]]; then
  read -p "> Do you want to sign the package? (y/n) [y]: " sign_package
  case $sign_package in
    [Yy]* ) gpg_key_exists=$(/usr/bin/gpg --list-keys | grep "$name" | grep "$email")
            if [[ -z $gpg_key_exists ]]; then
                breakline 2
                while [[ ! $create_gpg_key ]]; do
                    read -p "> Do you want to create a GPG key to sign the package? (y/n) [y]: " create_gpg_key
                    case $create_gpg_key in
                        [Yy]* ) /usr/bin/gpg --gen-key;
                                break;;
                        [Nn]* ) sign_package=false;
                                break;;
                            * ) echo "Please answer 'Y' or 'N'.";;
                    esac
                done
            fi;
            break;;
         *) sign_package=false;
#            break;;
  esac
else
  if [[ $sign_package == true ]]; then
    test -f /etc/init.d/rng-tools && sudo /etc/init.d/rng-tools start
    gpg_key_exists=$(/usr/bin/gpg --list-keys | grep "$name" | grep "$email")
    # Look for key. If it does not exist, create
    if [[ -z $gpg_key_exists ]]; then
      breakline 2
      echo ">> Generating key for $name <$email>..."
      gpg --batch --gen-key <<EOF
        Key-Type: DSA
        Key-Length: 2048
        Subkey-Type: ELG-E
        Subkey-Length: 2048
        Name-Real: $name
        # The following forbids literal string matching. Avoiding!
        #Name-Comment: used for unixpackage
        Name-Email: $email
        #Passphrase: unixpackage
EOF
      test -f /etc/init.d/rng-tools && sudo /etc/init.d/rng-tools stop
      #gpg --list-keys
    fi
  fi
fi

breakline 2
# Move to proper location for dpkg-buildpackage
cd $path_to_package
export DH_COMPAT=5
if [[ $sign_package == true ]]; then
    echo "> Performing (signed) dpkg-buildpackage..."
    /usr/bin/dpkg-buildpackage -F || error "Could not dpkg-buildpackage (signed) on $path_to_package"
else
    echo "> Performing (unsigned) dpkg-buildpackage..."
    /usr/bin/dpkg-buildpackage -F -us -uc || error "Could not dpkg-buildpackage (unsigned) on $path_to_package"
fi

breakline 1
generated_deb_file_location=$(find $root_script/ -name "*.deb" | head -1)
generated_deb_file_name=$(basename $generated_deb_file_location)
generated_deb_desc_location=$(find $root_script/ -name "*.dsc" | head -1)
echo "> Review final info for package $generated_deb_file_name..."
/usr/bin/dpkg --info $generated_deb_file_location || error "Could not show info for debian package"

breakline 1
echo "> Please check the correctness of the package..."
/usr/bin/lintian $generated_deb_file_location #|| error "Could not show info about the correctness of the .deb file"
/usr/bin/lintian $generated_deb_desc_location #|| error "Coult not show info about the correctness of the .dsc file"

breakline 1
echo "> Ending package generation..."
move_to_output

# Cleaning temporary files
clean_debianized

breakline 1
echo "  ********** DEBIAN GENERATOR: END **********"
