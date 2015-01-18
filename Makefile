# Related paths
SRC_DIR = src
LIB_DIR = lib
BIN_DIR = bin
BUILD_DIR = build

# Filename of the JAR package
JAR_PKG = $(BIN_DIR)/unixpackage.jar

# Set your entry point(s) of your java app:
ENTRY_POINT = $(SRC_DIR)/com/github/unixpackage/MainApp.java
ENTRY_POINT_JAVA = com.github.unixpackage.MainApp
ENTRY_POINT_CLASS = $(BUILD_DIR)/com/github/unixpackage/MainApp.class

# Java, flags and classpath
JAVA = java
JAVAC = javac
JAR = jar
JFLAGS = -encoding UTF-8 -Xlint:none
CLASSPATH = $(SRC_DIR):$(BUILD_DIR):media:packages:script:$(LIB_DIR)/commons-io-1.2.jar

build: 		
		mkdir -p $(BUILD_DIR)
		find $(SRC_DIR) -iname *.java > sources.txt
		$(JAVAC) -cp $(CLASSPATH) -d $(BUILD_DIR) @sources.txt -encoding UTF-8
		rm sources.txt
		#$(JAVAC) -cp $(CLASSPATH) -d $(BUILD_DIR) -sourcepath $(SRC_DIR) $(ENTRY_POINT) $(JFLAGS)
		cp -Rp media $(BUILD_DIR)
		cp -Rp script $(BUILD_DIR)
		cp -Rp packages $(BUILD_DIR)
		cp -p README.md $(BUILD_DIR)
		cp -p LICENCE.txt $(BUILD_DIR)
		test -d $(BUILD_DIR) || echo "Error: $(BUILD_DIR) directory is not found"

run-class: 	
		$(JAVA) -cp $(CLASSPATH) $(ENTRY_POINT_JAVA)

jar:		
		cp -p $(LIB_DIR)/commons-io-1.2.jar $(BUILD_DIR)/
		# Extract contents of dependencies under BUILD_DIR
		$(JAR) xf $(BUILD_DIR)/commons-io-1.2.jar org -C $(BUILD_DIR)/ .
		mv org $(BUILD_DIR)/
		$(JAR) cvfm $(JAR_PKG) MANIFEST.MF -C $(BUILD_DIR) .
		#$(JAR) cvfe $(JAR_PKG) $(ENTRY_POINT_CLASS) src -C $(BUILD_DIR) .

run-jar: 	
		$(JAVA) -$(JAR) $(JAR_PKG)

clean:		
		rm -rf $(BUILD_DIR)
		rm -f $(JAR_PKG)

all:		clean build jar run-jar

