package com.github.unixpackages.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.unixpackages.data.Constants;

@SuppressWarnings("serial")
public class TitlePanel extends JPanel {

	protected Image backgroundImage;

	TitlePanel(LayoutManager layout) {
		this.setLayout(layout);
		this.setBackground(Constants.TITLE_BACKGROUND);
		this.setMinimumSize(Constants.TITLE_DIMENSION);
		this.setPreferredSize(Constants.TITLE_DIMENSION);
	}

	TitlePanel(LayoutManager layout, Component[] components) {
		// Calls basic constructor
		this(layout);
		// Then add the given components to the panel
		for (Component component : components) {
			this.add(component);
		}
	}

	/**
	 * Constructor that creates a label with the given title.
	 * 
	 * @param layout LayoutManager to be used
	 * @param title Some text for the header
	 */
	TitlePanel(LayoutManager layout, String title) {
		// Calls basic constructor
		this(layout);
		JLabel title_label = new JLabel(title);
		title_label.setFont(Constants.TITLE_FONT);
		title_label.setForeground(Constants.TITLE_FOREGROUND);
		this.add(title_label, BorderLayout.EAST);
	}

	@Override
	public void paintComponent(Graphics g) {
		try {
			// Load images as class path resources
            URL imgURL = getClass().getClassLoader().getResource(Constants.APP_IMAGE);
            if (imgURL != null) {
            	backgroundImage = Toolkit.getDefaultToolkit().getImage(imgURL);
            } else {
            	backgroundImage = ImageIO.read(new File(Constants.APP_IMAGE));
            }
		    super.paintComponent(g);
		    // Draw the background image
		    g.drawImage(backgroundImage, 0, 0, this);
		} catch (Exception e) {
		}
	}
}
