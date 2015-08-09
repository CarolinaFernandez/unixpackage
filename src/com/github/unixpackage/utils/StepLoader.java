package com.github.unixpackage.utils;

import java.awt.Component;
import java.util.Collection;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.UnixLogger;
import com.github.unixpackage.data.Variables;
//import com.github.unixpackage.steps.GeneratePackage;
import com.github.unixpackage.steps.GeneratePackage;

public class StepLoader {

	public static final Collection<String> steps = Constants.STEPS_METHODS
			.values();
	public static int currentStep;

	/**
	 * Gets step panel by number.
	 * 
	 * @param stepNumber
	 *            number of the desired step
	 * @return class (CommonStep) for that step
	 */
	public static Class<CommonStep> getStepAt(int stepNumber) {
		return getStep(Constants.STEPS_METHODS.get(stepNumber));
	}

	/**
	 * Gets number for current step.
	 * 
	 * @param currentClass
	 *            class being displayed in the panel
	 * @return number corresponding to the current step
	 */
	public static int getStepNumber(CommonStep currentClass) {
		StepLoader.currentStep = 1;
		for (String step : StepLoader.steps) {
			if (currentClass.getClass().getSimpleName().indexOf(step) == 0) {
				break;
			}
			StepLoader.currentStep++;
		}

		StepLoader.performOperationByStepNumber(StepLoader.currentStep);
		return StepLoader.currentStep;
	}

	public static Class<CommonStep> getCurrentStep() {
		return getStep((Constants.STEPS_METHODS.get(StepLoader.currentStep)));
	}

	public static Class<CommonStep> getNextStep() {
		/**
		 * Simple mode: when on 5 (sources choice screen), move to 7 (detail
		 * screen) Advanced mode: when on 4 (bundle choice screen), move to 7
		 * (detail screen)
		 */
		if (StepLoader.currentStep < Constants.STEPS_METHODS.size()) {
			switch (StepLoader.currentStep) {
			// Advanced mode (either when signing the package or not) skips to step #7
			case 4:
					if (!Variables.isNull("BUNDLE_MODE")
							&& Variables.get("BUNDLE_MODE").equals(
									Constants.BUNDLE_MODE_ADVANCED)) {
						return getStep((Constants.STEPS_METHODS.get(7)));
					}
					break;
			case 5:
				if (!Variables.isNull("BUNDLE_MODE")
						&& Variables.get("BUNDLE_MODE").equals(
								Constants.BUNDLE_MODE_SIMPLE)) {
					return getStep((Constants.STEPS_METHODS.get(7)));
				}
				break;
			}
			return getStep((Constants.STEPS_METHODS
					.get(StepLoader.currentStep + 1)));
		} else {
			return null;
		}
	}

	public static Class<CommonStep> getPreviousStep() {
		/**
		 * Simple mode: when on 7 (detail screen), move to 5 (sources choice
		 * screen) Advanced mode: when on 7 (detail screen), move to 4 (bundle
		 * choice screen)
		 */
		if (StepLoader.currentStep > 1) {
			switch (StepLoader.currentStep) {
			case 7:
				if (!Variables.isNull("BUNDLE_MODE")
						&& Variables.get("BUNDLE_MODE").equals(
								Constants.BUNDLE_MODE_SIMPLE)) {
					return getStep((Constants.STEPS_METHODS.get(5)));
				} else if (!Variables.isNull("BUNDLE_MODE")
						&& Variables.get("BUNDLE_MODE").equals(
								Constants.BUNDLE_MODE_ADVANCED)) {
					// Advanced mode & signing must go back to step #4
					return getStep((Constants.STEPS_METHODS.get(4)));
				}
				break;
			}
			return getStep((Constants.STEPS_METHODS
					.get(StepLoader.currentStep - 1)));
		} else {
			return null;
		}
	}

	/**
	 * Obtain class from a given name.
	 * 
	 * @param className
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class<CommonStep> getStep(String className) {
		Class<CommonStep> classObject = null;
		String classPackage = StepLoader.class.getPackage().getName()
				.replace(".utils", ".steps");
		// Use full package path (e.g. 'com.github.unixpackages.steps')
		if (className != null) {
			className = classPackage + "." + className;
		}
		try {
			classObject = (Class<CommonStep>) Class.forName(className);
		} catch (Exception e) {
			UnixLogger.LOGGER.debug("Exception looking for class: " + e);
		}
		return classObject;
	}

	/**
	 * Does some magic retrieving a class from its name :)
	 * 
	 * @param className
	 * @return
	 */
	public static Component getStepInstance(Class<CommonStep> className) {
		Component stepInstance = null;
		try {
			stepInstance = (Component) className.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return stepInstance;
	}

	/**
	 * Performs operations bound to a specific step number. E.g. generates or
	 * deletes appropriate temporary files.
	 * 
	 * @param stepNumber
	 *            number of active step
	 */
	public static void performOperationByStepNumber(int stepNumber) {
		// Switch case requires constants computable at compilation time. Using
		// if instead
		boolean result = false;
		if (stepNumber == 1) {
			// Load file data into variables upon start
			Listeners.onLoad();
			result = Files.copyScriptSourcesIntoTempFolder();
			// In case of failure, revert program to original state and exit
			if (!result) {
				try {
					UnixLogger.LOGGER.debug("Cleaning temporal folder because of some previous error...");
					Shell.postProcess();
				} catch (Exception e) {
					UnixLogger.LOGGER.fatal("Program exiting due to failure. Exception: "
									+ e);
					System.exit(0);
				}
			}
		// SetPackageInfo
		} else if (stepNumber == 3) {
			// Hack: change validators for later steps
			if (Variables.isNull("PACKAGE_TYPE") || Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
				// DEB
				Constants.VARIABLES_REGEXPS.put("PACKAGE_SECTION", Constants.RE_PACKAGE_SECTION_DEB);
				Constants.VARIABLES_REGEXPS.put("PACKAGE_CLASS", Constants.RE_PACKAGE_CLASS_DEB);
				Constants.VARIABLES_REGEXPS.put("PACKAGE_LICENCE", Constants.RE_PACKAGE_LICENCE_DEB);
			} else {
				// RPM
				Constants.VARIABLES_REGEXPS.put("PACKAGE_SECTION", Constants.RE_PACKAGE_GROUP_RPM);
				Constants.VARIABLES_REGEXPS.put("PACKAGE_CLASS", Constants.RE_PACKAGE_CLASS_RPM);
				Constants.VARIABLES_REGEXPS.put("PACKAGE_LICENCE", Constants.RE_PACKAGE_LICENCE_RPM);
			}
		// EditPackageFiles step
		} else if (stepNumber == 5) {
			if (!Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED)) {
				if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
					GeneratePackage.generateDebianFiles();
				} else {
					GeneratePackage.generateRedHatFiles();
				}
			}
		} else if (stepNumber == 6) {
			if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
				GeneratePackage.generateDebianFiles();
			}
		}
	}
}
