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
default_name="Carolina Fernandez"
default_email="carolina.fernandez@i2cat.net"
default_license="lgpl3"
default_package_name="test-deb"
package_version="0.1"
default_package_class="i"
default_create_gpg_key=true
default_sign_package=true

#source_path=$1
##source_path={$1:=$default_source_path}
#name=$2
#email=$3
#license=$4
#package_name=$5
#package_version=$6
#package_class=$7
#create_gpg_key=$8
#sign_package=$9

source_path=""
name=""
email=""
license=""
package_name=""
package_version=""
package_class=""
create_gpg_key=""
sign_package=""

# Default: interactive. May be disabled via parameters
interactive=true
required_arguments=($package_name $package_version $package_class $name $email $license $source_path)
native_package=""

########################################

## Obtains root folder
#path=${PWD##/*/}
root_script=${PWD}
root_package=$root_script/../../package/deb
path_to_script="."
output_path=$root_script/../../../unix_package_output__$(date +"%Y-%m-%d_%H-%M-%S")
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
  for dpkg_dependency in ${dpkg_dependencies[@]}; do
    dependency_exists=$(dpkg -l | grep "$dpkg_dependency")
    if [[ -z $dependency_exists ]]; then
      apt-get install -y $dpkg_dependency
    fi
  done
}

function parse_arguments()
{
  path_to_script=$(dirname $0)
  while [[ $# > 1 ]]; do
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

# Exit on failure
# XXX FIXME
function validate_parameters()
{
  for required_argument in ${required_arguments[@]}; do
    if [[ -z $required_argument ]]; then
      error "E: \$required_argument is undefined"
    fi
  done

#  if [[ -z $source_path ]]; then
#    error "E: source path is undefined"
#  fi
}

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
  if [[ -d $root_script/${source_path}_${package_version}/debian ]]; then
    rm -r $root_script/${source_path}_${package_version}/debian;
  fi
  if [[ -f $root_script/$path.orig.tar.gz ]]; then
    rm $root_script/$path.orig.tar.gz;
  fi
  if [[ -f $root_script/$path*.dsc ]]; then
    rm $root_script/$path*.dsc;
  fi
  if [[ -f build-stamp ]]; then
    rm build-stamp;
  fi
}

function create_sources_folder()
{
  if [[ ! -d $root_script/${source_path}_${package_version} ]]; then
    mkdir -p $root_script/${source_path}_${package_version}
  fi
}

function copy_samples()
{
  if [[ ! -d $root_script/create_deb/debian ]]; then
    mkdir -p $root_script/create_deb/debian
  fi
  cp -Rp $root_package/* $root_script/create_deb/debian/ || error "Could not copy sample files into $root_script/create_deb/debian/";
}

function copy_edit_samples()
{
  if [[ -f $root_package/install ]]; then
    cp -p $root_package/install $root_script/create_deb/debian/ || error "Could not copy debian/install file"
  fi
  if [[ -f $root_package/rules ]]; then
    cp -p $root_packages/rules $root_script/create_deb/debian/ || error "Could not copy debian/rules file"
  fi
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
    do_copy_samples
  fi
  # Symlink so as to dh_install finds the source at fallback
  ln -s  $root_script/$source_path $root_script/create_deb/debian/$source_path || error "Could not link the $source_path code from debian/"
}

function do_copy_samples()
{
  if [[ -d $root_script/create_deb/debian ]]; then
    rm -r $root_script/create_deb/debian || error "Could not remove automatically generated files under $path/debian/";
  fi;
  copy_samples;
}

function move_to_output()
{
  if [[ ! -d $output_path ]]; then
    mkdir -p $output_path
  fi
  mv $root_script/${source_path}_${package_version}* $output_path/
  echo "> The output is located under $output_path"
}


breakline 1
echo "  ********** DEBIAN GENERATOR: START **********"
cd $path_to_script

breakline 1
echo "> Installing dependencies..."
install_dependencies

breakline 1
echo "> Fetching parameters..."
parse_arguments $@

breakline 1
echo "> Validating parameters..."
validate_parameters

breakline 1
echo "> Creating sources..."
create_sources_folder

breakline 1
echo  "*NOTE* If you want to SIGN this package please see that the name and e-mail you use are already part of one of your GPG signatures. If this were not the case you will have the chance to generate it later."

create_dir=$PWD

breakline 2
echo "> Generating $path.tar.gz..."
tar --ignore-failed-read -pczf ${source_path}_${package_version}.tar.gz ${source_path}_${package_version} --exclude='create_deb' || echo "Could not create ${source_path}_${package_version}.tar.gz"

echo "> Performing dh_make -$package_class -e $email -c $license -p ${source_path}_${package_version} -f ${source_path}_${package_version}.tar.gz..."
# Export DEBFULLNAME environment variable to set the Maintainer name
export DEBFULLNAME=$name
# Use "yes" to run non-interactively
cd ${source_path}_${package_version}
# Use native (-n) to avoid having a revision appended to the version
if [[ $package_version == *"-"* ]]; then
  native_package="-n"
fi
/usr/bin/dh_make --yes $native_package -$package_class -e $email -c $license -p ${source_path}_${package_version} -f $root_script/${source_path}_${package_version}.tar.gz || error "Could not dh_make in $source_path with ${source_path}_${package_version}.tar.gz"
rm $root_script/${source_path}_${package_version}.tar.gz || error "Could not delete ${source_path}_${package_version}.tar.gz"

breakline 2
echo "> Cleaning & copying sample files under debian/ ..."
copy_samples
cd  $root_script/create_deb/debian || error "Could not remove non-relevant files under $path/debian/"
ls | rm -r `awk '{ if ($i != "changelog" && $i != "control" && $i != "copyright" && $i != "install" && $i != "README.Debian" && $i != "README.source" && $i != "rules") { print $i;} }'` || error "Could not remove non-relevant files under $path/debian/"
cd $root_script/create_deb || error "Could not remove non-relevant files under $root_script/create_deb/debian/"
copy_edit_samples

breakline 2
echo "> Checking keys for package signing..."
if [[ $interactive == true ]]; then
  read -p "> Do you want to sign the package? (y/n) [y]: " sign_package
  sign_package=${sign_package:-$default_sign_package}
  case $sign_package in
#    [Nn]* ) break;;
    [Yy]* ) gpg_key_exists=`/usr/bin/gpg --list-keys | grep "$name" | grep "$email"`
            if [[ $gpg_key_exists == "" ]]; then
                breakline 2
                while [[ $create_gpg_key == "" ]]; do
                    read -p "> Do you want to create a GPG key to sign the package? (y/n) [y]: " create_gpg_key
                    create_gpg_key=${create_gpg_key:-$default_create_gpg_key}
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
  # XXX DUPLICATED CODE, MOVE TO FUNCTION
  if [[ $sign_package == true ]]; then
    gpg_key_exists=`/usr/bin/gpg --list-keys | grep "$name" | grep "$email"`
    if [[ $gpg_key_exists == "" ]]; then
      breakline 2
      if [[ $create_gpg_key == true ]]; then
        /usr/bin/gpg --gen-key
      fi
    fi
  fi
fi

breakline 2
# Move to proper location for dpkg-buildpackage
cd $root_script/${source_path}_${package_version}
if [[ $sign_package == true ]]; then
    echo "> Performing (signed) dpkg-buildpackage -F..."
    /usr/bin/dpkg-buildpackage -F || error "Could not dpkg-buildpackage (signed) on $path"
else
    echo "> Performing (unsigned) dpkg-buildpackage -F..."
    echo "ls... "
    ls -la
    echo "pwd..."
    pwd
    /usr/bin/dpkg-buildpackage -F -us -uc || error "Could not dpkg-buildpackage (unsigned) on $path"
fi
#rm $root_script/$source_path.orig.tar.gz || error "Could not clean after dpkg-buildpackage"

breakline 1
echo "> Review final info for package ${source_path}_${package_version}.deb..."
/usr/bin/dpkg --info $root_script/${source_path}_${package_version}*.deb || error "Could not show info for debian package"

breakline 1
echo "> Please check the correctness of the package..."
#/usr/bin/lintian  $root_script/${source_path}_${package_version}*.deb || error "Could not show info about the correctness of the .deb file"
/usr/bin/lintian  $root_script/${source_path}_${package_version}*.deb
#/usr/bin/lintian  $root_script/${source_path}_${package_version}*.dsc || error "Coult not show info about the correctness of the .dsc file"
/usr/bin/lintian  $root_script/${source_path}_${package_version}*.dsc

clean_debianized

breakline 1
echo "> Ending package generation..."
move_to_output
#echo "Check the generated DEB package at $root_script/$source_path_$package_version.deb"

breakline 1
echo "  ********** DEBIAN GENERATOR: END **********"
