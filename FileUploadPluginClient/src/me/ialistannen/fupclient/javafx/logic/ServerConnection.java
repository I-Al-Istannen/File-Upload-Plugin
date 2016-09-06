package me.ialistannen.fupclient.javafx.logic;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import me.ialistannen.fupclient.javafx.util.Util;
import me.ialistannen.fupclient.model.Token;
import me.ialistannen.fupclient.network.packets.Packet;
import me.ialistannen.fupclient.network.packets.PacketManager;
import me.ialistannen.fupclient.network.packets.client.PacketRequestAvailablePaths;
import me.ialistannen.fupclient.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fupclient.network.packets.server.PacketAuthenticationStatus;
import me.ialistannen.fupclient.network.packets.server.PacketAuthenticationStatus.AuthenticationState;
import me.ialistannen.fupclient.network.packets.server.PacketListAvailablePaths;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * The connection to the server
 */
public class ServerConnection {
	private Socket socket;
	private String host;
	private int port;

	private String tokenID;
	private Token token;

	private State state = State.NOT_CONNECTED;

	private OutputStream outputStream;
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream inputStream;

	private Semaphore packetCount = new Semaphore(0);
	private Queue<Packet> packetQueue = new ArrayDeque<>();

	private PacketListener packetListener;

	/**
	 * @param host    The host
	 * @param port    The port
	 * @param tokenID The tokenId
	 */
	public ServerConnection(String host, int port, String tokenID) {
		this.host = host;
		this.port = port;
		this.tokenID = tokenID;
	}

	/**
	 * @throws IllegalStateException If {@link #getState()} is not {@link State#NOT_CONNECTED}
	 */
	public void authenticate() {
		if (state != State.NOT_CONNECTED) {
			throw new IllegalStateException("The state is not " + State.NOT_CONNECTED + "." +
					" Reset this connection first.");
		}
		try {
			socket = new Socket(host, port);
			socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(20));

			outputStream = socket.getOutputStream();
			objectOutputStream = new ObjectOutputStream(outputStream);

			writePacket(new PacketTokenTransmit(tokenID));

			inputStream = new ObjectInputStream(socket.getInputStream());

			packetListener = new PacketListener(this);
			{
				Thread thread = new Thread(packetListener);
				thread.setDaemon(true);
				thread.start();
			}

			boolean gotIt = tryAcquirePacket("PacketAuthenticationStatus");

			if (!gotIt) {
				return;
			}
			Packet packet = packetQueue.poll();

			if (!(packet instanceof PacketAuthenticationStatus)) {
				Util.showWrongPacketReceivedAlert(PacketAuthenticationStatus.class, packet);
				state = State.ERROR_OCCURRED;
				return;
			}

			AuthenticationState authenticationState = ((PacketAuthenticationStatus) packet).getAuthenticationState();

			if (authenticationState == AuthenticationState.INVALID_TOKEN) {
				state = State.INVALID_TOKEN;
				// at this point we have been kicked and the server closed the connection. Do the same.
				close();
				return;
			}

			state = State.AUTHENTICATED;
		} catch (UnknownHostException e) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Unknown host");
				alert.setContentText("The host is unknown.");
				TextArea textArea = new TextArea(Util.getStackTrace(e));
				textArea.setFont(Font.font("monospaced"));
				alert.getDialogPane().setExpandableContent(textArea);
				alert.show();
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean tryAcquirePacket(String name) {
		try {
			boolean gotIt = packetCount.tryAcquire(30, TimeUnit.SECONDS);
			if (!gotIt) {
				close();
				Util.showAlert(AlertType.ERROR, "Timeout", "Timed out while waiting for packet " + name, "");
			}
			return gotIt;
		} catch (InterruptedException e) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Interrupted");
				alert.setHeaderText("Interrupted while waiting for packet request available paths");
				TextArea textArea = new TextArea(Util.getStackTrace(e));
				textArea.setFont(Font.font("monospaced"));
				alert.getDialogPane().setExpandableContent(textArea);
				alert.show();
			});
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Creates the token (the paths)
	 * <p>
	 * <i><b>Do not call from FX-Platform thread. Blocks.</b></i>
	 */
	public void createToken() {
		if (state != State.AUTHENTICATED) {
			throw new IllegalStateException("Not authenticated");
		}

		try {
			writePacket(new PacketRequestAvailablePaths());

			boolean gotIt = tryAcquirePacket("PacketListAvailablePaths");
			if (!gotIt) {
				return;
			}

			Packet packet = packetQueue.poll();

			if (!(packet instanceof PacketListAvailablePaths)) {
				Util.showWrongPacketReceivedAlert(PacketListAvailablePaths.class, packet);
				return;
			}

			token = new Token(tokenID, ((PacketListAvailablePaths) packet).getPaths());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @return The packet to write
	 *
	 * @throws SocketException If an error occurs
	 */
	Packet readPacket() throws SocketException {
		return PacketManager.INSTANCE.readPacket(inputStream, (int) TimeUnit.MINUTES.toMillis(2));
	}

	/**
	 * @param packet The packet to write
	 *
	 * @throws SocketException If an error occurs
	 */
	public void writePacket(Packet packet) throws SocketException {
		PacketManager.INSTANCE.writePacket(packet, outputStream, objectOutputStream);
	}

	public void handlePacket(Packet packet) {
		packetQueue.add(packet);
		packetCount.release();
	}

	/**
	 * The token containing the paths the user has access to.
	 *
	 * @return The current token. May be null
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * Retrieves the first packet from the queue
	 *
	 * @return The first packet from the queue, or null if none
	 */
	public synchronized Packet pollQueue() {
		return packetQueue.poll();
	}

	/**
	 * Performs the {@link Semaphore#acquire()} method on the packet counter.
	 */
	public synchronized void downCounter() throws InterruptedException {
		packetCount.acquire();
	}

	/**
	 * Resets this object to allow another connection
	 */
	public void reset() {
		if (state.areResourcesClosed()) {
			close();
		}
		state = State.NOT_CONNECTED;
	}

	public void close() {
		if (packetListener != null) {
			packetListener.stop();
		}
		tryClose(socket);
		tryClose(inputStream);
		tryClose(outputStream);
		tryClose(objectOutputStream);
	}

	private void tryClose(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the current state
	 *
	 * @return The current state
	 */
	public State getState() {
		return state;
	}

	public enum State {
		NOT_CONNECTED(true),
		AUTHENTICATED(false),
		ERROR_OCCURRED(true),
		INVALID_TOKEN(true);

		private boolean areResourcesClosed;

		/**
		 * @param areResourcesClosed Whether the resources should be closed in this state
		 */
		State(boolean areResourcesClosed) {
			this.areResourcesClosed = areResourcesClosed;
		}

		/**
		 * Checks if the resources should be closed in this state
		 *
		 * @return True if the resources should be closed in this state
		 */
		public boolean areResourcesClosed() {
			return areResourcesClosed;
		}
	}
}
