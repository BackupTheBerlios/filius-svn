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
package filius.gui.anwendungssicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import filius.Main;
import filius.rahmenprogramm.EingabenUeberpruefung;
import filius.software.dns.DNSServer;
import filius.software.dns.ResourceRecord;
import filius.software.vermittlungsschicht.IP;

public class GUIApplicationDNSServerWindow extends GUIApplicationWindow {

	private static final long serialVersionUID = 1L;

	private JPanel backPanel, aPanel, mxPanel;

	private JTextField aDomainField, mxURLField, aIpField, mxMaildomainField;

	private JLabel aDomainLabel, aIpLabel, mxURLLabel, mxMaildomainLabel;

	private JTabbedPane tabbedPane;

	private JButton mxAddButton, aAddButton, buttonStart, buttonEntfernen,
			buttonMXEntfernen;

	private JTableEditable aRecordsTable;
	private JTableEditable mxRecordsTable;

	public GUIApplicationDNSServerWindow(final GUIDesktopPanel desktop,
			String appName) {
		super(desktop, appName);

		initialisiereKomponenten();
	}

	private void initialisiereKomponenten() {
		Box hBox;

		tabbedPane = new JTabbedPane();
		backPanel = new JPanel(new BorderLayout());

		buttonStart = new JButton(messages.getString("dnsserver_msg1"));
		buttonStart.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (buttonStart.getText().equals(messages.getString("dnsserver_msg1"))) {
					((DNSServer) holeAnwendung()).setAktiv(true);
				}
				else {
					((DNSServer) holeAnwendung()).setAktiv(false);
				}
				aktualisieren();
			}
		});
		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		hBox.add(buttonStart);
		backPanel.add(hBox, BorderLayout.NORTH);

		initAPanel();
		initMXPanel();

		tabbedPane.addTab(messages.getString("dnsserver_msg2"), new ImageIcon(getClass().getResource("/gfx/desktop/peertopeer_netzwerk_klein.png")), aPanel);
		tabbedPane.addTab(messages.getString("dnsserver_msg3"), new ImageIcon(getClass().getResource("/gfx/desktop/peertopeer_netzwerk_klein.png")), mxPanel);

		hBox = Box.createHorizontalBox();
		hBox.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		hBox.add(tabbedPane);
		backPanel.add(hBox, BorderLayout.CENTER);

		getContentPane().add(backPanel);
		pack();

		updateMXRecordsTable();
		updateARecordsTable();
		aktualisieren();
	}

	private void initAPanel() {
		Box vBox, hBox;
		DefaultTableModel tabellenModell;
		TableColumnModel tcm;
		JScrollPane scrollPane;

		aPanel = new JPanel(new BorderLayout());

		vBox = Box.createVerticalBox();
		vBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(5));

		aDomainLabel = new JLabel(messages.getString("dnsserver_msg4"));
		aDomainLabel.setPreferredSize(new Dimension(170, 25));
		hBox.add(aDomainLabel);
		hBox.add(Box.createHorizontalStrut(5));

		aDomainField = new JTextField();
		aDomainField.setPreferredSize(new Dimension(275, 25));
		hBox.add(aDomainField);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(5));

		aIpLabel = new JLabel(messages.getString("dnsserver_msg5"));
		aIpLabel.setPreferredSize(new Dimension(170, 25));
		hBox.add(aIpLabel);
		hBox.add(Box.createHorizontalStrut(5));

		aIpField = new JTextField(this.holeAnwendung().getSystemSoftware().holeIPAdresse());
		aIpField.setPreferredSize(new Dimension(275, 25));
		aIpField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				ipPruefen(aIpField);
			}
		});
		hBox.add(aIpField);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(5));

		aAddButton = new JButton(messages.getString("dnsserver_msg6"));
		aAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				{
					if(!aDomainField.getText().trim().isEmpty() && !aIpField.getText().trim().isEmpty() && IP.ipCheck(aIpField.getText())!=null) {
						((DNSServer) holeAnwendung()).hinzuRecord(aDomainField
							.getText(), ResourceRecord.ADDRESS, aIpField
							.getText());
						aDomainField.setText("");
						aIpField.setText("");
						updateARecordsTable();
					}
				}
			}
		});
		hBox.add(aAddButton);
		hBox.add(Box.createHorizontalStrut(5));

		buttonEntfernen = new JButton(messages.getString("dnsserver_msg7"));
		buttonEntfernen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int zeilenNummer = aRecordsTable.getSelectedRow();
				if (zeilenNummer != -1) {
					String domainname = aRecordsTable.getValueAt(zeilenNummer,
							0).toString();
					((DNSServer) holeAnwendung()).loescheResourceRecord(
							domainname, ResourceRecord.ADDRESS);
					Main.debug.println("GUIApplicationDNSServerWindow: A-Eintrag "
									+ domainname + " geloescht");

					updateARecordsTable();
				}
			}
		});
		hBox.add(buttonEntfernen);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(5));

		tabellenModell = new DefaultTableModel(0, 2);
		aRecordsTable = new JTableEditable(tabellenModell, true, "A");
		aRecordsTable.setParentGUI(this);   // tell the table who presents its values, such that the back-end DNS
											// server can be found for adapting resource entries
		aRecordsTable.setIntercellSpacing(new Dimension(5,5));
		aRecordsTable.setRowHeight(30);
		aRecordsTable.setShowGrid(false);
		aRecordsTable.setFillsViewportHeight(true);
		aRecordsTable.setBackground(Color.WHITE);
		aRecordsTable.setShowHorizontalLines(true);
		
		tcm = aRecordsTable.getColumnModel();
		tcm.getColumn(0).setHeaderValue(messages.getString("dnsserver_msg8"));
		tcm.getColumn(1).setHeaderValue(messages.getString("dnsserver_msg9"));
		scrollPane = new JScrollPane(aRecordsTable);

		vBox.add(scrollPane);
		aPanel.add(vBox, BorderLayout.CENTER);
	}

	private void initMXPanel() {
		Box vBox, hBox;
		DefaultTableModel tabellenModell;
		TableColumnModel tcm;
		JScrollPane scrollPane;

		mxPanel = new JPanel(new BorderLayout());

		vBox = Box.createVerticalBox();
		vBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(5));

		mxMaildomainLabel = new JLabel(messages.getString("dnsserver_msg10"));
		mxMaildomainLabel.setPreferredSize(new Dimension(170, 25));
		hBox.add(mxMaildomainLabel);
		hBox.add(Box.createHorizontalStrut(5));

		mxMaildomainField = new JTextField();
		mxMaildomainField.setPreferredSize(new Dimension(275, 25));
		hBox.add(mxMaildomainField);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(5));

		mxURLLabel = new JLabel(messages.getString("dnsserver_msg11"));
		mxURLLabel.setPreferredSize(new Dimension(170, 25));
		hBox.add(mxURLLabel);
		hBox.add(Box.createHorizontalStrut(5));

		mxURLField = new JTextField();
		mxURLField.setPreferredSize(new Dimension(275, 25));
		hBox.add(mxURLField);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(5));

		hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(5));

		mxAddButton = new JButton(messages.getString("dnsserver_msg6"));
		mxAddButton.addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				{
					if(!mxMaildomainField.getText().trim().isEmpty() && !mxURLField.getText().trim().isEmpty()) {
						((DNSServer) holeAnwendung()).hinzuRecord(mxMaildomainField
							.getText(), ResourceRecord.MAIL_EXCHANGE,
							mxURLField.getText());
						mxMaildomainField.setText("");
						mxURLField.setText("");
						updateMXRecordsTable();
					}
				}
			}
		});
		hBox.add(mxAddButton);

		buttonMXEntfernen = new JButton(messages.getString("dnsserver_msg7"));
		buttonMXEntfernen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int zeilenNummer = mxRecordsTable.getSelectedRow();
				if (zeilenNummer != -1) {
					String domainname = mxRecordsTable.getValueAt(zeilenNummer,
							0).toString();
					((DNSServer) holeAnwendung()).loescheResourceRecord(
							domainname, ResourceRecord.MAIL_EXCHANGE);
					Main.debug
							.println("GUIApplicationDNSServerWindow: MX-Eintrag "
									+ domainname + " geloescht");
					updateMXRecordsTable();
				}
			}
		});
		hBox.add(buttonMXEntfernen);

		vBox.add(hBox);
		vBox.add(Box.createVerticalStrut(5));

		tabellenModell = new DefaultTableModel(0, 2);
		mxRecordsTable = new JTableEditable(tabellenModell, true, "MX");
		mxRecordsTable.setParentGUI(this);   // tell the table who presents its values, such that the back-end DNS
											 // server can be found for adapting resource entries
		mxRecordsTable.setIntercellSpacing(new Dimension(5,5));
		mxRecordsTable.setRowHeight(30);
		mxRecordsTable.setShowGrid(false);
		mxRecordsTable.setFillsViewportHeight(true);
		mxRecordsTable.setBackground(Color.WHITE);
		mxRecordsTable.setShowHorizontalLines(true);

		tcm = mxRecordsTable.getColumnModel();
		tcm.getColumn(0).setHeaderValue(messages.getString("dnsserver_msg12"));
		tcm.getColumn(1).setHeaderValue(messages.getString("dnsserver_msg13"));

		scrollPane = new JScrollPane(mxRecordsTable);
		vBox.add(scrollPane);
		mxPanel.add(vBox, BorderLayout.CENTER);
	}

	/**
	 * Aktualisiert die Tabelle der A-Records
	 *
	 * @author Thomas Gerding & Johannes Bade
	 */
	public void updateARecordsTable() {
		ResourceRecord rr;

		DefaultTableModel tabellenModell = (DefaultTableModel) aRecordsTable.getModel();
		tabellenModell.setRowCount(0);

		LinkedList tempListe = ((DNSServer) holeAnwendung()).holeResourceRecords();

		ListIterator it = tempListe.listIterator();
		while (it.hasNext()) {
			rr = (ResourceRecord) it.next();
			if (rr.getType().equals(ResourceRecord.ADDRESS)) {
				Vector<Comparable> v = new Vector<Comparable>();
				v.add(rr.getDomainname());
				v.add(rr.getRdata());
				tabellenModell.addRow(v);
			}
		}
		updateUI();
	}

	/**
	 * Aktualisiert die Tabelle der MX-Records
	 *
	 * @author Thomas Gerding & Johannes Bade
	 */
	public void updateMXRecordsTable() {
		ResourceRecord rr;

		DefaultTableModel tabellenModell = (DefaultTableModel) mxRecordsTable
				.getModel();
		tabellenModell.setRowCount(0);

		LinkedList tempListe = ((DNSServer) holeAnwendung())
				.holeResourceRecords();
		ListIterator it = tempListe.listIterator();

		while (it.hasNext()) {
			rr = (ResourceRecord) it.next();
			if (rr.getType().equals(ResourceRecord.MAIL_EXCHANGE)) {

				Vector<Comparable> v = new Vector<Comparable>();
				v.add(rr.getDomainname());
				v.add(rr.getRdata());
				tabellenModell.addRow(v);
			}
		}

		updateUI();
	}

	/**
	 * Funktion die während der Eingabe ueberprueft ob die bisherige Eingabe
	 * einen korrekten Wert darstellt.
	 *
	 * @author Johannes Bade & Thomas Gerding
	 * @param pruefRegel
	 * @param feld
	 */
	private void ipPruefen(JTextField feld) {
		if (EingabenUeberpruefung.isGueltig(feld.getText(),
				EingabenUeberpruefung.musterIpAdresse)) {
			feld.setForeground(EingabenUeberpruefung.farbeRichtig);
		}
		else {
			feld.setForeground(EingabenUeberpruefung.farbeFalsch);
		}
	}

	private void aktualisieren() {
		if (((DNSServer) holeAnwendung()).isAktiv()) {
			buttonStart.setText(messages.getString("dnsserver_msg14"));
		}
		else {
			buttonStart.setText(messages.getString("dnsserver_msg1"));
		}
	}

	public void update(Observable arg0, Object arg1) {
		aktualisieren();
	}
}
