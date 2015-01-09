package com.github.unixpackage.steps;

import java.awt.GridLayout;

import javax.swing.JLabel;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;

@SuppressWarnings("serial")
public class ReviewPackageInfo extends CommonStep {

	public ReviewPackageInfo() {
		// Clear screen first
		this.removeAll();

		JLabel l;
		int numRows = 0;

		// Package info
		l = new JLabel("Type of UNIX package: ", JLabel.TRAILING);
		this.add(l);
		String packageTypeOS;
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			packageTypeOS = "Debian";
		} else {
			packageTypeOS = "Red Hat";
		}
		l = new JLabel(Variables.PACKAGE_TYPE + " (" + packageTypeOS + ")");
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		this.add(l);
		numRows++;

		l = new JLabel("Bundle mode: ", JLabel.TRAILING);
		this.add(l);
		l = new JLabel(Variables.BUNDLE_MODE);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		this.add(l);
		numRows++;

		// Path of source files only present in advanced mode
		if (!Variables.isNull("BUNDLE_MODE")
				&& Variables.BUNDLE_MODE.equals(Constants.BUNDLE_MODE_ADVANCED)) {
			l = new JLabel("Path of source files: ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.BUNDLE_MODE_ADVANCED_PATH);
			l.setToolTipText(Variables.BUNDLE_MODE_ADVANCED_PATH);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;
		}

		l = new JLabel("Sign with GPG: ", JLabel.TRAILING);
		this.add(l);
		String signWithGPG;
		if (Variables.PACKAGE_SIGN) {
			signWithGPG = "yes";
		} else {
			signWithGPG = "no";
		}
		l = new JLabel(signWithGPG);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		this.add(l);
		numRows++;

		if (!(Variables.isNull("BUNDLE_MODE") || Variables.BUNDLE_MODE
				.equals(Constants.BUNDLE_MODE_ADVANCED))) {
			// Author info
			l = new JLabel("Maintainer name: ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.MAINTAINER_NAME);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;

			l = new JLabel("Maintainer e-mail: ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.MAINTAINER_EMAIL);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;

			l = new JLabel("Description (short): ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.PACKAGE_SHORT_DESCRIPTION);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;

			l = new JLabel("Description: ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.PACKAGE_DESCRIPTION);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;

			l = new JLabel("Version[-Revision]: ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.PACKAGE_VERSION);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;

			l = new JLabel("License: ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.PACKAGE_LICENSE);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;

			l = new JLabel("Class: ", JLabel.TRAILING);
			this.add(l);
			l = new JLabel(Variables.PACKAGE_CLASS);
			l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
			this.add(l);
			numRows++;
		}

		this.setLayout(new GridLayout(numRows, 2));
	}
}
