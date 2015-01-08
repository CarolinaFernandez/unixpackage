package com.github.unixpackage.steps;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.components.TablePanel;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.Files;

@SuppressWarnings("serial")
public class SetPackageSources extends CommonStep {

	JScrollPane destinationInstallGraphicalList;
	ArrayList<String> sourceInstallPair = new ArrayList<String>(2);
	private String[] columnNames = { "Source", "Destination" };
	private TablePanel gregsPanel = new TablePanel(fillAndRetrieveFileList(),
			columnNames);

	// Action buttons
	JButton addSourcePath;
	JButton addInstallationPath;
	JButton removePathTupleFromList;

	// NOTE: Use this afterwards
	public SetPackageSources() {
		// Clear screen first
		this.removeAll();

		// Populate the panel
		JLabel splashLabel = new JLabel(
				"Choose the sources to be added to the package");
		// Another initialization
		sourceInstallPair = null;// new ArrayList<String>(2);

		// Source path
		addSourcePath = new JButton("Add source path");
		addSourcePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sourcePath = Files.choosePath();
				if (sourcePath != null && !sourcePath.isEmpty()) {
					try {
						sourceInstallPair.add(sourcePath);
					} catch (Exception ex) {
					}
					addSourcePath.setEnabled(false);
					addInstallationPath.setEnabled(true);
					// addPathTupleToList.setEnabled(false);
					removePathTupleFromList.setEnabled(false);
				}
			}
		});
		this.add(addSourcePath);

		// Installation path
		addInstallationPath = new JButton("Add installation path");
		addInstallationPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String installPath = Files.choosePath();
				if (installPath != null && !installPath.isEmpty()) {
					try {
						sourceInstallPair.add(installPath);
					} catch (Exception ex) {
					}
					addInstallationPath.setEnabled(false);
					updateSourceInstallPairsList();
					addSourcePath.setEnabled(true);
					removePathTupleFromList.setEnabled(false);
				}
			}
		});
		this.add(addInstallationPath);

		removePathTupleFromList = new JButton("Remove paths");
		removePathTupleFromList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] chosenPaths = gregsPanel.getTable().getSelectedRows();
				if (chosenPaths.length > 0) {
					// "Thou shall not access array components bigger than the size of the modified array"
					int i = chosenPaths.length - 1;
					while (i >= 0) {
						Variables.PACKAGE_SOURCE_INSTALL_PAIRS
								.remove(chosenPaths[i]);
						i--;
					}
					updateSourceInstallPairsList();
					// After removing an entry in the table, focus is lost; thus
					// button shall be disabled
					removePathTupleFromList.setEnabled(false);
				}
			}
		});
		this.add(removePathTupleFromList);

		// Button initialization
		addSourcePath.setEnabled(true);
		addInstallationPath.setEnabled(false);
		// addPathTupleToList.setEnabled(false);
		removePathTupleFromList.setEnabled(false);

		// Focus listener on the JTable to ensure that the "Remove" button
		// updates properly
		gregsPanel.getTable().addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				// Re-enable focus after selection is processed
				gregsPanel.getTable().setFocusable(true);
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// Redundant check
				if (gregsPanel.getTable().getSelectedRowCount() > 0) {
					removePathTupleFromList.setEnabled(true);
				} else {
					removePathTupleFromList.setEnabled(false);
				}
				gregsPanel.getTable().setFocusable(false);
			}
		});

		this.add(gregsPanel.getMainPanel());
		updateSourceInstallPairsList();

		/* Layout */
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		this.add(splashLabel);
		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(addSourcePath))
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(
														addInstallationPath))
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(
														removePathTupleFromList)))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								gregsPanel.getMainPanel())));
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(addSourcePath)
								.addComponent(addInstallationPath)
								.addComponent(removePathTupleFromList))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								gregsPanel.getMainPanel())));
	}

	public static <T extends JComponent> T findParent(JComponent comp,
			Class<T> clazz) {
		if (comp == null) {
			return null;
		}
		if (clazz.isInstance(comp)) {
			return (clazz.cast(comp));
		} else {
			return SetPackageSources.findParent((JComponent) comp.getParent(),
					clazz);
		}
	}

	// Properly cleaning array before adding to the table...
	private void cleanInstallPairs() {
		for (int i = 0; i < Variables.PACKAGE_SOURCE_INSTALL_PAIRS.size(); i++) {
			if (Variables.PACKAGE_SOURCE_INSTALL_PAIRS.get(i).isEmpty()) {
				Variables.PACKAGE_SOURCE_INSTALL_PAIRS.remove(i);
			}
		}
	}

	private void setInstallPairsInFileList() {
		try {
			// Cleaning the file list
			if (sourceInstallPair == null) {
				sourceInstallPair = new ArrayList<String>(2);
			}
			if (Variables.PACKAGE_SOURCE_INSTALL_PAIRS == null) {
				// If PACKAGE_SOURCE_INSTALL_PAIRS not set, fill with empty
				// array
				Variables.PACKAGE_SOURCE_INSTALL_PAIRS = new ArrayList<ArrayList<String>>();
			} else {
				Variables.PACKAGE_SOURCE_INSTALL_PAIRS.add(sourceInstallPair);
				// Clean pairs of (source, install) before adding to final list
				this.cleanInstallPairs();
			}
			sourceInstallPair = new ArrayList<String>(2);
		} catch (Exception e) {
			System.out
					.println("Could not find source/install pairs...? More info: "
							+ e);
		}
	}

	private ArrayList<ArrayList<String>> fillAndRetrieveFileList() {
		this.updateSourceInstallPairsList();
		return Variables.PACKAGE_SOURCE_INSTALL_PAIRS;
	}

	private void updateSourceInstallPairsList() {
		// Fill file list with pair of source/install pairs
		this.setInstallPairsInFileList();
		// If panel already rendered, update
		if (gregsPanel != null) {
			gregsPanel.setTableModelDataVector(
					Variables.PACKAGE_SOURCE_INSTALL_PAIRS, columnNames);
			gregsPanel.fireTableDataChanged();
		}
	}

	/*
	 * private void errorMessage(String message) { JDialog dialog = new
	 * JDialog(); JLabel errorLabel = new JLabel(message);
	 * dialog.getContentPane().add(errorLabel, BorderLayout.NORTH);
	 * dialog.add(errorLabel); // FIXME set button again // JButton button = new
	 * JButton("OK"); // dialog.getContentPane().add(button,
	 * BorderLayout.SOUTH); // dialog.add(button); dialog.setSize(300, 300);
	 * dialog.setVisible(true); }
	 */
}
