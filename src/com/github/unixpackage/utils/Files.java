package com.github.unixpackage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFileChooser;

import org.apache.commons.io.IOUtils;

import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.UnixLogger;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.steps.GeneratePackage;

import sun.net.www.protocol.file.FileURLConnection;

public class Files {

	public static String choosePath() {
		return choosePath(Variables.BUNDLE_MODE_ADVANCED_PATH);
	}

	public static String choosePath(String path) {
		String chosenPath = "";
		JFileChooser pathChooser;
		// Initialized with the current directory. Otherwise use home folder
		if (path == null) {
			pathChooser = new JFileChooser();
		} else {
			pathChooser = new JFileChooser(new File(path));
		}
		pathChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		int returnValue = pathChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = pathChooser.getSelectedFile();
			chosenPath = selectedFile.getAbsolutePath();
		}
		return chosenPath;
	}

	public static boolean isFolder(final String location,
			final String folderName) {
		File folder = new File(location);
		return folder.getName().equals(folderName);
	}

	public static boolean containsFolder(String location,
			final String folderName) {
		File locationPath = new File(location);
		// Filter directories inside a given location
		String[] locationFilesNames = locationPath.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		ArrayList<String> locationFiles = new ArrayList<String>();
		if (locationFilesNames != null) {
			locationFiles = new ArrayList<String>(
					Arrays.asList(locationFilesNames));
		}
		// Call the searching method
		return containsFolder(location, folderName, locationFiles);
	}

	public static boolean containsFolder(String location,
			final String folderName, ArrayList<String> locationFiles) {
		boolean foundFolder = false;
		for (String contentString : locationFiles) {
			File contentFile = new File(contentString);
			if (contentFile.getName().equals(folderName)) {
				foundFolder = true;
				break;
			} else {
				// If current location is not appropriate, remove and continue
				locationFiles.remove(0);
				return containsFolder(contentFile.getAbsolutePath(),
						folderName, locationFiles);
			}
		}
		return foundFolder;
	}

	public static boolean copyFile(final File toCopy, final File destFile) {
		try {
			return copyStream(new FileInputStream(toCopy),
					new FileOutputStream(destFile));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean copyFilesRecursively(final File toCopy,
			final File destDir) {
		assert destDir.isDirectory();

		if (!toCopy.isDirectory()) {
			return Files.copyFile(toCopy, new File(destDir, toCopy.getName()));
		} else {
			final File newDestDir = new File(destDir, toCopy.getName());
			if (!newDestDir.exists() && !newDestDir.mkdir()) {
				return false;
			}
			for (final File child : toCopy.listFiles()) {
				if (!copyFilesRecursively(child, newDestDir)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void copyJarResourcesRecursively(File destination,
			JarURLConnection jarConnection) throws IOException {
		JarFile jarFile = jarConnection.getJarFile();
		ArrayList<JarEntry> jarEntries = Collections.list(jarFile.entries());
		for (JarEntry entry : jarEntries) {
			if (entry.getName().startsWith(jarConnection.getEntryName())) {
				String fileName = Files.removeStart(entry.getName(),
						jarConnection.getEntryName());
				if (!entry.isDirectory()) {
					InputStream entryInputStream = null;
					try {
						entryInputStream = jarFile.getInputStream(entry);
						Files.copyStream(entryInputStream, new File(
								destination, fileName));
					} finally {
						Files.safeClose(entryInputStream);
					}
				} else {
					Files.ensureDirectoryExists(new File(destination, fileName));
				}
			}
		}
	}

	public static boolean copyFolderIntoTempFolder(String source,
			String destination) {
		boolean result = false;

		Locations loc = new Locations();
		String validatedSource = loc.getAbsolutePath(source);
		File sourceDir = new File(validatedSource);

		File destDir = new File(destination);
		try {
			// Create first all intermediate directories as needed
			destDir.mkdirs();
			result = true;
		} catch (SecurityException se) {
			result = false;
		}

		try {
			Files.copyResourcesRecursively(new URL(sourceDir.toString()),
					destDir);
		} catch (IOException e1) {
			UnixLogger.LOGGER.trace("Error: cannot copy sources into "
					+ destination);
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean copyPackageSourcesIntoTempFolder() {
		Locations loc = new Locations();
		String validatedSource = loc
				.getAbsolutePath(Constants.ROOT_PACKAGE_FILES_PATH);
		String validatedDestination = Constants.ROOT_TMP_PACKAGE_FILES_PATH;
		if (validatedSource.startsWith("jar:")) {
			validatedDestination = Constants.TMP_PACKAGE_FILES_PATH;
		}
		return Files.copyFolderIntoTempFolder(
				Constants.ROOT_PACKAGE_FILES_PATH, validatedDestination);
	}

	public static boolean generatePackageSourcesInTempFolder() {
		// Generate package files first
		GeneratePackage.generateDebianFiles();
		String packageFilesPath = "/deb/" + Variables.PACKAGE_NAME + "_"
				+ Variables.PACKAGE_VERSION + "/debian";
		String validatedSource = Constants.TMP_SCRIPT_FILES_PATH
				+ packageFilesPath;
		String validatedDestination = Constants.TMP_PACKAGE_DEBIAN_FILES_PATH;
		// Default is "DEB"
		if (!Variables.isNull("PACKAGE_TYPE")
				&& Variables.PACKAGE_TYPE.equals("RPM")) {
			validatedDestination = Constants.TMP_PACKAGE_REDHAT_FILES_PATH;
		}
		if (validatedDestination.length() > 0) {
			UnixLogger.LOGGER.debug("Generating package sources under "
					+ validatedDestination);
		} else {
			UnixLogger.LOGGER.error("Could not generate package sources");
		}
		return Files.copyFolderIntoTempFolder(validatedSource,
				validatedDestination);
	}

	public static boolean isPackageSourcesOnDisk() {
		boolean packageSourcesOnDisk = false;
		File packageSourcesPath = new File(Files.getAbsolutePathPackageFile(""));
		if (packageSourcesPath.isDirectory()) {
			packageSourcesOnDisk = true;
		}
		return packageSourcesOnDisk;
	}

	public static File[] getPackageSourcesFiles() {
		File[] packageSourcesFiles = null;
		File packageSourcesPath = new File(Files.getAbsolutePathPackageFile(""));
		if (packageSourcesPath.isDirectory()) {
			packageSourcesFiles = packageSourcesPath.listFiles();
		}
		return packageSourcesFiles;
	}

	/**
	 * Compute location of generated package files in order to show them.
	 * 
	 * @param file
	 * @return
	 */
	public static String getAbsolutePathPackageFile(String file) {
		String absolutePathFile = "";
		// Default is "DEB"
		if (Variables.PACKAGE_TYPE.equals("RPM")) {
			absolutePathFile = Constants.ROOT_TMP_PACKAGE_FILES_PATH + "/"
					+ Constants.TMP_SCRIPT_REDHAT_FILES_PATH + "/"
					+ Variables.PACKAGE_NAME + "_" + Variables.PACKAGE_VERSION
					+ "/" + Constants.BUNDLE_TYPE_RPM_FOLDER;
		} else {
			absolutePathFile = Constants.ROOT_TMP_PACKAGE_FILES_PATH + "/"
					+ Constants.TMP_SCRIPT_DEBIAN_FILES_PATH + "/"
					+ Variables.PACKAGE_NAME + "_" + Variables.PACKAGE_VERSION
					+ "/" + Constants.BUNDLE_TYPE_DEB_FOLDER;
		}
		if (!file.equals("")) {
			absolutePathFile = absolutePathFile + "/" + file;
		}
		return absolutePathFile;
	}

	public static boolean copyScriptSourcesIntoTempFolder() {
		Locations loc = new Locations();
		String validatedSource = loc
				.getAbsolutePath(Constants.ROOT_SCRIPT_FILES_PATH);
		String validatedDestination = Constants.ROOT_TMP_PACKAGE_FILES_PATH;
		if (validatedSource.startsWith("jar:")) {
			validatedDestination = Constants.TMP_SCRIPT_FILES_PATH;
		}
		UnixLogger.LOGGER.debug("Copying script sources under "
				+ validatedDestination);
		return copyFolderIntoTempFolder(Constants.ROOT_SCRIPT_FILES_PATH,
				validatedDestination);
	}

	public static void copyResourcesRecursively(URL originUrl, File destination)
			throws Exception {
		URLConnection urlConnection = originUrl.openConnection();

		if (urlConnection instanceof JarURLConnection) {
			Files.copyJarResourcesRecursively(destination,
					(JarURLConnection) urlConnection);
		} else if (urlConnection instanceof FileURLConnection) {
			Files.copyFilesRecursively(new File(originUrl.getPath()),
					destination);
		} else {
			throw new Exception("URLConnection["
					+ urlConnection.getClass().getSimpleName()
					+ "] is not a recognized/implemented connection type.");
		}
	}

	private static boolean copyStream(final InputStream is, final File f) {
		try {
			return Files.copyStream(is, new FileOutputStream(f));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean copyStream(final InputStream is,
			final OutputStream os) {
		try {
			final byte[] buf = new byte[1024];

			int len = 0;
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			is.close();
			os.close();
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean ensureDirectoryExists(final File f) {
		return f.exists() || f.mkdir();
	}

	public static String getHash(String file) {
		return getHash(new File(file));
	}

	public static String getHash(File file) {
		FileInputStream fis;
		MessageDigest md = null;
		String fileHash = null;
		StringBuffer hexString = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			md = MessageDigest.getInstance(Constants.MESSAGE_DIGEST);
			byte[] bytesOfMessage = IOUtils.toByteArray(fis);
			fis.close();
			byte[] fileBytes = md.digest(bytesOfMessage);
			for (int i = 0; i < fileBytes.length; i++) {
				if ((0xff & fileBytes[i]) < 0x10) {
					hexString.append("0"
							+ Integer.toHexString((0xFF & fileBytes[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & fileBytes[i]));
				}
			}
			fileHash = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			UnixLogger.LOGGER
					.trace("Error: could not generate hash due to digest algorithm problem");
			e.printStackTrace();
		} catch (IOException e) {
			UnixLogger.LOGGER
					.trace("Error: could not generate hash due to I/O error on file ("
							+ file + ")");
		}
		return fileHash;
	}

	// StringUtils method
	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	// StringUtils method
	public static String removeStart(String str, String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.startsWith(remove)) {
			return str.substring(remove.length());
		}
		return str;
	}

	private static void safeClose(InputStream is) {
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * When not present, log folder is created and assigned full permissions.
	 */
	public static void initialiseLog() {
		File logFolder = new File(Constants.APP_LOG_FOLDER);
		if (!logFolder.exists()) {
			logFolder.mkdirs();
			logFolder.setReadable(true);
			logFolder.setWritable(true);
			logFolder.setExecutable(true);
		}
	}

	/**
	 * Retrieve current OS and distribution.
	 * 
	 * @return main OS distribution (i.e. Debian- or Fedora- based)
	 */
	public static String getOSDistro() {
		String distroOS = null;
		StringBuilder distroOSFull = new StringBuilder();
		// distroOS = Shell.execute("cat /etc/*-release");
		// distroOS = Shell.execute("lb_release -a");
		File locationPath = new File("/etc");
		FilenameFilter releaseFilter = new FilenameFilter() {
			@Override
			public boolean accept(File directory, String file) {
				if (file.endsWith("release")) {
					return true;
				} else {
					return false;
				}
			}
		};
		String[] etcFiles = locationPath.list(releaseFilter);
		for (String releaseFile : etcFiles) {
			// Retrieve every content from the release files
			// Do NOT output this via System.out
			distroOSFull.append(Shell.execute("cat /etc/" + releaseFile, null));
		}
		if (distroOSFull.toString().indexOf("Debian") > -1
				|| distroOSFull.toString().indexOf("Ubuntu") > -1) {
			distroOS = "Debian";
		} else if (distroOSFull.toString().indexOf("Red") > -1
				|| distroOSFull.toString().indexOf("CentOS") > -1
				|| distroOSFull.toString().indexOf("Fedora") > -1) {
			distroOS = "Fedora";
		}
		return distroOS;
	}
}
