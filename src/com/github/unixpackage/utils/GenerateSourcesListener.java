package com.github.unixpackage.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.Variables;

public class GenerateSourcesListener implements ActionListener {

    private JScrollPane textareaScrollPane;
    private JTextArea textArea;
    private JLabel textAreaLabel;

    public GenerateSourcesListener(JScrollPane textareaScrollPane, JTextArea textArea, JLabel textAreaLabel) {
        this.textareaScrollPane = textareaScrollPane;
        this.textArea = textArea;
        this.textAreaLabel = textAreaLabel;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// Send full name of the calling class
		String runPackageName = ((JButton) e.getSource()).getParent().getClass().getPackage().getName();
		String generateMethod = "generateDebianPackage";
		if (Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_RPM)) {
			generateMethod = "generateRedHatPackage";
		}
        Thread thread = new UnixThread(runPackageName + ".GeneratePackage", generateMethod);
        thread.start();
		// Block "Generate" button after packet is processed
		((JButton) e.getSource()).setEnabled(false);
		
		this.textareaScrollPane.setVisible(true);
		this.textArea.setVisible(true);
		this.textAreaLabel.setVisible(true);
        
        // Now create a new TextAreaOutputStream to write to our JTextArea control and wrap a
        // PrintStream around it to support the println/printf methods.
        PrintStream out = new PrintStream( new TextAreaOutputStream( textArea ) );

        // Redirect standard output stream to the TextAreaOutputStream
        System.setOut( out );

        // Redirect standard error stream to the TextAreaOutputStream
//        System.setErr( out );
	}
}
