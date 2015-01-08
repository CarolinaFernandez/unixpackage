package com.github.unixpackage.utils;

import java.awt.Component;
import java.util.Collection;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;

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
			// Advanced mode may skip to step #7 if there is no signing involved
			case 2:
				if (!Variables.isNull("BUNDLE_MODE")
						&& Variables.get("BUNDLE_MODE").equals(
								Constants.BUNDLE_MODE_ADVANCED)
						&& (!(Boolean) Variables.get("PACKAGE_SIGN"))) {
					return getStep((Constants.STEPS_METHODS.get(7)));
				}
				break;
			// Advanced mode & signing must go through step #3, then skip to #7
			case 3:
				if (!Variables.isNull("BUNDLE_MODE")
						&& Variables.get("BUNDLE_MODE").equals(
								Constants.BUNDLE_MODE_ADVANCED)
						&& ((Boolean) Variables.get("PACKAGE_SIGN"))) {
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
					if (!(Boolean) Variables.get("PACKAGE_SIGN")) {
						return getStep((Constants.STEPS_METHODS.get(2)));
						// Advanced mode & signing must go back to step #3
					} else {
						return getStep((Constants.STEPS_METHODS.get(3)));
					}
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
			System.out.println("Exception looking for class: " + e);
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
			result = Shell.preProcess();
			// In case of failure, revert program to original state and exit
			if (!result) {
				try {
					System.out
							.println("Cleaning temporal folder because of some previous error...");
					Shell.postProcess();
				} catch (Exception e) {
					System.out
							.print("Program exiting due to failure. Exception: "
									+ e);
					System.exit(0);
				}
			}
		}
	}
}
