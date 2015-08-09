#!/bin/bash

# More: http://fedoraproject.org/wiki/How_to_create_an_RPM_package
# More: http://www.tldp.org/HOWTO/RPM-HOWTO/build.html
# More: http://www.linuxuser.co.uk/tutorials/make-your-own-deb-and-rpm-packages
# Spec header: http://www.techrepublic.com/article/making-rpms-part-1-the-spec-file-header/
# Licenses: https://fedoraproject.org/wiki/Licensing:Main?rd=Licensing
# Validation: http://linux.die.net/man/1/rpmlint
# Validation: http://www.thegeekstuff.com/2015/02/rpm-build-package-example/
# Signing: https://iuscommunity.org/pages/CreatingAGPGKeyandSigningRPMs.html
# Expect: http://aaronhawley.livejournal.com/10615.html
# SRPMS: http://www.rpm.org/max-rpm/s1-rpm-miscellania-srpms.html
# Macros: https://fedoraproject.org/wiki/Packaging:RPMMacros

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
package_version_original=""
package_version=""
package_release=""
package_website=""
package_group=""
package_priority=""
package_class=""
package_files=""
package_install_files=""
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
package_version_re='^[0-9]+([.][0-9]+)?$'
package_release_re='^[0-9]+?$'


# Required parameters for simple/manual executions
# In advanced mode, pre-configured rpm files are retrieved from another location
required_arguments_dh=('$package_name' '$package_version' '$package_class' '$name' '$email' '$copyright')
required_arguments_sign=('$name' '$email')
# Helps installing dependencies
rpm_dependencies=('rpm-build' 'rpmlint' 'gzip' 'xdg-utils')
# Common arguments passed to rpmbuild
rpmbuild_params="-ba"
# Rpmbuild to be run as normal user
unixpackage_user="unixpackage"
run_with_su="su $unixpackage_user"

# Internal variables
gpg_key_exists=""
create_gpg_key=false
spec_contents=""

# Paths
#path=${PWD##/*/}
base_path=${PWD}
# Get path where the script is running
root_script=$(cd $(dirname $0); pwd -P; cd $base_path)
root_package=$(readlink -e $root_script/../../packages/rpm)
path_to_script=$root_script
current_date=$(date +"%Y-%m-%d_%H-%M-%S")
generated_rpm_file_location=""
generated_rpm_file_name=""

# Output, spec and package locations (not yet existing, to be defined later)
path_to_package=""
path_to_package_spec=""
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
    rpm_dependencies=(${rpm_dependencies[@]} 'rng-tools' 'expect')
  fi
  # Install this package prior to rsync any folder
  if [[ ! -z $rpm_files_location ]]; then
    rpm_dependencies=(${rpm_dependencies[@]} 'rsync')
  fi
  for rpm_dependency in ${rpm_dependencies[@]}; do
    #dependency_exists=$(yum list | grep "$rpm_dependency")
    dependency_exists=$(rpm -qa | grep "$rpm_dependency")
    # If package does not exist OR it is not properly installed, do install it
    #if [[ -z $dependency_exists || $dependency_exists != *installed* ]]; then
    if [[ -z $dependency_exists ]]; then
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
    # Read first argument w/o setting spaces on it
    package_group="$1";
    shift
    while [[ $1 != -* ]]; do
      package_group="$package_group $1"
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
    package_version_original="$package_version"
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
      cp -Rp $rpm_files_location $path_to_package/SPECS/
      rpm_files_location=$path_to_package/SPECS/
    fi
    # Correction: use parent folder if the user chose the SPECS folder itself
    if [[ $(basename $rpm_files_location) =~ ^SPECS.* ]]; then
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

# Validate correct format of release version
function validate_release_version()
{
  if ! [[ $package_release =~ $package_release_re ]]; then
  error "E: Package release '$package_release' is not correct"
  fi
}

function prepare_unprivileged_user() {
  # Create user if needed
  unixpackage_user_exists=$(cat /etc/group | grep "$unixpackage_user")
  if [[ -z $unixpackage_user_exists ]]; then
    useradd $unixpackage_user -m -s /bin/bash
  fi
  # Change permissions of tmp folder
  chown $unixpackage_user:$unixpackage_user -R $path_to_package
}

# Exit on failure
function error()
{
  echo_v $1
  exit
}

function move_to_output()
{
  if [[ ! -d $output_path ]]; then
    mkdir -p $output_path
  fi
  mv $generated_rpm_file_location $output_path/
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
    destination_path_dirname=$(dirname ${file_tuple[1]})
    destination_path_tmp=${destination_path:1:${#destination_path}}

    mkdir -p $path_to_package/SOURCES$destination_path_dirname
    cp -Rp $source_path $path_to_package/SOURCES$destination_path
    package_files="$package_files\n$destination_path"
    package_install_files="$package_install_files\nmkdir -p %%{buildroot}$destination_path_dirname"
    package_install_files="$package_install_files\ncp -Rp %%{_topdir}/SOURCES$destination_path %%{buildroot}$destination_path_dirname/"
  done
}

function generate_structure()
{
  mkdir -p $path_to_package
  mkdir -p $path_to_package/{RPMS,SRPMS,BUILD,SOURCES,SPECS}
}

function get_version_release()
{
  if [[ ! -z $package_version ]]; then
    old_ifs=$IFS
    IFS="-"
    set -- $package_version
    package_version="$1"
    package_release="$2"
    IFS=$old_ifs
  fi
}

function append_to_spec()
{
  new_addition="$@"
  spec_separator="\n"
  spec_contents="${spec_contents}${spec_separator}${new_addition}"
}

function generate_spec()
{
  path_to_package_spec=$path_to_package/SPECS/$package_name.spec
  spec_contents="#\n# Spec file for package $package_name\n#\n"

  append_to_spec "%%define _topdir   $path_to_package\n"

  if [[ ! -z $package_name ]]; then
    spec_package_name="Name: $package_name"
    append_to_spec $spec_package_name
  fi
  if [[ ! -z $package_short_description ]]; then
    spec_package_short_description="Summary: $package_short_description"
    append_to_spec $spec_package_short_description
  fi
  if [[ ! -z $package_version ]]; then
    get_version_release
    spec_package_version="Version: $package_version"
    append_to_spec $spec_package_version
    spec_package_release="Release: $package_release"
    append_to_spec $spec_package_release
  fi
  if [[ ! -z $copyright ]]; then
    spec_package_license="License: $copyright"
    append_to_spec $spec_package_license
  fi
  if [[ ! -z $package_group ]]; then
    spec_package_group="Group: $package_group"
    append_to_spec $spec_package_group
  fi
  if [[ ! -z $package_source ]]; then
    spec_package_source="Source: $package_source"
    append_to_spec $spec_package_source
  fi
  if [[ ! -z $package_website ]]; then
    spec_package_website="URL: $package_website"
    append_to_spec $spec_package_website
  fi
  if [[ ! -z $package_dependencies ]]; then
    spec_package_dependencies="Requires: $package_dependencies"
    # TODO PROCESS (HERE OR AT BEGINNING)
    append_to_spec $spec_package_dependencies
  fi
  if [[ ! -z $package_class ]]; then
    spec_package_class="BuildArch: $package_class"
    append_to_spec $spec_package_class
    #rpmbuild_params="$rpmbuild_params --buildarch"
  fi

  # Add BuildRoot to define output of files
  spec_buildroot="BuildRoot: /home/${unixpackage_user}/${package_name}_${package_version_original}"
  append_to_spec $spec_buildroot

  if [[ ! -z $package_distribution ]]; then
    spec_package_distribution="Distribution: $package_distribution"
    append_to_spec $spec_package_distribution
  fi

  if [[ ! -z $name ]] && [[ ! -z $email ]]; then
    spec_package_packager="Packager: $name <$email>"
    append_to_spec $spec_package_packager
  fi


  if [[ ! -z $package_description ]]; then
    spec_package_description="\n%%description\n$package_description"
    append_to_spec $spec_package_description
  fi
  if [[ -z $package_prep ]]; then
    spec_package_prep="\n%%prep"
  else
    spec_package_prep="\n%%prep\n$package_prep\n"
  fi
  if [[ ! -z $package_install_files ]]; then
    spec_package_prep="$spec_package_prep$package_install_files"
  fi

  append_to_spec $spec_package_prep
  if [[ -z $package_build ]]; then
    spec_package_build="\n%%build"
  else
    spec_package_build="\n%%build\n$package_build"
  fi
  append_to_spec $spec_package_build
  if [[ -z $package_install ]]; then
    spec_package_install="\n%%install"
  else
    spec_package_install="\n%%install\n$package_install"
  fi
  append_to_spec $spec_package_install
  if [[ ! -z $package_files ]]; then
    spec_package_files="\n%%files$package_files"
    spec_package_files="$spec_package_files\n#%%doc README CHANGELOG"
    append_to_spec $spec_package_files
  fi

  # Add clean line
  append_to_spec "\n%%clean"

  # Add changelog line
  append_to_spec "\n%%changelog\n* $(date +'%a %b %d %Y') $name <$email> $package_version_original\n- Initial packaging"

  # Dump everything to the spec file
  printf "$spec_contents" > $path_to_package_spec
}

function perform_rpmbuild()
{
  # Enforce package basic details
  validate_package_name
  validate_package_website # Optional
  validate_package_version
  validate_release_version

  # Check there is an unprivileged user to run rpmbuild with
  breakline 2
  echo_v "> Setting up unprivileged user..."
  prepare_unprivileged_user

  cd $path_to_package

  breakline 1
  # Perform rpmbuild
  if [[ $sign_package == true ]]; then
    echo_v "> Performing (signed) rpmbuild..."
    # Problematic
    #$run_with_su -c "rpmbuild $rpmbuild_params --sign $path_to_package_spec" || error "Could not rpmbuild (signed) on $path_to_package"
  else
    echo_v "> Performing (unsigned) rpmbuild..."
    #$run_with_su -c "rpmbuild $rpmbuild_params $path_to_package_spec" || error "Could not rpmbuild (unsigned) on $path_to_package"
  fi
  $run_with_su -c "rpmbuild $rpmbuild_params $path_to_package_spec" || error "Could not rpmbuild (unsigned) on $path_to_package"

  # XXX ORIGINAL - TODO: PLACE BACK THIS ONE
  #generated_rpm_file_location=$(find $path_to_package/*RPMS -name "*$package_class.rpm" | head -1)
  generated_rpm_file_location=$(find $path_to_package/*RPMS -name "*$package_name*" | grep ".rpm$" | head -1)
  generated_rpm_file_name=$(basename $generated_rpm_file_location)

  # Sign (working - yet still problematic when it comes to sending the passphrase)
  if [[ $sign_package == true ]]; then
    # Set proper directives in ~/.rpmmacros
    gpg_key_macro="%%_signature  gpg\n"
    if [[ ! -z $name ]]; then
      gpg_key_macro="$gpg_key_macro\n%%_gpg_name   $name <$email>"
    fi
    printf "$gpg_key_macro" > /home/${unixpackage_user}/.rpmmacros
    # Change permissions of tmp folder
    chown $unixpackage_user:$unixpackage_user -R /home/${unixpackage_user}/.rpmmacros
    # Send proper commands to sign the package in batch mode
    $run_with_su -c "expect $path_to_package/../rpm_sign.sh -d $generated_rpm_file_location \"\""
  fi
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
path_to_package=$root_script/${package_name}_${package_version_original}
output_path=$(readlink -m $root_script/../../../unix_package_output__${current_date})
generate_structure

cd $path_to_package

# Move to proper location for rpmbuild
place_user_files_in_package

cd $path_to_package/SOURCES

# Generates *.src.rpm package
#breakline 1
#echo_v "> Generating ${package_name}.tar.gz..."
#tar --ignore-failed-read -zcvf ${package_name}.tar.gz * --exclude="*.tar.gz" || echo_v "Could not create ${package_name}.tar.gz"
#find $path_to_package/SOURCES/ ! -name "*.tar.gz" -delete

cd $path_to_package

if [[ $no_build == true || $build == false ]]; then
  breakline 1
  echo_v "> Creating sources..."
  mkdir -p $path_to_package
  generate_spec

  if [[ $interactive == true ]]; then 
    echo_v "*NOTE* If you want to SIGN this package please see that the name and e-mail you use are already part of one of your GPG signatures. If this were not the case you will have the chance to generate it later."
  fi
  
  create_dir=$PWD
fi

if [[ $no_build == true ]]; then
  exit 0
fi

breakline 1
echo_v "> Installing dependencies..."
install_dependencies

breakline 1
echo_v "> Checking keys for package signing..."
if [[ $interactive == true ]]; then
  read -p "> Do you want to sign the package? (y/n) [y]: " sign_package
  case $sign_package in
  [Yy]* ) gpg_key_exists=$(/usr/bin/gpg --list-keys | grep "$name" | grep "$email")
    if [[ -z $gpg_key_exists ]]; then
      breakline 1
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
#      break;;
  esac
else
  if [[ $sign_package == true ]]; then
    test -f /etc/init.d/rng-tools && sudo /etc/init.d/rng-tools start
    gpg_key_exists=$(/usr/bin/gpg --list-keys | grep "$name" | grep "$email")
    # Look for key. If it does not exist, create
    # No passphrase in use for automation. This should not be done at all!
    if [[ -z $gpg_key_exists ]]; then
      breakline 1
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

# Finally, generate .rpm package
perform_rpmbuild

breakline 1
# Display information for a non-installed RPM
echo_v "> Review final info for package $generated_rpm_file_name..."
rpm -qip $generated_rpm_file_location || error "Could not show info for rpm package"

breakline 1
echo_v "> Please check the correctness of the package..."
rpmlint $generated_rpm_file_location #|| error "Could not show info about the correctness of the .rpm file"

if [[ ! -z $rpm_files_location ]]; then
  breakline 1
  echo_v "*NOTE* In case the rpm package was not properly generated, consider checking the \"install\" file at the directory $rpm_files_location, and ensure the paths are relative to its parent folder."
fi

breakline 1
echo_v "> Ending package generation..."
move_to_output

breakline 1
echo_v "  ********** RPM GENERATOR: END **********"
