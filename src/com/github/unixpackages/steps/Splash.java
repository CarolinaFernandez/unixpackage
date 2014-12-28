package com.github.unixpackages.steps;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SpringLayout;

import com.github.unixpackages.components.CommonStep;
import com.github.unixpackages.utils.SpringUtilities;


@SuppressWarnings("serial")
public class Splash extends CommonStep {

	public Splash() {
        // Clear screen first
        this.removeAll();
        this.setLayout(new SpringLayout());
        // Populate the panel
        JLabel splashLabel = new JLabel("Please follow the next steps in order to configure your UNIX (DEB or RPM) package.");
        this.add(splashLabel, BorderLayout.CENTER);
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(this,
                                        1, 1, //rows, cols
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad
	}
}
