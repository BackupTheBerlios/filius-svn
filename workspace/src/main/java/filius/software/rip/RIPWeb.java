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

import filius.software.www.WebServerPlugIn;

/**
 * 
 * @author pyropeter
 *
 */
public class RIPWeb extends WebServerPlugIn {
	private RIPTable table;

	public RIPWeb(RIPTable table) {
		super();

		this.table = table;
	}

	public String holeHtmlSeite(String postDaten) {
		String html = "<html>";
		html += "<title>RIP Routen</title>";
		html += "<h1>RIP Routen</h1>";

		html += "<table border=1>";
		html += "<tr>";
		html += "<th colspan=2>Netz</th>";
		html += "<th>Hops</th>";
		html += "<th>Gültig</th>";
		html += "<th colspan=2>N&auml;chster Hop</th>";
		html += "</tr>";

		html += "<tr>";
		html += "<th>Adresse</th>";
		html += "<th>Maske</th>";
		html += "<th></th>";
		html += "<th>(sec)</th>";
		html += "<th>privat</th>";
		html += "<th>&ouml;ffentlich</th>";
		html += "</tr>";

		synchronized (table) {
			for (RIPRoute route : table.routes) {
				html += routeToHtml(route);
			}
		}

		html += "</table>";

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
		html += "<td><a href=\"http://" + route.hopPublicIp + "/routes.html\">" + route.hopPublicIp + "</a></td>";

		if (route.hops == 0) {
			return "<tr style='background-color:#aaffaa'>" + html + "</tr>";
		} else if (route.hops == 16) {
			return "<tr style='background-color:#ffaaaa'>" + html + "</tr>";
		}
		return "<tr>" + html + "</tr>";
	}
}
