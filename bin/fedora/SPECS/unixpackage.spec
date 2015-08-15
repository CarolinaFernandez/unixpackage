#
# Spec file for package unixpackage
#

%define _topdir   /tmp/unix_package/script/rpm/unixpackage_0.1-1

Name: unixpackage
Summary: Create a UNIX package
Version: 0.1
Release: 1
License: GPLv3
Group: System Environment/Libraries
URL: http://carolinafernandez.github.io/unixpackage
BuildArch: noarch
BuildRoot: /home/unixpackage/unixpackage_0.1-1
Packager: Carolina Fernandez <cfermart@gmail.com>

%description
Easily create Debian and Fedora based UNIX packages through a UI

%prep
# SOURCES
mkdir -p %{buildroot}/opt/unixpackage
cp -Rp %{_topdir}/SOURCES/opt/unixpackage %{buildroot}/opt/
# JAR
cp -Rp %{_topdir}/SOURCES/opt/unixpackage/build/unixpackage.jar %{buildroot}/usr/lib/unixpackage/unixpackage.jar
# Binaries
cp -Rp %{_topdir}/SOURCES/opt/unixpackage/bin/fedora/unixpackage.sbin %{buildroot}/usr/sbin/unixpackage
cp -Rp %{_topdir}/SOURCES/opt/unixpackage/bin/fedora/unixpackage.sbin %{buildroot}/usr/sbin/upkg
# Man files
install -g 0 -o 0 -m 0644 %{_topdir}/SOURCES/opt/unixpackage/bin/fedora/unixpackage.8 /usr/local/man/man8/
gzip /usr/local/man/man8/unixpackage.8

%build

%install
chmod +x %{buildroot}/usr/sbin/unixpackage
chmod +x %{buildroot}/usr/sbin/upkg

%files
/opt/unixpackage
/var/lib/unixpackage/unixpackage.jar
/usr/sbin/unixpackage
/usr/sbin/upkg
/usr/local/man/man8/unixpackage.8.gz

%clean

%changelog
* Fri Aug 14 2015 Carolina Fernandez <cfermart@gmail.com> 0.1-1
- Initial packaging
