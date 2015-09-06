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
		if (Variables.isNull("PACKAGE_TYPE")
				|| Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			packageTypeOS = "Debian";
			Variables.set("PACKAGE_TYPE", Constants.BUNDLE_TYPE_DEB);
		} else {
			packageTypeOS = "Red Hat";
			Variables.set("PACKAGE_TYPE", Constants.BUNDLE_TYPE_RPM);
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

		// Author info
		l = new JLabel("Maintainer: ", JLabel.TRAILING);
		this.add(l);
		String maintainerData = Variables.MAINTAINER_NAME + " ("
				+ Variables.MAINTAINER_EMAIL + ")";
		l = new JLabel(maintainerData);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		l.setToolTipText(maintainerData);
		this.add(l);
		numRows++;

		l = new JLabel("Description (short): ", JLabel.TRAILING);
		this.add(l);
		l = new JLabel(Variables.PACKAGE_SHORT_DESCRIPTION);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		l.setToolTipText(Variables.PACKAGE_SHORT_DESCRIPTION);
		this.add(l);
		numRows++;

		l = new JLabel("Description: ", JLabel.TRAILING);
		this.add(l);
		l = new JLabel(Variables.PACKAGE_DESCRIPTION);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		l.setToolTipText(Variables.PACKAGE_DESCRIPTION);
		this.add(l);
		numRows++;

		String versionRevisionLabel = "Version[-Revision]";
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			versionRevisionLabel = "Version-Revision";
		}
		l = new JLabel(versionRevisionLabel + ": ", JLabel.TRAILING);
		this.add(l);
		l = new JLabel(Variables.PACKAGE_VERSION);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		this.add(l);
		numRows++;

		l = new JLabel("Licence: ", JLabel.TRAILING);
		this.add(l);
		l = new JLabel(Variables.PACKAGE_LICENCE);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		this.add(l);
		numRows++;

		String classLabel = "Class";
		String classLabelContents = Variables.PACKAGE_CLASS + " ("
				+ Constants.PACKAGE_CLASSES_DEB.get(Variables.PACKAGE_CLASS)
				+ ")";
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			classLabel = "Architecture";
			classLabelContents = Variables.PACKAGE_CLASS
					+ " ("
					+ Constants.PACKAGE_CLASSES_RPM
							.get(Variables.PACKAGE_CLASS) + ")";
		}
		l = new JLabel(classLabel + ": ", JLabel.TRAILING);
		this.add(l);
		l = new JLabel(classLabelContents);
		l.setPreferredSize(Constants.TEXTFIELD_DIMENSION);
		this.add(l);
		numRows++;

		String sectionLabel = "Section, Priority";
		String sectionLabelContents = Variables.PACKAGE_SECTION + ", "
				+ Variables.PACKAGE_PRIORITY;
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			sectionLabel = "Group";
			sectionLabelContents = Variables.PACKAGE_SECTION;
		}
		l = new JLabel(sectionLabel + ": ", JLabel.TRAILING);
		this.add(l);
		l = new JLabel(sectionLabelContents);
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

		this.setLayout(new GridLayout(numRows, 2));
	}
}
