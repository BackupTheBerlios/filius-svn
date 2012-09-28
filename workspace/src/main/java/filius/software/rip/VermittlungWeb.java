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
package filius.software.rip;

import java.io.IOException;

import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.www.WebServerPlugIn;

/**
 * 
 * @author pyropeter
 * @author stefanf
 * 
 */
public class VermittlungWeb extends WebServerPlugIn implements I18n {
	private RIPTable table;

	public VermittlungWeb(RIPTable table) {
		super();

		this.table = table;
	}

	public String holeHtmlSeite(String postDaten) {

		StringBuffer routingEntries = new StringBuffer();
		synchronized (table) {
			for (RIPRoute route : table.routes) {
				routingEntries.append(routeToHtml(route));
			}
		}

		String html = null;
		try {
			html = textDateiEinlesen("tmpl/routing_" + Information.getInformation().getLocale().toString() + ".html");
			html = html.replaceAll(":routing_entries:", routingEntries.toString());
			html = html.replaceAll(":hint:", messages.getString("sw_vermittlungweb_msg1"));
		} catch (IOException e) {
			System.err.println("routing table template could not be read.");
			e.printStackTrace();
		}

		return html;
	}

	private String routeToHtml(RIPRoute route) {
		String html = "";

		html += "<td>" + route.netAddr + "</td>";
		html += "<td>" + route.netMask + "</td>";
		html += "<td>" + route.hops + "</td>";

		if (route.expires == 0) {
			html += "<td>(dauerhaft)</td>";
		} else {
			long gueltig = (route.expires - RIPUtil.getTime()) / 1000;
			html += "<td>" + gueltig + "</td>";
		}

		html += "<td>" + route.nextHop + "</td>";
		html += "<td><a href=\"http://" + route.hopPublicIp + "/routes\">" + route.hopPublicIp + "</a></td>";

		if (route.hops == 0) {
			return "<tr style='background-color:#aaffaa'>" + html + "</tr>";
		} else if (route.hops == 16) {
			return "<tr style='background-color:#ffaaaa'>" + html + "</tr>";
		}
		return "<tr>" + html + "</tr>";
	}
}
