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

	// RegExp for e-mail
	private static final Pattern rfc2822 = Pattern.compile(
	        "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
	);
	// RegExp for package name
	private static final Pattern packageNameRE = Pattern.compile(
	        "^[a-z]{2,}([a-z]|[0-9]|[+]|[-]|[.])*?$"
	);
	// RegExp for package website
	private static final Pattern packageWebsiteRE = Pattern.compile(
			"^([a-z]*[://])?([a-zA-Z0-9]|[-_./#?&%$=])*?$"
			);
	// RegExp for package version
	private static final Pattern packageVersionRE = Pattern.compile(
			"^[0-9]+([.][0-9]+)(-[0-9]+)?$"
	);
	
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
	
	private void indicateErrorCondition(Component component) {
		JLabel foundLabel = this.findLabelForComponent(component);
		if (foundLabel != null) {
			foundLabel.setForeground(Color.RED);
		}
	}
	
	private void indicateNormalCondition(Component component) {
		JLabel foundLabel = this.findLabelForComponent(component);
		if (foundLabel != null) {
			foundLabel.setForeground(Color.BLACK);
		}
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
			if (fieldName.equals("PACKAGE_NAME")) {
				if (!packageNameRE.matcher(text).matches()) {
					throw new Exception();
				}
			} else if (fieldName.equals("PACKAGE_SHORT_DESCRIPTION")) {
				if (text.length() > Constants.PACKAGE_SHORT_DESCRIPTION_MAX_LENGTH) {
					throw new Exception();
				}
			} else if (fieldName.equals("PACKAGE_WEBSITE")) {
				if (text.length() > 0) {
					if (!packageWebsiteRE.matcher(text).matches()) {
						throw new Exception();
					}
				}
			} else if (fieldName.equals("PACKAGE_VERSION")) {
				if (!packageVersionRE.matcher(text).matches()) {
					throw new Exception();
				}
			} else if (fieldName.equals("MAINTAINER_EMAIL")) {
				if (!rfc2822.matcher(text).matches()) {
					throw new Exception();
				}
			}
			this.indicateNormalCondition(input);
		} catch (Exception e) {
			this.indicateErrorCondition(input);
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
			String inputCanonicalName = Constants.FIELDS_CANONICAL_NAME.get(input.getName());
			if (inputCanonicalName == null) {
				inputCanonicalName = input.getName();
			}
			String inputFormatExplanation = Constants.FIELDS_FORMAT_EXPLANATION.get(input.getName());
			if (inputFormatExplanation == null) {
				inputFormatExplanation = "";
			} else {
				// Formatting nuisances
				inputFormatExplanation = ": " + inputFormatExplanation;
			}
			// Parse output to fit on the message dialog
			inputFormatExplanation = inputCanonicalName + " is invalid" + inputFormatExplanation;
			inputFormatExplanation = inputFormatExplanation.replaceAll("(.{" + Constants.VALIDATION_FORMAT_EXPLANATION_MAX_LENGTH + "})", "$1\n");
			JOptionPane.showMessageDialog(null, inputFormatExplanation);
		}

		return valid;
	}
}
