/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
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
package filius.gui.anwendungssicht;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import filius.gui.netzwerksicht.JFirewallDialog;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.dns.DNSServer;
import filius.software.firewall.Firewall;
import filius.software.www.WebServer;

public class JTableEditable extends JTable {

	public class ColorTableCellRenderer implements TableCellRenderer {
		private HashMap<Integer, Color> cellData = new HashMap<Integer, Color>();

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		        boolean hasFocus, int row, int column) {
			JLabel label = new JLabel(value != null ? value.toString() : "");
			int key = ((row + 1) * 1000) + column;
			label.setOpaque(true);
			if (isSelected)
				label.setBackground(Color.CYAN);
			else {
				if (cellData.containsKey(key)) {
					label.setBackground(cellData.get(key));
				} else { // default color
					label.setBackground(Color.WHITE);
				}
			}
			return label;
		}

		public void setColor(int row, int column, Color color) {
			int key = ((row + 1) * 1000) + column;
			cellData.put(key, color);
		}

		public void resetColor(int row, int column) {
			int key = ((row + 1) * 1000) + column;
			cellData.remove(key);
		}

		public void resetColors() {
			cellData.clear();
		}
	}

	private static final long serialVersionUID = 1L;
	private boolean editable;

	// optional parameter for identifying the table, e.g., whether storing MX or
	// A entries for DNS
	// --> not specific to DNS server, but provides an additional flag to
	// control processes (s. editingStopped)
	private String typeID = null;

	private Object parentGUI;

	public JTableEditable(TableModel model, boolean editable) {
		super(model);
		setEditable(editable);
		for (int i = 0; i < getColumnCount(); i++)
			this.getColumnModel().getColumn(i).setCellRenderer(new ColorTableCellRenderer());
	}

	public JTableEditable(TableModel model, boolean editable, String type) {
		super(model);
		setEditable(editable);
		this.typeID = type;
		for (int i = 0; i < getColumnCount(); i++)
			this.getColumnModel().getColumn(i).setCellRenderer(new ColorTableCellRenderer());
	}

	public void setParentGUI(Object parent) {
		this.parentGUI = parent;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable
	 *            the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isCellEditable(int row, int column) {
		return editable;
	}

	public void editingStopped(ChangeEvent e) {
		// Main.debug.println("editingStopped, source='"+e.getSource()+"'");

		TableCellEditor editor = getCellEditor();
		if (editor != null) {
			// Take in the new value
			String formerValue = (String) getValueAt(editingRow, editingColumn);
			String value = (String) editor.getCellEditorValue();
			if (value == null)
				value = "";
			setValueAt(value, editingRow, editingColumn);

			// store value in DNS records
			if (parentGUI instanceof GUIApplicationDNSServerWindow) {
				if (typeID != null && typeID.equals("A")) {
					if (editingColumn == 0) {
						((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(
						        editingRow, 0, filius.software.dns.ResourceRecord.ADDRESS, value);
					} else {
						((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(
						        editingRow, 3, filius.software.dns.ResourceRecord.ADDRESS, value);
					}
				} else if (typeID != null && typeID.equals("MX")) {
					if (editingColumn == 0) {
						((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(
						        editingRow, 0, filius.software.dns.ResourceRecord.MAIL_EXCHANGE, value);
					} else {
						((DNSServer) ((GUIApplicationDNSServerWindow) parentGUI).holeAnwendung()).changeSingleEntry(
						        editingRow, 3, filius.software.dns.ResourceRecord.MAIL_EXCHANGE, value);
					}
				}
			}
			if (parentGUI instanceof GUIApplicationWebServerWindow) {
				if (typeID != null && typeID.equals("WWW")) {
					((WebServer) ((GUIApplicationWebServerWindow) parentGUI).holeAnwendung()).changeVHostTable(
					        editingRow, editingColumn, value);
				}
			}
			if (parentGUI instanceof JFirewallDialog) {
				if (editingColumn == 0)
					setValueAt(formerValue, editingRow, editingColumn);
				else {
					String retValue = ((Firewall) ((JFirewallDialog) parentGUI).getFirewall()).changeSingleEntry(
					        editingRow, editingColumn, value);
					if (!retValue.equals(value))
						setValueAt(retValue, editingRow, editingColumn);
					if (editingColumn == 1 || editingColumn == 3) {
						if (!EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterIpAdresseAuchLeer)) {
							((ColorTableCellRenderer) getCellRenderer(editingRow, editingColumn)).setColor(editingRow,
							        editingColumn, EingabenUeberpruefung.farbeFalsch);
						} else {
							((ColorTableCellRenderer) getCellRenderer(editingRow, editingColumn)).resetColor(
							        editingRow, editingColumn);
						}
						if (!value.isEmpty() && ((String) getValueAt(editingRow, editingColumn + 1)).isEmpty())
							setValueAt("255.255.255.255", editingRow, editingColumn + 1);
					}
					if (editingColumn == 6) {
						if (!EingabenUeberpruefung.isGueltig(value, EingabenUeberpruefung.musterPortAuchLeer)) {
							((ColorTableCellRenderer) getCellRenderer(editingRow, editingColumn)).setColor(editingRow,
							        editingColumn, EingabenUeberpruefung.farbeFalsch);
						} else {
							((ColorTableCellRenderer) getCellRenderer(editingRow, editingColumn)).resetColor(
							        editingRow, editingColumn);
						}
					}
				}
			}
			removeEditor();
		}
	}
}
