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
**         
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

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Observable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;

import filius.Main;
import filius.rahmenprogramm.Base64;
import filius.rahmenprogramm.Information;


import filius.software.lokal.ImageViewer;
import filius.software.system.Betriebssystem;
import filius.software.system.Datei;

public class GUIApplicationImageViewerWindow extends GUIApplicationWindow{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel backPanel;
	private JButton prevButton, nextButton, oeffnen;

	public GUIApplicationImageViewerWindow(final GUIDesktopPanel desktop, String appName)
	{
		super(desktop, appName);

		backPanel = new JPanel(new BorderLayout());

		JMenuBar mb = new JMenuBar();

		JMenu menuDatei = new JMenu(messages.getString("imageviewer_msg1"));

		menuDatei.add(new AbstractAction(messages.getString("imageviewer_msg2")) {
			public void actionPerformed(ActionEvent arg0) {
				oeffnen();
			}
	  });

		mb.add(menuDatei);

		this.setJMenuBar(mb);

		this.getContentPane().add(backPanel);
		pack();

	}

	public void oeffnen()
	{
		DMTNFileChooser fc;
		int rueckgabe;
		Datei aktuelleDatei;

		fc = new DMTNFileChooser((Betriebssystem)holeAnwendung().getSystemSoftware());
		rueckgabe = fc.openDialog();

		if (rueckgabe == DMTNFileChooser.OK)
		{
			aktuelleDatei = holeAnwendung().getSystemSoftware().getDateisystem().holeDatei(fc.getAktuellerOrdner(), fc.getAktuellerDateiname());
			if (aktuelleDatei != null)
			{
				this.setTitle(aktuelleDatei.getName());
				Base64.decodeToFile(aktuelleDatei.getDateiInhalt(), Information.getInformation().getTempPfad()
						+ aktuelleDatei.getName());

				JLabel titelgrafik = new JLabel(new ImageIcon(getClass().getResource("/"+Information.getInformation().getTempPfad() + aktuelleDatei.getName())));
        		backPanel.add(titelgrafik, BorderLayout.CENTER);
        		backPanel.updateUI();
			}
			else {
				Main.debug.println("ERROR ("+this.hashCode()+"): Fehler beim oeffnen einer Datei: keine Datei ausgewaehlt");
			}

		}
		else {
			Main.debug.println("ERROR ("+this.hashCode()+"): Fehler beim oeffnen einer Datei");
		}
	}

	public void update(Observable arg0, Object arg1) {

	}
}
