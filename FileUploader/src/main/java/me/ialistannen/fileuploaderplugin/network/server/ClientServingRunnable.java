package me.ialistannen.fileuploaderplugin.network.server;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;
import me.ialistannen.fileuploaderplugin.network.packets.PacketManager;
import me.ialistannen.fileuploaderplugin.network.packets.UncheckedTimeoutException;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketHeartBeatResponse;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketPostFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketHeartbeatSend;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketTransmitFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Serves a client. May be started by an executor service.
 */
public class ClientServingRunnable implements Runnable {

	private Socket socket;

	private INetHandler handler;

	private OutputStream outputStream;
	private ObjectOutputStream objectOutputStream;

	private volatile AtomicBoolean cancelled = new AtomicBoolean(false);

	/**
	 * Creates a new serving thread
	 *
	 * @param socket  The client socket
	 * @param handler The {@link INetHandler} to use
	 */
	public ClientServingRunnable(Socket socket, INetHandler handler) {
		this.socket = socket;
		this.handler = handler;
	}

	@Override
	public void run() {
		try (InputStream inputStream = socket.getInputStream();
		     ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		     OutputStream outputStream = socket.getOutputStream();
		     ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

			this.outputStream = outputStream;
			this.objectOutputStream = objectOutputStream;
			PacketManager manager = PacketManager.INSTANCE;

			// MAIN LOOP
			while (!Thread.currentThread().isInterrupted() && !cancelled.get()) {
				try {
					Packet packet = manager.readPacket(objectInputStream);

					if (packet instanceof PacketHeartBeatResponse) {
						handler.handlePacketHeartbeatResponse((PacketHeartBeatResponse) packet, this);
					} else if (packet instanceof PacketPostFile) {
						handler.handlePostFile((PacketPostFile) packet, this);
					} else if (packet instanceof PacketTransmitFile) {
						handler.handleTransmitFile((PacketTransmitFile) packet, this);
					} else if (packet instanceof PacketRequestAvailablePaths) {
						handler.handlePacketRequestAvailablePaths((PacketRequestAvailablePaths) packet, this);
					} else if (packet instanceof PacketRequestFile) {
						handler.handleRequestFile((PacketRequestFile) packet, this);
					} else if (packet instanceof PacketTokenTransmit) {
						handler.handlePacketTokenTransmit((PacketTokenTransmit) packet, this);
					}

				} catch (SocketException e) {
					return;
				}
				catch (UncheckedTimeoutException e) {
					PacketHeartbeatSend heartbeat = new PacketHeartbeatSend();
					sendPacket(heartbeat);
					handler.heartbeatSend(heartbeat.getId(), this);
				}
			}

		} catch (IOException ignore) {
			// TODO: gets thrown (from a line without text) and I don't know why. Too frustrated to fix.
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends a packet to the client
	 *
	 * @param packet The packet to send
	 */
	public void sendPacket(Packet packet) {
		if(cancelled.get()) {
			return;
		}
		try {
			PacketManager.INSTANCE.writePacket(packet, outputStream, objectOutputStream);
		} catch (SocketException e) {
			stop();
		}
	}

	/**
	 * Stops this listener.
	 */
	public void stop() {
		cancelled.set(true);
		Thread.currentThread().interrupt();
	}
}
