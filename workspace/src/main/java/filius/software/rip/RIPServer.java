package filius.software.rip;

import filius.software.clientserver.UDPServerAnwendung;
import filius.software.transportschicht.Socket;

public class RIPServer extends UDPServerAnwendung {
	public RIPServer() {
		super();

		port = 520;
	}

	protected void neuerMitarbeiter(Socket socket) {
		RIPServerMitarbeiter ripMitarbeiter;

		ripMitarbeiter = new RIPServerMitarbeiter(this, socket);
		ripMitarbeiter.starten();
		mitarbeiter.add(ripMitarbeiter);
	}

}
