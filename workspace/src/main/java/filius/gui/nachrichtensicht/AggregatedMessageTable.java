/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         AndrÃ© Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja HaÃŸler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 ** 
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.gui.nachrichtensicht;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.nachrichten.Lauscher;
import filius.rahmenprogramm.nachrichten.LauscherBeobachter;

public class AggregatedMessageTable extends JTable implements LauscherBeobachter, I18n {

	private static final long serialVersionUID = 1L;

	/** Index der Spalte, in der die Schicht des Protokollschichtenmodells steht */
	public static final int SCHICHT_SPALTE = 5;

	private String interfaceId;

	private JCheckBoxMenuItem checkbox;

	private JDialog dialog;
	private JScrollPane scrollPane = null;
	private boolean autoscroll = true;
	private JPopupMenu menu;

	public AggregatedMessageTable(JDialog dialog, String macAddress) {
		super();

		TableColumn col;
		DefaultTableColumnModel columnModel;

		this.dialog = dialog;
		setinterfaceId(macAddress);

		this.initTableModel();

		this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		columnModel = new DefaultTableColumnModel();
		String[] spalten = Lauscher.SPALTEN;
		for (int i = 0; i < spalten.length; i++) {
			col = new TableColumn();
			col.setHeaderValue(spalten[i]);
			col.setIdentifier(spalten[i]);
			col.setModelIndex(i);
			columnModel.addColumn(col);
		}

		this.setColumnModel(columnModel);

		this.setIntercellSpacing(new Dimension(0, 5));
		this.setRowHeight(25);
		this.setEnabled(true);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setShowGrid(false);
		this.setColumnSelectionAllowed(false);
		this.setBackground(Color.DARK_GRAY);
		this.getTableHeader().setReorderingAllowed(false);
		this.setFillsViewportHeight(true);

		initTableColumnWidth();
		this.setDefaultRenderer(Object.class, new LauscherTableCellRenderer());
		initKontextMenue();

		this.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 3) {
					menu.setVisible(true);
					menu.show(getTabelle(), e.getX(), e.getY());
				}
			}
		});

		update();
	}

	private void initTableModel() {
		LauscherTableModel tableModel = new LauscherTableModel();
		tableModel.addTableModelListener(this);
		tableModel.setColumnIdentifiers(Lauscher.SPALTEN);
		this.setModel(tableModel);
	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	private AggregatedMessageTable getTabelle() {
		return this;
	}

	private void initTableColumnWidth() {

		this.getColumnModel().getColumn(0).setMaxWidth(40);
		this.getColumnModel().getColumn(0).setPreferredWidth(30);
		this.getColumnModel().getColumn(1).setMaxWidth(120);
		this.getColumnModel().getColumn(1).setPreferredWidth(90);
		this.getColumnModel().getColumn(2).setMaxWidth(180);
		this.getColumnModel().getColumn(2).setPreferredWidth(140);
		this.getColumnModel().getColumn(3).setMaxWidth(180);
		this.getColumnModel().getColumn(3).setPreferredWidth(140);
		this.getColumnModel().getColumn(4).setMaxWidth(100);
		this.getColumnModel().getColumn(4).setPreferredWidth(70);
		this.getColumnModel().getColumn(5).setMaxWidth(140);
		this.getColumnModel().getColumn(5).setPreferredWidth(100);
		this.getColumnModel().getColumn(6).setMaxWidth(Integer.MAX_VALUE);
		this.getColumnModel().getColumn(6).setPreferredWidth(500);
		this.getColumnModel().getColumn(6).setResizable(true);
	}

	private void initKontextMenue() {
		JMenuItem menuItem;

		menu = new JPopupMenu();

		menuItem = new JMenuItem(messages.getString("nachrichtentabelle_msg7"));
		menuItem.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				menu.setVisible(false);

				Lauscher.getLauscher().reset();
			}
		});
		menu.add(menuItem);

		checkbox = new JCheckBoxMenuItem(messages.getString("nachrichtentabelle_msg8"), autoscroll);
		checkbox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (autoscroll != checkbox.getState()) {
					menu.setVisible(false);
					autoscroll = checkbox.getState();
					update();
				}
			}
		});
		menu.add(checkbox);

		menu.setVisible(false);
		dialog.getRootPane().getLayeredPane().add(menu);
	}

	private void setinterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;

		Lauscher.getLauscher().addBeobachter(interfaceId, this);
	}

	public synchronized void update() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (NachrichtenTabelle), update()");
		Object[][] daten;

		daten = Lauscher.getLauscher().getDaten(interfaceId, true);

		if (daten.length == 0) {
			initTableModel();
			initTableColumnWidth();
		}
		int lastNo = -1;
		if (getModel().getRowCount() > 0) {
			lastNo = Integer.parseInt(this.getModel().getValueAt(this.getModel().getRowCount() - 1, 0).toString());
		}

		int currentNo = 1;
		int previousNo = 1;
		for (int i = 0; i < daten.length; i++) {
			currentNo = Integer.parseInt(daten[i][0].toString());
			if (currentNo <= lastNo || currentNo == previousNo) {
				continue;
			}

			if (currentNo > previousNo && previousNo > lastNo && i > 0) {
				Object[] row = daten[i - 1];
				addRowData(row);
				previousNo = currentNo;
			}
		}
		if (currentNo > lastNo && daten.length > 0) {
			addRowData(daten[daten.length - 1]);
		}

		((DefaultTableModel) this.getModel()).fireTableDataChanged();
		if (this.getRowCount() > 0 && scrollPane != null && scrollPane.getViewport() != null && autoscroll) {
			scrollPane.getViewport().setViewPosition(new Point(0, this.getHeight()));
		}
	}

	private void addRowData(Object[] row) {
		Vector<Object> rowData = new Vector<Object>(row.length);
		for (int col = 0; col < row.length; col++) {
			rowData.add(col, row[col]);
		}
		((DefaultTableModel) this.getModel()).addRow(rowData);
	}
}
