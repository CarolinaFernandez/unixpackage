package com.github.unixpackage.components;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.github.unixpackage.data.Constants;

@SuppressWarnings("serial")
public class CommonPanel extends JPanel {

	protected JFrame parentFrame;
	protected JPanel headerPanel;
	protected JPanel titlePanel;
	protected JPanel contentPanel;

	public CommonPanel() {
	}

	public CommonPanel(JFrame frame) {
		this.parentFrame = frame;
		this.contentPanel = new JPanel();
		this.headerPanel = new TitlePanel(new BorderLayout(),
				Constants.APP_DESCRIPTION);
		this.titlePanel = new StepTitlePanel(new BorderLayout());
		// Set dimension (minimum, maximum, preferred)
		this.contentPanel.setPreferredSize(Constants.CONTENT_DIMENSION);
		Border topBorder = BorderFactory.createMatteBorder(1, 0, 0, 0,
				Color.black);
		this.titlePanel.setBorder(topBorder);
		Border topBottomBorder = BorderFactory.createMatteBorder(1, 0, 1, 0,
				Color.black);
		this.contentPanel.setBorder(topBottomBorder);
		// Add panels to the main panel
		this.add(this.headerPanel, BorderLayout.PAGE_START);
		this.add(this.titlePanel, BorderLayout.WEST);

		// Add step content panel within normal content panel
		this.add(this.contentPanel, BorderLayout.WEST);
	}

	public JPanel getContentPanel() {
		return this.contentPanel;
	}
}
