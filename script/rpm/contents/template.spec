#%define _topdir                /home/rpm

#
# Spec file for <package_name>
#
Name: <package_name>
Summary: <package_description_short>
Version: <package_version>
Release: <package_release>
License: <package_license>
Group: <package_group>
Source: <package_source>
URL: <package_url>
Requires: <package_dependencies>
BuildArch: <package_type>
Distribution: <pakage_distribution>
Vendor: <package_vendor>
Packager: <packager_name> <<packager_email>>

%description
<package_description_long>

%prep
<package_prep>

%build
<package_build>

%install
<package_install>

%files
<package_files>
