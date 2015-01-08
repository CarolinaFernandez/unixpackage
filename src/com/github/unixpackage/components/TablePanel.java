package com.github.unixpackage.components;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.github.unixpackage.data.Constants;

@SuppressWarnings("serial")
public class TablePanel extends DefaultTableModel {
	private JPanel mainPanel = new JPanel();
	private DefaultTableModel dm;
	private JTable table = new JTable();

	// private JButton changeTableBtn = new JButton();
	private JScrollPane scrollpane = new JScrollPane(table);

	/*
	 * Simple table
	 */
	public TablePanel(ArrayList<ArrayList<String>> data, String[] columnNames) {
		this("", convertDoubleArrayListToDoubleVector(data), columnNames);
	}

	public TablePanel(Object[][] data, Object[] columnNames) {
		this("", data, columnNames);
	}

	/*
	 * Table inside frame with title
	 */
	public TablePanel(String title, Object[][] data, Object[] columnNames) {
		dm = new DefaultTableModel(data, columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// Disable edition of cells
				return false;
			}
		};
		table.setModel(dm);
		table.setAutoscrolls(true);
		table.setRowSelectionAllowed(true);
		JPanel btnPanel = new JPanel();
		if (title != "") {
			mainPanel.setBorder(BorderFactory.createTitledBorder(title));
		}
		mainPanel.setLayout(new BorderLayout(5, 5));
		mainPanel.add(scrollpane, BorderLayout.CENTER);
		mainPanel.add(btnPanel, BorderLayout.PAGE_END);
		// Determine size
		mainPanel.setPreferredSize(Constants.FILECHOOSER_DIMENSION);
	}

	private static Object[][] convertDoubleArrayListToDoubleVector(
			ArrayList<ArrayList<String>> data) {
		int dataLength = data.size();
		Object[][] dataParsed = new Object[dataLength][];

		for (int i = 0; i < data.size(); i++) {
			dataParsed[i] = data.get(i).toArray();
		}
		return dataParsed;
	}

	// TODO: Parameterize ArrayList<ArrayList<T>>
	public void setTableModelDataVector(ArrayList<ArrayList<String>> data,
			Object[] columnNames) {
		Object[][] dataParsed = TablePanel
				.convertDoubleArrayListToDoubleVector(data);
		setTableModelDataVector(dataParsed, columnNames);
	}

	public void setTableModelDataVector(Object[][] data, Object[] columnNames) {
		dm.setDataVector(data, columnNames);
	}

	public JTable getTable() {
		return table;
	}

	@Override
	public void fireTableDataChanged() {
		dm.fireTableDataChanged();
	}

	public JComponent getMainPanel() {
		return mainPanel;
	}
}