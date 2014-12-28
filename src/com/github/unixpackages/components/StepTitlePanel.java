package com.github.unixpackages.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.unixpackages.data.Constants;
import com.github.unixpackages.utils.StepLoader;

@SuppressWarnings("serial")
public class StepTitlePanel extends JPanel {

	StepTitlePanel(LayoutManager layout) {
		// Calls more complex constructor with current step as a parameter
		this(layout, StepLoader.currentStep);
	}

	StepTitlePanel(LayoutManager layout, Component[] components) {
		// Calls basic constructor
		this(layout);
		// Then add the given components to the panel
		for (Component component : components) {
			this.add(component);
		}
	}

	/**
	 * Constructor that creates a label for the text
	 * associated to the given step number.
	 * 
	 * @param layout LayoutManager to be used
	 * @param title Some text for the header
	 */
	StepTitlePanel(LayoutManager layout, int step) {
		this.setLayout(layout);
		this.setMinimumSize(Constants.STEPTITLE_DIMENSION);
		this.setPreferredSize(Constants.STEPTITLE_DIMENSION);

		JLabel title_label = new JLabel(Constants.STEPS_DESCRIPTIONS.get(step));
		title_label.setFont(Constants.STEPTITLE_FONT);
		title_label.setForeground(Constants.STEPTITLE_FOREGROUND);
		this.add(title_label, BorderLayout.WEST);
	}
}
