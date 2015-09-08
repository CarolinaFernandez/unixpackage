#!/bin/bash

# More: http://www.cs.rug.nl/~jurjen/DebPackaging/dh_make_and_dpkg_buildpackage.html
# Other: http://people.debian.org/~jaldhar/make_package1.html
# Other: http://www.webupd8.org/2010/01/how-to-create-deb-package-ubuntu-debian.html
# Other: http://www.debian.org/doc/manuals/maint-guide/dreq.en.html
# Other: http://www.debian.org/doc/manuals/maint-guide/dother.en.html


# *NOTE*
# [1] Name "packagename_x.y" after your package and version
# [2] Name "packagename" after your package too and put all the necessary code inside

########### Load variables
# Arguments
name=""
email=""
copyright=""
package_name=""
package_description=""
package_short_description=""
package_version=""
package_website=""
package_section=""
package_priority=""
package_class=""
debian_files_location=""
sign_package=false
# Default: interactive. May be disabled via parameters
interactive=true
# If true, prints output messages
verbose=false

# If true, generates Debian sources only (avoid Debian package)
no_build=false
# If true, generates Debian packages only (skip Debian sources)
build=false

# Regular expressions to validate
package_name_re='^[a-z]{2,}([a-z]|[0-9]|[+]|[-]|[.])*?$'
package_website_re='^([a-z]*[://])?([a-zA-Z0-9]|[-_./#?&%$=])*?$'
package_version_re='^[0-9]+([.][0-9]+)(-[0-9]+)?$'


# Required parameters for simple/manual executions
# In advanced mode, pre-configured debian files are retrieved from another location
required_arguments_dh=('$package_name' '$package_version' '$package_class' '$name' '$email' '$copyright')
required_arguments_sign=('$name' '$email')
# Helps installing dependencies
dpkg_dependencies=('dh-make' 'lintian' 'gzip' 'xdg-utils')


# Internal variables
native_package=""
gpg_key_exists=""
create_gpg_key=false
dhmake_help=$(dh_make -h)

# Paths
#path=${PWD##/*/}
base_path=${PWD}
# Get path where the script is running
root_script=$(cd $(dirname $0); pwd -P; cd $base_path)
root_package=$(readlink -e $root_script/../../packages/deb)
path_to_script=$root_script
current_date=$(date +"%Y-%m-%d_%H-%M-%S")
# Output and package locations (not yet existing, to be defined later)
path_to_package=""
output_path=""


# Aux functions
function breakline()
{
  i=$1
  while [[ $i -gt 0 ]]; do
    i=$((i-1))
    echo_v ""
  done
}

function echo_v()
{
  if [[ $verbose == true ]]; then
  echo $@
  fi
}

function install_dependencies()
{
  # Install this package prior to sign any package
  if [[ $sign_package == true ]]; then
    dpkg_dependencies=(${dpkg_dependencies[@]} 'rng-tools')
  fi
  # Install this package prior to rsync any folder
  if [[ ! -z $debian_files_location ]]; then
    dpkg_dependencies=(${dpkg_dependencies[@]} 'rsync')
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
  -b|--batch)
    interactive=false
    ;;
  -c|--copyright)
    copyright="$1"
    shift
    ;;
  -C|--class)
    package_class="$1"
    shift
    ;;
  -d|--description-short)
    # Read first argument w/o setting spaces on it
    package_short_description="$1"
    shift
    while [[ $1 != -* ]]; do
      package_short_description="$package_short_description $1"
      shift
    done
    ;;
  -D|--description-long)
    # Read first argument w/o setting spaces on it
    package_description="$1"
    shift
    while [[ $1 != -* ]]; do
      package_description="$package_description $1"
      shift
    done
    ;;
  -e|--email)
    email="$1"
    shift
    ;;
  -f|--files)
    files="$1"
    shift
    # Allow placing files as middle or final argument
    while [[ $1 != -* && ! -z $1 ]]; do
      files="$files $1"
      shift
    done
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
  -m|--no-build)
    no_build=true;
    ;;
  -M|--build)
    build=true;
    ;;
  -p|--package-name)
    package_name="$1"
    shift
    ;;
  -P|--priority)
    package_priority="$1"
    shift
    ;;
  -s|--section)
    package_section="$1"
    shift
    ;;
  -S|--sign)
    sign_package=true
    create_gpg_key=true
    ;;
  -t|--templates)
    # Only one path allowed
    debian_files_location="$1"
    debian_files_location_root=$debian_files_location
    shift
    ;;
  -v|--verbose)
    verbose=true
    ;;
  -V|--package-version)
    package_version="$1"
    shift
    ;;
  -w|--website)
    # Only one URL allowed
    package_website="$1"
    shift
    ;;
  *)
    ;;
  esac
  done
}

# Validate input to script
function validate_parameters()
{
  # Recompute output location
  path_to_package=$root_script/${package_name}_${package_version}
  output_path=$(readlink -m $root_script/../../../unix_package_output__${current_date})
  
  # No file location for debian means it is simple/manual mode
  # and thus no further information is needed
  if [[ -z $debian_files_location ]]; then
    for required_argument_dh in ${required_arguments_dh[@]}; do
      if [[ -z $(eval echo $required_argument_dh) ]]; then
        error "E: Argument '$required_argument_dh' is required for dh-make"
      fi
    done
  # Otherwise, it is advanced mode (templates are used)
  else
    # Copy the debian folder to a temporal, appropriate location
    if [[ -d $debian_files_location ]]; then
      mkdir -p $path_to_package/debian_templates/
      cp -Rp $debian_files_location/* $path_to_package/debian_templates/
      debian_files_location=$path_to_package/debian_templates/debian
    fi
    cat $debian_files_location/debian/control
    # Correction: use parent folder if the user chose the debian folder itself
    if [[ $(basename $debian_files_location) =~ ^debian.* ]]; then
      debian_files_location_root="$(dirname "$debian_files_location")"
    fi
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

# Validate existing package name
function validate_package_name()
{
  if ! [[ $package_name =~ $package_name_re ]]; then
  echo_v "E: Package name '$package_name' is not correct"
  fi
}

# Validate correct format of package website
function validate_package_website()
{
  if ! [[ $package_website =~ $package_website_re ]]; then
  echo_v "E: Package name '$package_website' is not correct"
  fi
}

# Validate correct format of package version
function validate_package_version()
{
  if ! [[ $package_version =~ $package_version_re ]]; then
  error "E: Package version '$package_version' is not correct"
  fi
}

# Exit on failure
function error()
{
  echo_v $1
  clean_debianized || echo_v "Could not remove temporary files for 'debian'ization"
  exit
}

function clean_debianized()
{
  breakline 1
  echo_v "> Cleaning the house..."
  if [[ $interactive == true ]]; then
  while true; do
    read -p "> Do you want to remove temporary files created for the 'debian'ization?: " yn
    case $yn in
    [Yy]* ) do_clean_debianized; do_clean_dh_make;
        break;;
    [Nn]* ) break;;
    * ) echo_v "Please answer 'Y' or 'N'.";;
    esac
  done
  else
  do_clean_debianized
  fi
  breakline 1
}

function do_clean_debianized()
{
  if [[ -f $path_to_package.orig.tar.gz ]]; then
    rm -f $path_to_package.orig.tar.gz;
  fi
  if [[ -f $path_to_package*.dsc ]]; then
    rm -f $path_to_package*.dsc;
  fi
  if [[ -f build-stamp ]]; then
    rm -f build-stamp;
  fi
}

function do_clean_dh_make()
{
  find $path_to_package -name "*.tar.gz" -exec rm -f "{}" \;
  if [[ -d $path_to_package/debian ]]; then
    rm -rf $path_to_package/debian
  fi
  if [[ -d $outputh_path ]]; then
    rm -rf $outputh_path
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
  echo_v ">> The output is located under $output_path/"
}

function place_user_files_in_package()
{
  # Place source files in the corresponding places
  IFS=' ' read -ra ADDR <<< "$files"
  for file_tuple in "${ADDR[@]}"; do
    file_tuple=(${file_tuple//:/ })
    source_path=${file_tuple[0]}
    source_file=$(basename $source_path)
    destination_path=${file_tuple[1]}
    destination_path_tmp=${destination_path:1:${#destination_path}}
    destination_path_tmp_dir=$(dirname ${destination_path:1:${#destination_path}})
    mkdir -p $path_to_package/debian/contents/$destination_path_tmp_dir
    # If source is a folder, copy recursively into the parent's folder
    if [ -d $source_path ]; then
      cp -Rp $source_path/ $path_to_package/debian/contents/$destination_path_tmp
    # Otherwise, perform a normal copy with the given folder
    else
      cp -p $source_path $path_to_package/debian/contents/$destination_path_tmp
    fi
    # If source is an application, add to a different file ("include-binaries")
    if [[ $(file $source_path --mime) =~ .*application.* ]]; then
      if [ ! -d $path_to_package/debian/source ]; then
        mkdir $path_to_package/debian/source
      fi
      if [ ! -f $path_to_package/debian/source/include-binaries ]; then
        touch $path_to_package/debian/source/include-binaries
      fi
      echo "debian/contents/$destination_path_tmp" >> $path_to_package/debian/source/include-binaries
    fi
    if [ ! -f $path_to_package/debian/install ]; then
      touch $path_to_package/debian/install
    fi
    echo "debian/contents/$destination_path_tmp $destination_path_tmp_dir/" >> $path_to_package/debian/install
  done
}

function perform_dh_make()
{  
  echo_v "> Generating ${package_name}_${package_version}.tar.gz..."
  tar --ignore-failed-read -pczf ${package_name}_${package_version}.tar.gz ${package_name}_${package_version} --exclude='create_deb' || echo_v "Could not create ${package_name}_${package_version}.tar.gz"

  cd $path_to_package

  # Export DEBFULLNAME environment variable to set the maintainer name
  export DEBFULLNAME=$name

  # Use native (-n) to avoid having a revision appended to the version
  if [[ $package_version != *"-"* ]]; then
    native_package="-n"
  fi

  # Enforce package basic details
  validate_package_name
  validate_package_website # Optional
  validate_package_version

  # Common parameters
  # Use "yes" to run in batch mode
  dh_make_pre=""
  # Verify that the "--yes" parameter is available for curent dh_make
  test "${dhmake_help#*"--yes"}" != "$dhmake_help" && dh_make_params="--yes" || dh_make_pre="yes | "
  dh_make_params="$dh_make_params $native_package -$package_class -c $copyright -e $email -p ${package_name}_${package_version}"
  dh_make_params="$dh_make_params -f $path_to_package.tar.gz"
  # Verify again that the "--yes" parameter can be used
  if [ -z $dh_make_pre ]; then
    echo_v "$dh_make_pre /usr/bin/dh_make $dh_make_params"
    dh_make $dh_make_params || error "Could not dh_make with ${package_name}_${package_version}.tar.gz"
  else
    echo_v "yes | $dh_make_pre /usr/bin/dh_make $dh_make_params"
    yes | dh_make $dh_make_params || error "Could not dh_make with ${package_name}_${package_version}.tar.gz"
  fi
  #dh_installman || error "Could not dh_installman for ${package_name}_${package_version} package"

  rm $path_to_package.tar.gz || error "Could not delete ${package_name}_${package_version}.tar.gz"
  echo_v "ls -la $PWD/debian (dh-make)"
  ls -la $PWD/debian
}

breakline 1
echo_v "  ********** DEBIAN GENERATOR: START **********"
cd $path_to_script

breakline 1
echo_v "> Fetching parameters..."
parse_arguments $@

breakline 1
echo_v "> Validating parameters..."
validate_parameters

# Script can be run without passing the templates (rather uninteresting, though)
if [[ -f $debian_files_location/control ]]; then
  if [[ -z $package_name ]]; then
    package_name=$(grep --only-matching --perl-regex "(?<=^Package: ).*" $debian_files_location/control)
  fi
  if [[ -z $package_version ]]; then
    package_version=$(grep --only-matching --perl-regex "(?<=^Version: ).*" $debian_files_location/control)
  fi
#else
#  error "E: file $debian_files_location/control is needed when using templates. Consider passing the template parameter"
fi

breakline 1
echo_v "> Installing dependencies..."
install_dependencies

#breakline 1
#echo_v "> Cleaning previous files (if any)..."
#do_clean_dh_make

if [[ $no_build == true || $build == false ]]; then
#  breakline 1
#  echo_v "> Cleaning previous files (if any)..."
#  do_clean_dh_make
  
  breakline 1
  echo_v "> Creating sources..."
  #create_debian_folder
  mkdir -p $path_to_package  
  breakline 1
  if [[ $interactive == true ]]; then 
    echo_v "*NOTE* If you want to SIGN this package please see that the name and e-mail you use are already part of one of your GPG signatures. If this were not the case you will have the chance to generate it later."
  fi
  
  create_dir=$PWD
  
  breakline 2
  perform_dh_make
  
  # Replace with RegEx
  if [[ -f $path_to_package/debian/control ]]; then
    # Using different separator (URL usually has slashes)
    if [[ ! -z $package_short_description ]]; then
      sed -i "s}<insert up to 60 chars description>}$package_short_description}" $path_to_package/debian/control
    fi
    if [[ ! -z $package_description ]]; then
      sed -i "s}<insert long description, indented with spaces>}$package_description}" $path_to_package/debian/control
    fi
    if [[ ! -z $package_website ]]; then
      sed -i "s}<insert the upstream URL, if relevant>}$package_website}" $path_to_package/debian/control
    fi
    if [[ ! -z $package_section ]]; then
      sed -i "s}^\(Section: *\).*\$}\1$package_section}" $path_to_package/debian/control
    fi
    if [[ ! -z $package_priority ]]; then
      sed -i "s}^\(Priority: *\).*\$}\1$package_priority}" $path_to_package/debian/control
    fi
  else
    error "E: file $path_to_package/debian/control is needed when using templates"
  fi

  cat $path_to_package/debian/control
  
  if [[ ! -z $debian_files_location ]]; then
    rsync -av $debian_files_location/* $path_to_package/debian/
    cat $debian_files_location/control
  fi
fi

# Keep interesting files
if [[ -f $path_to_package/debian/$package_name.default.ex ]]; then
  mv $path_to_package/debian/$package_name.default.ex $path_to_package/debian/$package_name.default
fi

#if [[ $no_build == false || $build == true ]]; then
# Clean sample files on destination (when not performing just the dh_make process)
rm -f $path_to_package/debian/*.ex
#fi

if [[ $no_build == true || $build == false ]]; then
  breakline 2
  echo_v "> Checking keys for package signing..."
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
              * ) echo_v "Please answer 'Y' or 'N'.";;
            esac
          done
        fi;
        break;;
       *) sign_package=false;
#        break;;
    esac
  else
    if [[ $sign_package == true ]]; then
      test -f /etc/init.d/rng-tools && sudo /etc/init.d/rng-tools start
      gpg_key_exists=$(/usr/bin/gpg --list-keys | grep "$name" | grep "$email")
      # Look for key. If it does not exist, create
      if [[ -z $gpg_key_exists ]]; then
        breakline 2
        echo_v ">> Generating key for $name <$email>..."
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
fi

if [[ $no_build == true ]]; then
  exit 0
fi

breakline 2
# Move to proper location for dpkg-buildpackage
cd $path_to_package
place_user_files_in_package

export DH_COMPAT=5

if [[ $sign_package == true ]]; then
  echo_v "> Performing (signed) dpkg-buildpackage..."
  #/usr/bin/dpkg-buildpackage -F || error "Could not dpkg-buildpackage (signed) on $path_to_package"
  /usr/bin/dpkg-buildpackage -F --source-option=--include-binaries || error "Could not dpkg-buildpackage (signed) on $path_to_package"
else
  echo_v "> Performing (unsigned) dpkg-buildpackage..."
  #/usr/bin/dpkg-buildpackage -F -us -uc || error "Could not dpkg-buildpackage (unsigned) on $path_to_package"
  /usr/bin/dpkg-buildpackage -F -us -uc --source-option=--include-binaries || error "Could not dpkg-buildpackage (unsigned) on $path_to_package"
fi

breakline 1
generated_deb_file_location=$(find $root_script/ -name "*.deb" | head -1)
generated_deb_file_name=$(basename $generated_deb_file_location)
generated_deb_desc_location=$(find $root_script/ -name "*.dsc" | head -1)
echo_v "> Review final info for package $generated_deb_file_name..."
/usr/bin/dpkg --info $generated_deb_file_location || error "Could not show info for debian package"

breakline 1
echo_v "> Please check the correctness of the package..."
/usr/bin/lintian $generated_deb_file_location #|| error "Could not show info about the correctness of the .deb file"
/usr/bin/lintian $generated_deb_desc_location #|| error "Coult not show info about the correctness of the .dsc file"

if [[ ! -z $debian_files_location ]]; then
  breakline 1
  echo_v "*NOTE* In case the Debian package was not properly generated, consider checking the \"install\" file at the directory $debian_files_location, and ensure the paths are relative to its parent folder."
fi

breakline 1
echo_v "> Ending package generation..."
move_to_output

# Cleaning temporary files
clean_debianized

breakline 1
echo_v "  ********** DEBIAN GENERATOR: END **********"
