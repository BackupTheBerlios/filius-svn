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
package filius.gui.netzwerksicht;

import java.io.Serializable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import filius.Main;
import filius.gui.GUIContainer;
import filius.gui.GUIMainMenu;
import filius.gui.JBackgroundPanel;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;

/**
 * Diese Klasse dient als Oberklasse für die verschiedenen Sichten im Haupt-Bereich der GUI.
 */
public class GUIMainArea extends JBackgroundPanel implements Serializable {

    private static final long serialVersionUID = 1L;
    protected double minX = Integer.MAX_VALUE, maxX = 0, minY = Integer.MAX_VALUE, maxY = 0;

    /**
     * Macht ein Update des Panels. Dabei wird der gesamte Inhalt des Panels geloescht und ganz neu mit den Elementen
     * der itemlist und cablelist befuellt.
     * 
     * @author Johannes Bade & Thomas Gerding
     * @param docuItemsEnabled TODO
     */
    public void updateViewport(List<GUIKnotenItem> knoten, List<GUIKabelItem> kabel, List<GUIDocuItem> docuItems, boolean docuItemsEnabled) {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIMainArea), updateViewport("
                + knoten + "," + kabel + ")");
        removeAll();
        minX = Integer.MAX_VALUE;
        maxX = 0;
        minY = Integer.MAX_VALUE;
        maxY = 0;

        for (GUIKnotenItem tempitem : knoten) {
            Knoten tempKnoten = tempitem.getKnoten();
            JSidebarButton templabel = tempitem.getImageLabel();

            tempKnoten.addObserver(templabel);
            tempKnoten.getSystemSoftware().addObserver(templabel);

            templabel.setSelektiert(false);
            templabel.setText(tempKnoten.holeAnzeigeName());
            templabel.setTyp(tempKnoten.holeHardwareTyp());
            if (tempitem.getKnoten() instanceof Switch) {
                if (((Switch) tempitem.getKnoten()).isCloud())
                    templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH_CLOUD)));
                else
                    templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH)));
            } else if (tempitem.getKnoten() instanceof Vermittlungsrechner) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.VERMITTLUNGSRECHNER)));
            } else if (tempitem.getKnoten() instanceof Rechner) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.RECHNER)));
            } else if (tempitem.getKnoten() instanceof Notebook) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.NOTEBOOK)));
            } else if (tempitem.getKnoten() instanceof Modem) {
                templabel.setIcon(new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.MODEM)));
            }

            templabel.setBounds(tempitem.getImageLabel().getBounds());
            add(templabel);

            updateClipBounds(templabel);
        }

        for (GUIKabelItem tempcable : kabel) {
            add(tempcable.getKabelpanel());
        }
        for (GUIDocuItem item : docuItems) {
            add(item.asDocuElement());
            updateClipBounds(item.asDocuElement());
            item.asDocuElement().setEnabled(
                    docuItemsEnabled);
        }
    }

    private void updateClipBounds(JComponent elem) {
        if (elem.getBounds().getMinX() < minX) {
            minX = Math.max(0, elem.getBounds().getMinX());
        }
        if (elem.getBounds().getMaxX() > maxX) {
            maxX = Math.min(elem.getBounds().getMaxX(), this.getWidth());
        }
        if (elem.getBounds().getMinY() < minY) {
            minY = Math.max(0, elem.getBounds().getMinY());
        }
        if (elem.getBounds().getMaxY() > maxY) {
            maxY = Math.min(elem.getBounds().getMaxY(), this.getHeight());
        }
    }
}
