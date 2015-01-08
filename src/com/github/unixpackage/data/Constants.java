package com.github.unixpackage.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class Constants {

	/**
	 * Information needed for application
	 */

	// Application-related
	public static final String APP_NAME = "upkg";
	public static final String APP_DESCRIPTION = "Create a UNIX package";

	// Could be under /etc; but root permissions would be needed
	public static final String APP_PREFERENCES_PATH = System
			.getProperty("user.home") + "/." + APP_NAME;
	public static final String APP_PREFERENCES_FILE_PATH = APP_PREFERENCES_PATH
			+ "/" + APP_NAME + ".properties";

	// Media files
	public static final String ROOT_MEDIA = "media";
	public static final String APP_ICON = ROOT_MEDIA + "/cat-in-a-box.ico";
	public static final String APP_IMAGE = ROOT_MEDIA + "/cat-in-a-box.png";

	public static final String ROOT_FILES_PATH = ".";

	// Package files
	public static final String ROOT_PACKAGE_FILES_PATH = "packages";
	public static final String DEBIAN_FILES_PATH = ROOT_PACKAGE_FILES_PATH
			+ "/deb";
	public static final String REDHAT_FILES_PATH = ROOT_PACKAGE_FILES_PATH
			+ "/rpm";

	public static final String ROOT_TMP_FILES_PATH = "/tmp";
	public static final String ROOT_TMP_PACKAGE_FILES_PATH = ROOT_TMP_FILES_PATH
			+ "/unix_package_" + (new Random().nextInt(1000 - 1 + 1) + 1);
	// Random number used to name folder used under /tmp
	public static final String TMP_PACKAGE_FILES_PATH = ROOT_TMP_PACKAGE_FILES_PATH
			+ "/" + ROOT_PACKAGE_FILES_PATH;
	public static final String TMP_PACKAGE_DEBIAN_FILES_PATH = TMP_PACKAGE_FILES_PATH
			+ "/deb";
	public static final String TMP_PACKAGE_REDHAT_FILES_PATH = TMP_PACKAGE_FILES_PATH
			+ "/rpm";

	// Script files
	public static final String ROOT_SCRIPT_FILES_PATH = "script";
	public static final String DEBIAN_SCRIPT_PATH = ROOT_SCRIPT_FILES_PATH
			+ "/deb/create_package.sh";
	public static final String REDHAT_SCRIPT_PATH = ROOT_SCRIPT_FILES_PATH
			+ "/rpm/create_package.sh";

	public static final String TMP_SCRIPT_FILES_PATH = ROOT_TMP_PACKAGE_FILES_PATH
			+ "/" + ROOT_SCRIPT_FILES_PATH;
	public static final String TMP_SCRIPT_DEBIAN_PATH = TMP_SCRIPT_FILES_PATH
			+ "/deb/create_package.sh";
	public static final String TMP_SCRIPT_REDHAT_PATH = TMP_SCRIPT_FILES_PATH
			+ "/rpm/create_package.sh";

	// Fonts
	public static final Font TITLE_FONT = new Font("Courier", Font.BOLD
			| Font.ITALIC, 19);
	public static final Font STEPTITLE_FONT = new Font("Times New Roman",
			Font.BOLD | Font.ITALIC, 14);

	// Colors
	public static final Color TITLE_BACKGROUND = new Color(29, 29, 29);
	public static final Color TITLE_FOREGROUND = new Color(180, 255, 180);

	public static final Color STEPTITLE_FOREGROUND = new Color(0, 0, 0);

	// Size and dimension for components
	public static final Dimension SCREEN_DIMENSION = Toolkit
			.getDefaultToolkit().getScreenSize();

	public static final int FRAME_WIDTH = 700;
	public static final int FRAME_HEIGHT = 500;

	public static final int NAVIGATION_BUTTON_WIDTH = 150;
	public static final int NAVIGATION_HEIGHT = 30;
	public static final int STEPTITLE_HEIGHT = 15;
	public static final int TITLE_HEIGHT = 65;

	// Dimension = (width, height)
	public static final Dimension CONTENT_DIMENSION = new Dimension(
			FRAME_WIDTH, (int) (FRAME_HEIGHT - STEPTITLE_HEIGHT - 1.2
					* TITLE_HEIGHT - 2 * NAVIGATION_HEIGHT));
	public static final Dimension NAVIGATION_DIMENSION = new Dimension(
			NAVIGATION_BUTTON_WIDTH, NAVIGATION_HEIGHT);
	public static final Dimension STEPTITLE_DIMENSION = new Dimension(
			FRAME_WIDTH, STEPTITLE_HEIGHT);
	public static final Dimension TITLE_DIMENSION = new Dimension(FRAME_WIDTH,
			TITLE_HEIGHT);
	public static final Dimension TEXTFIELD_DIMENSION = new Dimension(350, 30);
	public static final int TEXTFIELD_COLUMNS_MAX = 30;
	public static final Dimension FILECHOOSER_DIMENSION = new Dimension(
			CONTENT_DIMENSION.width,
			(int) (CONTENT_DIMENSION.height - 2 * STEPTITLE_DIMENSION.height));

	// Values for the bundle modes
	public static final String BUNDLE_MODE_SIMPLE = "Simple";
	public static final String BUNDLE_MODE_MANUAL = "Manual";
	public static final String BUNDLE_MODE_ADVANCED = "Advanced";

	// Values for the bundle types
	public static final String BUNDLE_TYPE_DEB = "DEB";
	public static final String BUNDLE_TYPE_RPM = "RPM";

	// Mappings
	public static final Map<Integer, String> STEPS_METHODS;
	public static final Map<Integer, String> STEPS_DESCRIPTIONS;
	public static final Map<String, String> PACKAGE_LICENSES;
	public static final Map<String, String> PACKAGE_CLASSES;
	public static final Map<String, String> BUNDLE_MODE_DESCRIPTIONS;

	// Static initializer
	static {
		STEPS_METHODS = new HashMap<Integer, String>();
		STEPS_METHODS.put(1, "Splash");
		STEPS_METHODS.put(2, "SetBundleMode");
		STEPS_METHODS.put(3, "SetAuthorInfo");
		STEPS_METHODS.put(4, "SetPackageInfo");
		STEPS_METHODS.put(5, "SetPackageSources");
		STEPS_METHODS.put(6, "EditPackageFiles");
		STEPS_METHODS.put(7, "ReviewPackageInfo");
		STEPS_METHODS.put(8, "GeneratePackage");

		STEPS_DESCRIPTIONS = new HashMap<Integer, String>();
		STEPS_DESCRIPTIONS.put(1, "");
		STEPS_DESCRIPTIONS.put(2, "Define how to bundle the sources");
		STEPS_DESCRIPTIONS.put(3, "Edit the author information");
		STEPS_DESCRIPTIONS.put(4, "Set the package information");
		STEPS_DESCRIPTIONS.put(5, "Choose the package sources");
		STEPS_DESCRIPTIONS.put(6, "Modify the package files");
		STEPS_DESCRIPTIONS.put(7, "Review the package information");
		STEPS_DESCRIPTIONS.put(8, "Generate the final package");

		PACKAGE_LICENSES = new HashMap<String, String>();
		PACKAGE_LICENSES.put("Apache", "apache");
		PACKAGE_LICENSES.put("Artistic", "artistic");
		PACKAGE_LICENSES.put("BSD", "bsd");
		PACKAGE_LICENSES.put("GPL", "gpl");
		PACKAGE_LICENSES.put("GPL2", "gpl2");
		PACKAGE_LICENSES.put("GPL3", "gpl3");
		PACKAGE_LICENSES.put("LGPL", "lgpl");
		PACKAGE_LICENSES.put("LGPL2", "lgpl2");
		PACKAGE_LICENSES.put("LGPL3", "lgpl3");
		PACKAGE_LICENSES.put("MIT", "mit");

		PACKAGE_CLASSES = new HashMap<String, String>();
		PACKAGE_CLASSES.put("Single binary", "s");
		PACKAGE_CLASSES.put("Arch-Independent", "i");
		PACKAGE_CLASSES.put("Kernel module", "k");
		PACKAGE_CLASSES.put("Multiple binary", "m");
		PACKAGE_CLASSES.put("Library", "l");

		BUNDLE_MODE_DESCRIPTIONS = new HashMap<String, String>();
		BUNDLE_MODE_DESCRIPTIONS.put(BUNDLE_MODE_SIMPLE,
				"Automated, but must define location of sources");
		BUNDLE_MODE_DESCRIPTIONS.put(BUNDLE_MODE_MANUAL,
				"Control every file used for bundling the package");
		BUNDLE_MODE_DESCRIPTIONS.put(BUNDLE_MODE_ADVANCED,
				"Copy bundle files from location");
	}

	public static final int STEPS_METHODS_LENGTH = STEPS_METHODS.size();
}
