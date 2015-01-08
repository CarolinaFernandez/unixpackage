# Set the file name of your jar package:
JAR_PKG = CreateUNIXPackage.jar

# Set your entry point(s) of your java app:
ENTRY_POINT = src/com/github/unixpackages/MainApp.java
ENTRY_POINT_JAVA = com.github.unixpackages.MainApp
ENTRY_POINT_CLASS = bin/com/github/unixpackages/MainApp.class

SRC_DIR = src
BIN_DIR = bin

JAVAC = javac
# Use UTF-8 (and ignore all warnings)
JFLAGS = -encoding UTF-8 -Xlint:none

CLASSPATH = src:bin:media:packages:script:lib/commons-io-1.2.jar

build: 		# Create BIN folder if it does not exist
		test -d $(BIN_DIR) || mkdir $(BIN_DIR)
		$(JAVAC) -cp $(CLASSPATH) -d $(BIN_DIR) -sourcepath src $(ENTRY_POINT) $(JFLAGS)
		#find . -name "*.java" > sources.txt
		#$(JAVAC) -cp $(CLASSPATH) -d $(BIN_DIR) -sourcepath @sources.txt $(ENTRY_POINT) $(JFLAGS)
		#rm sources.txt
		cp -Rp media bin/
		cp -Rp script bin/
		cp -Rp packages bin/
		cp -p README.md bin/
		cp -p LICENSE.txt bin/

.PHONY:		clean run jar

clean:		
		test -d $(BIN_DIR) && rm -r $(BIN_DIR) || echo "" > /dev/null
		test -d $(JAR_PKG) && rm $(JAR_PKG) || echo "" > /dev/null

all:		clean build jar run

run-class: 	
		java -cp $(CLASSPATH) $(ENTRY_POINT_JAVA)

jar:		
		cp -p lib/commons-io-1.2.jar $(BIN_DIR)/
		# Extract contents of dependencies under BIN_DIR
		#jar xf $(BIN_DIR)/commons-io-1.2.jar org -C $(BIN_DIR)/ .
		#mv org $(BIN_DIR)/
		#jar cvfe $(JAR_PKG) $(ENTRY_POINT_CLASS) src -C $(BIN_DIR) .
		jar cvfm $(JAR_PKG) MANIFEST.MF -C $(BIN_DIR) .
		#jar cvfe $(JAR_PKG) $(ENTRY_POINT) -C $(CLASSPATH) .

run-jar: 	
		java -jar $(JAR_PKG)

