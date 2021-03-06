package com.github.unixpackage.components;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.unixpackage.data.Constants;
import com.github.unixpackage.utils.Listeners;

@SuppressWarnings("serial")
public class CommonFrame extends JFrame {

	protected static JPanel contentPanel;
	protected static JPanel stepContentPanel;
	protected ImageIcon icon = new ImageIcon(Constants.APP_ICON);

	public CommonFrame(JPanel stepContentPanel, String textPrevious,
			String textNext) {
		CommonFrame.stepContentPanel = stepContentPanel;
		createIcon();
		show(textPrevious, textNext);
	}

	public CommonFrame(JPanel contentPanel) {
		// Calls basic constructor
		this(contentPanel, "Previous", "Next");
	}

	/**
	 * Creates the GUI and shows it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private void show(String textPrevious, String textNext) {
		// Set title
		this.setTitle(Constants.APP_DESCRIPTION);
		// Determine size
		this.setPreferredSize(new Dimension(Constants.FRAME_WIDTH,
				Constants.FRAME_HEIGHT));
		// Center frame
		this.setLocationRelativeTo(null);
		centreWindow(this);
		// Set default behaviors on exit (button + ESC)
		Listeners.bindExitButton(this);
		Listeners.bindEscapeKey(this);
		this.getContentPane().setLayout(new GridLayout()); // new BorderLayout()
		// Disable resizing
		this.setResizable(false);
		// Create a panel and add components to it
		contentPanel = new CommonStep(this, stepContentPanel);
		// Set the given panel into frame
		this.setContentPane(contentPanel);
		// Display the window.
		this.pack();
		this.setVisible(true);
	}

	private void createIcon() {
		// Assign icon when possible
		try {
			this.icon = new ImageIcon(getClass().getClassLoader().getResource(
					Constants.APP_IMAGE));
		} catch (Exception e) {
		}
		if (this.icon != null) {
			this.setIconImage(this.icon.getImage());
		}
	}

	private void centreWindow(Window frame) {
		int x = (int) ((Constants.SCREEN_DIMENSION.getWidth() - Constants.FRAME_WIDTH) / 2);
		int y = (int) ((Constants.SCREEN_DIMENSION.getHeight() - Constants.FRAME_HEIGHT) / 2);
		frame.setLocation(x, y);
	}
}
