UnixPackage README
==================

UnixPackage provides an easier way to bundle software into
packages for UNIX Operating Systems. Currently either Debian
or Fedora based distros are supported.

This package is distributed under the terms of the GNU General
Public License, Version 3.0. The full terms and conditions
of this licence are detailed in the LICENCE file.

UnixPackage consists mainly on Java sources and, optionally,
on a JAR file that can be run stand-alone.

If some bugs are discovered, you are welcome to report this
on the GitHub issue tracker or to contribute with your fix via
pull request.

Further instructions on use can be checked in the GitHub wiki
and in the README files available per UNIX operating system.

1. Requirements
---------------
* A GNU/Linux Debian or Fedora-based distros
* Java 1.6 and JDK installed

2. Running
----------
2.1 Clone the UnixPackage repository under folder /opt:
  ```
  unixpackage_git=/opt/unixpackage
  git clone http://CarolinaFernandez.github.io/unixpackage.git $unixpackage_git
  ```
2.2 Create the log folder and grant full permissions:
  ```
  mkdir -p /var/log/unixpackage
  chmod 777 /var/log/unixpackage/
  ```
2.3 Compile the sources through the Makefile:
  ```
  cd $unixpackage_git
  make build
  ```
2.4 Create the JAR file:
  ```
  cd $unixpackage_git
  make jar
  ```
2.5 Run the JAR file:
  ```
  cd $unixpackage_git
  make run-jar
  ```

3. Installing
-------------
UnixPackage can be installed as a binary in your system. If
you wish to use this as a normal application rather than using
the JAR, perform the following steps:

3.1 Create the package for your system:
  ```
  # Pick one of the following
  make deb
  make rpm
  ```
3.2 Move to the folder specified by the above step and install
the package with your preferred package manager.
