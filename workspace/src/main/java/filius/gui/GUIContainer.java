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
package filius.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileFilter;

import filius.Main;
import filius.gui.anwendungssicht.GUIDesktopWindow;
import filius.gui.nachrichtensicht.AggregatedExchangeDialog;
import filius.gui.nachrichtensicht.ExchangeDialog;
import filius.gui.nachrichtensicht.LayeredExchangeDialog;
import filius.gui.netzwerksicht.GUIDesignPanel;
import filius.gui.netzwerksicht.GUIDesignSidebar;
import filius.gui.netzwerksicht.GUIDocuItem;
import filius.gui.netzwerksicht.GUIDocumentationSidebar;
import filius.gui.netzwerksicht.GUIKabelItem;
import filius.gui.netzwerksicht.GUIKnotenItem;
import filius.gui.netzwerksicht.GUIPrintPanel;
import filius.gui.netzwerksicht.GUISimulationPanel;
import filius.gui.netzwerksicht.JCablePanel;
import filius.gui.netzwerksicht.JDocuElement;
import filius.gui.netzwerksicht.JKonfiguration;
import filius.gui.netzwerksicht.JSidebarButton;
import filius.hardware.Kabel;
import filius.hardware.knoten.Host;
import filius.hardware.knoten.Knoten;
import filius.hardware.knoten.Modem;
import filius.hardware.knoten.Notebook;
import filius.hardware.knoten.Rechner;
import filius.hardware.knoten.Switch;
import filius.hardware.knoten.Vermittlungsrechner;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.rahmenprogramm.SzenarioVerwaltung;
import filius.software.system.Betriebssystem;

public class GUIContainer implements Serializable, I18n {

    public static final int NONE = 0;
    public static final int MOVE = 1;
    public static final int LEFT_SIZING = 2;
    public static final int RIGHT_SIZING = 3;
    public static final int UPPER_SIZING = 4;
    public static final int LOWER_SIZING = 5;
    private static final long serialVersionUID = 1L;

    public static final int FLAECHE_BREITE = 2000;
    public static final int FLAECHE_HOEHE = 1500;

    private static GUIContainer ref;

    private GUIMainMenu menu;

    private JKonfiguration property = JKonfiguration.getInstance(null);

    private List<GUIDesktopWindow> desktopWindowList = new LinkedList<GUIDesktopWindow>();

    private GUIDesignSidebar draftSidebar;
    private GUIDocumentationSidebar documentSidebar;

    private GUIDesignPanel draftPanel = new GUIDesignPanel();
    private JBackgroundPanel docuPanel = new JBackgroundPanel();
    private JLayeredPane designLayeredPane = new JLayeredPane();
    private GUISimulationPanel simulationPanel = new GUISimulationPanel();
    private JScrollPane designView;
    private JScrollPane simulationView;
    private JScrollPane draftSidebarScrollpane;
    private JScrollPane documentSidebarScrollpane;

    private JDocuElement docuElement;

    private JSidebarButton dragVorschau, kabelvorschau;
    private JSidebarButton ziel2Label;
    private JCablePanel kabelPanelVorschau;

    /** covered area during mouse pressed; visual representation of this area */
    private static JMarkerPanel auswahl;
    /** actual area containing selected objects */
    private JMarkerPanel markierung;
    private JPanel docuDragPanel = new JPanel();

    /** enthält einen Integerwert dafür welche Ansicht gerade aktiv ist */
    private int activeSite = GUIMainMenu.MODUS_ENTWURF;

    private List<GUIKnotenItem> nodeItems = new LinkedList<GUIKnotenItem>();
    private List<GUIKabelItem> cableItems = new LinkedList<GUIKabelItem>();

    private List<GUIDocuItem> docuItems = new ArrayList<GUIDocuItem>();

    public List<GUIKnotenItem> getKnotenItems() {
        return nodeItems;
    }

    public JSidebarButton getZiel2Label() {
        return ziel2Label;
    }

    public void setZiel2Label(JSidebarButton ziel2Label) {
        this.ziel2Label = ziel2Label;
    }

    public void nachrichtenDialogAnzeigen() {
        ((JDialog) getExchangeDialog()).setVisible(true);
    }

    public ExchangeDialog getExchangeDialog() {
        ExchangeDialog exchangeDialog;
        if (Information.getInformation().isOldExchangeDialog()) {
            exchangeDialog = LayeredExchangeDialog.getInstance(JMainFrame.getJMainFrame());
        } else {
            exchangeDialog = AggregatedExchangeDialog.getInstance(JMainFrame.getJMainFrame());
        }
        return exchangeDialog;
    }

    /**
     * Die Prozedur wird erst aufgerufen wenn der Container c zugewiesen wurde. Sie Initialisiert die einzelnen Panels
     * und weist die ActionListener zu.
     * 
     * @author Johannes Bade & Thomas Gerding
     */
    public void initialisieren() {
        Container contentPane = JMainFrame.getJMainFrame().getContentPane();
        designLayeredPane.setSize(FLAECHE_BREITE, FLAECHE_HOEHE);
        designLayeredPane.setMinimumSize(new Dimension(FLAECHE_BREITE, FLAECHE_HOEHE));
        designLayeredPane.setPreferredSize(new Dimension(FLAECHE_BREITE, FLAECHE_HOEHE));

        /*
         * auswahl: area covered during mouse pressed, i.e., area with components to be selected
         */
        auswahl = new JMarkerPanel();
        auswahl.setBounds(0, 0, 0, 0);
        auswahl.setBackgroundImage("gfx/allgemein/auswahl.png");
        auswahl.setOpaque(false);
        auswahl.setVisible(true);
        designLayeredPane.add(auswahl, JLayeredPane.DRAG_LAYER);

        /*
         * Kabelvorschau wird erstellt und dem Container hinzugefuegt. Wird Anfangs auf Invisible gestellt, und nur bei
         * Verwendung sichtbar gemacht.
         */
        kabelvorschau = new JSidebarButton("", new ImageIcon(getClass().getResource("/gfx/allgemein/ziel1.png")), null);
        kabelvorschau.setVisible(false);
        designLayeredPane.add(kabelvorschau, JLayeredPane.DRAG_LAYER);

        /*
         * Die Vorschau für das drag&drop, die das aktuelle Element anzeigt wird initialisiert und dem layeredpane
         * hinzugefügt. Die Visibility wird jedoch auf false gestellt, da ja Anfangs kein drag&drop vorliegt.
         */
        dragVorschau = new JSidebarButton("", null, null);
        dragVorschau.setVisible(false);
        designLayeredPane.add(dragVorschau, JLayeredPane.DRAG_LAYER);

        /*
         * Hauptmenü wird erstellt und dem Container hinzugefügt
         */
        menu = new GUIMainMenu();
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(menu.getMenupanel(), BorderLayout.NORTH);

        setProperty(null);

        /* sidebar wird erstellt und anschliessend dem Container c zugefüt */
        draftSidebar = GUIDesignSidebar.getGUIDesignSidebar();
        documentSidebar = GUIDocumentationSidebar.getGUIDocumentationSidebar();

        /* markierung: actual area covering selected objects */
        markierung = new JMarkerPanel();
        markierung.setBounds(0, 0, 0, 0);
        markierung.setBackgroundImage("gfx/allgemein/markierung.png");
        markierung.setOpaque(false);
        markierung.setVisible(false);
        markierung.setCursor(new Cursor(Cursor.MOVE_CURSOR));
        designLayeredPane.add(markierung, JLayeredPane.DRAG_LAYER);

        /* scrollpane für das Linke Panel (sidebar) */
        draftSidebarScrollpane = new JScrollPane(draftSidebar.getLeistenpanel());
        draftSidebarScrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (Information.isLowResolution()) {
            draftSidebarScrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            draftSidebarScrollpane.getVerticalScrollBar().setUnitIncrement(10);
        } else {
            draftSidebarScrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }

        documentSidebarScrollpane = new JScrollPane(documentSidebar.getLeistenpanel());
        documentSidebarScrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        /* scrollpane für das Mittlere Panel */
        designLayeredPane.add(draftPanel, JLayeredPane.PALETTE_LAYER);
        docuPanel.setBackgroundImage("gfx/allgemein/entwurfshg.png");
        docuPanel.setBounds(0, 0, FLAECHE_BREITE, FLAECHE_HOEHE);
        designLayeredPane.add(docuPanel, JLayeredPane.DEFAULT_LAYER);
        docuDragPanel.setBounds(0, 0, FLAECHE_BREITE, FLAECHE_HOEHE);
        docuDragPanel.setOpaque(false);
        designLayeredPane.add(docuDragPanel, JLayeredPane.DRAG_LAYER);

        designView = new JScrollPane(designLayeredPane);
        designView.getVerticalScrollBar().setUnitIncrement(10);

        simulationView = new JScrollPane(simulationPanel);
        simulationView.getVerticalScrollBar().setUnitIncrement(10);

        /*
         * Wird auf ein Item der Sidebar geklickt, so wird ein neues Vorschau-Label mit dem entsprechenden Icon
         * erstellt.
         * 
         * Wird die Maus auf dem Entwurfspanel losgelassen, während ein Item gedragged wird, so wird eine neue
         * Komponente erstellt.
         */
        draftSidebarScrollpane.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {

                JSidebarButton button = draftSidebar.findButtonAt(e.getX(), e.getY()
                        + draftSidebarScrollpane.getVerticalScrollBar().getValue());
                if (button != null) {
                    neueVorschau(button.getTyp(), e.getX() - draftSidebarScrollpane.getWidth()
                            + GUIContainer.getGUIContainer().getXOffset(), e.getY()
                            + GUIContainer.getGUIContainer().getYOffset());
                    GUIEvents.getGUIEvents().resetAndHideCablePreview();
                }
            }

            public void mouseReleased(MouseEvent e) {
                int xPosMainArea, yPosMainArea;

                xPosMainArea = e.getX() - draftSidebarScrollpane.getWidth();
                yPosMainArea = e.getY();
                if (dragVorschau.isVisible() && xPosMainArea >= 0 && xPosMainArea <= designView.getWidth()
                        && yPosMainArea >= 0 && yPosMainArea <= designView.getHeight()) {
                    neuerKnoten(xPosMainArea, yPosMainArea, dragVorschau);
                }
                dragVorschau.setVisible(false);
            }
        });

        /*
         * Sofern die Drag & Drop Vorschau sichtbar ist, wird beim draggen der Maus die entsprechende Vorschau auf die
         * Mausposition verschoben.
         */
        draftSidebarScrollpane.addMouseMotionListener(new MouseInputAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragVorschau.isVisible()) {
                    dragVorschau.setBounds(e.getX() - draftSidebarScrollpane.getWidth()
                            + GUIContainer.getGUIContainer().getXOffset(), e.getY()
                            + GUIContainer.getGUIContainer().getYOffset(), dragVorschau.getWidth(),
                            dragVorschau.getHeight());
                }
            }
        });

        documentSidebarScrollpane.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                JSidebarButton button = documentSidebar.findButtonAt(e.getX(), e.getY());
                if (button != null) {
                    if (GUIDocumentationSidebar.TYPE_RECTANGLE.equals(button.getTyp())) {
                        docuElement = new JDocuElement(false);
                    } else if (GUIDocumentationSidebar.TYPE_TEXTFIELD.equals(button.getTyp())) {
                        docuElement = new JDocuElement(true);
                    }
                    docuElement.setSelected(true);
                    docuElement.setLocation(e.getX() - draftSidebarScrollpane.getWidth()
                            + GUIContainer.getGUIContainer().getXOffset(), e.getY()
                            + GUIContainer.getGUIContainer().getYOffset());
                    docuDragPanel.add(docuElement);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.getX() > documentSidebarScrollpane.getWidth() && docuElement != null) {
                    docuItems.add(GUIDocuItem.createDocuItem(docuElement));
                    SzenarioVerwaltung.getInstance().setzeGeaendert();
                    docuElement.setSelected(false);
                    docuDragPanel.remove(docuElement);
                    draftPanel.add(docuElement);
                    GUIContainer.this.updateViewport();
                } else if (docuElement != null) {
                    docuDragPanel.remove(docuElement);
                    docuDragPanel.updateUI();
                }
                docuElement = null;
            }
        });

        documentSidebarScrollpane.addMouseMotionListener(new MouseInputAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (docuElement != null) {
                    docuElement.setLocation(e.getX() - draftSidebarScrollpane.getWidth()
                            + GUIContainer.getGUIContainer().getXOffset(), e.getY()
                            + GUIContainer.getGUIContainer().getYOffset());
                }
            }
        });

        /*
         * Erzeugen und transformieren des Auswahlrahmens, und der sich darin befindenden Objekte.
         */
        designView.addMouseMotionListener(new MouseInputAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (activeSite == GUIMainMenu.MODUS_ENTWURF) {
                    GUIEvents.getGUIEvents().mausDragged(e);
                }
            }
        });

        designView.addMouseListener(new MouseInputAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (activeSite == GUIMainMenu.MODUS_ENTWURF) {
                    GUIEvents.getGUIEvents().mausReleased();
                }
            }

            public void mousePressed(MouseEvent e) {
                if (activeSite == GUIMainMenu.MODUS_ENTWURF) {
                    GUIEvents.getGUIEvents().mausPressedDesignMode(e);
                }
            }
        });

        designView.addMouseMotionListener(new MouseInputAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (kabelvorschau.isVisible()) {
                    kabelvorschau.setBounds(e.getX() + designView.getHorizontalScrollBar().getValue(), e.getY()
                            + designView.getVerticalScrollBar().getValue(), kabelvorschau.getWidth(),
                            kabelvorschau.getHeight());
                    if (ziel2Label != null)
                        ziel2Label.setLocation(e.getX() + designView.getHorizontalScrollBar().getValue(), e.getY()
                                + designView.getVerticalScrollBar().getValue());
                    if (kabelPanelVorschau != null) {
                        kabelPanelVorschau.updateBounds();
                    }

                }
            }
        });

        simulationView.addMouseListener(new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                GUIEvents.getGUIEvents().mausPressedSimulationMode(e);
            }
        });

        JMainFrame.getJMainFrame().setVisible(true);
        setActiveSite(GUIMainMenu.MODUS_ENTWURF);
    }

    public void exportAsImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "Portable Network Graphics";
            }
        });
        String path = SzenarioVerwaltung.getInstance().holePfad();
        if (path != null) {
            String szenarioFile = new File(path).getAbsolutePath();
            File preselectedFile = new File(szenarioFile.substring(0, szenarioFile.lastIndexOf(".")) + ".png");

            fileChooser.setSelectedFile(preselectedFile);
        }

        if (fileChooser.showSaveDialog(JMainFrame.getJMainFrame()) == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                GUIPrintPanel printPanel = new GUIPrintPanel();
                printPanel
                        .updateViewport(nodeItems, cableItems, docuItems, SzenarioVerwaltung.getInstance().holePfad());
                BufferedImage bi = new BufferedImage(printPanel.getWidth(), printPanel.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.createGraphics();
                printPanel.paint(g);
                g.dispose();
                BufferedImage printArea = bi.getSubimage(printPanel.getClipX(), printPanel.getClipY(),
                        printPanel.getClipWidth(), printPanel.getClipHeight());
                String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
                imagePath = imagePath.endsWith(".png") ? imagePath : imagePath + ".png";
                Main.debug.println("export to file: " + imagePath);
                ImageOutputStream outputStream = null;
                try {
                    outputStream = new FileImageOutputStream(new File(imagePath));
                    ImageIO.write(printArea, "png", outputStream);
                } catch (Exception ex) {
                    Main.debug.println("export of file failed: " + ex.getMessage());
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }
    }

    private GUIContainer() {
        Image image;

        image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gfx/hardware/kabel.png"));
        JMainFrame.getJMainFrame().setIconImage(image);
    }

    /**
     * Erstellt ein neues Item. Der Dateiname des Icons wird über den String "komponente" angegeben, die Position über x
     * und y. Das Item wird anschließend dem Entwurfspanel hinzugefügt, und das Entwurfspanel wird aktualisiert.
     * 
     * FIXME -> Gibt immer TRUE zurück! FIXME -> Vielleicht neu bezeichnen "neuesItem" o.ä.
     * 
     * @author Johannes Bade & Thomas Gerding
     * 
     * @param komponente
     * @param x
     * @param y
     * @return boolean
     */
    private boolean neuerKnoten(int x, int y, JSidebarButton label) {
        Knoten neuerKnoten = null;
        GUIKnotenItem item;
        JSidebarButton templabel;
        ImageIcon tempIcon = null;

        SzenarioVerwaltung.getInstance().setzeGeaendert();

        ListIterator<GUIKnotenItem> it = nodeItems.listIterator();
        while (it.hasNext()) {
            item = (GUIKnotenItem) it.next();
            item.getImageLabel().setSelektiert(false);
        }

        if (label.getTyp().equals(Switch.TYPE)) {
            neuerKnoten = new Switch();
            tempIcon = new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.SWITCH));

        } else if (label.getTyp().equals(Rechner.TYPE)) {
            neuerKnoten = new Rechner();
            tempIcon = new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.RECHNER));
        } else if (label.getTyp().equals(Notebook.TYPE)) {
            neuerKnoten = new Notebook();
            tempIcon = new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.NOTEBOOK));
        } else if (label.getTyp().equals(Vermittlungsrechner.TYPE)) {
            neuerKnoten = new Vermittlungsrechner();
            tempIcon = new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.VERMITTLUNGSRECHNER));

            Object[] possibleValues = { "2", "3", "4", "5", "6", "7", "8" };
            Object selectedValue = JOptionPane.showInputDialog(JMainFrame.getJMainFrame(),
                    messages.getString("guicontainer_msg1"), messages.getString("guicontainer_msg2"),
                    JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
            if (selectedValue != null) {
                ((Vermittlungsrechner) neuerKnoten).setzeAnzahlAnschluesse(Integer.parseInt((String) selectedValue));
            }
        } else if (label.getTyp().equals(Modem.TYPE)) {
            neuerKnoten = new Modem();
            tempIcon = new ImageIcon(getClass().getResource("/" + GUIDesignSidebar.MODEM));
        } else {
            Main.debug.println("ERROR (" + this.hashCode() + "): " + "unbekannter Hardwaretyp " + label.getTyp()
                    + " konnte nicht erzeugt werden.");
        }

        if (tempIcon != null && neuerKnoten != null) {
            templabel = new JSidebarButton(neuerKnoten.holeAnzeigeName(), tempIcon, neuerKnoten.holeHardwareTyp());
            templabel.setBounds(x + designView.getHorizontalScrollBar().getValue(), y
                    + designView.getVerticalScrollBar().getValue(), templabel.getWidth(), templabel.getHeight());

            item = new GUIKnotenItem();
            item.setKnoten(neuerKnoten);
            item.setImageLabel(templabel);

            setProperty(item);
            item.getImageLabel().setSelektiert(true);
            nodeItems.add(item);

            draftPanel.add(templabel);
            draftPanel.repaint();

            GUIEvents.getGUIEvents().setNewItemActive(item);

            return true;
        } else {
            return false;
        }

    }

    /**
     * 
     * Entwurfsmuster: Singleton
     * 
     * Da die GUI nur einmal erstellt werden darf, und aus verschiedenen Klassen auf sie zugegriffen wird, ist diese als
     * Singleton realisiert.
     * 
     * getGUIContainer() ruft den private Konstruktor auf, falls dies noch nicht geschehen ist, ansonsten wird die
     * Referenz auf die Klasse zurückgegeben.
     * 
     * @author Johannes Bade & Thomas Gerding
     * 
     * @return ref
     */
    public static GUIContainer getGUIContainer() {
        if (ref == null) {
            ref = new GUIContainer();
            if (ref == null)
                Main.debug.println("ERROR (static) getGUIContainer(): Fehler!!! ref==null");
        }

        return ref;
    }

    private void neueVorschau(String hardwareTyp, int x, int y) {
        String tmp;

        Main.debug.println("GUIContainer: die Komponenten-Vorschau wird erstellt.");
        if (hardwareTyp.equals(Kabel.TYPE)) {
            tmp = GUIDesignSidebar.KABEL;
        } else if (hardwareTyp.equals(Switch.TYPE)) {
            tmp = GUIDesignSidebar.SWITCH;
        } else if (hardwareTyp.equals(Rechner.TYPE)) {
            tmp = GUIDesignSidebar.RECHNER;
        } else if (hardwareTyp.equals(Notebook.TYPE)) {
            tmp = GUIDesignSidebar.NOTEBOOK;
        } else if (hardwareTyp.equals(Vermittlungsrechner.TYPE)) {
            tmp = GUIDesignSidebar.VERMITTLUNGSRECHNER;
        } else if (hardwareTyp.equals(Modem.TYPE)) {
            tmp = GUIDesignSidebar.MODEM;
        } else {
            tmp = null;
            Main.debug.println("GUIContainer: ausgewaehlte Hardware-Komponente unbekannt!");
        }

        dragVorschau.setTyp(hardwareTyp);
        dragVorschau.setIcon(new ImageIcon(getClass().getResource("/" + tmp)));
        dragVorschau.setBounds(x, y, dragVorschau.getWidth(), dragVorschau.getHeight());
        dragVorschau.setVisible(true);
    }

    /**
     * Prüft ob ein Punkt (definiert durch die Parameter x & y) auf einem Objekt (definiert durch den Parameter komp)
     * befindet.
     * 
     * @author Johannes Bade
     */
    public boolean aufObjekt(Component komp, int x, int y) {
        if (x > komp.getX() && x < komp.getX() + komp.getWidth() && y > komp.getY()
                && y < komp.getY() + komp.getHeight()) {
            return true;
        }
        return false;
    }

    /**
     * Löscht alle Elemente der Item- und Kabelliste und frischt den Viewport auf. Dies dient dem Reset vor dem Laden
     * oder beim Erstellen eines neuen Projekts.
     * 
     * @author Johannes Bade & Thomas Gerding
     */
    public void clearAllItems() {
        nodeItems.clear();
        cableItems.clear();
        docuItems.clear();
        updateViewport();
    }

    public void updateViewport() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIContainer), updateViewport()");
        if (activeSite == GUIMainMenu.MODUS_AKTION) {
            simulationPanel.updateViewport(nodeItems, cableItems, docuItems, false);
            simulationPanel.updateUI();
        } else {
            draftPanel.updateViewport(nodeItems, cableItems, docuItems, activeSite == GUIMainMenu.MODUS_DOKUMENTATION);
            draftPanel.updateUI();
        }
    }

    /**
     * 
     * Geht die Liste der Kabel durch und ruft bei diesen updateBounds() auf. So werden die Kabel neu gezeichnet.
     * 
     * @author Thomas Gerding & Johannes Bade
     * 
     */
    public void updateCables() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (GUIContainer), updateCables()");
        ListIterator<GUIKabelItem> it = cableItems.listIterator();
        while (it.hasNext()) {
            GUIKabelItem tempCable = (GUIKabelItem) it.next();
            tempCable.getKabelpanel().updateBounds();
        }
    }

    public JSidebarButton getKabelvorschau() {
        return kabelvorschau;
    }

    public void setKabelvorschau(JSidebarButton kabelvorschau) {
        this.kabelvorschau = kabelvorschau;
    }

    public int getActiveSite() {
        return activeSite;
    }

    public void setActiveSite(int activeSite) {
        this.activeSite = activeSite;

        if (activeSite == GUIMainMenu.MODUS_ENTWURF) {
            closeDesktops();
            designView.getVerticalScrollBar().setValue(simulationView.getVerticalScrollBar().getValue());
            designView.getHorizontalScrollBar().setValue(simulationView.getHorizontalScrollBar().getValue());
            JMainFrame.getJMainFrame().removeFromContentPane(this.documentSidebarScrollpane);
            JMainFrame.getJMainFrame().addToContentPane(this.draftSidebarScrollpane, BorderLayout.WEST);
            JMainFrame.getJMainFrame().removeFromContentPane(this.simulationView);
            JMainFrame.getJMainFrame().addToContentPane(this.designView, BorderLayout.CENTER);
            JMainFrame.getJMainFrame().addToContentPane(property, BorderLayout.SOUTH);
            draftSidebarScrollpane.updateUI();
            designView.updateUI();
            property.updateUI();
        } else if (activeSite == GUIMainMenu.MODUS_DOKUMENTATION) {
            closeDesktops();
            designView.getVerticalScrollBar().setValue(simulationView.getVerticalScrollBar().getValue());
            designView.getHorizontalScrollBar().setValue(simulationView.getHorizontalScrollBar().getValue());
            JMainFrame.getJMainFrame().removeFromContentPane(this.draftSidebarScrollpane);
            JMainFrame.getJMainFrame().addToContentPane(this.documentSidebarScrollpane, BorderLayout.WEST);
            JMainFrame.getJMainFrame().removeFromContentPane(this.simulationView);
            auswahl.setVisible(false);
            markierung.setVisible(false);
            JMainFrame.getJMainFrame().addToContentPane(this.designView, BorderLayout.CENTER);
            JMainFrame.getJMainFrame().removeFromContentPane(property);
            documentSidebarScrollpane.updateUI();
            designView.updateUI();
        } else if (activeSite == GUIMainMenu.MODUS_AKTION) {
            simulationView.getVerticalScrollBar().setValue(designView.getVerticalScrollBar().getValue());
            simulationView.getHorizontalScrollBar().setValue(designView.getHorizontalScrollBar().getValue());
            JMainFrame.getJMainFrame().removeFromContentPane(this.documentSidebarScrollpane);
            JMainFrame.getJMainFrame().removeFromContentPane(this.draftSidebarScrollpane);
            auswahl.setVisible(false);
            markierung.setVisible(false);
            JMainFrame.getJMainFrame().removeFromContentPane(this.designView);
            JMainFrame.getJMainFrame().addToContentPane(this.simulationView, BorderLayout.CENTER);
            JMainFrame.getJMainFrame().removeFromContentPane(property);
            simulationView.updateUI();
        }
        GUIEvents.getGUIEvents().resetAndHideCablePreview();
        JMainFrame.getJMainFrame().invalidate();
        JMainFrame.getJMainFrame().validate();
        updateViewport();
    }

    public List<GUIKabelItem> getCableItems() {
        return cableItems;
    }

    public void setCablelist(List<GUIKabelItem> cablelist) {
        this.cableItems = cablelist;
    }

    public int getAbstandLinks() {
        int abstand = 0;
        abstand = GUIContainer.getGUIContainer().getSidebar().getLeistenpanel().getWidth();
        return abstand;
    }

    public int getAbstandOben() {
        int abstand = 0;
        abstand = GUIContainer.getGUIContainer().getMenu().getMenupanel().getHeight();
        return abstand;
    }

    @Deprecated
    public JMarkerPanel getMarkierung() {
        return markierung;
    }

    public boolean isMarkerVisible() {
        return markierung.isVisible();
    }

    public void moveMarker(int incX, int incY, List<GUIKnotenItem> markedlist) {
        int newMarkerX = markierung.getX() + incX;
        if (newMarkerX + markierung.getWidth() >= FLAECHE_BREITE || newMarkerX < 0) {
            incX = 0;
        }
        int newMarkerY = markierung.getY() + incY;
        if (newMarkerY + markierung.getHeight() >= FLAECHE_HOEHE || newMarkerY < 0) {
            incY = 0;
        }
        markierung.setBounds(markierung.getX() + incX, markierung.getY() + incY, markierung.getWidth(),
                markierung.getHeight());

        for (GUIKnotenItem knotenItem : markedlist) {
            JSidebarButton templbl = knotenItem.getImageLabel();
            templbl.setLocation(templbl.getX() + incX, templbl.getY() + incY);
        }
        updateCables();
    }

    public static JMarkerPanel getAuswahl() {
        return auswahl;
    }

    public JSidebarButton getDragVorschau() {
        return dragVorschau;
    }

    public JKonfiguration getProperty() {
        return property;
    }

    public void showDesktop(GUIKnotenItem hardwareItem) {
        ListIterator<GUIDesktopWindow> it;
        Betriebssystem bs;
        GUIDesktopWindow tmpDesktop = null;
        boolean fertig = false;

        if (hardwareItem != null && hardwareItem.getKnoten() instanceof Host) {
            bs = (Betriebssystem) ((Host) hardwareItem.getKnoten()).getSystemSoftware();

            it = desktopWindowList.listIterator();
            while (!fertig && it.hasNext()) {
                tmpDesktop = it.next();
                if (bs == tmpDesktop.getBetriebssystem()) {
                    tmpDesktop.setVisible(true);
                    fertig = true;
                }
            }

            if (!fertig) {
                tmpDesktop = new GUIDesktopWindow(bs);
                setDesktopPos(tmpDesktop);
                tmpDesktop.setVisible(true);
                tmpDesktop.toFront();

                fertig = true;
            }

            if (tmpDesktop != null)
                this.desktopWindowList.add(tmpDesktop);
        }
    }

    private void setDesktopPos(GUIDesktopWindow tmpDesktop) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension desktopSize = tmpDesktop.getSize();
        int numberOfDesktopsPerRow = (int) (screenSize.getWidth() / desktopSize.getWidth());
        int numberOfDesktopsPerColumn = (int) (screenSize.getHeight() / desktopSize.getHeight());
        int totalNumberOfDesktops = numberOfDesktopsPerRow * numberOfDesktopsPerColumn;
        int xPos = 0;
        int yPos = 0;
        if (desktopWindowList.size() < totalNumberOfDesktops
                && !Information.getDesktopWindowMode().equals(GUIDesktopWindow.Mode.STACK)) {
            if (Information.getDesktopWindowMode().equals(GUIDesktopWindow.Mode.COLUMN)) {
                xPos = (desktopWindowList.size() / numberOfDesktopsPerColumn) * (int) desktopSize.getWidth();
                yPos = (desktopWindowList.size() % numberOfDesktopsPerColumn) * (int) desktopSize.getHeight();
            } else {
                xPos = (desktopWindowList.size() % numberOfDesktopsPerRow) * (int) desktopSize.getWidth();
                yPos = (desktopWindowList.size() / numberOfDesktopsPerRow) * (int) desktopSize.getHeight();
            }
        } else {
            int overlappingDesktops = Information.getDesktopWindowMode().equals(GUIDesktopWindow.Mode.STACK) ? desktopWindowList
                    .size() : desktopWindowList.size() - totalNumberOfDesktops;
            xPos = (overlappingDesktops + 1) * 20;
            yPos = (overlappingDesktops + 1) * 20;
            if (xPos + desktopSize.getWidth() > screenSize.getWidth()) {
                xPos = 0;
            }
            if (yPos + desktopSize.getHeight() > screenSize.getHeight()) {
                yPos = 0;
            }
        }
        tmpDesktop.setBounds(xPos, yPos, tmpDesktop.getWidth(), tmpDesktop.getHeight());
    }

    public void addDesktopWindow(GUIKnotenItem hardwareItem) {
        GUIDesktopWindow tmpDesktop = null;
        Betriebssystem bs;

        if (hardwareItem != null && hardwareItem.getKnoten() instanceof Host) {
            bs = (Betriebssystem) ((Host) hardwareItem.getKnoten()).getSystemSoftware();
            tmpDesktop = new GUIDesktopWindow(bs);
            desktopWindowList.add(tmpDesktop);
        }
    }

    public void closeDesktops() {
        ListIterator<GUIDesktopWindow> it;

        it = desktopWindowList.listIterator();
        while (it.hasNext()) {
            it.next().setVisible(false);
            it.remove();
        }
    }

    public void setProperty(GUIKnotenItem hardwareItem) {
        boolean maximieren = false;

        if (property != null) {
            // do actions required prior to getting unselected (i.e.,
            // postprocessing)
            property.doUnselectAction();
            maximieren = property.isMaximiert();
            JMainFrame.getJMainFrame().removeFromContentPane(property);
        }

        if (hardwareItem == null) {
            property = JKonfiguration.getInstance(null);
        } else {
            property = JKonfiguration.getInstance(hardwareItem.getKnoten());
        }
        JMainFrame.getJMainFrame().addToContentPane(property, BorderLayout.SOUTH);
        property.updateAttribute();
        property.updateUI();
        if (hardwareItem == null || !maximieren) {
            property.minimieren();
        } else {
            property.maximieren();
        }
    }

    public JScrollPane getScrollPane() {
        return designView;
    }

    public int getXOffset() {
        if (activeSite == GUIMainMenu.MODUS_AKTION) {
            return simulationView.getHorizontalScrollBar().getValue();
        }
        return designView.getHorizontalScrollBar().getValue();
    }

    public int getYOffset() {
        if (activeSite == GUIMainMenu.MODUS_AKTION) {
            return simulationView.getVerticalScrollBar().getValue();
        }
        return designView.getVerticalScrollBar().getValue();
    }

    public JScrollPane getSidebarScrollpane() {
        if (activeSite == GUIMainMenu.MODUS_ENTWURF) {
            return draftSidebarScrollpane;
        }
        return documentSidebarScrollpane;
    }

    public GUIDesignPanel getDesignpanel() {
        return draftPanel;
    }

    public GUISimulationPanel getSimpanel() {
        return simulationPanel;
    }

    public GUIDesignSidebar getSidebar() {
        return draftSidebar;
    }

    public GUIMainMenu getMenu() {
        return menu;
    }

    public JCablePanel getKabelPanelVorschau() {
        return kabelPanelVorschau;
    }

    public void setKabelPanelVorschau(JCablePanel kabelPanelVorschau) {
        this.kabelPanelVorschau = kabelPanelVorschau;
    }

    public JSidebarButton getLabelforKnoten(Knoten node) {
        List<GUIKnotenItem> list = getKnotenItems();
        for (int i = 0; i < list.size(); i++) {
            if (((GUIKnotenItem) list.get(i)).getKnoten().equals(node)) {
                return ((GUIKnotenItem) list.get(i)).getImageLabel();
            }
        }
        return null;
    }

    public List<GUIDocuItem> getDocuItems() {
        return docuItems;
    }

    public void removeDocuElement(JDocuElement elem) {
        for (GUIDocuItem item : docuItems) {
            if (item.asDocuElement().equals(elem)) {
                docuItems.remove(item);
                break;
            }
        }
    }

    public void removeCableItem(GUIKabelItem cableItem) {
        cableItems.remove(cableItem);
    }

}
