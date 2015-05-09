#!/bin/bash

# More: http://fedoraproject.org/wiki/How_to_create_an_RPM_package
# Other: http://linux.die.net/man/1/rpmlint


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
package_group=""
package_priority=""
package_class=""
rpm_files_location=""
sign_package=false
# Default: interactive. May be disabled via parameters
interactive=true
# If true, prints output messages
verbose=false

# If true, generates rpm sources only (avoid rpm package)
no_build=false
# If true, generates rpm packages only (skip rpm sources)
build=false

# Regular expressions to validate
package_name_re='^[a-z]{2,}([a-z]|[0-9]|[+]|[-]|[.])*?$'
package_website_re='^([a-z]*[://])?([a-zA-Z0-9]|[-_./#?&%$=])*?$'
package_version_re='^[0-9]+([.][0-9]+)(-[0-9]+)?$'


# Required parameters for simple/manual executions
# In advanced mode, pre-configured rpm files are retrieved from another location
required_arguments_dh=('$package_name' '$package_version' '$package_class' '$name' '$email' '$copyright')
required_arguments_sign=('$name' '$email')
# Helps installing dependencies
rpm_dependencies=('rpm-build' 'rpmlint' 'gzip')
# Common arguments passed to rpmbuild
rpmbuild_params="-ba"

# Internal variables
gpg_key_exists=""
create_gpg_key=false


# Paths
#path=${PWD##/*/}
base_path=${PWD}
# Get path where the script is running
root_script=$(cd $(dirname $0); pwd -P; cd $base_path)
root_package=$(readlink -e $root_script/../../packages/rpm)
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
    rpm_dependencies=(${rpm_dependencies[@]} 'rng-tools')
  fi
  # Install this package prior to rsync any folder
  if [[ ! -z $rpm_files_location ]]; then
    rpm_dependencies=(${rpm_dependencies[@]} 'rsync')
  fi
  for rpm_dependency in ${rpm_dependencies[@]}; do
    dependency_exists=$(yum list | grep "$rpm_dependency")
    # If package does not exist OR it is not properly installed, do install it
    if [[ -z $dependency_exists || $dependency_exists != *ii* ]]; then
      yum install -y $rpm_dependency
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
    # Understood as "architecture" in RPMs
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
    while [[ $1 != -* ]]; do
      files="$files $1"
      shift
    done
    ;;
  -g|--group)
    package_group="$1";
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
  -r|--requirements)
    package_dependencies="$1"
    shift
    while [[ $1 != -* ]]; do
      package_dependencies="$package_dependencies $1"
      shift
    done
    ;;
  -R|--distribution)
    package_distribution="$1"
    shift
    while [[ $1 != -* ]]; do
      package_distribution="$package_distribution $1"
      shift
    done
    ;;
  -s|--source)
    package_source="$1"
    shift
    ;;
  -S|--sign)
    sign_package=true
    create_gpg_key=true
    ;;
  -t|--templates)
    # Only one path allowed
    rpm_files_location="$1"
    rpm_files_location_root=$rpm_files_location
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
  # No file location for rpm means it is simple/manual mode
  # and thus no further information is needed
  if [[ -z $rpm_files_location ]]; then
    for required_argument_dh in ${required_arguments_dh[@]}; do
      if [[ -z $(eval echo $required_argument_dh) ]]; then
        error "E: Argument '$required_argument_dh' is required for dh-make"
      fi
    done
  else
    # Copy the rpm folder to a temporal, appropriate location
    if [[ -d $rpm_files_location ]]; then
      mkdir -p $path_to_package/rpm_templates/
      cp -Rp $rpm_files_location $path_to_package/rpm_templates/
      rpm_files_location=$path_to_package/rpm_templates/rpm
    fi
    # Correction: use parent folder if the user chose the rpm folder itself
    if [[ $(basename $rpm_files_location) =~ ^rpm.* ]]; then
      rpm_files_location_root="$(dirname "$rpm_files_location")"
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
  clean_rpmized || echo_v "Could not remove temporary files for 'rpm'ization"
  exit
}

function clean_rpmized()
{
  breakline 1
  echo_v "> Cleaning the house..."
  if [[ $interactive == true ]]; then
  while true; do
    read -p "> Do you want to remove temporary files created for the 'rpm'ization?: " yn
    case $yn in
    [Yy]* ) do_clean_rpmized
        break;;
    [Nn]* ) break;;
    * ) echo_v "Please answer 'Y' or 'N'.";;
    esac
  done
  else
  do_clean_rpmized
  fi
  breakline 1
}

function do_clean_rpmized()
{
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
    mkdir -p $path_to_package/rpm/contents/$destination_path_tmp
    cp -p $source_path $path_to_package/rpm/contents/$destination_path_tmp/$source_file
    if [ ! -f $path_to_package/rpm/install ]; then
      touch $path_to_package/rpm/install
    fi
    cat $path_to_package/rpm/install
    echo "rpm/contents/$destination_path_tmp/$source_file $destination_path" >> $path_to_package/rpm/install
  done
}

function generate_structure()
{
  la -la $path_to_package
  mkdir -p $path_to_package/{RPMS,SRPMS,BUILD,SOURCES,SPECS,tmp}

  cat <<EOF >$path_to_package/.rpmmacros
%_topdir   %(echo $HOME)/rpmbuild
%_tmppath  %{_topdir}/tmp
EOF
}

function get_version_release()
{
  version_release=("" "")
  if [[ ! -z $package_version ]]; then
    old_ifs=$IFS
    IFS="-"
    set -- $var
    version_release=("$1" "$2")
    IFS=$old_ifs
  fi
  return $version_release
}

function append_to_spec()
{
  spec_contents=$1
  new_addition=$2
  spec_newline="\n"

  spec_contents=$(printf "${spec_contents}${spec_newline}${new_addition}")
  return spec_contents
}

function generate_spec()
{
  spec_contents=$(printf "#\nSpec file for $package_name\n#\n")
  if [[ ! -z $package_name ]]; then
    spec_package_name="Name: $package_name"
    spec_contents=$(append_to_spec $spec_contents $spec_package_name)
  fi
  if [[ ! -z $package_short_description ]]; then
    spec_package_short_description="Summary: $package_short_description"
    spec_contents=$(append_to_spec $spec_contents $spec_package_short_description)
  fi
  if [[ ! -z $package_version ]]; then
    version_release=$(get_version_release)
    spec_package_version="Version: ${version_release[0]}"
    spec_package_release="Release: ${version_release[1]}"
    spec_contents=$(append_to_spec $spec_contents $spec_package_version)
    spec_contents=$(append_to_spec $spec_contents $spec_package_release)
  fi
  if [[ ! -z $copyright ]]; then
    spec_package_license="License: $copyright"
    spec_contents=$(append_to_spec $spec_contents $spec_package_license)
  fi
  if [[ ! -z $package_group ]]; then
    spec_package_group="Group: $package_group"
    spec_contents=$(append_to_spec $spec_contents $spec_package_group)
  fi
  if [[ ! -z $package_source ]]; then
    spec_package_source="Source: $package_source"
    spec_contents=$(append_to_spec $spec_contents $spec_package_source)
  fi
  if [[ ! -z $package_website ]]; then
    spec_package_website="URL: $package_website"
    spec_contents=$(append_to_spec $spec_contents $spec_package_website)
  fi
  if [[ ! -z $package_dependencies ]]; then
    spec_package_dependencies="Requires: $package_dependencies"
    spec_contents=$(append_to_spec $spec_contents $spec_package_dependencies)
  fi
  if [[ ! -z $package_class ]]; then
    spec_package_class="BuildArch: $package_class"
    spec_contents=$(append_to_spec $spec_contents $spec_package_class)
    rpmbuild_params="$rpmbuild_params --buildarch"
  fi


  if [[ ! -z $package_distribution ]]; then
    spec_package_distribution="Distribution: $package_distribution"
    spec_contents=$(append_to_spec $spec_contents $spec_package_distribution)
  fi
  if [[ ! -z $name ]] && [[ ! -z $email ]]; then
    spec_package_packager="Packager: $name <$email>"
    spec_contents=$(append_to_spec $spec_contents $spec_package_packager)
  fi
  if [[ ! -z $package_description ]]; then
    spec_package_description="\n%description\n$package_description"
    spec_contents=$(append_to_spec $spec_contents $spec_package_description)
  fi
  if [[ ! -z $package_prep ]]; then
    spec_package_prep="\n%prep\n$package_prep"
    spec_contents=$(append_to_spec $spec_contents $spec_package_prep)
  fi
  if [[ ! -z $package_build ]]; then
    spec_package_build="\n%build\n$package_build"
    spec_contents=$(append_to_spec $spec_contents $spec_package_build)
  fi
  if [[ ! -z $package_install ]]; then
    spec_package_install="\n%install\n$package_install"
    spec_contents=$(append_to_spec $spec_contents $spec_package_install)
  fi
  if [[ ! -z $package_files ]]; then
    spec_package_files="\n%files\n$package_files"
    spec_contents=$(append_to_spec $spec_contents $spec_package_files)
  fi
}

function perform_rmpbuild()
{
  echo_v "> Generating ${package_name}_${package_version}.tar.gz..."
  tar --ignore-failed-read -cvzf $path_to_package/SOURCES/${package_name}_${package_version}.tar.gz $path_to_package || echo_v "Could not create ${package_name}_${package_version}.tar.gz"

  cd $path_to_package

  # Enforce package basic details
  validate_package_name
  validate_package_website # Optional
  validate_package_version

  rpmbuild $rpmbuild_params || error "Could not rpmbuild with ${package_name}_${package_version}.tar.gz"
}

breakline 1
echo_v "  ********** RPM GENERATOR: START **********"
cd $path_to_script

breakline 1
echo_v "> Fetching parameters..."
parse_arguments $@

breakline 1
echo_v "> Validating parameters..."
validate_parameters

breakline 1
echo_v "> Generating structure..."
# Compute output location
path_to_package=$root_script/${package_name}_${package_version}
output_path=$(readlink -m $root_script/../../../unix_package_output__${current_date})
generate_structure

breakline 1
echo_v "> Installing dependencies..."
install_dependencies

if [[ $no_build == true || $build == false ]]; then
  breakline 1
  echo_v "> Creating sources..."
  mkdir -p $path_to_package
  generate_spec

  breakline 1
  if [[ $interactive == true ]]; then 
    echo_v "*NOTE* If you want to SIGN this package please see that the name and e-mail you use are already part of one of your GPG signatures. If this were not the case you will have the chance to generate it later."
  fi
  
  create_dir=$PWD
  
  breakline 2
  perform_rmpbuild
fi

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
# Move to proper location for rpm-buildpackage
cd $path_to_package
place_user_files_in_package

if [[ $sign_package == true ]]; then
  echo_v "> Performing (signed) rpmbuild..."
  rpmbuild $rpmbuild_params --sign SPECS/$package_name-$package_version.spec || error "Could not rpmbuild (signed) on $path_to_package"
else
  echo_v "> Performing (unsigned) rpmbuild..."
  rpmbuild $rpmbuild_params SPECS/$package_name-$package_version.spec || error "Could not rpmbuild (unsigned) on $path_to_package"
fi

breakline 1
generated_rpm_file_location=$(find $root_script/ -name "*.rpm" | head -1)
generated_rpm_file_name=$(basename $generated_rpm_file_location)
echo_v "> Review final info for package $generated_rpm_file_name..."
rpm --info $generated_rpm_file_location || error "Could not show info for rpm package"

breakline 1
echo_v "> Please check the correctness of the package..."
/usr/bin/rpmlint $generated_rpm_file_location #|| error "Could not show info about the correctness of the .rpm file"

if [[ ! -z $rpm_files_location ]]; then
  breakline 1
  echo_v "*NOTE* In case the rpm package was not properly generated, consider checking the \"install\" file at the directory $rpm_files_location, and ensure the paths are relative to its parent folder."
fi

breakline 1
echo_v "> Ending package generation..."
move_to_output

# Cleaning temporary files
clean_rpmized

breakline 1
echo_v "  ********** RPM GENERATOR: END **********"
