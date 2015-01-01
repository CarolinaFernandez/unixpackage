package com.github.unixpackages.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.github.unixpackages.data.Constants;
import com.github.unixpackages.data.Variables;
import com.github.unixpackages.utils.StepLoader;

@SuppressWarnings("serial")
public class CommonStep extends CommonPanel {

	protected JPanel stepPanel;
	protected JPanel navigationPanel;

	private static Class<CommonStep> PREVIOUS_STEP;
	private static Class<CommonStep> NEXT_STEP;

	public CommonStep() {
		// Retrieve step number from current step
		StepLoader.getStepNumber(this);
		computeLinkedSteps();
	}

	public CommonStep(JFrame frame, JPanel panel) {
		super(frame);
		// Generate navigation panel
		this.navigationPanel = generateNavigationPanel();
        
		// Add step content panel within normal content panel
        this.stepPanel = panel;
        this.contentPanel.add(this.stepPanel);
        // Compute proper height for content panel, leaving space for the rest of menus
        this.contentPanel.setPreferredSize(new Dimension(Constants.FRAME_WIDTH, (int) (Constants.FRAME_HEIGHT - 1.4*((int) Constants.NAVIGATION_HEIGHT + Constants.TITLE_HEIGHT + Constants.STEPTITLE_HEIGHT))));
		this.add(this.navigationPanel, BorderLayout.PAGE_END);
	}

    public JPanel generateNavigationPanel() {
    	computeLinkedSteps();
    	// Clear if possible
		if (this.navigationPanel != null) {
			this.navigationPanel.removeAll();
		}
    	// Create inferior panel for buttons
    	this.navigationPanel = new JPanel(new BorderLayout());
        JButton buttonPrevious = new JButton("Previous");
        buttonPrevious.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Execute when button is pressed
                previousStep(e);
            }
        });
        String textNext = "Next";
        if (StepLoader.currentStep == Constants.STEPS_METHODS.size()) {
        	textNext = "Finish";
        }
        JButton buttonNext = new JButton(textNext);
        buttonNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Execute when button is pressed
                nextStep(e);
            }
        });
        // Set dimension for navigation panel and buttons within
        this.navigationPanel.setPreferredSize(new Dimension(Constants.FRAME_WIDTH, Constants.NAVIGATION_HEIGHT));
        buttonPrevious.setPreferredSize(Constants.NAVIGATION_DIMENSION);
        buttonNext.setPreferredSize(Constants.NAVIGATION_DIMENSION);
        // Do not add buttons when no step is available
        if (PREVIOUS_STEP != null) {
        	this.navigationPanel.add(buttonPrevious, BorderLayout.WEST);
        }
        if (NEXT_STEP != null) {
        	this.navigationPanel.add(buttonNext, BorderLayout.EAST);
        }
		if (this.parentFrame != null) {
			this.parentFrame.getRootPane().setDefaultButton(buttonNext);
		}

		JLabel stepCounter = new JLabel("Step: " + StepLoader.currentStep + " / " + StepLoader.steps.size(), JLabel.CENTER);
        this.navigationPanel.add(stepCounter);
        return this.navigationPanel;
    }

    private void nextStep(ActionEvent e) {    	
    	// Execute when button is pressed. Override per step
    	this.saveDataIntoVariables();
        // Calls to the navigation panel method with 'next' flag
    	this.addNavigationPanel(NEXT_STEP);
    }
    
    private void previousStep(ActionEvent e) {
    	// Execute when button is pressed
    	this.saveDataIntoVariables();
        // Calls to the navigation panel method with 'previous' flag
    	this.addNavigationPanel(PREVIOUS_STEP);
    }
    
    private void setDefaultButton() {
        // Default to the 'Next' button when possible (if button exists)
    	if (this.parentFrame != null) {
    		int numberComponents = 1+1;
        	if (this.navigationPanel.getComponent(numberComponents-1) != null) {
        	if (this.navigationPanel.getComponent(numberComponents-1).getClass().toString().contains("JButton")) {
        		this.parentFrame.getRootPane().setDefaultButton((JButton) this.navigationPanel.getComponent(numberComponents-1));
        	}
        	}
        }
    }

    private void addNavigationPanel(Class<CommonStep> buttonDirection) {
        this.contentPanel.removeAll();
        // Get corresponding title
        this.titlePanel.removeAll();
        // XXX IMPORTANT: recompute current step when navigation panel is generated
    	StepLoader.currentStep = StepLoader.getStepNumber((CommonStep) StepLoader.getStepInstance(buttonDirection));
		this.titlePanel.add(new StepTitlePanel(new BorderLayout()));
        // Retrieve step instance for either previous or next button (depending on 'buttonDirection' value)
        this.contentPanel.add(StepLoader.getStepInstance(buttonDirection));
        this.navigationPanel.add(generateNavigationPanel());
        this.parentFrame.setContentPane(this);
        // Re-packing seems to reduce the overall height of the frame!
//        this.parentFrame.pack();
        // Default to the 'Next' button when possible (if button exists)
        this.setDefaultButton();
    }

    protected void saveDataIntoVariables() {
    	// 1st and only component should be a CommonStep panel
        CommonStep step_panel = (CommonStep) this.contentPanel.getComponents()[0];
        try {
    	    for (Component component : step_panel.getComponents()) {
				if (component.getClass().getName().contains("JTextField")) {
					if (!((JTextField) component).getText().isEmpty()) {
						// Set textfield value into given variable name (name of textfield component)
						Variables.set(component.getName(), ((JTextField) component).getText().toString());
					}
				} else if (component.getClass().getName().contains("JCheckBox")) {
					if (((JCheckBox) component).isSelected()) {
						// Set checkbox value into given variable name (name of checkbox component)
						Variables.set(component.getName(), "true");
					} else {
						Variables.set(component.getName(), "false");
					}
				} else if (component.getClass().getName().contains("JComboBox")) {
					if (!((JComboBox) component).getSelectedItem().toString().isEmpty()) {
						// Set combobox value into given variable name (name of combobox component)
						Variables.set(component.getName(), ((JComboBox) component).getSelectedItem().toString());
					}
				} else if (component.getClass().getName().contains("JRadioButton")) {
					setJRadioButtonValues(component);
				}
    	    }
        } catch (Exception e) {
        	System.out.println("Exception saving data into variables: " + e);
        }
    }

	private void setJRadioButtonValues(Component component) {
		JRadioButton radioPackages = (JRadioButton) component;
		boolean radioSelected = radioPackages.isSelected();
		if (radioSelected) {
			// XXX Custom processing... Not nice.
			Variables.set("PACKAGE_TYPE", component.getName().split("_PACKAGE")[0]); // Retrieve first part of the value
		}
	}

	/**
	 * Determines previous, current and next steps by looking
	 * at the dictionary set in the 'Constants' class.
	 */
	private void computeLinkedSteps() {
		PREVIOUS_STEP = StepLoader.getPreviousStep();
		NEXT_STEP = StepLoader.getNextStep();
	}
}
