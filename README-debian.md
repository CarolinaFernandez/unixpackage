UnixPackage for Debian README
=============================

The script below shows an example of how to use UnixPackage
in background mode to generate an DEB package. Alternatively
you may have a look at the `deb` rule in the Makefile.

The DEB package generation has been tested under Debian 7.4
with OpenJDK Java 1.6 (v1.6.0_36) and Java 1.8 (v1.8.0_60).

```
UNIXPACKAGE_GIT=/opt/unixpackage
UNIXPACKAGE_DIR=/tmp/unixpackage
FANCYPACKAGE_DIR=/tmp/fancypackage
git clone http://CarolinaFernandez.github.io/unixpackage.git $UNIXPACKAGE_GIT
cp -Rp $UNIXPACKAGE_GIT $UNIXPACKAGE_DIR
make jar

AUTHOR_NAME="Jane Doe"
AUTHOR_EMAIL = "jane.doe@random-website.io"
PACKAGE_NAME = "fancy-package"
PACKAGE_LICENCE = "gpl3"
PACKAGE_VERSION = "0.1-1"
PACKAGE_WEBSITE = "http://www.random-website.io"
PACKAGE_SECTION = "admin"
PACKAGE_ARCH = "i"
DESCRIPTION_SHORT="Create a fancy package"
DESCRIPTION_LONG="Create a fancy package that does fancy stuff"

java -jar $UNIXPACKAGE_DIR/build/unixpackage.jar -b -c $PACKAGE_LICENCE -d $DESCRIPTION_SHORT -C $PACKAGE_ARCH -D $DESCRIPTION_LONG -s $PACKAGE_SECTION -e $AUTHOR_EMAIL -f $UNIXPACKAGE_GIT:$FANCYPACKAGE_DIR -n $PACKAGE_NAME -p $PACKAGE_VERSION -V $PACKAGE_VERSION -w $PACKAGE_WEBSITE
```
