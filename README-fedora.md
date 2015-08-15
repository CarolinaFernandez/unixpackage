UnixPackage for Fedora README
=============================

The script below shows an example of how to use UnixPackage
in background mode to generate an RPM package. Alternatively
you may have a look at the `rpm` rule in the Makefile.

The RPM package generation has been tested under CentOS 5.11
with OpenJDK Java 1.6 (v1.6.0_36).

```
UNIXPACKAGE_GIT=/opt/unixpackage
FANCYPACKAGE_DIR=/tmp/fancypackage
git clone http://CarolinaFernandez.github.io/unixpackage.git $UNIXPACKAGE_GIT
cp -Rp $UNIXPACKAGE_GIT /tmp/unixpackage
make build
make jar

AUTHOR_NAME="Jane Doe"
AUTHOR_EMAIL = "jane.doe@random-website.io"
PACKAGE_NAME = "fancy-package"
PACKAGE_LICENCE = "GPLv3"
PACKAGE_VERSION = "0.1-1"
PACKAGE_WEBSITE = "http://www.random-website.io"
PACKAGE_GROUP = "System Environment/Libraries"
PACKAGE_ARCH = "noarch"
DESCRIPTION_SHORT="Create a fancy package"
DESCRIPTION_LONG="Create a fancy package that does fancy stuff"

java -jar /tmp/unixpackage/build/unixpackage.jar -b -c $PACKAGE_LICENCE -d $DESCRIPTION_SHORT -C $PACKAGE_ARCH -D $DESCRIPTION_LONG -g $PACKAGE_GROUP -e $AUTHOR_EMAIL -f $UNIXPACKAGE_GIT:$FANCYPACKAGE_DIR -p $PACKAGE_NAME -V $PACKAGE_VERSION -w $PACKAGE_WEBSITE
```
