package pis.hue2.client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Die GUI Des Client
 *
 */
public class ClientGUI extends JFrame {

	/**
	 * aktive GUI-Instanz des Clients
	 */
	public static ClientGUI clientGUI;

	/**
	 * ContenPane
	 */
	private final JPanel contentPane;
	/**
	 * Textfeld, wo der User, die Host-Addresse des Servers eintragen kann.
	 */
	private final JTextField hostTextField;
	/**
	 * ClientSocketManager zum verwalten der Verbindung zum Server
	 */
	private ClientSocketManager clientSocketManager;

	/**
	 * Liste mit Dateien vom Server
	 */
	public final JList fileList;
	/**
	 * Listenmodel zum speichern der Daten des Servers
	 */
	private final DefaultListModel listModel;
	/**
	 * Listenmodel zum protokollieren von Protokoll-Logs
	 */
	public DefaultListModel logModel;
	/**
	 * Verbindungsstatuslabel
	 */
	public final JLabel lblConnectionState;

	/**
	 * Erstellung der UI
	 */
	public ClientGUI() {
		this.setResizable(false);
		ClientGUI.clientGUI = this;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 847, 520);
		this.contentPane = new JPanel();
		this.contentPane.setBackground(Color.ORANGE);
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setContentPane(this.contentPane);
		this.contentPane.setLayout(null);

		final JLabel lblHost = new JLabel("HOST:");
		lblHost.setBounds(10, 10, 82, 22);
		this.contentPane.add(lblHost);
		lblHost.setForeground(Color.WHITE);
		lblHost.setFont(new Font("Arial", Font.BOLD, 25));

		this.hostTextField = new JTextField();
		this.hostTextField.setFont(new Font("Arial", Font.PLAIN, 17));
		this.hostTextField.setBounds(105, 10, 500, 25);
		this.contentPane.add(this.hostTextField);
		this.hostTextField.setColumns(10);

		final JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if (ClientGUI.this.clientSocketManager == null) {
					try {
						if (ClientGUI.this.clientSocketManager != null) {
							ClientGUI.this.clientSocketManager.CloseConnection();
						}
						final Socket socket = new Socket(ClientGUI.this.hostTextField.getText(), 7777);
						System.out.println("Socket created!");
						ClientGUI.this.clientSocketManager = new ClientSocketManager(socket);
					} catch (final Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null,
								"Error " + ex.getMessage() + " maybe host is offline or unknown ");
					}
				}
			}
		});
		btnConnect.setFont(new Font("Arial", Font.PLAIN, 13));
		btnConnect.setBounds(620, 10, 89, 23);
		this.contentPane.add(btnConnect);

		final JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if (ClientGUI.this.clientSocketManager != null) {
					ClientGUI.this.clientSocketManager.CloseConnection();
					ClientGUI.this.clientSocketManager = null;
					ClientGUI.clientGUI.RefreshGUIFileList(new String[] {});
				}
			}
		});
		btnDisconnect.setFont(new Font("Arial", Font.PLAIN, 13));
		btnDisconnect.setBounds(720, 10, 100, 23);
		this.contentPane.add(btnDisconnect);

		final JPanel panel = new JPanel();
		panel.setBackground(Color.DARK_GRAY);
		panel.setBounds(0, 0, 831, 43);
		this.contentPane.add(panel);

		this.fileList = new JList();
		this.listModel = new DefaultListModel();
		this.fileList.setModel(this.listModel);
		this.fileList.setBackground(Color.WHITE);
		this.fileList.setBounds(192, 79, 234, 341);
		final JScrollPane scrollPane = new JScrollPane(this.fileList);
		scrollPane.setLocation(10, 80);
		scrollPane.setSize(300, 400);
		this.contentPane.add(scrollPane);

		final JLabel lblNewLabel = new JLabel("Dateien auf dem Server:");
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 17));
		lblNewLabel.setBounds(10, 54, 227, 25);
		this.contentPane.add(lblNewLabel);

		final JButton btnNewButton = new JButton("Datei herunterladen");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (ClientGUI.this.fileList.getSelectedValue() != null) {
					final JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Wo möchtest du die Datei speichern?");

					final int userSelection = fileChooser.showSaveDialog(ClientGUI.clientGUI);

					if (userSelection == JFileChooser.APPROVE_OPTION) {
						final File fileToSave = fileChooser.getSelectedFile();
						ClientGUI.this.clientSocketManager.RequestFile(
								ClientGUI.this.fileList.getSelectedValue().toString(), fileToSave.getAbsolutePath());
					}
				}
			}
		});
		btnNewButton.setFont(new Font("Arial", Font.PLAIN, 17));
		btnNewButton.setBounds(320, 78, 500, 52);
		this.contentPane.add(btnNewButton);

		final JButton btnDateiLschen = new JButton("Datei l\u00F6schen");
		btnDateiLschen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (ClientGUI.this.fileList.getSelectedValue() != null) {
					ClientGUI.this.clientSocketManager
							.DeleteFile(ClientGUI.this.fileList.getSelectedValue().toString());
				}
			}

		});
		btnDateiLschen.setFont(new Font("Arial", Font.PLAIN, 17));
		btnDateiLschen.setBounds(320, 141, 500, 52);
		this.contentPane.add(btnDateiLschen);

		final JButton btnDateiHochladen = new JButton("Datei hochladen");
		btnDateiHochladen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final FileDialog fd = new FileDialog(ClientGUI.clientGUI, "Datei auswählen", FileDialog.LOAD);
				fd.setDirectory("C:\\");
				fd.setFile("*.*");
				fd.setVisible(true);
				final String filename = fd.getFile();
				if (filename == null) {
					System.out.println("Dateiauswahl abgebrochen");
				} else {
					ClientGUI.this.clientSocketManager.UploadFile(fd.getFiles()[0].getAbsolutePath());
				}
			}
		});
		btnDateiHochladen.setFont(new Font("Arial", Font.PLAIN, 17));
		btnDateiHochladen.setBounds(320, 204, 500, 52);
		this.contentPane.add(btnDateiHochladen);

		final JButton btnDateilisteAktualisieren = new JButton("Dateiliste aktualisieren");
		btnDateilisteAktualisieren.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ClientGUI.this.clientSocketManager.RefreshServerFileList();
			}

		});
		btnDateilisteAktualisieren.setFont(new Font("Arial", Font.PLAIN, 17));
		btnDateilisteAktualisieren.setBounds(320, 267, 500, 52);
		this.contentPane.add(btnDateilisteAktualisieren);

		this.lblConnectionState = new JLabel("Verbindungsstatus zum Server: Nicht verbunden");
		this.lblConnectionState.setFont(new Font("Arial", Font.PLAIN, 17));
		this.lblConnectionState.setBounds(320, 330, 375, 25);
		this.contentPane.add(this.lblConnectionState);
		this.logModel = new DefaultListModel();
		final JList logList = new JList();
		logList.setModel(this.logModel);
		logList.setBounds(0, 0, 0, 0);
		final JScrollPane scrollPane_1 = new JScrollPane(logList);
		scrollPane_1.setLocation(320, 359);
		scrollPane_1.setSize(510, 121);
		this.contentPane.add(scrollPane_1);
	}

	/**
	 * Logt eine konkrete Protokollinstruktion (auch andere Nachrichten sind hier
	 * jedoch möglich)
	 *
	 * @param logInstruction die zu loggende Instruktion
	 */
	public void log(final String logInstruction) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ClientGUI.this.logModel.addElement(logInstruction);
			}
		});

	}

	/**
	 * Aktualisiert das Dateiverzeichnis des Servers mit den übergebenen Dateien
	 *
	 * @param files abrufbare Dateien des Servers
	 */
	public void RefreshGUIFileList(final String[] files) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ClientGUI.clientGUI.listModel.clear();
				for (final String file : files) {
					if (file != null && !file.equals("")) {
						ClientGUI.clientGUI.listModel.addElement(file);
					}
				}

			}

		});
	}

	/**
	 * Aktualisiert das Statuslabel mit dem Verbindungsstatus zum Server
	 *
	 * @param connected true = Verbunden, false = Nicht Verbunden
	 */
	public void SetConnectionStateLabel(final boolean connected) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (connected) {
					ClientGUI.this.lblConnectionState.setText("Verbindungsstatus zum Server: Verbunden");
				} else {
					ClientGUI.this.lblConnectionState.setText("Verbindungsstatus zum Server: Nicht Verbunden");
				}

			}

		});
	}
}
