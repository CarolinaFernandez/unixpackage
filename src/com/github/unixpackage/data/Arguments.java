package com.github.unixpackage.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.github.unixpackage.utils.Shell;
import com.github.unixpackage.utils.StepLoader;

public class Arguments {

	public static String ARGUMENT_RE1 = "-{1,2}";
	public static HashMap<String, String> acceptedArguments;
	// RegExp with format for argument
	private static final Pattern ARGUMENT_RE = Pattern
			.compile("(-\\w|(-){2}(\\w|-)+)");

	public static boolean parseInputArguments(String[] args) {
		// First of all, load any possible variables from the properties file
		UnixPreferences preferences = new UnixPreferences();
		try {
			preferences.loadFromFile();
		} catch (Exception e1) {
			UnixLogger.LOGGER.warn("Error: could not load properties from file");
		}

		// Update afterwards with command-line arguments (higher priority)
		Boolean correctlyParsed = true;
		Boolean argumentWithValue = true;
		Boolean argumentIsVerified = true;
		ArrayList<String> arguments = parseArgumentsString(args);
		String variableName = null;
		String variableData = null;

		for (int i = 0; i < arguments.size(); i++) {
			try {
				argumentWithValue = true;
				variableName = null;
				variableData = null;
				// Check first whether argument is key or value
				if (Constants.ARGUMENTS_ACCEPTED.containsKey(arguments.get(i))) {
					variableName = Constants.ARGUMENTS_VARIABLES.get(arguments
							.get(i));
				} else if (Constants.ARGUMENTS_ACCEPTED.containsValue(arguments
						.get(i))) {
					variableName = Arguments.getKeyByValue(
							Constants.ARGUMENTS_ACCEPTED, arguments.get(i));
					variableName = Constants.ARGUMENTS_VARIABLES
							.get(variableName);
				} else {
					// If it is a non-accepted argument key, throw Exception
					if (arguments.get(i).startsWith("-")) {
						throw new Exception();
						// Otherwise, it may be any value after the argument key
					} else {
						variableName = null;
						argumentWithValue = false;
					}
				}

				// Place value of argument in its related variable
				// No variable has an initial value on the first run, so ignore
				// that
				if (argumentWithValue && variableName != null
						&& variableName != "") {
					try {
						if (Variables.get(variableName) != null) {
							variableData = Variables.get(variableName)
									.toString();
						}
						// If string is passed to argument (via terminal),
						// replace the preferences file with it
						if (arguments.get(i).startsWith("-")
								&& !arguments.get(i + 1).startsWith("-")) {
							variableData = arguments.get(i + 1);
							if (variableName
									.equals("PACKAGE_SOURCE_INSTALL_PAIRS")) {
								Variables.PACKAGE_SOURCE_INSTALL_PAIRS = new ArrayList<ArrayList<String>>();
								ArrayList<String> packageSourceInstallPairsFile = new ArrayList<String>(
										Arrays.asList(variableData.split(" ")));
								for (String packageSourceInstallPair : packageSourceInstallPairsFile) {
									if (!packageSourceInstallPair.isEmpty()) {
										ArrayList<String> packageSourceInstallPairFile = new ArrayList<String>(
												Arrays.asList(packageSourceInstallPair
														.split(":")));
										Variables.PACKAGE_SOURCE_INSTALL_PAIRS
												.add(packageSourceInstallPairFile);
									}
								}
							}
						}
					} catch (Exception e) {
						variableData = "";
					}
					Field variableField = Variables.class
							.getDeclaredField(variableName);
					if (variableField.getType().getSimpleName()
							.equals("String")) {
						variableData = arguments.get(i + 1);
					} else if (variableField.getType().getSimpleName()
							.equals("Boolean")) {
						variableData = Boolean.TRUE.toString();
						correctlyParsed &= true;
					}
					// Verify contents of variable prior to update it
					variableData = variableData.trim();
					argumentIsVerified = UnixVerifier.verify(variableName,
							variableData);
					if (argumentIsVerified) {
						// Avoid parsing the argument for files (-f)
						if (!arguments.get(i).equals("-f")) {
							Variables.set(variableName, variableData);
						}
					} else {
						variableName = null;
						variableData = null;
						throw new Exception();
					}
				} else {
					// Invalid arguments make everything else fail
					// throw new Exception();
				}
			} catch (Exception e) {
				correctlyParsed &= false;
				UnixLogger.LOGGER.error("Could not parse argument '"
						+ arguments.get(i) + "'. Details: " + e);
				if (!Variables.BATCH_MODE) {
					e.printStackTrace();
				}
			}
		}
		return correctlyParsed;
	}

	private static ArrayList<String> parseArgumentsString(String[] args) {
		// Define maximum possible size of the new Array
		ArrayList<String> arguments = new ArrayList<String>(args.length);
		ArrayList<String> unrecognisedArguments = new ArrayList<String>(
				args.length);

		String aggregatedArgument = "";
		for (String arg : args) {
			if (arg.matches(Arguments.ARGUMENT_RE.toString())) {
				if (!(Constants.ARGUMENTS_ACCEPTED.containsKey(arg) || Constants.ARGUMENTS_ACCEPTED
						.containsValue(arg))) {
					unrecognisedArguments.add(arg);
					// If help is implicitly or explicitly requested, show
					// it
				} else if (arg.equals(Constants.ARGUMENT_HELP)
						|| arg.equals(Constants.ARGUMENT_HELP_LONG)) {
					Shell.outputHelpInformation();
					System.exit(0);
					// Show package version if explicitly requested
				} else if (arg.equals(Constants.ARGUMENT_VERSION)
						|| arg.equals(Constants.ARGUMENT_VERSION_LONG)) {
					Shell.outputVersionInformation();
					System.exit(0);
				}
				if (!aggregatedArgument.equals("")) {
					arguments.add(aggregatedArgument);
				}
				arguments.add(arg);
				aggregatedArgument = "";
			} else {
				aggregatedArgument += arg + " ";
			}
		}
		arguments.add(aggregatedArgument.trim());
		return arguments;
	}

	public static String generateFileStringForDebianFiles() {
		StringBuilder fileString = new StringBuilder();
		for (ArrayList<String> sourceInstallPair : Variables.PACKAGE_SOURCE_INSTALL_PAIRS) {
			if (sourceInstallPair.size() == 2) {
				fileString.append(sourceInstallPair.get(0) + ":"
						+ sourceInstallPair.get(1) + " ");
			}
		}
		// Remove last space
		if (fileString.length() >= 1) {
			fileString.deleteCharAt(fileString.length() - 1);
		}
		return fileString.toString();
	}

	public static List<String> generateArgumentsForDebianFiles() {
		List<String> commandList = Arguments
				.generateArgumentsForDebianPackage();
		// Generate Debian files only (dh_make) and do not remove sample (.ex)
		// files
		// In advanced mode, a path is passed to copy the user's templates from
		if (!Variables.isNull("BUNDLE_MODE")
				&& !Variables.BUNDLE_MODE
						.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			commandList.add(Constants.ARGUMENT_NO_BUILD);
		}
		// Remove verbose argument
		commandList.remove(Constants.ARGUMENT_VERBOSE);
		commandList.remove(Constants.ARGUMENT_VERBOSE_LONG);
		// Should also remove Constants.ARGUMENT_FILE and what follows...
		return commandList;
	}

	public static List<String> generateArgumentsForDebianPackage() {
		List<String> commandList = new ArrayList<String>();
		List<String> commandListValidated = new ArrayList<String>();
		HashMap<String, String> argumentList = new HashMap<String, String>();
		// Both simple and manual modes carry a first source generation and a
		// final build
		if (!Variables.isNull("BUNDLE_MODE")
				&& !Variables.BUNDLE_MODE
						.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			if (StepLoader.currentStep == Constants.STEPS_METHODS_LENGTH) {
				// Do a build only during the last step (source files generated
				// previously)
				argumentList.put(Constants.ARGUMENT_BUILD, null);
			}
			// Add user's files to the package
			String sourceFiles = generateFileStringForDebianFiles();
			if (sourceFiles.length() > 0) {
				argumentList.put(Constants.ARGUMENT_FILES, sourceFiles);
			}
		}
		argumentList.put(Constants.ARGUMENT_BATCH, null);
		argumentList.put(Constants.ARGUMENT_WEBSITE, Variables.PACKAGE_WEBSITE);
		argumentList.put(Constants.ARGUMENT_PACKAGE_VERSION,
				Variables.PACKAGE_VERSION);
		// Translate to something understandable by the script
		if (Constants.PACKAGE_LICENCES_DEB
				.containsKey(Variables.PACKAGE_LICENCE)) {
			argumentList.put(Constants.ARGUMENT_COPYRIGHT,
					Constants.PACKAGE_LICENCES_DEB
							.get(Variables.PACKAGE_LICENCE));
		} else if (Constants.PACKAGE_LICENCES_DEB
				.containsValue(Variables.PACKAGE_LICENCE)) {
			// Class in its short version
			argumentList.put(Constants.ARGUMENT_COPYRIGHT,
					Variables.PACKAGE_LICENCE);
		}
		// Translate to something understandable by the script
		if (Constants.PACKAGE_CLASSES_DEB.containsKey(Variables.PACKAGE_CLASS)) {
			// Class is a full string
			argumentList.put(Constants.ARGUMENT_CLASS,
					Constants.PACKAGE_CLASSES_DEB.get(Variables.PACKAGE_CLASS));
		} else if (Constants.PACKAGE_CLASSES_DEB
				.containsValue(Variables.PACKAGE_CLASS)) {
			// Class in its short version
			argumentList.put(Constants.ARGUMENT_CLASS, Variables.PACKAGE_CLASS);
		}
		argumentList.put(Constants.ARGUMENT_PACKAGE_SECTION,
				Variables.PACKAGE_SECTION);
		argumentList.put(Constants.ARGUMENT_PACKAGE_PRIORITY,
				Variables.PACKAGE_PRIORITY);
		argumentList.put(Constants.ARGUMENT_PACKAGE_NAME,
				Variables.PACKAGE_NAME);
		argumentList.put(Constants.ARGUMENT_DESCRIPTION_SHORT,
				Variables.PACKAGE_SHORT_DESCRIPTION);
		argumentList.put(Constants.ARGUMENT_DESCRIPTION,
				Variables.PACKAGE_DESCRIPTION);

		// In simple and manual modes, the majority of the arguments are passed
		argumentList.put(Constants.ARGUMENT_NAME, Variables.MAINTAINER_NAME);
		argumentList.put(Constants.ARGUMENT_EMAIL, Variables.MAINTAINER_EMAIL);

		// In advanced mode, a path is passed to copy the user's templates from
		if (!Variables.isNull("BUNDLE_MODE")
				&& Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			argumentList.put(Constants.ARGUMENT_TEMPLATES,
					Variables.BUNDLE_MODE_ADVANCED_PATH);
		}

		// Notify if package is to be signed
		if (!Variables.isNull("PACKAGE_SIGN") && Variables.PACKAGE_SIGN) {
			argumentList.put(Constants.ARGUMENT_SIGN, null);
		}

		// Verbosity
		argumentList.put(Constants.ARGUMENT_VERBOSE_LONG, null);

		for (Entry<String, String> entry : argumentList.entrySet()) {
			commandListValidated.add(entry.getKey());
			// Check arguments not eligible for a 2nd argument
			if (entry.getKey().matches("-(\\w?[^bSvmM]){1}")) {
				if (entry.getValue() != null) {
					commandListValidated.add(entry.getValue());
				} else {
					// If value is null, entry shall not be added
					commandListValidated.remove(entry.getKey());
				}
			}
		}
		commandList = commandListValidated;
		return commandList;
	}

	public static List<String> generateArgumentsForRedHatFiles() {
		List<String> commandList = Arguments
				.generateArgumentsForRedHatPackage();
		// Generate Debian files only (dh_make) and do not remove sample (.ex)
		// files
		// In advanced mode, a path is passed to copy the user's templates from
		if (!Variables.isNull("BUNDLE_MODE")
				&& !Variables.BUNDLE_MODE
						.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			commandList.add(Constants.ARGUMENT_NO_BUILD);
		}
		// Remove verbose argument
		commandList.remove(Constants.ARGUMENT_VERBOSE);
		commandList.remove(Constants.ARGUMENT_VERBOSE_LONG);
		// Should also remove Constants.ARGUMENT_FILE and what follows...
		return commandList;
	}

	public static List<String> generateArgumentsForRedHatPackage() {
		List<String> commandList = new ArrayList<String>();
		List<String> commandListValidated = new ArrayList<String>();
		HashMap<String, String> argumentList = new HashMap<String, String>();
		// Both simple and manual modes carry a first source generation and a
		// final build
		if (!Variables.isNull("BUNDLE_MODE")
				&& !Variables.BUNDLE_MODE
						.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			if (StepLoader.currentStep == Constants.STEPS_METHODS_LENGTH
					|| Variables.BATCH_MODE.equals(Constants.BUNDLE_TYPE_RPM)) {
				// Do a build only during the last step (source files generated
				// previously)
				argumentList.put(Constants.ARGUMENT_BUILD, null);
			}
			// Add user's files to the package
			String sourceFiles = generateFileStringForDebianFiles();
			if (sourceFiles.length() > 0) {
				argumentList.put(Constants.ARGUMENT_FILES, sourceFiles);
			}
		}
		argumentList.put(Constants.ARGUMENT_BATCH, null);
		argumentList.put(Constants.ARGUMENT_WEBSITE, Variables.PACKAGE_WEBSITE);
		argumentList.put(Constants.ARGUMENT_PACKAGE_VERSION,
				Variables.PACKAGE_VERSION);

		// No translation performed for RPM
		argumentList.put(Constants.ARGUMENT_COPYRIGHT,
				Variables.PACKAGE_LICENCE);
		// Translate to something understandable by the script
		if (Constants.PACKAGE_CLASSES_RPM.containsKey(Variables.PACKAGE_CLASS)) {
			// Class is a full string
			argumentList.put(Constants.ARGUMENT_CLASS,
					Constants.PACKAGE_CLASSES_RPM.get(Variables.PACKAGE_CLASS));
		} else if (Constants.PACKAGE_CLASSES_RPM
				.containsValue(Variables.PACKAGE_CLASS)) {
			// Class in its short version
			argumentList.put(Constants.ARGUMENT_CLASS, Variables.PACKAGE_CLASS);
		}
		argumentList.put(Constants.ARGUMENT_PACKAGE_GROUP,
				Variables.PACKAGE_SECTION);
		argumentList.put(Constants.ARGUMENT_PACKAGE_NAME,
				Variables.PACKAGE_NAME);
		argumentList.put(Constants.ARGUMENT_DESCRIPTION_SHORT,
				Variables.PACKAGE_SHORT_DESCRIPTION);
		argumentList.put(Constants.ARGUMENT_DESCRIPTION,
				Variables.PACKAGE_DESCRIPTION);

		// In simple and manual modes, the majority of the arguments are passed
		argumentList.put(Constants.ARGUMENT_NAME, Variables.MAINTAINER_NAME);
		argumentList.put(Constants.ARGUMENT_EMAIL, Variables.MAINTAINER_EMAIL);

		// FIXME: Template usage not working as expected (fix RPM generation
		// script)
		// In advanced mode, a path is passed to copy the user's templates from
		if (!Variables.isNull("BUNDLE_MODE")
				&& Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			argumentList.put(Constants.ARGUMENT_TEMPLATES,
					Variables.BUNDLE_MODE_ADVANCED_PATH);
		}

		// Notify if package is to be signed
		if (!Variables.isNull("PACKAGE_SIGN") && Variables.PACKAGE_SIGN) {
			argumentList.put(Constants.ARGUMENT_SIGN, null);
		}

		// Verbosity
		argumentList.put(Constants.ARGUMENT_VERBOSE_LONG, null);

		for (Entry<String, String> entry : argumentList.entrySet()) {
			commandListValidated.add(entry.getKey());
			// Check arguments not eligible for a 2nd argument
			if (entry.getKey().matches("-(\\w?[^bSv]){1}")) {
				if (entry.getValue() != null) {
					commandListValidated.add(entry.getValue());
				} else {
					// If value is null, entry shall not be added
					commandListValidated.remove(entry.getKey());
				}
			}
		}
		commandList = commandListValidated;
		return commandList;
	}

	private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}
