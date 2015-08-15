# Related paths
SRC_DIR = src
LIB_DIR = lib
BUILD_DIR = build
TMP_DIR = /tmp/unixpackage
OPT_DIR = /opt/unixpackage
SBIN_DIR = /usr/sbin
MAN8_DIR = /usr/share/man/man8

# Filename of the JAR package
JAR_PKG = $(BUILD_DIR)/unixpackage.jar

# Set your entry point(s) of your java app:
ENTRY_POINT = $(SRC_DIR)/com/github/unixpackage/MainApp.java
ENTRY_POINT_JAVA = com.github.unixpackage.MainApp
ENTRY_POINT_CLASS = $(BUILD_DIR)/com/github/unixpackage/MainApp.class

# Java, flags and classpath
JAVA = java
JAVAC = javac
JAR = jar
JFLAGS = -encoding UTF-8 -Xlint:none
CLASSPATH = $(SRC_DIR):$(BUILD_DIR):media:script:$(LIB_DIR)/commons-io-1.2.jar:$(LIB_DIR)/log4j-1.2.17.jar

# Package generation
UNIXPKG_GIT = $(PWD)
AUTHOR_NAME = "Carolina Fernandez"
AUTHOR_EMAIL = "cfermart@gmail.com"
PACKAGE_NAME = "unixpackage"
PACKAGE_LICENCE = "GPLv3"
PACKAGE_VERSION = "0.1-1"
PACKAGE_WEBSITE = "http://carolinafernandez.github.io/unixpackage"
PACKAGE_GROUP = "System Environment/Libraries"
PACKAGE_ARCH = "noarch"
DESCRIPTION_SHORT = "Create a UNIX package"
DESCRIPTION_LONG = "Easily create Debian and Fedora based UNIX packages through a UI"

build: 		
		mkdir -p $(BUILD_DIR)
		find $(SRC_DIR) -iname *.java > sources.txt
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
		$(JAR) cvfm $(JAR_PKG) MANIFEST.MF -C $(BUILD_DIR) .
		#$(JAR) cvfe $(JAR_PKG) $(ENTRY_POINT_CLASS) src -C $(BUILD_DIR) .

run-jar: 	
		$(JAVA) -$(JAR) $(JAR_PKG)

rpm:
		test -d $(TMP_DIR) || cp -Rup $(UNIXPKG_GIT) $(TMP_DIR)/
		$(JAVA) -$(JAR) $(JAR_PKG) -b -c $(PACKAGE_LICENCE) -d $(DESCRIPTION_SHORT) -C $(PACKAGE_ARCH) -D $(DESCRIPTION_LONG) -g $(PACKAGE_GROUP) -e $(AUTHOR_EMAIL) -f $(TMP_DIR):$(OPT_DIR) $(TMP_DIR)/build/unixpackage.jar:/usr/lib/unixpackage/unixpackage.jar $(TMP_DIR)/bin/fedora/unixpackage.sbin:$(SBIN_DIR)/unixpackage $(TMP_DIR)/bin/fedora/unixpackage.sbin:$(SBIN_DIR)/upkg $(TMP_DIR)/bin/fedora/unixpackage.8.gz:$(MAN8_DIR)/unixpackage.8.gz -n $(AUTHOR_NAME) -p $(PACKAGE_NAME) -V $(PACKAGE_VERSION) -w $(PACKAGE_WEBSITE)

clean:		
		rm -rf $(BUILD_DIR)
		rm -f $(JAR_PKG)

all:		clean build jar run-jar

