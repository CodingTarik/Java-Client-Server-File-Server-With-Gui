package pis.hue2.common;

import java.time.LocalDateTime;

/**
 * Die Modellierung vom Protokoll-Spezifischen Datenpaket
 *
 */
public class InstructionDatagram {
	/**
	 * Die übergebene Instruktion
	 */
	public Instruction instruction;
	/**
	 * falls vorhanden erster Parameter als String
	 */
	public String param1;
	/**
	 * falls vorhanden zweiter Parameter als byte[]
	 */
	public byte[] param2;
	/**
	 * Host, von welchem die Nachricht gesendet wurde
	 */
	public String SentBy;
	/**
	 * Datum und Uhrzeit, wann das Datenpaket (bei seinem Host-System) erstellt
	 * wurde
	 */
	public LocalDateTime time = LocalDateTime.now();
}
