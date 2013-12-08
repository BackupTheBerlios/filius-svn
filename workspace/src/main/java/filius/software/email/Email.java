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
package filius.software.email;

import java.util.ArrayList;
import java.util.List;

import filius.Main;

/**
 * 
 * @author Andre Asschoff
 * 
 */
public class Email {
    private Object absender;
    private List<Object> empfaenger = new ArrayList<Object>();
    private List<Object> cc = new ArrayList<Object>();
    private List<Object> bcc = new ArrayList<Object>();
    private String betreff;
    private String text = "";

    /**
     * dient dazu, um festzustellen, ob eine Email schon mal abgerufen, und somit wahrscheinlich schon mal gelesen
     * wurde. Standardmaessig ist das true, d.h. sie wurde noch nicht gelesen.
     */
    private boolean neu = true;

    /**
     * dieser Wert wird vom Server, wenn erhalten gesetzt
     */
    private String dateReceived = "";

    /**
     * wird als zu loeschen markiert, wenn sich ausgeloggt wird, wird
     */
    private boolean delete = false;

    /** sie endgueltig geloescht. Sie wird dann nur nicht mehr angezeigt. */
    private boolean versendet = false;

    public Email() {}

    /**
     * Dies ist der Konstruktor der Email. Immer wenn eine neue Email erzeugt wird, wird ihr der String ihrer Attribute
     * übergeben, die dann ausgelesen werden, und in ihren jeweiligen Attributen gespeichert werden. Bsp.: <br />
     * <code>
     * From: <bob@filius.de>
     * To: <eve@filius.de>, <ken@uni.de>
     * Cc: <john@uni.de>
     * Bcc: <berta@filius.de>
     * Subject: Eine kurze Nachricht
     * 
     * Das ist der Nachrichtentext.
     * </code>
     * 
     * @param nachricht
     */
    public Email(String nachricht) {
        String fieldName, fieldData;
        String[] liste;
        String[] emaildaten = nachricht.split("\n");
        int pos1, pos2;
        boolean header = true;

        for (int i = 0; i < emaildaten.length; i++) {
            pos1 = 0;
            pos2 = emaildaten[i].indexOf(":");
            if (pos2 > pos1) {
                fieldName = emaildaten[i].substring(pos1, pos2).trim();
                fieldData = emaildaten[i].substring(pos2 + 1).trim();
            } else {
                fieldName = null;
                fieldData = null;
            }
            if (header) {
                if ("from".equalsIgnoreCase(fieldName)) {
                    absender = new AddressEntry(fieldData);
                } else if ("to".equalsIgnoreCase(fieldName)) {
                    empfaenger.clear();
                    liste = fieldData.split(",");
                    for (int j = 0; j < liste.length; j++) {
                        empfaenger.add(new AddressEntry(liste[j]));
                    }
                } else if ("cc".equalsIgnoreCase(fieldName)) {
                    cc.clear();
                    liste = fieldData.split(",");
                    for (int j = 0; j < liste.length; j++) {
                        cc.add(new AddressEntry(liste[j]));
                    }
                } else if ("bcc".equalsIgnoreCase(fieldName)) {
                    bcc.clear();
                    liste = fieldData.split(",");
                    for (int j = 0; j < liste.length; j++) {
                        bcc.add(new AddressEntry(liste[j]));
                    }
                } else if ("subject".equalsIgnoreCase(fieldName)) {
                    betreff = fieldData;
                } else if (text.equals("")) {
                    header = false;
                }
            } else {
                text = text + emaildaten[i] + "\n";
            }
        }
    }

    /**
     * In dieser Methode werden die Attribute der Email wieder zu einem langen String zusammen gesetzt. Er hat
     * anschliessend wieder die gleiche Form, wie der String, der beim Erzeugen eines neuen Email-Objektes mit
     * uebergeben wurde. <br />
     * <b> Achtung: </b> Die BCC-Empfaenger werden nicht mit ausgegeben! <br />
     * Bsp.: <br />
     * <code>
     * From: <bob@filius.de>
     * To: <eve@filius.de>, <ken@uni.de>
     * Cc: <john@uni.de>
     * Subject: Eine kurze Nachricht
     * 
     * Das ist der Nachrichtentext.
     * </code>
     */
    public String toString() {
        Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (Email), toString()");
        String ergebnis;
        String toListe = "", ccListe = "";

        toListe = holeEmpfaengerListe();

        for (Object rcpt : cc) {
            ccListe += rcpt.toString() + ", ";
        }
        if (!ccListe.isEmpty()) {
            ccListe = ccListe.substring(0, ccListe.length() - 2);
        }

        ergebnis = "";
        if (absender != null) {
            ergebnis += "From: " + absender.toString() + "" + "\n";
        }
        if (!toListe.equals("")) {
            ergebnis += "To: " + toListe + "\n";
        }
        if (!ccListe.equals("")) {
            ergebnis += "Cc: " + ccListe + "\n";
        }
        if (betreff != null) {
            ergebnis += "Subject: " + betreff.trim() + "\n";
        }
        if (!dateReceived.equals("")) {
            ergebnis += "Date Received: " + dateReceived + "\n";
        }
        if (text != null) {
            ergebnis += "\n" + text.trim();
        }

        return ergebnis;
    }

    public String holeEmpfaengerListe() {
        String toListe = "";
        for (Object rcpt : empfaenger) {
            toListe += rcpt.toString() + ", ";
        }
        if (!toListe.isEmpty()) {
            toListe = toListe.substring(0, toListe.length() - 2);
        }
        return toListe;
    }

    public boolean getDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean getNeu() {
        return neu;
    }

    public void setNeu(boolean neu) {
        this.neu = neu;
    }

    public AddressEntry getAbsender() {
        return new AddressEntry(absender);
    }

    public List<AddressEntry> getEmpfaenger() {
        ArrayList<AddressEntry> addressList = new ArrayList<AddressEntry>(empfaenger.size());
        for (Object entry : empfaenger) {
            addressList.add(new AddressEntry(entry));
        }
        return addressList;
    }

    public String getBetreff() {
        return betreff;
    }

    public String getText() {
        return text;
    }

    public void setAbsender(String absender) {
        this.absender = new AddressEntry(absender);
    }

    public void setAbsender(AddressEntry absender) {
        this.absender = absender;
    }

    public void setBetreff(String betreff) {
        this.betreff = betreff;
    }

    public void setEmpfaenger(List<AddressEntry> recipients) {
        for (AddressEntry entry : recipients) {
            this.empfaenger.add((Object) entry);
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(String dateReceived) {
        this.dateReceived = dateReceived;
    }

    public boolean isVersendet() {
        return versendet;
    }

    public void setVersendet(boolean versendet) {
        this.versendet = versendet;
    }

    public List<AddressEntry> getBcc() {
        ArrayList<AddressEntry> addressList = new ArrayList<AddressEntry>(empfaenger.size());
        for (Object entry : bcc) {
            addressList.add(new AddressEntry(entry));
        }
        return addressList;
    }

    public void setBcc(List<AddressEntry> bcc) {
        for (AddressEntry entry : bcc) {
            this.bcc.add((Object) entry);
        }
    }

    public List<AddressEntry> getCc() {
        ArrayList<AddressEntry> addressList = new ArrayList<AddressEntry>(empfaenger.size());
        for (Object entry : cc) {
            addressList.add(new AddressEntry(entry));
        }
        return addressList;
    }

    public void setCc(List<AddressEntry> cc) {
        for (AddressEntry entry : cc) {
            this.cc.add((Object) entry);
        }
    }
}
