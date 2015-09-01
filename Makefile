# Related paths
SRC_DIR = src
TEST_DIR = test
LIB_DIR = lib
BUILD_DIR = build
TMP_DIR = /tmp/unixpackage
LOG_DIR = /var/log/unixpackage
OPT_DIR = /opt/unixpackage
SBIN_DIR = /usr/sbin
MAN8_DIR = /usr/share/man/man8

# Filename of the JAR package
JAR_PKG = $(BUILD_DIR)/unixpackage.jar

# Set your entry point(s) of your java app:
ENTRY_POINT = $(SRC_DIR)/com/github/unixpackage/MainApp.java
ENTRY_POINT_JAVA = com.github.unixpackage.MainApp
ENTRY_POINT_JUNIT = org.junit.runner.JUnitCore

# Java, flags and classpath
JAVA = java
JAVAC = javac
JAR = jar
JFLAGS = -encoding UTF-8 -Xlint:none
CLASSPATH = $(SRC_DIR):$(BUILD_DIR):media:script:$(LIB_DIR)/commons-io-1.2.jar:$(LIB_DIR)/log4j-1.2.17.jar:$(LIB_DIR)/hamcrest-core-1.3.jar:$(LIB_DIR)/junit-4.12.jar

# Generate JAR file
JAR_CMD = $(JAR) cvfm $(JAR_PKG) MANIFEST.MF -C `find $(BUILD_DIR) -not -path "*/unixpackage.jar" -not -path "*/.git*"`

# Package generation
UNIXPKG_GIT = $(PWD)
AUTHOR_NAME = "Carolina Fernandez"
AUTHOR_EMAIL = "cfermart@gmail.com"
PACKAGE_NAME = "unixpackage"
PACKAGE_LICENCE_DEB = "gpl3"
PACKAGE_LICENCE_RPM = "GPLv3"
PACKAGE_VERSION = "0.1-1"
PACKAGE_WEBSITE = "http://carolinafernandez.github.io/unixpackage"
PACKAGE_SECTION = "admin"
PACKAGE_GROUP = "System Environment/Libraries"
PACKAGE_ARCH_DEB = "i"
PACKAGE_ARCH_RPM = "noarch"
DESCRIPTION_SHORT = "Create a UNIX package"
DESCRIPTION_LONG = "Easily create Debian and Fedora based UNIX packages through a UI"

configure:
		mkdir -p $(LOG_DIR)
		chmod 777 $(LOG_DIR)

build:
		mkdir -p $(BUILD_DIR)
		find $(SRC_DIR) -iname *.java >> sources.txt
		find $(TEST_DIR) -iname *.java >> sources.txt
		$(JAVAC) -cp $(CLASSPATH) -d $(BUILD_DIR) @sources.txt -encoding UTF-8
		rm sources.txt
		#$(JAVAC) -cp $(CLASSPATH) -d $(BUILD_DIR) -sourcepath $(SRC_DIR) $(ENTRY_POINT) $(JFLAGS)
		cp -Rup media $(BUILD_DIR)
		cp -Rup script $(BUILD_DIR)
		cp -up README* $(BUILD_DIR)
		cp -up LICENCE $(BUILD_DIR)
		cp -up log4j.properties $(BUILD_DIR)
		test -d $(BUILD_DIR) || echo "Error: $(BUILD_DIR) directory is not found"

run-class:	
		$(JAVA) -cp $(CLASSPATH) $(ENTRY_POINT_JAVA)

jar:
		cp -up $(LIB_DIR)/commons-io-1.2.jar $(BUILD_DIR)/
		cp -up $(LIB_DIR)/log4j-1.2.17.jar $(BUILD_DIR)/
		# Extract contents of dependencies under BUILD_DIR
		$(JAR) xf $(BUILD_DIR)/commons-io-1.2.jar org -C $(BUILD_DIR)/ .
		$(JAR) xf $(BUILD_DIR)/log4j-1.2.17.jar org -C $(BUILD_DIR)/ .
		#ifneq ($(man mv | grep -- "-n"), ""); mv -un org $(BUILD_DIR)/; endif
		#ifneq ($(man mv | grep -- "--backup"), ""); mv -u --backup=t org $(BUILD_DIR)/; endif
		# Se non e vaca e boi
		mv -un org $(BUILD_DIR)/ || (mv -u --backup=t org $(BUILD_DIR)/ || echo "Error: impossible to copy required libraries")
		#$(JAR) cvfm $(JAR_PKG) MANIFEST.MF -C $(BUILD_DIR) .
		#$(JAR) cvfm $(JAR_PKG) MANIFEST.MF -C `find $(BUILD_DIR) -not -path "*/unixpackage.jar" -not -path "*/.git*"`
		$(JAR_CMD) || $(JAR_CMD) & 1>&2 > /dev/null

run-jar:
		$(JAVA) -$(JAR) $(JAR_PKG)

tests:
		$(JAVA) -cp $(CLASSPATH) $(ENTRY_POINT_JUNIT) com.github.unixpackage.data.ArgumentsTest

deb:
		test -d $(TMP_DIR) || cp -Rup $(UNIXPKG_GIT) $(TMP_DIR)/
		$(JAVA) -$(JAR) $(JAR_PKG) -b -c $(PACKAGE_LICENCE_DEB) -d $(DESCRIPTION_SHORT) -C $(PACKAGE_ARCH_DEB) -D $(DESCRIPTION_LONG) -s $(PACKAGE_SECTION) -e $(AUTHOR_EMAIL) -f $(TMP_DIR):$(OPT_DIR) $(TMP_DIR)/build/unixpackage.jar:/usr/lib/unixpackage/unixpackage.jar $(TMP_DIR)/bin/debian/unixpackage.sbin:$(SBIN_DIR)/unixpackage $(TMP_DIR)/bin/debian/unixpackage.sbin:$(SBIN_DIR)/upkg $(TMP_DIR)/bin/debian/unixpackage.8.gz:$(MAN8_DIR)/unixpackage.8.gz -n $(AUTHOR_NAME) -p $(PACKAGE_NAME) -V $(PACKAGE_VERSION) -w $(PACKAGE_WEBSITE)

rpm:
		test -d $(TMP_DIR) || cp -Rup $(UNIXPKG_GIT) $(TMP_DIR)/
		$(JAVA) -$(JAR) $(JAR_PKG) -b -c $(PACKAGE_LICENCE_RPM) -d $(DESCRIPTION_SHORT) -C $(PACKAGE_ARCH_RPM) -D $(DESCRIPTION_LONG) -g $(PACKAGE_GROUP) -e $(AUTHOR_EMAIL) -f $(TMP_DIR):$(OPT_DIR) $(TMP_DIR)/build/unixpackage.jar:/usr/lib/unixpackage/unixpackage.jar $(TMP_DIR)/bin/fedora/unixpackage.sbin:$(SBIN_DIR)/unixpackage $(TMP_DIR)/bin/fedora/unixpackage.sbin:$(SBIN_DIR)/upkg $(TMP_DIR)/bin/fedora/unixpackage.8.gz:$(MAN8_DIR)/unixpackage.8.gz -n $(AUTHOR_NAME) -p $(PACKAGE_NAME) -V $(PACKAGE_VERSION) -w $(PACKAGE_WEBSITE)

clean:
		rm -rf $(BUILD_DIR)
		rm -f $(JAR_PKG)
		sudo rm -rf $(LOG_DIR)

all:	clean configure build tests jar run-jar
