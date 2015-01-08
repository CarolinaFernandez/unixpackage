package com.github.unixpackage;

import com.github.unixpackage.components.CommonFrame;
import com.github.unixpackage.utils.StepLoader;

import javax.swing.JPanel;

public class MainApp {

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// 1st step
				new CommonFrame((JPanel) StepLoader.getStepInstance(StepLoader
						.getStepAt(1)));
			}
		});
	}
}
