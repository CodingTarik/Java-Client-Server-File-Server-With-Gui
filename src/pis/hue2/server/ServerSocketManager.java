package pis.hue2.server;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

import pis.hue2.common.FileManager;
import pis.hue2.common.Instruction;
import pis.hue2.common.InstructionDatagram;
import pis.hue2.common.SocketManager;

/**
 * Der ServerSocketManager verwaltet eine aktive SocketVerbindung zu einem
 * Client dafür nutzt dieser den SocketManager. Der ServerSocketManager erbt von
 * Thread, hier wurde die run Methode als Message-Loop verwendet.
 */
public class ServerSocketManager extends Thread {
	/**
	 * Das Verzeichnis, welches der Dateiserver als Datenablage benutzen soll
	 */
	private final String ServerFilesDirectory = "C:\\Users\\Tarik\\Desktop\\FilesFolder\\";
	/**
	 * Die Verbindung zum Socket durch ein SocketManager
	 */
	public final SocketManager socketManager;
	/**
	 * Verbindung erlaubt
	 */
	private final boolean allowConnection;
	/**
	 * Handshake erfolgreich durchgeführt
	 */
	private boolean handshakeDone;

	/**
	 * Address of client
	 */
	private final String clientAddress;

	/**
	 * Initalisiert den Server Socket Manager
	 *
	 * @param socket          die aktive Socket Verbindung zum Client
	 * @param allowConnection Sollte dieser Parameter auf false sein, so wird dem
	 *                        Socket ein Verbindungsaufbau untersagt
	 * @throws IOException
	 */
	public ServerSocketManager(final Socket socket, final boolean allowConnection) throws IOException {
		this.socketManager = new SocketManager(socket);
		this.allowConnection = allowConnection;
		this.clientAddress = socket.getInetAddress().getHostAddress();
	}

	/**
	 * Schließt die Verbindung
	 */
	public void CloseConnection() {
		try {
			this.socketManager.CloseConnection();
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Wird ausgeführt, wenn der Sitzungsaufbau noch nicht erfolgt ist, sendet ACK
	 * bei Erfolg ansonsten DND
	 *
	 * @param instruction das eingehende Datenpaket mit dem vermeitlichen Handshake
	 * @throws IOException
	 */
	private void DoHandshake(final InstructionDatagram instruction) throws IOException {
		System.out.println("Checking handshake of client: " + instruction.instruction.name());
		if (instruction.instruction == Instruction.CON) {
			if (this.allowConnection) {
				this.LogAndSendInstruction(new InstructionDatagram() {
					{
						this.instruction = Instruction.ACK;
					}
				});
				this.handshakeDone = true;
			}
		}
		if (!this.handshakeDone) {
			this.LogAndSendInstruction(new InstructionDatagram() {
				{
					this.instruction = Instruction.DND;
				}
			});
		}
	}

	/**
	 * Logt und sendet eien Instruktion über einen Socketmanager
	 *
	 * @param instruction die Instruktion als InstructionDatagram
	 * @throws IOException
	 */
	private void LogAndSendInstruction(final InstructionDatagram instruction) throws IOException {
		ServerGUI.activeGUI.LogProtocol(instruction.instruction.name(), "HOST", instruction.time.toString(),
				instruction.param1);
		this.socketManager.SendInstruction(instruction);
	}

	@Override
	/**
	 * Die Message-Loop des ServerSocketManagers zum Verwalten für eine spezifische
	 * Socket-Verbindung
	 */
	public void run() {
		System.out.println("messageloop started");
		while (true && !this.isInterrupted()) {
			try {
				System.out.println("Waiting for action of client");
				final InstructionDatagram instruction = this.socketManager.ReceiveDatagram();
				ServerGUI.activeGUI.LogProtocol(instruction.instruction.name(), instruction.SentBy,
						instruction.time.toString(), instruction.param1);
				// Überprüfe, ob der Handshake bereits stattgefunden hat
				if (!this.handshakeDone) {
					this.DoHandshake(instruction);
				}
				// Überprüfe, ob der Client ein Sitzungsabbau anfordert
				else if (instruction.instruction == Instruction.DSC) {
					this.LogAndSendInstruction(new InstructionDatagram() {
						{
							this.instruction = Instruction.DSC;
						}
					});
					this.CloseConnection();
					break;
				} else {
					// Ansonsten überprüfe auf die Instruktionen vom Client
					switch (instruction.instruction) {
					// Auflisten der Dateien
					case LST:
						final String fileList = FileManager.GetFileList(this.ServerFilesDirectory);
						this.LogAndSendInstruction(new InstructionDatagram() {
							{
								this.instruction = Instruction.DAT;
								this.param2 = fileList.getBytes();
								this.param1 = String.valueOf(this.param2.length);
							}
						});
						break;
					// Client möchte Datei hochladen
					case PUT:
						this.LogAndSendInstruction(new InstructionDatagram() {
							{
								this.instruction = Instruction.ACK;
							}
						});
						final InstructionDatagram instruction2 = this.socketManager.ReceiveDatagram();
						FileManager.PutFile(instruction2.param2, this.ServerFilesDirectory + instruction.param1);
						break;
					// Client möchte Datei herunterladen
					case GET:
						final byte[] bytes = FileManager
								.GetBytesFromFile(this.ServerFilesDirectory + instruction.param1);
						if (bytes != null) {
							this.LogAndSendInstruction(new InstructionDatagram() {
								{
									this.instruction = Instruction.DAT;
									this.param1 = String.valueOf(bytes.length);
									this.param2 = bytes;
								}
							});
							ServerGUI.activeGUI.LogFileTransfer(instruction.param1, this.clientAddress,
									LocalDateTime.now().toString());
						} else {
							this.LogAndSendInstruction(new InstructionDatagram() {
								{
									this.instruction = Instruction.DND;
								}
							});
						}
						break;
					// Client möchte Datei löschen
					case DEL:
						final boolean DeleteSuccess = FileManager
								.DeleteFile(this.ServerFilesDirectory + instruction.param1);
						if (DeleteSuccess) {
							this.LogAndSendInstruction(new InstructionDatagram() {
								{
									this.instruction = Instruction.ACK;
								}
							});
						} else {
							this.LogAndSendInstruction(new InstructionDatagram() {
								{
									this.instruction = Instruction.DND;
								}
							});
						}
						break;
					}

				}
			} catch (final IOException e) {
				// Bei einem Fehler, der nicht bereits oben durch If-Else verhindert werden
				// konnte wir der Fehler ausgegeben
				e.printStackTrace();
				// Ein letzter Versuch wird unternehmen, dem Client Bescheid zu geben, dass die
				// Operation gescheitert ist
				try {
					this.LogAndSendInstruction(new InstructionDatagram() {
						{
							this.instruction = Instruction.DND;
						}
					});
				} catch (final Exception ex) {
					// Sollte dieser letzte Versuch auch scheitern, wird der Fehler ausgegeben und
					// die Verbindung geschlossen
					// (Client hat wahrscheinlich einfach nur die Verbindung zum Internet verloren)
					ex.printStackTrace();
					this.CloseConnection();
					break;
				}

			}
		}
		this.CloseConnection();

	}

}
