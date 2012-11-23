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
package filius.software.vermittlungsschicht;

import java.util.LinkedList;

import filius.Main;
import filius.exception.VerbindungsException;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.rahmenprogramm.I18n;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.system.SystemSoftware;

/**
 * This class implements the ICMP protocol -- at least for echo
 * request/response.
 */
public class ICMP extends VermittlungsProtokoll implements I18n {

	public static final int ECHO_REPLY = 0;
	public static final int ECHO_REQUEST = 8;
	public static final int DESTINATION_UNREACHABLE = 3;
	public static final int TIME_EXCEEDED = 11;

	public static final int CODE_ECHO_REPLY = 0;
	public static final int CODE_DEST_NETWORK_UNREACHABLE = 0;
	public static final int CODE_DEST_HOST_UNREACHABLE = 1;
	public static final int CODE_ECHO_REQUEST = 0;
	public static final int CODE_TTL_EXPIRED = 0;

	/**
	 * 
	 */
	private ICMPThread thread;

	/**
	 * Standard-Konstruktor zur Initialisierung der zugehoerigen Systemsoftware
	 * 
	 * @param systemAnwendung
	 */
	public ICMP(SystemSoftware systemAnwendung) {
		super(systemAnwendung);
		Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (ICMP), constr: ICMP("
		        + systemAnwendung + ")");
	}

	public void starten() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ICMP), starten()");
		thread = new ICMPThread(this);
		thread.starten();
	}

	public void beenden() {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ICMP), beenden()");
		if (thread != null)
			thread.beenden();
	}

	private void placeLocalICMPPacket(IcmpPaket icmpPacket) {
		Main.debug.println("INVOKED (" + this.hashCode() + ") " + getClass() + " (ICMP), placeLocalICMPPacket("
		        + icmpPacket.toString() + ")");
		LinkedList<IcmpPaket> icmpPakete = ((InternetKnotenBetriebssystem) holeSystemSoftware()).holeEthernet()
		        .holeICMPPuffer();
		synchronized (icmpPakete) {
			icmpPakete.add(icmpPacket);
			icmpPakete.notify();
		}
	}

	/** Hilfsmethode zum Versenden eines ICMP Echo Requests */
	public void sendEchoRequest(String destIp, int seqNr) {
		sendeICMP(ECHO_REQUEST, CODE_ECHO_REQUEST, seqNr, null, destIp);
	}

	/** Hilfsmethode zum Versenden eines ICMP Echo Reply */
	public void sendEchoReply(IcmpPaket rcvPacket) {
		sendeICMP(ECHO_REPLY, CODE_ECHO_REPLY, rcvPacket.getSeqNr(), rcvPacket.getZielIp(), rcvPacket.getQuellIp());
	}

	public void sendeICMP(int typ, int code, String zielIP) {
		sendeICMP(typ, code, 0, null, zielIP);
	}

	public void sendeICMP(int typ, int code, int seqNr, String quellIP, String zielIP) {
		sendeICMP(typ, code, 64, seqNr, quellIP, zielIP);
	}

	public void sendeICMP(int typ, int code, int ttl, int seqNr, String quellIP, String zielIP) {
		IcmpPaket icmpPaket = new IcmpPaket();
		icmpPaket.setProtokollTyp(EthernetFrame.IP);
		icmpPaket.setZielIp(zielIP);
		icmpPaket.setIcmpType(typ);
		icmpPaket.setIcmpCode(code);
		icmpPaket.setSeqNr(seqNr);
		icmpPaket.setTtl(ttl);

		try {
			InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
			Route route = bs.getWeiterleitungstabelle().holeWeiterleitungsEintrag(zielIP);
			if (quellIP == null) {
				icmpPaket.setQuellIp(route.getInterfaceIpAddress());
			} else {
				icmpPaket.setQuellIp(quellIP);
			}
			sendeUnicast(icmpPaket, zielIP, route);
		} catch (RouteNotFoundException e) {
			// Es wurde keine Route gefunden, ueber die das Paket versendet
			// werden koennte.
			// Falls das weiterzuleitende Paket ein ICMP Echo Request ist,
			// muss ein ICMP Destination Unreachable: Network Unreachable (3/0)
			// zurueckgesendet werden. Andere ICMP-Paket muessen verworfen
			// werden.
			if (typ == ECHO_REQUEST && code == CODE_ECHO_REQUEST) {
				sendeICMP(DESTINATION_UNREACHABLE, CODE_DEST_NETWORK_UNREACHABLE, seqNr, null, quellIP);
			}
		}
	}

	/**
	 * Hilfsmethode zum versenden eines Unicast-Pakets. Hier wird unterschieden,
	 * ob sich die Ziel-IP-Adresse im lokalen Rechnernetz befindet oder ueber
	 * das Gateway verschickt werden muss.
	 * 
	 * @param paket
	 *            das zu versendende IP-Paket
	 * @param zielIp
	 * @param route
	 *            TODO
	 * @throws RouteNotFoundException
	 * @throws VerbindungsException
	 */
	private void sendeUnicast(IcmpPaket paket, String zielIp, Route route) throws RouteNotFoundException {
		InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();

		String gateway = route.getGateway();
		String schnittstelle = route.getInterfaceIpAddress();
		InternetKnoten knoten = (InternetKnoten) bs.getKnoten();
		NetzwerkInterface nic = knoten.getNetzwerkInterfaceByIp(schnittstelle);
		String netzmaske = nic.getSubnetzMaske();
		String macAddressOfNicToUse = nic.getMac();

		String nextHopIpAddress = null;
		if (gleichesRechnernetz(zielIp, schnittstelle, netzmaske)) {
			// adressierter Knoten befindet sich im lokalen Rechnernetz
			nextHopIpAddress = paket.getZielIp();
		} else {
			// adressierter Knoten ist ueber Gateway zu erreichen
			nextHopIpAddress = gateway;
		}
		sendUnicastToNextHop(paket, nextHopIpAddress, macAddressOfNicToUse);
	}

	/**
	 * Hilfsmethode zum Versenden eines Unicast-Pakets im lokalen Rechnernetz.
	 * 
	 * @param paket
	 *            das zu versendende IP-Paket
	 * @param ziel
	 *            die Ziel-IP
	 * @param macOfNicToUse
	 */
	private void sendUnicastToNextHop(IcmpPaket paket, String ziel, String macOfNicToUse) {
		InternetKnotenBetriebssystem bs = (InternetKnotenBetriebssystem) holeSystemSoftware();
		String zielMacAdresse = bs.holeARP().holeARPTabellenEintrag(ziel);

		if (this.isLocalAddress(ziel)) {
			placeLocalICMPPacket(paket);
		} else if (zielMacAdresse != null) {
			// MAC-Adresse konnte bestimmt werden
			bs.holeEthernet().senden(paket, macOfNicToUse, zielMacAdresse, EthernetFrame.IP);
		} else {
			// Es konnte keine MAC-Adresse bestimmt werden.
			// Falls das weiterzuleitende Paket ein ICMP Echo Request ist,
			// muss ein ICMP Destination Unreachable: Host Unreachable (3/1)
			// zurueckgesendet werden. Andere ICMP-Paket muessen verworfen
			// werden.
			if (paket.getIcmpType() == ECHO_REQUEST && paket.getIcmpCode() == CODE_ECHO_REQUEST) {
				sendeICMP(DESTINATION_UNREACHABLE, CODE_DEST_HOST_UNREACHABLE, paket.getSeqNr(), null,
				        paket.getQuellIp());
			}
		}
	}

	/**
	 * Methode zum Weiterleiten eines ICMP-Pakets. Zunaechst wird geprueft, ob
	 * das Feld Time-to-Live-Feld (TTL) noch nicht abgelaufen ist (d. h. TTL
	 * groesser 0). Wenn diese Bedingung erfuellt ist, wird zunaechst geprueft,
	 * ob es sich um einen Broadcast handelt. Ein solches Paket wird nicht
	 * weitergeleitet sondern nur an die Transportschicht weitergegeben. Sonst
	 * wird die Weiterleitungstabelle nach einem passenden Eintrag abgefragt.
	 * Anhand des zurueckgegebenen Eintrags wird geprueft, ob das Paket fuer den
	 * eigenen Rechner ist oder ob ein Unicast-Paket verschickt werden muss.
	 * 
	 * @param icmpPaket
	 *            das zu versendende ICMP-Paket
	 */
	public void weiterleitenPaket(IcmpPaket icmpPaket) {
		if (icmpPaket.getTtl() <= 0) {
			// TTL ist abgelaufen.
			// (wird in ICMPThread.verarbeiteDatenEinheit()
			// dekrementiert, bevor diese Funktion aufgerufen
			// wird)
			// ICMP Timeout Expired In Transit (11/0) zuruecksenden:
			sendeICMP(TIME_EXCEEDED, CODE_TTL_EXPIRED, icmpPaket.getSeqNr(), null, icmpPaket.getQuellIp());
		} else {
			// TTL ist nicht abgelaufen.
			// Paket weiterleiten:
			sendeICMP(icmpPaket.getIcmpType(), icmpPaket.getIcmpCode(), icmpPaket.getSeqNr(), icmpPaket.getQuellIp(),
			        icmpPaket.getZielIp());
		}
	}

	public int startSinglePing(String destIp, int seqNr) throws java.util.concurrent.TimeoutException {
		return thread.startSinglePing(destIp, seqNr);
	}

	public IcmpPaket sendProbe(String destIp, int ttl, int seqNr) {
		return thread.sendProbe(destIp, ttl, seqNr);
	}

	public ICMPThread getICMPThread() {
		return thread;
	}
}
