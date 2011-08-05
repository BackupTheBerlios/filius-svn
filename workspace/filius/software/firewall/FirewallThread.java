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
package filius.software.firewall;


import java.util.LinkedList;

import filius.Main;
import filius.hardware.NetzwerkInterface;
import filius.hardware.knoten.InternetKnoten;
import filius.software.ProtokollThread;
import filius.software.netzzugangsschicht.Ethernet;
import filius.software.netzzugangsschicht.EthernetThread;
import filius.software.system.Betriebssystem;
import filius.software.system.VermittlungsrechnerBetriebssystem;
import filius.software.transportschicht.Segment;
import filius.software.transportschicht.TcpSegment;
import filius.software.vermittlungsschicht.IpPaket;

/*
 * @author Weyer
 * Die Klasse schiebt sich zwischen die Ethernetschicht und die Vermittlungsschicht. Sie
 * tauscht den Ip-Pakete-Puffer aus, sodass sie nach Regeln selektieren kann, welche Pakete
 * als gueltig weitergeleitet werden
 */
public class FirewallThread extends ProtokollThread {

	private LinkedList<IpPaket> ipPufferAusgang;
	private Firewall firewall;

	public FirewallThread(Firewall firewall) {
		super(new LinkedList<IpPaket>());
		Main.debug.println("INVOKED-2 ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FirewallThread), constr: FirewallThread("+firewall+")");
		this.firewall = firewall;
	}

	/*
	 * tauscht den IP-Puffer zwischen Ethernetschicht und Vermittlungsschicht aus, und startet
	 * den Thread zur Überwachung den Datenaustausches
	 */
	public void starten(){
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FirewallThread), starten()");
		LinkedList<IpPaket> ipPufferEingang;

		super.starten();

		ipPufferAusgang = firewall.getSystemSoftware().holeEthernet().holeIPPuffer();
		ipPufferEingang = (LinkedList<IpPaket>) holeEingangsPuffer();
		firewall.getSystemSoftware().holeEthernet().setzeIPPuffer(ipPufferEingang);

	}

	//getter und setter:

	@Override
	protected void verarbeiteDatenEinheit(Object datenEinheit) {
		Main.debug.println("INVOKED ("+this.hashCode()+", T"+this.getId()+") "+getClass()+" (FirewallThread), verarbeiteDatenEinheit("+datenEinheit.toString()+")");
		IpPaket ipPaket = (IpPaket) datenEinheit;

//		Hier erfolgt nun die Abfrage, ob die Pakete laut Firewall in Ordnung sind:
		 //Bei false werden die Pakete weitergeleitet
		//Am Ende in den Ausgangspuffer schreiben, und weiterreichen an EthernetThread
		//oder nicht weiterleiten

		if(!firewall.istPaketZulaessig(ipPaket)){
			synchronized(ipPufferAusgang){
				//Main.debug.println("FirewallThread: Paket wurde von FirewallThread weitergeleitet");

				ipPufferAusgang.add(ipPaket);
				ipPufferAusgang.notify();
			}
		}
	}

}
