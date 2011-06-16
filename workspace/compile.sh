#!/bin/bash

echo "Remove .class files..."
rm `find filius -name '*.class'`

echo "Compile ..."

package_files="
filius/exception/HauptException
filius/gui/anwendungssicht/DMTNFileChooser
filius/gui/anwendungssicht/GUIApplicationClientBausteinWindow
filius/gui/anwendungssicht/GUIApplicationDNSServerWindow
filius/gui/anwendungssicht/GUIApplicationEmailAnwendungWindow
filius/gui/anwendungssicht/GUIApplicationEmailServerWindow
filius/gui/anwendungssicht/GUIApplicationFileExplorerWindow
filius/gui/anwendungssicht/GUIApplicationFirewallWindow
filius/gui/anwendungssicht/GUIApplicationImageViewerWindow
filius/gui/anwendungssicht/GUIApplicationPeerToPeerAnwendungWindow
filius/gui/anwendungssicht/GUIApplicationServerBausteinWindow
filius/gui/anwendungssicht/GUIApplicationTerminalWindow
filius/gui/anwendungssicht/GUIApplicationTextEditorWindow
filius/gui/anwendungssicht/GUIApplicationWebBrowserWindow
filius/gui/anwendungssicht/GUIApplicationWebServerWindow
filius/gui/anwendungssicht/GUIApplicationWindow
filius/gui/anwendungssicht/GUIDesktopIcon
filius/gui/anwendungssicht/GUIInstallationsDialog
filius/gui/anwendungssicht/GUINetworkWindow
filius/gui/anwendungssicht/GUITreeRenderer
filius/gui/GUIEvents
filius/gui/GUIHilfe
filius/gui/InfoDialog
filius/gui/nachrichtensicht/ButtonTabComponent
filius/gui/nachrichtensicht/LauscherTableCellRenderer
filius/gui/nachrichtensicht/NachrichtenTabelle
filius/gui/netzwerksicht/GUIMainArea
filius/gui/netzwerksicht/JAendernButton
filius/gui/netzwerksicht/JDHCPKonfiguration
filius/gui/netzwerksicht/JFirewallDialog
filius/gui/netzwerksicht/JWeiterleitungsTabelle
filius/gui/quelltextsicht/PanelCompiler
filius/gui/quelltextsicht/PanelQuelltext
filius/gui/quelltextsicht/PanelVerwaltung
filius/hardware/SimplexVerbindung
filius/rahmenprogramm/KnotenPersistenceDelegate
filius/rahmenprogramm/LogEintrag
filius/software/clientserver/ServerBausteinMitarbeiter
filius/software/dateiaustausch/PeerToPeerClient
filius/software/dateiaustausch/PeerToPeerClientMitarbeiter
filius/software/dateiaustausch/PeerToPeerServer
filius/software/dateiaustausch/PeerToPeerServerMitarbeiter
filius/software/dhcp/DHCPServerMitarbeiter
filius/software/dhcp/IPEintrag
filius/software/dns/DNSServerMitarbeiter
filius/software/email/POP3Mitarbeiter
filius/software/email/POP3Server
filius/software/email/SMTPMitarbeiter
filius/software/email/SMTPServer
filius/software/firewall/FirewallThread
filius/software/lokal/FileImporter
filius/software/lokal/TextEditor
filius/software/lokal/Terminal
filius/software/transportschicht/TransportProtokollThread
filius/software/vermittlungsschicht/ARPThread
filius/software/vermittlungsschicht/IPThread
filius/software/www/WebServerMitarbeiter
filius/Main
"

for jfile in $package_files; do 
  javac -cp .:lib/htmlparser.jar:lib/tools.jar:lib/jna.jar $jfile.java
  if [[ $? != 0 ]]; then 
    echo
    echo "Error during compilation process!!!!"; 
    echo "JAR file will not be created.";
    exit 99;
  fi
done

echo "Create JAR file..."
jar cfm filius.jar Manifest.mf \
    filius/messages/*properties \
    filius/*.class \
    filius/sysinternal/*.class \
    filius/exception/*.class \
    filius/gui/*.class \
    filius/gui/anwendungssicht/*.class \
    filius/gui/nachrichtensicht/*.class \
    filius/gui/netzwerksicht/*.class \
    filius/gui/quelltextsicht/*.class \
    filius/hardware/*.class \
    filius/hardware/knoten/*.class \
    filius/rahmenprogramm/*.class \
    filius/rahmenprogramm/nachrichten/*.class \
    filius/software/*.class \
    filius/software/clientserver/*.class \
    filius/software/dateiaustausch/*.class \
    filius/software/dhcp/*.class \
    filius/software/dns/*.class \
    filius/software/email/*.class \
    filius/software/firewall/*.class \
    filius/software/lokal/*.class \
    filius/software/netzzugangsschicht/*.class \
    filius/software/system/*.class \
    filius/software/transportschicht/*.class \
    filius/software/vermittlungsschicht/*.class \
    filius/software/www/*.class \
    gfx/allgemein/*.png \
    gfx/desktop/*.png \
    gfx/desktop/*.gif \
    gfx/hardware/*.png;

echo "Create EXE file..."
../launch4j/launch4j ../workspace/filius_launch4j.xml
