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
** (at your option) any later version.
** 
** Filius is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied
** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
** PURPOSE. See the GNU General Public License for more details.
** 
** You should have received a copy of the GNU General Public License
** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
*/
package filius.gui.netzwerksicht;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.awt.color.*;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import filius.Main;
import filius.rahmenprogramm.*;
import filius.software.firewall.*;

public class JFirewallDialog extends JDialog implements I18n {

	JFirewallDialog jfd = null;
	Firewall firewall;

	private JTable tTabelle;
	private JTable tTabelleAbsender;
	private JTable tTabelleZiel;
	private JTable tTabellePort;
	DefaultTableModel dtmTabelle;
	DefaultTableModel dtmTabellePort;
	JScrollPane spTabelle;
	JScrollPane spTabellePort;
	Box boxFirewall;
	Box boxTabellen;

	private JTextField tfAbsenderVon, tfAbsenderBis;
	private JTextField tfZielVon, tfZielBis;
	private JTextField tfPort;

	private JCheckBox cbEinAus;
	private JCheckBox cbSynAck;


	public JFirewallDialog(Firewall firewall, JFrame dummyFrame){
		super(dummyFrame, messages.getString("jfirewalldialog_msg1"), true);
	  	Main.debug.println("INVOKED-2 ("+this.hashCode()+") "+getClass()+", constr: JFirewallDialog("+firewall+","+dummyFrame+")");
		this.firewall = firewall;
		jfd = this;
		this.setBounds(this.getX(), this.getY(), 600,300);

		erzeugeFenster();

	}

	private Box erzeugeAbsenderBox() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", erzeugeAbsenderBox()");
		JScrollPane scrollPane;
		Box vBox, hBox;
		DefaultTableModel model;
		TableColumnModel columnModel;
		JButton button;
		JLabel label;
		JTextArea textArea;

		vBox = Box.createVerticalBox();
		vBox.add(Box.createVerticalStrut(10));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		textArea = new JTextArea();
		textArea.setText(messages.getString("jfirewalldialog_msg2"));
		textArea.setEditable(false);

		hBox.add(textArea);
		hBox.add(Box.createHorizontalStrut(10));
		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		label = new JLabel(messages.getString("jfirewalldialog_msg3"));
		hBox.add(label);
		hBox.add(Box.createHorizontalStrut(10));

		tfAbsenderVon = new JTextField();
		tfAbsenderVon.setPreferredSize(new Dimension(40, 20));
		hBox.add(tfAbsenderVon);

		label = new JLabel(" - ");
		hBox.add(label);

		tfAbsenderBis = new JTextField();
		tfAbsenderBis.setPreferredSize(new Dimension(40, 20));
		hBox.add(tfAbsenderBis);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg4"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (EingabenUeberpruefung.isGueltig(tfAbsenderVon.getText(), EingabenUeberpruefung.musterIpAdresse)) {
					if (tfAbsenderBis.getText().trim().equals("")) {
						firewall.eintragHinzufuegen(tfAbsenderVon.getText(), tfAbsenderVon.getText(), Firewall.ABSENDER_FILTER);
					}
					else if (EingabenUeberpruefung.isGueltig(tfAbsenderBis.getText(), EingabenUeberpruefung.musterIpAdresse)) {
						firewall.eintragHinzufuegen(tfAbsenderVon.getText(), tfAbsenderBis.getText(), Firewall.ABSENDER_FILTER);
					}
				}
				tfAbsenderVon.setText("");
				tfAbsenderBis.setText("");
				updateAttribute();
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg5"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tTabelleAbsender.getSelectedRow() != -1) {
					firewall.entferneAbsenderRegel(tTabelleAbsender.getSelectedRow());
				}
				updateAttribute();
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		model = new DefaultTableModel(0, 2);
		tTabelleAbsender = new JTable(model);
		tTabelleAbsender.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tTabelleAbsender.setIntercellSpacing(new Dimension(10,5));
		tTabelleAbsender.setRowHeight(30);
		tTabelleAbsender.setShowGrid(false);
		tTabelleAbsender.setFillsViewportHeight(true);
		tTabelleAbsender.setBackground(Color.WHITE);
		tTabelleAbsender.setShowHorizontalLines(true);

		columnModel = tTabelleAbsender.getColumnModel();
		columnModel.getColumn(0).setHeaderValue(messages.getString("jfirewalldialog_msg6"));
		columnModel.getColumn(1).setHeaderValue(messages.getString("jfirewalldialog_msg7"));

		scrollPane = new JScrollPane(tTabelleAbsender);
		scrollPane.setPreferredSize(new Dimension(150,250));

		vBox.add(scrollPane);
		vBox.add(Box.createVerticalStrut(10));

		return vBox;
	}

	private Box erzeugeEmpfaengerBox() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", erzeugeEmpfaengerBox()");
		JScrollPane scrollPane;
		Box vBox, hBox;
		DefaultTableModel model;
		TableColumnModel columnModel;
		JButton button;
		JLabel label;
		JTextArea textArea;

		vBox = Box.createVerticalBox();
		vBox.add(Box.createVerticalStrut(10));


		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		textArea = new JTextArea();
		textArea.setText(messages.getString("jfirewalldialog_msg8"));
		textArea.setEditable(false);
		textArea.setBackground(getBackground());

		hBox.add(textArea);
		hBox.add(Box.createHorizontalStrut(10));
		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		label = new JLabel(messages.getString("jfirewalldialog_msg9"));
		hBox.add(label);
		hBox.add(Box.createHorizontalStrut(10));

		tfZielVon = new JTextField();
		tfZielVon.setPreferredSize(new Dimension(40, 20));
		hBox.add(tfZielVon);

		label = new JLabel(" - ");
		hBox.add(label);

		tfZielBis = new JTextField();
		tfZielBis.setPreferredSize(new Dimension(40, 20));
		hBox.add(tfZielBis);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg4"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (EingabenUeberpruefung.isGueltig(tfZielVon.getText(), EingabenUeberpruefung.musterIpAdresse)) {
					if (tfZielBis.getText().trim().equals("")) {
						firewall.eintragHinzufuegen(tfZielVon.getText(), tfZielVon.getText(), Firewall.EMPFAENGER_FILTER);
					}
					else if (EingabenUeberpruefung.isGueltig(tfZielBis.getText(), EingabenUeberpruefung.musterIpAdresse)) {
						firewall.eintragHinzufuegen(tfZielVon.getText(), tfZielBis.getText(), Firewall.EMPFAENGER_FILTER);
					}
				}
				tfZielVon.setText("");
				tfZielBis.setText("");
				updateAttribute();
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg5"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tTabelleZiel.getSelectedRow() != -1) {
					firewall.entferneEmpfaengerRegel(tTabelleZiel.getSelectedRow());
				}
				updateAttribute();
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		model = new DefaultTableModel(0, 2);
		tTabelleZiel = new JTable(model);
		tTabelleZiel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tTabelleZiel.setIntercellSpacing(new Dimension(10,5));
		tTabelleZiel.setRowHeight(30);
		tTabelleZiel.setShowGrid(false);
		tTabelleZiel.setFillsViewportHeight(true);
		tTabelleZiel.setBackground(Color.WHITE);
		tTabelleZiel.setShowHorizontalLines(true);

		columnModel = tTabelleZiel.getColumnModel();
		columnModel.getColumn(0).setHeaderValue(messages.getString("jfirewalldialog_msg6"));
		columnModel.getColumn(1).setHeaderValue(messages.getString("jfirewalldialog_msg7"));

		scrollPane = new JScrollPane(tTabelleZiel);
		scrollPane.setPreferredSize(new Dimension(150,250));

		vBox.add(scrollPane);
		vBox.add(Box.createVerticalStrut(10));

		return vBox;
	}

	private Box erzeugePortBox() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", erzeugePortBox()");
		JScrollPane scrollPane;
		Box vBox, hBox;
		DefaultTableModel model;
		TableColumnModel columnModel;
		JButton button;
		JLabel label;
		JTextArea textArea;

		vBox = Box.createVerticalBox();
		vBox.add(Box.createVerticalStrut(10));


		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		textArea = new JTextArea();
		textArea.setText(messages.getString("jfirewalldialog_msg10"));
		textArea.setEditable(false);
		textArea.setBackground(getBackground());

		hBox.add(textArea);
		hBox.add(Box.createHorizontalStrut(10));
		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		label = new JLabel(messages.getString("jfirewalldialog_msg11"));
		hBox.add(label);
		hBox.add(Box.createHorizontalStrut(10));

		tfPort = new JTextField();
		tfPort.setPreferredSize(new Dimension(40, 20));
		hBox.add(tfPort);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg4"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Integer.parseInt(tfPort.getText());
					firewall.eintragHinzufuegenPort(tfPort.getText());
				}
				catch (Exception ex) {}
				tfPort.setText("");
				updateAttribute();
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		button = new JButton(messages.getString("jfirewalldialog_msg5"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tTabellePort.getSelectedRow() != -1) {
					firewall.entferneRegelPort(tTabellePort.getSelectedRow());
				}
				updateAttribute();
			}
		});
		hBox.add(button);
		hBox.add(Box.createHorizontalStrut(10));

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(10));

		model = new DefaultTableModel(0, 1);
		tTabellePort = new JTable(model);
		tTabellePort.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tTabellePort.setIntercellSpacing(new Dimension(10,5));
		tTabellePort.setRowHeight(30);
		tTabellePort.setShowGrid(false);
		tTabellePort.setFillsViewportHeight(true);
		tTabellePort.setBackground(Color.WHITE);
		tTabellePort.setShowHorizontalLines(true);

		columnModel = tTabellePort.getColumnModel();
		columnModel.getColumn(0).setHeaderValue(messages.getString("jfirewalldialog_msg12"));

		scrollPane = new JScrollPane(tTabellePort);
		scrollPane.setPreferredSize(new Dimension(150,250));

		vBox.add(scrollPane);
		vBox.add(Box.createVerticalStrut(10));

		return vBox;
	}


	/*
	 * @author Weyer
	 * hier wird das ganze Fenster bestückt
	 */
	private void erzeugeFenster(){
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", erzeugeFenster()");
		JTabbedPane tp;
		JPanel hauptPanel;

		hauptPanel = new JPanel(new BorderLayout());

		cbEinAus = new JCheckBox(messages.getString("jfirewalldialog_msg13"));//Bei true nur mit 1 durchlassen
		cbEinAus.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent arg0) {
				firewall.setAktiviert(cbEinAus.isSelected());
				updateAttribute();

			}
		});
		hauptPanel.add(cbEinAus, BorderLayout.NORTH);

		tp = new JTabbedPane();
		tp.add(messages.getString("jfirewalldialog_msg14"), erzeugeAbsenderBox());
		tp.add(messages.getString("jfirewalldialog_msg15"), erzeugeEmpfaengerBox());
		tp.add(messages.getString("jfirewalldialog_msg16"), erzeugePortBox());

		tp.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0) {
				updateAttribute();
			}
		});

		hauptPanel.add(tp, BorderLayout.CENTER);

		getContentPane().add(hauptPanel);
		pack();
	}

	/*
	 * @author  Weyer
	 */
	public void updateAttribute() {
	  	Main.debug.println("INVOKED ("+this.hashCode()+") "+getClass()+", updateAttribute()");

		LinkedList liste;
		Vector<String> vector;
		DefaultTableModel model;
		String[] array;

		if(firewall.isAktiviert()){
			cbEinAus.setSelected(true);
		}
		else{
			cbEinAus.setSelected(false);
		}

		liste = firewall.getPortList();
		model = (DefaultTableModel)tTabellePort.getModel();
		model.setRowCount(0);
		//Main.debug.println("Portliste mit "+liste.size()+" Eintraegen");
		for (int i=0; i<liste.size(); i++) {
			vector = new Vector<String>();
			vector.addElement((String)((Object[])liste.get(i))[0]);
			model.addRow(vector);
		}

		liste = firewall.getAbsenderFilterList();
		model = (DefaultTableModel)tTabelleAbsender.getModel();
		model.setRowCount(0);
		//Main.debug.println("Absenderliste mit "+liste.size()+" Eintraegen");
		for (int i=0; i<liste.size(); i++) {
			array = ((String)liste.get(i)).split("#");
			vector = new Vector<String>();
			vector.addElement(array[0]);
			vector.addElement(array[1]);
			model.addRow(vector);
		}

		liste = firewall.getEmpfaengerFilterList();
		model = (DefaultTableModel)tTabelleZiel.getModel();
		model.setRowCount(0);
		//Main.debug.println("Empfaengerliste mit "+liste.size()+" Eintraegen");
		for (int i=0; i<liste.size(); i++) {
			array = ((String)liste.get(i)).split("#");
			vector = new Vector<String>();
			vector.addElement(array[0]);
			vector.addElement(array[1]);
			model.addRow(vector);
		}



	}
}
