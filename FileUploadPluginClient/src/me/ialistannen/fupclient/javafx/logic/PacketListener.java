package me.ialistannen.fupclient.javafx.logic;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import me.ialistannen.fupclient.javafx.util.Util;
import me.ialistannen.fupclient.network.packets.Packet;
import me.ialistannen.fupclient.network.packets.UncheckedTimeoutException;
import me.ialistannen.fupclient.network.packets.client.PacketHeartBeatResponse;
import me.ialistannen.fupclient.network.packets.server.PacketHeartbeatSend;

import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listens for and reacts to heartbeats
 */
class PacketListener implements Runnable {

	private ServerConnection connection;

	/**
	 * @param connection The {@link ServerConnection}
	 */
	PacketListener(ServerConnection connection) {
		this.connection = connection;
	}

	private volatile AtomicBoolean running = new AtomicBoolean(true);

	@Override
	public void run() {
		try {
			while (running.get()) {
				try {
					Packet packet = connection.readPacket();

					if (!running.get()) {
						return;
					}

					if (packet instanceof PacketHeartbeatSend) {
						System.out.println("Got: " + packet);
						System.out.println("Sending heartbeat: " + ((PacketHeartbeatSend) packet).getId());
						connection.writePacket(new PacketHeartBeatResponse(((PacketHeartbeatSend) packet).getId()));
					} else {
						connection.handlePacket(packet);
					}
				} catch (UncheckedTimeoutException e) {
					System.out.println("timed out");
				}
			}
		} catch (SocketException e) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				connection.close();
				alert.setTitle("Socket error");
				alert.setHeaderText("I got a socket error. Restart me.");

				Text text = new Text("Maybe the connection to the server was dropped?" +
						"\n\nWhat did you do??");
				text.setFont(Font.font("monospaced"));
				alert.getDialogPane().setContent(text);

				TextArea textArea = new TextArea(Util.getStackTrace(e));
				textArea.setFont(Font.font("monospaced"));

				alert.getDialogPane().setExpandableContent(textArea);

				alert.show();
			});
			e.printStackTrace();
		}
	}

	/**
	 * Stops this runnable as soon as it's call to connection.readPackets unblocks.
	 */
	void stop() {
		running.set(false);
	}
}
