package pis.hue2.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import pis.hue2.common.FileManager;
import pis.hue2.common.Instruction;
import pis.hue2.common.InstructionDatagram;
import pis.hue2.common.SocketManager;

/**
 * Verwaltet eine aktive Verbindung zu einem Server mit der Hilfe eines
 * SocketManagers
 *
 */
public class ClientSocketManager {
	/**
	 * Ein lockObjekt, um bei den meisten Methoden threadsicher zu bleiben
	 */
	private final Object lockObject = new Object();
	/**
	 * Der SocketManager zum senden und empfangen von Protokoll-Instruktionen
	 */
	public SocketManager socketManager;
	/**
	 * boolean, ob der handshake bereits durchgeführt wurde
	 */
	private boolean handshakeDone;
	/**
	 * Der aktive Socket zum Server
	 */
	private Socket socket;

	/**
	 * Initalisiert den SocketManager
	 *
	 * @param socket
	 */
	public ClientSocketManager(final Socket socket) {
		try {
			this.socket = socket;
			System.out.println("Creating socketmanager");
			this.socketManager = new SocketManager(socket);
			System.out.println("Initalizing handshake");
			this.DoHandshake();
			System.out.println("handshake finished");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Schließt die Verbindung zum Server
	 */
	public void CloseConnection() {
		new Thread(() -> {
			synchronized (this.lockObject) {
				if (!this.socket.isClosed()) {
					try {
						this.LogAndSendInstruction(new InstructionDatagram() {
							{
								this.instruction = Instruction.DSC;
							}
						});
						final InstructionDatagram receivedInstruction = this.socketManager.ReceiveDatagram();
						this.LogInstruction(receivedInstruction);
						if (receivedInstruction.instruction == Instruction.DSC) {
							this.socketManager.CloseConnection();
							this.handshakeDone = false;
							System.out.println("Verbindung erfolgreich abgebaut");
							ClientGUI.clientGUI.SetConnectionStateLabel(false);
						} else {
							this.socketManager.CloseConnection();
							this.handshakeDone = false;
							System.out.println("Verbindung gewaltsam untebrrochen");
							ClientGUI.clientGUI.SetConnectionStateLabel(false);
						}

					} catch (final IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * Löscht eine Datei
	 *
	 * @param filename der Name der Datei auf dem Server, die gelöscht werden soll
	 */
	public void DeleteFile(final String filename) {
		new Thread(() -> {
			synchronized (this.lockObject) {
				if (this.handshakeDone) {
					try {
						this.LogAndSendInstruction(new InstructionDatagram() {
							{
								this.instruction = Instruction.DEL;
								this.param1 = "\"" + filename + "\"";
							}
						});
						final InstructionDatagram receivedInstruction = this.socketManager.ReceiveDatagram();
						this.LogInstruction(receivedInstruction);

					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		this.RefreshServerFileList();
	}

	/**
	 * Der Sitzungsaufbau mit dem Server
	 *
	 * @throws IOException
	 */
	private void DoHandshake() throws IOException {
		synchronized (this.lockObject) {
			try {
				System.out.println("Sending CON-Request");
				this.LogAndSendInstruction(new InstructionDatagram() {
					{
						this.instruction = Instruction.CON;
					}
				});
				System.out.println("Waiting for answer");
				final InstructionDatagram receivedInstruction = this.socketManager.ReceiveDatagram();
				System.out.println("Answer received");
				this.LogInstruction(receivedInstruction);
				if (receivedInstruction.instruction == Instruction.ACK) {
					System.out.println("Handshake done");
					this.handshakeDone = true;
					ClientGUI.clientGUI.SetConnectionStateLabel(true);
				} else if (receivedInstruction.instruction == Instruction.DND) {
					ClientGUI.clientGUI.SetConnectionStateLabel(false);
					System.out.println("Handshake denied");
					this.CloseConnection();
				}
			} catch (final IOException e) {
				e.printStackTrace();
				ClientGUI.clientGUI.SetConnectionStateLabel(false);
				this.socketManager.CloseConnection();

			}
		}
		this.RefreshServerFileList();
	}

	/**
	 * Loggt und sendet eine Instruktion zum Server
	 *
	 * @param instruction
	 * @throws IOException
	 */
	private void LogAndSendInstruction(final InstructionDatagram instruction) throws IOException {
		this.LogInstruction(instruction);
		this.socketManager.SendInstruction(instruction);
	}

	private void LogInstruction(final InstructionDatagram instruction) {
		if (instruction.SentBy == null) {
			instruction.SentBy = "HOST";
		}
		ClientGUI.clientGUI.log(instruction.instruction.name() + " von " + instruction.SentBy + " um "
				+ instruction.time.toString() + " mit " + instruction.param1);
	}

	/**
	 * Aktualisert die Liste der aktuellen Datein vom Server
	 */
	public void RefreshServerFileList() {
		new Thread(() -> {
			ClientGUI.clientGUI.RefreshGUIFileList(this.RequestFileList());
		}).start();
	}

	/**
	 * Fragt eine Datei beim Dateiserver an und speichert diese im übergebeben Pfad
	 *
	 * @param filename       die Datei, welche herutnergeladen werden soll
	 * @param pathToSaveFile Pfad zum Speichern der Datei
	 */
	public void RequestFile(final String filename, final String pathToSaveFile) {

		new Thread(() -> {
			synchronized (this.lockObject) {
				if (this.handshakeDone) {
					System.out.println("Trying to download " + filename);
					try {
						this.LogAndSendInstruction(new InstructionDatagram() {
							{
								this.instruction = Instruction.GET;
								// Das Gänßefüßchen muss man nicht angeben, jedoch macht es Sinn bei Dateien mit
								// z.B. Leerzeichen
								this.param1 = "\"" + filename + "\"";
							}
						});
						final InstructionDatagram receivedInstruction = this.socketManager.ReceiveDatagram();
						this.LogInstruction(receivedInstruction);
						if (receivedInstruction.instruction == Instruction.DAT && receivedInstruction.param2 != null) {
							final File file = new File(pathToSaveFile);
							file.delete();
							final FileOutputStream fos = new FileOutputStream(file);
							fos.write(receivedInstruction.param2);
							fos.close();
						} else {
							System.out.println("Error getting file");
						}
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * Sendet eine LST-Anfrage an den Server
	 *
	 * @return Eine Liste mit allen Dateien des Servers oder null im Fehlerfall
	 */
	private String[] RequestFileList() {
		synchronized (this.lockObject) {
			if (this.handshakeDone) {
				try {
					System.out.println("Sending LST");
					this.LogAndSendInstruction(new InstructionDatagram() {
						{
							this.instruction = Instruction.LST;
						}
					});
					System.out.println("Waiting for answer");
					final InstructionDatagram receivedInstruction = this.socketManager.ReceiveDatagram();
					System.out.println("List received!");
					this.LogInstruction(receivedInstruction);
					if (receivedInstruction.param2 != null) {
						System.out.println("returning list of files");
						return new String(receivedInstruction.param2, StandardCharsets.UTF_8).split(";");
					} else {
						return null;
					}
				} catch (final IOException e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return null;
			}
		}
	}

	/**
	 * Lädt eien Datei auf den Server hoch
	 *
	 * @param pathToFile der Pfad zur Datei
	 */
	public void UploadFile(final String pathToFile) {
		new Thread(() -> {
			synchronized (this.lockObject) {
				if (this.handshakeDone) {
					try {
						final byte[] data = FileManager.GetBytesFromFile(pathToFile);
						if (data != null) {
							this.LogAndSendInstruction(new InstructionDatagram() {
								{
									this.instruction = Instruction.PUT;
									this.param1 = (new File(pathToFile)).getName();
								}
							});
							final InstructionDatagram receivedInstruction = this.socketManager.ReceiveDatagram();
							this.LogInstruction(receivedInstruction);
							if (receivedInstruction.instruction == Instruction.ACK) {
								this.LogAndSendInstruction(new InstructionDatagram() {
									{
										this.instruction = Instruction.DAT;
										this.param1 = String.valueOf(data.length);
										this.param2 = data;
									}
								});

							} else {
								throw new IllegalArgumentException("Expected ACK");
							}
						} else {
							System.out.println("Data of file was null");
						}
					} catch (final IOException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}).start();
	}
}
