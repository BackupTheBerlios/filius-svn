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

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;

import filius.gui.GUIContainer;

public class GUIPrintPanel extends GUIMainArea {
    private static final int EMPTY_BORDER = 10;

    private static final long serialVersionUID = 1L;

    private GUIMainArea mainArea = new GUIMainArea();

    public GUIPrintPanel() {
        this.setSize(GUIContainer.FLAECHE_BREITE + 2 * EMPTY_BORDER, GUIContainer.FLAECHE_HOEHE + 2 * EMPTY_BORDER);
        add(mainArea);
        mainArea.setBounds(EMPTY_BORDER, EMPTY_BORDER, GUIContainer.FLAECHE_BREITE, GUIContainer.FLAECHE_HOEHE);
        mainArea.setOpaque(false);
        setOpaque(false);
        // setBackgroundImage("gfx/allgemein/simulationshg.png");
    }

    @Override
    public void updateViewport(List<GUIKnotenItem> knoten, List<GUIKabelItem> kabel, List<GUIDocuItem> docuItems,
            boolean docuItemsEnabled) {
        mainArea.updateViewport(knoten, kabel, docuItems, false);
    }

    public void updateViewport(List<GUIKnotenItem> knoten, List<GUIKabelItem> kabel, List<GUIDocuItem> docuItems,
            String footerText) {
        mainArea.updateViewport(knoten, kabel, docuItems, false);

        JLabel footer = new JLabel(footerText);
        footer.setForeground(Color.lightGray);
        int footerWidth = footer.getFontMetrics(footer.getFont()).stringWidth(footerText) + 20;
        int footerHeight = footer.getFontMetrics(footer.getFont()).getHeight();
        int footerY = (int) mainArea.maxY + 30;
        int footerX = (int) mainArea.minX;
        footer.setBounds(footerX, footerY, footerWidth, footerHeight);
        mainArea.maxX = Math.max(footerX + footerWidth, mainArea.maxX);
        mainArea.maxY = footerY + footerHeight;
        mainArea.add(footer);
    }

    @Override
    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public int getClipY() {
        return (int) mainArea.minY;
    }

    public int getClipHeight() {
        return (int) (mainArea.maxY - mainArea.minY) + 2 * EMPTY_BORDER;
    }

    public int getClipWidth() {
        return (int) (mainArea.maxX - mainArea.minX) + 2 * EMPTY_BORDER;
    }

    public int getClipX() {
        return (int) mainArea.minX;
    }

    @Override
    public int getWidth() {
        return GUIContainer.FLAECHE_BREITE + 2 * EMPTY_BORDER;
    }

    @Override
    public int getHeight() {
        return GUIContainer.FLAECHE_HOEHE + 2 * EMPTY_BORDER;
    }
}
