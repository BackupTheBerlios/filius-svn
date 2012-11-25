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
package filius.software.dns;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import filius.Main;
import filius.software.clientserver.UDPServerAnwendung;
import filius.software.system.Datei;
import filius.software.system.Dateisystem;
import filius.software.transportschicht.Socket;

public class DNSServer extends UDPServerAnwendung {

	private boolean recursiveResolutionEnabled = false;

	public boolean isRecursiveResolutionEnabled() {
		return recursiveResolutionEnabled;
	}

	public void setRecursiveResolutionEnabled(boolean recursiveResolutionEnabled) {
		this.recursiveResolutionEnabled = recursiveResolutionEnabled;
	}

	public DNSServer() {
		super();
		Main.debug.println("INVOKED-2 (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), constr: DNSServer()");

		setPort(53);
	}

	public void starten() {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), starten()");
		super.starten();

		if (!getSystemSoftware().getDateisystem().dateiVorhanden("dns", "hosts")) {
			getSystemSoftware().getDateisystem().erstelleVerzeichnis("root", "dns");
		}
	}

	public void beenden() {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), beenden()");
		super.beenden();
	}

	public LinkedList<ResourceRecord> holeResourceRecords() {
		return leseRecordListe();
	}

	public void hinzuRecord(String domainname, String typ, String rdata) {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), hinzuRecord(" + domainname + "," + typ + "," + rdata + ")");
		ResourceRecord rr;

		rr = new ResourceRecord(domainname, typ, rdata);
		LinkedList<ResourceRecord> rrList = leseRecordListe();
		rrList.add(rr);
		this.schreibeRecordListe(rrList);
	}

	private LinkedList<ResourceRecord> leseRecordListe() {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + ", initialisiereRecordListe()");
		Datei hosts;
		StringTokenizer tokenizer;
		String line;
		ResourceRecord rr;
		Dateisystem dateisystem;
		LinkedList<ResourceRecord> resourceRecords;

		resourceRecords = new LinkedList<ResourceRecord>();

		dateisystem = getSystemSoftware().getDateisystem();
		hosts = dateisystem.holeDatei(dateisystem.holeRootPfad() + Dateisystem.FILE_SEPARATOR + "dns"
		        + Dateisystem.FILE_SEPARATOR + "hosts");

		if (hosts != null) {
			tokenizer = new StringTokenizer(hosts.getDateiInhalt(), "\n");

			while (tokenizer.hasMoreTokens()) {
				line = tokenizer.nextToken().trim();
				if (!line.equals("") && !(line.split(" ", 5).length < 4)) {
					rr = new ResourceRecord(line);
					resourceRecords.add(rr);
				}
			}
		}
		return resourceRecords;
	}

	private void schreibeRecordListe(List<ResourceRecord> records) {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), schreibeRecordListe()");
		Datei hosts;
		StringBuffer text;
		ListIterator<ResourceRecord> it;
		Dateisystem dateisystem;

		dateisystem = getSystemSoftware().getDateisystem();

		text = new StringBuffer();

		it = records.listIterator();
		while (it.hasNext()) {
			text.append(it.next().toString() + "\n");
		}

		hosts = new Datei();
		hosts.setDateiInhalt(text.toString());
		hosts.setName("hosts");

		dateisystem.speicherDatei(dateisystem.holeRootPfad() + Dateisystem.FILE_SEPARATOR + "dns", hosts);
	}

	public void changeSingleEntry(int recordIdx, int partIdx, String type, String newValue) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + ", changeSingleEntry(" + recordIdx + ","
		        + partIdx + "," + type + "," + newValue + ")");
		List<ResourceRecord> rrList = leseRecordListe();
		int countA = 0;
		// iterating whole list is necessary, since MX and A records are mixed
		// in the records list! :-(
		for (ResourceRecord rrec : rrList) {
			if (rrec.getType().equals(type)) {
				countA++;
			}
			if (countA - 1 == recordIdx) { // found A entry with (filtered)
				                           // index recordIdx
				// Main.debug.println("DEBUG ("+this.hashCode()+") "+getClass()+", changeSingleEntry:  aktuell: countA="+countA+", rrec="+rrec);
				if (partIdx == 0) { // change URL
					rrec.setDomainname(newValue);
				} else if (partIdx == 3) { // change IP
					rrec.setRdata(newValue);
				}
			}
		}
		this.schreibeRecordListe(rrList);
	}

	public void loescheResourceRecord(String domainname, String typ) {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), loescheResourceRecord(" + domainname + "," + typ + ")");
		List<ResourceRecord> rrList = leseRecordListe();

		for (ResourceRecord rr : rrList) {
			if (rr.getDomainname().equalsIgnoreCase(domainname) && rr.getType().equals(typ)) {
				rrList.remove(rr);
				break;
			}
		}
		this.schreibeRecordListe(rrList);
	}

	public ResourceRecord holeRecord(String domainname, String typ) {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), holeRecord(" + domainname + "," + typ + ")");

		for (ResourceRecord rr : leseRecordListe()) {
			if (rr.getDomainname().equalsIgnoreCase(domainname) && rr.getType().equals(typ)) {
				return rr;
			}
		}

		return null;
	}

	public ResourceRecord holeNSRecord(String domainname) {
		String domain;
		String[] parts = domainname.split("\\.");

		for (int i = 0; i < parts.length; i++) {
			domain = this.implodeDomain(parts, i);
			for (ResourceRecord rr : leseRecordListe()) {
				if (rr.getDomainname().equalsIgnoreCase(domain) && rr.getType().equals(ResourceRecord.NAME_SERVER)) {
					return rr;
				}
			}
		}
		for (ResourceRecord rr : leseRecordListe()) {
			if (rr.getDomainname().equalsIgnoreCase(".")) {
				return rr;
			}
		}

		return null;
	}

	private String implodeDomain(String[] parts, int start) {
		StringBuffer domain = new StringBuffer();
		for (int i = start; i < parts.length; i++) {
			domain.append(parts[i]);
			domain.append(".");
		}
		return domain.toString();
	}

	protected void neuerMitarbeiter(Socket socket) {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (DNSServer), neuerMitarbeiter(" + socket + ")");
		DNSServerMitarbeiter dnsMitarbeiter;

		dnsMitarbeiter = new DNSServerMitarbeiter(this, socket);
		dnsMitarbeiter.starten();
		mitarbeiter.add(dnsMitarbeiter);

	}

}
