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
import filius.hardware.Verbindung;
import filius.software.ProtokollThread;
import filius.software.system.InternetKnotenBetriebssystem;

/**
 * Klasse zur Ueberwachung des Puffers fuer eingehende ICMP-Pakete
 * 
 */
public class ICMPThread extends ProtokollThread {

	private ICMP vermittlung;

	/** der von dem Thread zu ueberwachende Puffer */
	private LinkedList<IcmpPaket> rcvdPackets;

	public ICMPThread(ICMP vermittlung) {
		super(((InternetKnotenBetriebssystem) vermittlung.holeSystemSoftware()).holeEthernet().holeICMPPuffer());
		Main.debug.println("INVOKED-2 (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (ICMPThread), constr: ICMPThread(" + vermittlung + ")");
		this.rcvdPackets = new LinkedList<IcmpPaket>();
		this.vermittlung = vermittlung;
	}

	/**
	 * Methode zur Verarbeitung eingehender ICMP-Pakete <br />
	 */
	protected void verarbeiteDatenEinheit(Object datenEinheit) {
		IcmpPaket icmpPaket = (IcmpPaket) datenEinheit;

		// TTL dekrementieren
		icmpPaket.setTtl(icmpPaket.getTtl() - 1);

		if (vermittlung.isLocalAddress(icmpPaket.getZielIp())) {
			// Paket wurde an diesen Rechner gesendet
			if (icmpPaket.getIcmpType() == ICMP.ECHO_REQUEST && icmpPaket.getIcmpCode() == ICMP.CODE_ECHO_REQUEST) {
				vermittlung.sendEchoReply(icmpPaket);
			} else {
				synchronized (rcvdPackets) {
					rcvdPackets.add(icmpPaket);
					rcvdPackets.notify();
				}
			}
		} else {
			// Paket wurde an anderen Rechner gesendet und
			// muss weitergeleitet werden
			vermittlung.weiterleitenPaket(icmpPaket);
		}
	}

	/**
	 * method to actually send a ping and compute the pong event
	 * 
	 * @return true if successful
	 */
	public int startSinglePing(String destIp, int seqNr) throws java.util.concurrent.TimeoutException {
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (ICMPThread), startSinglePing(" + destIp + "," + seqNr + ")");
		int resultTTL = -20; // return ttl field of received echo reply packet

		vermittlung.sendEchoRequest(destIp, seqNr);
		synchronized (rcvdPackets) {
			try {
				rcvdPackets.wait(Verbindung.holeRTT());
			} catch (InterruptedException e) {
			}
			if (rcvdPackets.size() == 0) {
				Main.debug.println("DEBUG (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
				        + " (ICMPThread), startSinglePing, NO reply in queue");
				throw new java.util.concurrent.TimeoutException("Destination Host Unreachable"); // not
				                                                                                 // absolutely
				                                                                                 // correct,
				                                                                                 // but
				                                                                                 // who
				                                                                                 // cares...
			} else {
				Main.debug.println("DEBUG (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
				        + " (ICMPThread), startSinglePing, reply in queue");
				IcmpPaket rcvdIcmpPaket = rcvdPackets.removeFirst();
				if (rcvdIcmpPaket != null
				        && !(destIp.equals(rcvdIcmpPaket.getQuellIp()) && seqNr == rcvdIcmpPaket.getSeqNr() && rcvdIcmpPaket
				                .getIcmpType() == 0)) {
					throw new java.util.concurrent.TimeoutException("Destination Host Unreachable");
				}
				resultTTL = rcvdIcmpPaket.getTtl();
			}
		}
		Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
		        + " (ICMPThread), startSinglePing, return " + resultTTL);
		return resultTTL;
	}

	/** method to send a traceroute probe (realized as ping) */
	public IcmpPaket sendProbe(String destIp, int ttl, int seqNr) {
		vermittlung.sendeICMP(ICMP.ECHO_REQUEST, ICMP.CODE_ECHO_REQUEST, ttl, seqNr, null, destIp);

		synchronized (rcvdPackets) {
			try {
				rcvdPackets.wait(Verbindung.holeRTT());
			} catch (InterruptedException e) {
			}

			if (rcvdPackets.size() > 0) {
				return rcvdPackets.removeFirst();
			}
		}
		return null;
	}
}
