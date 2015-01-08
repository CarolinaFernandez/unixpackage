package com.github.unixpackage.utils;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import com.github.unixpackage.components.CommonFrame;
import com.github.unixpackage.data.UnixPreferences;

@SuppressWarnings("serial")
public class Listeners {

	public static void bindEscapeKey(final CommonFrame component) {
		// Allows closing by pressing ESC key
		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
				0, false);
		Action escapeAction = new AbstractAction() {
			// Close the frame when the user presses escape
			public void actionPerformed(ActionEvent e) {
				Listeners.onExit(component);
			}
		};
		component.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(escapeKeyStroke, "ESCAPE");
		component.getRootPane().getActionMap().put("ESCAPE", escapeAction);
	}

	public static void bindExitButton(final CommonFrame component) {
		component.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Listeners.onExit(component, e);
			}
		});
		// Allow to close when clicking 'X' button
		component.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void onLoad() {
		// Load preferences from file
		UnixPreferences preferences = new UnixPreferences();
		preferences.loadFromFile();
	}

	private static void onExit() {
		/**
		 * Performs any needed post-processing operation
		 */
		// Save preferences into file
		UnixPreferences preferences = new UnixPreferences();
		preferences.saveToFile();
		// Perform post-processing
		Shell.postProcess();
	}

	private static void onExit(final CommonFrame component) {
		Listeners.onExit();
		component.dispose();
	}

	private static void onExit(final CommonFrame component, WindowEvent e) {
		Listeners.onExit();
		e.getWindow().dispose();
	}
}
