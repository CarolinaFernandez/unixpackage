package com.github.unixpackage.data;

import java.awt.Color;
import java.awt.Component;
import java.util.regex.Pattern;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.github.unixpackage.components.CommonStep;

public class UnixVerifier extends InputVerifier {
	
	private JLabel findLabelForComponent(Component component) {
		JLabel foundLabel = null;
		for (Component parentComponent : component.getParent().getComponents()) {
			if (parentComponent instanceof JLabel) {
				Component componentWithLabel = ((JLabel) parentComponent).getLabelFor();
				if (componentWithLabel != null && componentWithLabel.equals(component)) {
					foundLabel = (JLabel) parentComponent;
				}
			}
		}
		return foundLabel;
	}
	
	private static String indicateErrorCondition(String variableName) {
		return getFieldErrorExplanation(variableName);
	}
	
	private void indicateErrorConditionGUI(Component component) {
		JLabel foundLabel = this.findLabelForComponent(component);
		if (foundLabel != null) {
			foundLabel.setForeground(Color.RED);
		}
	}
	
	private void indicateNormalConditionGUI(Component component) {
		JLabel foundLabel = this.findLabelForComponent(component);
		if (foundLabel != null) {
			foundLabel.setForeground(Color.BLACK);
		}
	}
	
	/**
	 * Verify a single variable, depending on its name
	 */
	public static boolean verify(String variableName, String variableData) {
		Boolean variableContentVerified = true;
		System.out.println("033[0;32m&&&& VERIFIER (" + variableName + ":" + variableData + ")\033[0m");
		Pattern variableVerifier = Constants.VARIABLES_REGEXPS.get(variableName);
		// Only try to verify when the pattern is available
		if (variableVerifier != null) {
			System.out.println("&&&& VERIFIER Pattern: " + variableVerifier);
			if (!variableVerifier.matcher(variableData).matches()) {
				variableContentVerified = false;
			}
		}
		// TODO Leave this print
		if (!variableContentVerified && !Variables.isNull("BATCH_MODE") && Variables.BATCH_MODE) {
			System.out.println("Error: " + UnixVerifier.indicateErrorCondition(variableName));
		}
		System.out.println("&&&& VERIFIER Result: " + variableContentVerified);
		return variableContentVerified;
	}
	
	@Override
	/**
	 * Verify a single component
	 */
	public boolean verify(JComponent input) {
		String fieldName = null;
		String text = null;

		if (input instanceof JTextField) {
			text = ((JTextField) input).getText();
		} else if (input instanceof JComboBox) {
			text = ((JComboBox) input).getSelectedItem().toString();
		} else if (input instanceof JCheckBox) {
			text = new Boolean(((JCheckBox) input).isEnabled()).toString();
		}

		try {
			fieldName = input.getName();
			if (!UnixVerifier.verify(fieldName, text)) {
				throw new Exception();
			}
			this.indicateNormalConditionGUI(input);
		} catch (Exception e) {
			System.out.println("BATCH MODE????? " + Variables.get("BATCH_MODE"));
			this.indicateErrorConditionGUI(input);
			return false;
		}

		return true;
	}

	/**
	 * Verify all the components in a step
	 * @param step
	 * @return
	 */
	public boolean verify(CommonStep step) {
		return true;
	}
	
	@Override
	public boolean shouldYieldFocus(JComponent input) {
		boolean valid = verify(input);
		if (!valid) {
			String inputFormatExplanation = getFieldErrorExplanation(input.getName());
			// Adapt to comply to the size of the message dialog
			inputFormatExplanation = inputFormatExplanation.replaceAll("(.{" + Constants.VALIDATION_FORMAT_EXPLANATION_MAX_LENGTH + "})", "$1\n");
			JOptionPane.showMessageDialog(null, inputFormatExplanation);
		}

		return valid;
	}

	/**
	 *  Get explanation of the error for a given variable.
	 */
	private static String getFieldErrorExplanation(String variableName) {
		String inputCanonicalName = Constants.FIELDS_CANONICAL_NAME.get(variableName);
		if (inputCanonicalName == null) {
			inputCanonicalName = variableName;
		}
		String inputFormatExplanation = Constants.FIELDS_FORMAT_EXPLANATION.get(variableName);
		if (inputFormatExplanation == null) {
			inputFormatExplanation = "";
		} else {
			// Formatting nuisances
			inputFormatExplanation = ": " + inputFormatExplanation;
		}
		// Parse output to fit on the message dialog
		inputFormatExplanation = inputCanonicalName + " is invalid" + inputFormatExplanation;
		return inputFormatExplanation;
	}
}
