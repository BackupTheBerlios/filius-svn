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
package filius.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.SwingConstants;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.SystemColor;

public class InfoDialog extends JDialog implements I18n {

	private static final long serialVersionUID = 1L;

	private JBackgroundPanel jContentPane = null;

	private JLabel version = null;

	private JLabel personen = null;
	private JLabel maintainer = null;

	private JLabel erlaeuterung = null;

	/**
	 * @param owner
	 */
	public InfoDialog(Frame owner) {
		super(owner);
		this.setIconImage(owner.getIconImage());
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 450);
		setLocation((getToolkit().getScreenSize().width-getWidth())/2, (getToolkit().getScreenSize().height-getHeight())/2);
		this.setForeground(Color.blue);
		this.setTitle(messages.getString("infodialog_msg1"));
		this.setResizable(false);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 1;

			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 2;

			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 3;

			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 4;

			erlaeuterung = new JLabel();
			erlaeuterung.setText(messages.getString("infodialog_msg2"));
			erlaeuterung.setPreferredSize(new Dimension(270, 50));
			erlaeuterung.setBackground(SystemColor.activeCaption);
			erlaeuterung.setForeground(Color.WHITE);
			erlaeuterung.setFont(new Font("Dialog", Font.PLAIN, 12));
			erlaeuterung.setVerticalAlignment(SwingConstants.TOP);

			personen = new JLabel();
			personen.setText(
			                "<html>"+
					"<b>"+messages.getString("infodialog_msg3")+"</b>" +
					"<p> Andr&eacute; Asschoff, "+
					"Johannes Bade, "+
					"Carsten Dittich, "+
					"Thomas Gerding, "+
					"Nadja Ha&szlig;ler, "+
					"Ernst Johannes Klebert, "+
					"Michell Weyer <br /> &nbsp;</p>"+
					"<b>"+messages.getString("infodialog_msg4")+"</b>" +
					"<p> Stefan Freischlad, "+
					"Peer Stechert </p></html>");
			personen.setVerticalAlignment(SwingConstants.TOP);
			personen.setForeground(Color.WHITE);
			personen.setFont(new Font("Dialog", Font.PLAIN, 12));
			personen.setPreferredSize(new Dimension(270, 110));
			maintainer = new JLabel();
			maintainer.setText(
			                "<html>"+
					"<b>"+messages.getString("infodialog_msg5")+"</b>"+
					"<p>"+ messages.getString("infodialog_msg7")+
			                " Christian J. Eibl<br/>" +
					"(filius&#x40;fameibl.de)<br/> &nbsp;</p>" +
					"<p>"+messages.getString("infodialog_msg8")+"<br/>" +
					"http://bugs.fameibl.de" +
					"</p></html>");
			maintainer.setVerticalAlignment(SwingConstants.TOP);
			maintainer.setForeground(Color.WHITE);
			maintainer.setFont(new Font("Dialog", Font.PLAIN, 12));
			maintainer.setPreferredSize(new Dimension(270, 100));

			version = new JLabel();
			version.setText(messages.getString("infodialog_msg6")+" "+Information.getVersion());
			version.setFont(new Font("Dialog", Font.BOLD, 12));
			version.setForeground(Color.WHITE);
			version.setHorizontalAlignment(SwingConstants.CENTER);

			jContentPane = new JBackgroundPanel();
			jContentPane.setBackgroundImage("gfx/allgemein/info_hintergrund.png");
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.setBackground(new Color(0.9f, 0.9f, 0.95f));
			jContentPane.add(version, gridBagConstraints);
			jContentPane.add(maintainer, gridBagConstraints1);
			jContentPane.add(personen, gridBagConstraints2);
			jContentPane.add(erlaeuterung, gridBagConstraints3);
		}
		return jContentPane;
	}

}
