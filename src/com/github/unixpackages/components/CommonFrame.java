package com.github.unixpackages.components;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.unixpackages.data.Constants;
import com.github.unixpackages.utils.Listeners;

@SuppressWarnings("serial")
public class CommonFrame extends JFrame {

	protected static JPanel contentPanel;
	protected static JPanel stepContentPanel;
	protected ImageIcon icon = new ImageIcon(Constants.APP_ICON);

	public CommonFrame(JPanel stepContentPanel, String textPrevious, String textNext) {
		CommonFrame.stepContentPanel = stepContentPanel;
		createIcon();
		show(textPrevious, textNext);
	}

	public CommonFrame(JPanel contentPanel) {
		// Calls basic constructor
        this(contentPanel, "Previous", "Next");
	}

    /**
     * Creates the GUI and shows it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void show(String textPrevious, String textNext) {
        // Determine size
        this.setPreferredSize(new Dimension(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT));
        // Center frame
        this.setLocationRelativeTo(null);
        centreWindow(this);
        // Set default behaviors on exit (button + ESC)
        Listeners.bindExitButton(this);
        Listeners.bindEscapeKey(this);
        this.getContentPane().setLayout(new GridLayout());	// new BorderLayout()
        // Disable resizing
        this.setResizable(false);
        //Create a panel and add components to it
        contentPanel = new CommonStep(this, stepContentPanel);
        // Set the given panel into frame
        this.setContentPane(contentPanel);
        //Display the window.
        this.pack();
        this.setVisible(true);
    }

    private void createIcon() {
    	// Create icon
    	this.icon = new ImageIcon(getClass().getClassLoader().getResource(Constants.APP_ICON));
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
