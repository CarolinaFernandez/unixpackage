package com.github.unixpackage.steps;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;

import com.github.unixpackage.components.CommonStep;
import com.github.unixpackage.components.TablePanel;
import com.github.unixpackage.data.Constants;
import com.github.unixpackage.data.UnixLogger;
import com.github.unixpackage.data.Variables;
import com.github.unixpackage.utils.Files;
import com.github.unixpackage.utils.Shell;

@SuppressWarnings("serial")
public class EditPackageFiles extends CommonStep {

	private String[] columnNames = { "File", "Modified" };
	private TablePanel gregsPanel;
	public static ArrayList<String> commandList;

	// Action buttons
	JButton editFileButton;
	JButton removeFileButton;

	public EditPackageFiles() {
		// Clear screen first
		this.removeAll();

		// Initialisation
		if (Variables._PACKAGE_CONTENT_FILES == null) {
			Variables._PACKAGE_CONTENT_FILES = fillAndRetrieveFileListInitial();
		}
		gregsPanel = new TablePanel(Variables._PACKAGE_CONTENT_FILES,
				columnNames);

		// Populate the panel
		JLabel splashLabel = new JLabel("Edit any package file");
		String infoLabelContents = "";
		if (Variables.isNull("PACKAGE_TYPE")
				|| Variables.PACKAGE_TYPE.equals(Constants.BUNDLE_TYPE_DEB)) {
			infoLabelContents = "Note: every edited file ending in '.ex' will be added to the bundle";
		}
		JLabel infoLabel = new JLabel(infoLabelContents);

		// Edit file
		editFileButton = new JButton("Edit file");
		editFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					JTable table = gregsPanel.getTable();
					EditPackageFiles.editFileInPosition(table);
					updateTableContents();
				} catch (Exception ex) {
				}
			}
		});
		this.add(editFileButton);

		removeFileButton = new JButton("Remove file");
		removeFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				removeFileInPosition();
				removeFileButton.setEnabled(false);
				editFileButton.setEnabled(false);
				updateTableContents();
			}
		});
		this.add(removeFileButton);

		// Button initialization
		editFileButton.setEnabled(false);
		removeFileButton.setEnabled(false);

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
					editFileButton.setEnabled(true);
					removeFileButton.setEnabled(true);
					updateTableContents();
				}
				gregsPanel.getTable().setFocusable(false);
			}
		});

		// Update contents of table when mouse enters the table
		gregsPanel.getMainPanel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) {
				updateTableContents();
			}
		});

		// Focus listener on the JTable to ensure that the files can be edited
		// on double click
		gregsPanel.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				JTable table = (JTable) evt.getSource();
				if (evt.getClickCount() >= 2) {
					EditPackageFiles.editFileInPosition(table);
					updateTableContents();
				}
			}

			@Override
			public void mouseEntered(MouseEvent evt) {
				updateTableContents();
			}
		});

		this.add(gregsPanel.getMainPanel());

		/* Layout */
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		this.add(splashLabel);
		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(infoLabel)
								.addGroup(
										layout.createSequentialGroup()
												.addGroup(
														layout.createParallelGroup(GroupLayout.Alignment.LEADING))
												.addGroup(
														layout.createParallelGroup(
																GroupLayout.Alignment.LEADING)
																.addComponent(
																		editFileButton))
												.addGroup(
														layout.createParallelGroup(
																GroupLayout.Alignment.LEADING)
																.addComponent(
																		removeFileButton)))
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(
														gregsPanel
																.getMainPanel()))));
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(editFileButton)
								.addComponent(removeFileButton))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE).addComponent(
								infoLabel))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								gregsPanel.getMainPanel())));
	}

	private ArrayList<ArrayList<String>> fillAndRetrieveFileListInitial() {
		ArrayList<ArrayList<String>> directoryContentsFinal = new ArrayList<ArrayList<String>>();
		ArrayList<String> directoryContents = showDirectoryContents();
		for (String directoryContent : directoryContents) {
			String directoryContentAbsolutePath = directoryContent;
			ArrayList<String> directoryFiles = new ArrayList<String>();
			directoryFiles.add(directoryContent);
			// First time: add files and their hashes to internal tracking list
			directoryContentAbsolutePath = Files
					.getAbsolutePathPackageFile(directoryContent);
			UnixLogger.LOGGER.debug("Listing directory contents: "
					+ directoryContentAbsolutePath + "(hash: "
					+ Files.getHash(directoryContentAbsolutePath) + ")");
			if (Variables._PACKAGE_CONTENT_FILES_HASH == null) {
				Variables._PACKAGE_CONTENT_FILES_HASH = new HashMap<String, String>();
			}
			// Initialise structure with original hashes
			Variables._PACKAGE_CONTENT_FILES_HASH.put(directoryContent,
					Files.getHash(directoryContentAbsolutePath));

			if (Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS == null) {
				Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS = new HashMap<String, String>();
			}
			// Initialise structure with edition status
			Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS.put(
					directoryContent, "");
			directoryFiles.add(Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS
					.get(directoryContent));
			directoryContentsFinal.add(directoryFiles);
		}

		// First time: copy structure with hashes
		if (Variables._PACKAGE_CONTENT_FILES_HASH != null
				&& Variables._PACKAGE_CONTENT_FILES_MODIFIED_HASH == null) {
			Variables._PACKAGE_CONTENT_FILES_MODIFIED_HASH = new HashMap<String, String>(
					Variables._PACKAGE_CONTENT_FILES_HASH);
		}
		return directoryContentsFinal;
	}

	private ArrayList<ArrayList<String>> fillAndRetrieveFileList() {
		ArrayList<ArrayList<String>> directoryContentsFinal = new ArrayList<ArrayList<String>>();
		ArrayList<String> directoryContents = showDirectoryContents();
		for (String directoryContent : directoryContents) {
			String directoryContentAbsolutePath = directoryContent;
			ArrayList<String> directoryFiles = new ArrayList<String>();
			directoryFiles.add(directoryContent);
			// First time: add files and their hashes to internal tracking list
			directoryContentAbsolutePath = Files
					.getAbsolutePathPackageFile(directoryContent);
			if (Variables._PACKAGE_CONTENT_FILES_MODIFIED_HASH != null) {
				Variables._PACKAGE_CONTENT_FILES_MODIFIED_HASH.put(
						directoryContent,
						Files.getHash(directoryContentAbsolutePath));
			}
			// Check modified file hash against initial file hash to determine
			// if file was edited (*)
			if (Variables._PACKAGE_CONTENT_FILES_HASH != null
					&& Variables._PACKAGE_CONTENT_FILES_MODIFIED_HASH != null) {
				String selectedFilesHashValue = Variables._PACKAGE_CONTENT_FILES_HASH
						.get(directoryContent);
				String selectedFilesModifiedHashValue = Variables._PACKAGE_CONTENT_FILES_MODIFIED_HASH
						.get(directoryContent);
				if (selectedFilesHashValue != null
						&& selectedFilesModifiedHashValue != null) {
					if (selectedFilesHashValue
							.equals(selectedFilesModifiedHashValue)) {
						// Initialise structure with edition status
						Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS.put(
								directoryContent, "");
					} else {
						// Initialise structure with edition status
						Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS.put(
								directoryContent, "*");
					}
					directoryFiles
							.add(Variables._PACKAGE_CONTENT_FILES_EDITION_STATUS
									.get(directoryContent));
				}
			}
			directoryContentsFinal.add(directoryFiles);
		}
		return directoryContentsFinal;
	}

	// Add files under temporary folder into the list shown to the user
	private ArrayList<String> showDirectoryContents(String directory) {
		File dir = new File(directory);
		ArrayList<String> fileList = new ArrayList<String>();
		try {
			for (File file : dir.listFiles()) {
				// Ignore directories and files being edited
				if (file.isFile() && !file.getName().endsWith("~")) {
					fileList.add(file.getName());
				}
			}
		} catch (Exception e) {
			UnixLogger.LOGGER
					.error("Could not find UNIX package files. Exception: " + e);
		}
		UnixLogger.LOGGER.debug("Listing directory contents: " + fileList);
		return fileList;
	}

	private ArrayList<String> showDirectoryContents() {
		File dir = new File(Files.getAbsolutePathPackageFile(""));
		return showDirectoryContents(dir.toString());
	}

	public static Object[] getSelectedRows(JTable table) {
		int[] selectedRows = table.getSelectedRows();
		Object[] selectedValues = new Object[selectedRows.length];
		for (int i = 0; i < selectedRows.length; i++) {
			Object value = table.getValueAt(selectedRows[i], 0);
			if (value != null) {
				selectedValues[i] = value;
			}
		}
		return selectedValues;
	}

	public static void editFileInPosition(JTable table) {
		Object[] selectedValues = EditPackageFiles.getSelectedRows(table);
		for (Object value : selectedValues) {
			commandList = new ArrayList<String>();
			commandList.add(Constants.OPEN_COMMAND);
			commandList.add(Files.getAbsolutePathPackageFile((String) value));
			// Execute concurrently
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					try {
						// Much better than directly calling 'gedit'
						if (java.awt.Desktop.isDesktopSupported()) {
							java.awt.Desktop.getDesktop().open(
									new File(commandList.get(1)));
						} else {
							// If desktop is not available, perform a direct
							// call
							Shell.execute(commandList);

						}
					} catch (IOException e) {
						UnixLogger.LOGGER.trace("File '" + commandList.get(1)
								+ "' could not be edited. Details: " + e);
					}
				}
			});
			t1.start();
		}
	}

	public void removeFileInPosition() {
		int[] chosenRows = gregsPanel.getTable().getSelectedRows();
		for (int row : chosenRows) {
			String selectedValue = (String) gregsPanel.getTable().getValueAt(
					row, 0);
			// Remove from structures
			Variables._PACKAGE_CONTENT_FILES_HASH.remove(selectedValue);
			Variables._PACKAGE_CONTENT_FILES_MODIFIED_HASH
					.remove(selectedValue);
			selectedValue = Files.getAbsolutePathPackageFile(selectedValue);
			// Remove from disk
			File selectedFile = new File((String) selectedValue);
			if (selectedFile.isFile()) {
				selectedFile.delete();
			}
		}
	}

	private void updateTableContents() {
		// Fill file list with contents of directory
		if (gregsPanel != null) {
			UnixLogger.LOGGER
					.debug("Updating retrieved contents for directory");
			Variables._PACKAGE_CONTENT_FILES = fillAndRetrieveFileList();
			// Back up selected row to reselect after table is updated
			int selectedRow = gregsPanel.getTable().getSelectedRow();
			gregsPanel.fireTableDataChanged();
			gregsPanel.setTableModelDataVector(
					Variables._PACKAGE_CONTENT_FILES, columnNames);
			if (selectedRow >= 0
					&& selectedRow <= gregsPanel.getTable().getRowCount()) {
				gregsPanel.getTable().setRowSelectionInterval(selectedRow,
						selectedRow);
			}
		}
	}
}
