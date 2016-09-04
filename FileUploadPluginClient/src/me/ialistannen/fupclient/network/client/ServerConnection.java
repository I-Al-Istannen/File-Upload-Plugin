package me.ialistannen.fupclient.network.client;

import me.ialistannen.fupclient.model.Token;
import me.ialistannen.fupclient.network.packets.Packet;
import me.ialistannen.fupclient.network.packets.PacketManager;
import me.ialistannen.fupclient.network.packets.client.PacketRequestAvailablePaths;
import me.ialistannen.fupclient.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fupclient.network.packets.server.PacketAuthenticationStatus;
import me.ialistannen.fupclient.network.packets.server.PacketAuthenticationStatus.AuthenticationState;
import me.ialistannen.fupclient.network.packets.server.PacketHeartbeatSend;
import me.ialistannen.fupclient.network.packets.server.PacketListAvailablePaths;
import me.ialistannen.fupclient.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fupclient.network.packets.shared.PacketOperationSuccessful;
import me.ialistannen.fupclient.network.packets.shared.PacketReadException;
import me.ialistannen.fupclient.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fupclient.network.packets.shared.PacketWriteException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The connection to the server
 */
public class ServerConnection {

	private int port;
	private InetAddress address;

	private PacketManager packetManager = PacketManager.INSTANCE;
	private INetHandler netHandler;

	private OutputStream outputStream;
	private ObjectOutputStream objectOutputStream;

	private Token token;

	private volatile AtomicBoolean running = new AtomicBoolean(false);

	/**
	 * @param port       The port
	 * @param address    The address
	 * @param netHandler The {@link INetHandler} to use
	 */
	public ServerConnection(int port, InetAddress address, INetHandler netHandler) {
		this.port = port;
		this.address = address;
		this.netHandler = netHandler;
	}

	public void start(String tokenId) {
		try {
			Socket socket = new Socket(address, port);

			try (OutputStream outputStream = socket.getOutputStream();
			     ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

				this.outputStream = outputStream;
				this.objectOutputStream = objectOutputStream;

				// Transmit Token
				sendPacket(new PacketTokenTransmit(tokenId));

				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

				Packet packet = packetManager.readPacket(inputStream);

				if (!(packet instanceof PacketAuthenticationStatus)) {
					socket.close();
					System.out.println("DAMN");
					return;
				}
				AuthenticationState state = ((PacketAuthenticationStatus) packet).getAuthenticationState();

				// Authentication done
				if (state == AuthenticationState.INVALID_TOKEN) {
					System.out.println("Invalid token");
					socket.close();
					return;
				}

				// Request available paths
				sendPacket(new PacketRequestAvailablePaths());

				packet = packetManager.readPacket(inputStream);

				if (!(packet instanceof PacketListAvailablePaths)) {
					System.out.println("Dafuq: " + packet.getClass().getName());
					return;
				}

				// construct the token
				token = netHandler.handlePacketListAvailablePaths((PacketListAvailablePaths) packet, tokenId);

				if (token == null) {
					System.out.println("*Sigh*");
					return;
				}

				while (running.get() && !Thread.currentThread().isInterrupted()) {
					Packet readPacket = packetManager.readPacket(inputStream);

					if(readPacket instanceof PacketHeartbeatSend) {
						netHandler.handlePacketHeartbeatSend((PacketHeartbeatSend) readPacket, this);
					}
					else if(readPacket instanceof PacketPermissionDenied) {
						netHandler.handlePacketPermissionDenied((PacketPermissionDenied) readPacket, this);
					}
					else if(readPacket instanceof PacketOperationSuccessful) {
						netHandler.handlePacketOperationSuccessful((PacketOperationSuccessful) readPacket, this);
					}
					else if(readPacket instanceof PacketReadException) {
						netHandler.handlePacketReadException((PacketReadException) readPacket, this);
					}
					else if(readPacket instanceof PacketTransmitFile) {
						netHandler.handlePacketTransmitFile((PacketTransmitFile) readPacket, this);
					}
					else if(readPacket instanceof PacketWriteException) {
						netHandler.handlePacketWriteException((PacketWriteException) readPacket, this);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the used token
	 *
	 * @return The token, or null if not set
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * Sends a packet
	 *
	 * @param packet The packet to send
	 */
	public void sendPacket(Packet packet) {
		try {
			packetManager.writePacket(packet, outputStream, objectOutputStream);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}
