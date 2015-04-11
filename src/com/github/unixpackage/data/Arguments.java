package com.github.unixpackage.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.github.unixpackage.utils.Shell;

public class Arguments {
	
	public static String ARGUMENT_RE1 = "-{1,2}";
	public static HashMap<String, String> acceptedArguments;
	// RegExp with format for argument
	private static final Pattern ARGUMENT_RE = Pattern.compile(
	        "(-\\w|(-){2}(\\w|-)+)"
	);
	
	public static boolean parseInputArguments(String[] args) {
		// First of all, load any possible variables from the properties file
		UnixPreferences preferences = new UnixPreferences();
		preferences.loadFromFile();
		
		// Update afterwards with command-line arguments (higher priority)
		Boolean correctlyParsed = true;
		Boolean argumentWithValue = true;
		Boolean argumentIsVerified = false;
		ArrayList<String> arguments = parseArgumentsString(args);
		
		for (int i = 0; i < arguments.size(); i++) {
			try {
				System.out.println("\n\n*** ARGUMENT: " + arguments.get(i));
				argumentWithValue = true;
				String variableName = null;
				String variableData = null;
				if (Constants.ARGUMENTS_ACCEPTED.containsKey(arguments.get(i))) {
					variableName = Constants.ARGUMENTS_VARIABLES.get(arguments.get(i));
					System.out.println("variableName 1 >> " + variableName);
				} else if (Constants.ARGUMENTS_ACCEPTED.containsValue(arguments.get(i))) {
					variableName = Arguments.getKeyByValue(Constants.ARGUMENTS_ACCEPTED, arguments.get(i));
					System.out.println("variableName 2a >> " + variableName);
					variableName = Constants.ARGUMENTS_VARIABLES.get(variableName);
					System.out.println("variableName 2b >> " + variableName);
				} else {
					variableName = null;
					argumentWithValue = false;
				}

				System.out.println("*** ARGUMENT VARIABLE: " + variableName);
				// Place value of argument in its related variable
				// No variable has an initial value on the first run, so ignore that
				if (argumentWithValue && variableName != null && variableName != "") {
					try {
						variableData = Variables.get(variableName).toString();
					} catch (Exception e) {
						variableData = "";
					}
					Field variableField = Variables.class.getDeclaredField(variableName);
					System.out.println("type of variable: " + variableField.getType().getSimpleName());
					if (variableField.getType().getSimpleName().equals("String")) {
						variableData = arguments.get(i+1);
					} else if (variableField.getType().getSimpleName().equals("Boolean")) {
						variableData = Boolean.TRUE.toString();
						correctlyParsed &= true;
					}
					// Verify contents of variable prior to update it
					variableData = variableData.trim();
					argumentIsVerified = UnixVerifier.verify(variableName, variableData);
					System.out.println("...argument verified... " + argumentIsVerified);
					if (argumentIsVerified) {
						Variables.set(variableName, variableData);
						System.out.println(">>> Variable.get AFTER (GOOD) = " + Variables.get(variableName));
					} else {
						System.out.println(">>> Variable.get AFTER (BAD) = " + Variables.get(variableName));
						variableName = null;
						variableData = null;
						throw new Exception();
					}
//					variableName = null;
				}
			} catch (Exception e) {
				correctlyParsed &= false;
				System.out.println("Exception! > " + e);
			}
		}
		return correctlyParsed;
	}

	private static ArrayList<String> parseArgumentsString(String[] args) {
		// Define maximum possible size of the new Array
		ArrayList<String> arguments = new ArrayList<String>(args.length);
		String aggregatedArgument = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i].matches(Arguments.ARGUMENT_RE.toString())) {
				// In case help is implicitly or explicitly requested, show it
				if (!(Constants.ARGUMENTS_ACCEPTED.containsKey(args[i]) || Constants.ARGUMENTS_ACCEPTED.containsValue(args[i]))) {
					Shell.outputHelpInformation();
					System.exit(1);
				} else if (args[i].equals(Constants.ARGUMENT_HELP) || args[i].equals(Constants.ARGUMENT_HELP_LONG)) {
					Shell.outputHelpInformation();
					System.exit(0);
				// Show package version if explicitly requested
				} else if (args[i].equals(Constants.ARGUMENT_VERSION) || args[i].equals(Constants.ARGUMENT_VERSION_LONG)) {
					Shell.outputVersionInformation();
					System.exit(0);
				}
				if (!aggregatedArgument.equals("")) {
					arguments.add(aggregatedArgument);
				}
				arguments.add(args[i]);
				aggregatedArgument = "";
			} else {
				aggregatedArgument += args[i] + " ";
			}	
		}
		arguments.add(aggregatedArgument.trim());
		return arguments;
	}
	
	public static List<String> generateArgumentsForDebianFiles() {
		List<String> commandList = Arguments.generateArgumentsForDebianPackage();
		// Generate Debian files only (dh_make) and do not remove sample (.ex) files
		commandList.add(Constants.ARGUMENT_NO_BUILD);
		// Remove verbose argument
		commandList.remove(Constants.ARGUMENT_VERBOSE);
		commandList.remove(Constants.ARGUMENT_VERBOSE_LONG);
		return commandList;
	}
	
	public static List<String> generateArgumentsForDebianPackage() {
		List<String> commandList = new ArrayList<String>();
		List<String> commandListValidated = new ArrayList<String>();
		HashMap<String, String> argumentList = new HashMap<String, String>();

		argumentList.put(Constants.ARGUMENT_BATCH, null);
//		argumentList.put(Constants.ARGUMENT_SOURCE, Variables.PACKAGE_NAME);
		argumentList.put(Constants.ARGUMENT_WEBSITE, Variables.PACKAGE_WEBSITE);
		argumentList.put(Constants.ARGUMENT_PACKAGE_VERSION, Variables.PACKAGE_VERSION);
		// Translate to something understandable by the script
		if (Constants.PACKAGE_LICENCES.containsKey(Variables.PACKAGE_LICENCE)) {
			argumentList.put(Constants.ARGUMENT_COPYRIGHT,
				Constants.PACKAGE_LICENCES.get(Variables.PACKAGE_LICENCE));
		} else if (Constants.PACKAGE_LICENCES.containsValue(Variables.PACKAGE_LICENCE)){
			// Class in its short version
			argumentList.put(Constants.ARGUMENT_COPYRIGHT, Variables.PACKAGE_LICENCE);		
		}
		// Translate to something understandable by the script
		if (Constants.PACKAGE_CLASSES.containsKey(Variables.PACKAGE_CLASS)) {
			// Class is a full string
			argumentList.put(Constants.ARGUMENT_CLASS,
				Constants.PACKAGE_CLASSES.get(Variables.PACKAGE_CLASS));
		} else if (Constants.PACKAGE_CLASSES.containsValue(Variables.PACKAGE_CLASS)){
			// Class in its short version
			argumentList.put(Constants.ARGUMENT_CLASS, Variables.PACKAGE_CLASS);		
		}
		argumentList.put(Constants.ARGUMENT_PACKAGE_SECTION, Variables.PACKAGE_SECTION);
		argumentList.put(Constants.ARGUMENT_PACKAGE_PRIORITY, Variables.PACKAGE_PRIORITY);
		argumentList.put(Constants.ARGUMENT_PACKAGE_NAME, Variables.PACKAGE_NAME);
		argumentList.put(Constants.ARGUMENT_DESCRIPTION_SHORT, Variables.PACKAGE_SHORT_DESCRIPTION);
		argumentList.put(Constants.ARGUMENT_DESCRIPTION, Variables.PACKAGE_DESCRIPTION);
		
		// In simple and manual modes, the majority of the arguments are passed
//		if (Variables.isNull("BUNDLE_MODE")
//				|| !Variables.BUNDLE_MODE
//						.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			argumentList.put(Constants.ARGUMENT_NAME, Variables.MAINTAINER_NAME);
			argumentList.put(Constants.ARGUMENT_EMAIL, Variables.MAINTAINER_EMAIL);
//		}

		// In advanced mode, a path is passed to copy the user's templates from
		if (!Variables.isNull("BUNDLE_MODE")
				&& Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED)) {
//			argumentList.put(Constants.ARGUMENT_NAME, Variables.MAINTAINER_NAME);
//			argumentList.put(Constants.ARGUMENT_EMAIL, Variables.MAINTAINER_EMAIL);
			System.out.println("Variables.BUNDLE_MODE_ADVANCED_PATH: " + Variables.BUNDLE_MODE_ADVANCED_PATH);
			argumentList.put(Constants.ARGUMENT_TEMPLATES, Variables.BUNDLE_MODE_ADVANCED_PATH);
		}

		// Notify if package is to be signed
		if (!Variables.isNull("PACKAGE_SIGN") && Variables.PACKAGE_SIGN) {
			argumentList.put(Constants.ARGUMENT_SIGN, null);
//			argumentList.put(Constants.ARGUMENT_NAME, Variables.MAINTAINER_NAME);
//			argumentList.put(Constants.ARGUMENT_EMAIL, Variables.MAINTAINER_EMAIL);
		}

		// Verbosity
		argumentList.put(Constants.ARGUMENT_VERBOSE_LONG, null);
		
		for (Entry<String, String> entry : argumentList.entrySet()) {
			commandListValidated.add(entry.getKey());
			// Check arguments not eligible for a 2nd argument
			if (entry.getKey().matches("-(\\w?[^bSvmM]){1}")) {
				System.out.println("+++ adding key -- " + entry);
				if (entry.getValue() != null) {
					commandListValidated.add(entry.getValue());
				} else {
					// If value is null, entry shall not be added
					commandListValidated.remove(entry.getKey());
				}
			}
		}
		commandList = commandListValidated;
//		System.out.println("*** [V] Validated commandList: " + commandList);
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
