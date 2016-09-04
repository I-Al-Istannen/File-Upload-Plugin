package me.ialistannen.fileuploaderplugin.network.packets;

import me.ialistannen.bukkitutil.commandsystem.util.ReflectionUtil;
import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketHeartBeatResponse;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketPostFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketAuthenticationStatus;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketHeartbeatSend;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketListAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketOperationSuccessful;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketReadException;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketWriteException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A manager for all packets
 */
public enum PacketManager {
	INSTANCE;

	private final Map<Integer, Class<? extends Packet>> packetMap = new HashMap<>();

	private final ByteArrayOutputStream byteArrayOutputStream;

	PacketManager() {
		byteArrayOutputStream = new ByteArrayOutputStream();

		registerDefault();
	}

	private void registerDefault() {
		registerPacket(0, PacketTokenTransmit.class);
		registerPacket(1, PacketListAvailablePaths.class);
		registerPacket(2, PacketRequestFile.class);
		registerPacket(3, PacketTransmitFile.class);
		registerPacket(4, PacketPostFile.class);
		registerPacket(5, PacketHeartbeatSend.class);
		registerPacket(6, PacketHeartBeatResponse.class);
		registerPacket(7, PacketRequestAvailablePaths.class);
		registerPacket(8, PacketAuthenticationStatus.class);
		registerPacket(9, PacketPermissionDenied.class);
		registerPacket(10, PacketReadException.class);
		registerPacket(11, PacketWriteException.class);
		registerPacket(12, PacketOperationSuccessful.class);
	}

	/**
	 * Registers a packet
	 *
	 * @param id    The Id of the packet
	 * @param clazz The class of the packet
	 *
	 * @throws IllegalArgumentException If there is already a packet with that ID registered
	 * @throws NullPointerException     If the class doesn't fulfill the {@link Packet} class contract regarding the
	 *                                  constructor
	 */
	private void registerPacket(int id, Class<? extends Packet> clazz) {
		Objects.requireNonNull(clazz);

		if (packetMap.containsKey(id)) {
			throw new IllegalArgumentException("There is already a packet with that id.");
		}

		Objects.requireNonNull(ReflectionUtil.getConstructor(clazz, ObjectInputStream.class),
				"The class " + clazz.getSimpleName() + " doesn't have a public constructor with an InputStreamReader" +
						" as only parameter. Read the JavaDoc of the Packet class!");

		packetMap.put(id, clazz);
	}

	/**
	 * Reads a packet from the InputStream
	 *
	 * @param reader The reader to read from
	 *
	 * @return The corresponding packet
	 *
	 * @throws IllegalStateException     If the end of the stream is reached while reading the packet id
	 * @throws IllegalArgumentException  If the id is unknown
	 * @throws RuntimeException          As a substitute for the checked IOException. The IOException is set as cause.
	 * @throws UncheckedTimeoutException If a {@link SocketTimeoutException} occurs
	 * @throws SocketException           Delegated
	 */
	public Packet readPacket(ObjectInputStream reader) throws SocketException {
		Objects.requireNonNull(reader);

		return readPacket(reader, (int) FileUploaderPlugin.getInstance().getConfigWrapper()
				.getSocketTimeout().toMillis());
	}

	/**
	 * Reads a packet from the InputStream
	 *
	 * @param reader    The reader to read from
	 * @param timeoutMs The timeout in milliseconds.
	 *
	 * @return The corresponding packet
	 *
	 * @throws IllegalStateException     If the end of the stream is reached while reading the packet id
	 * @throws IllegalArgumentException  If the id is unknown
	 * @throws RuntimeException          As a substitute for the checked IOException. The IOException is set as cause.
	 * @throws UncheckedTimeoutException If a {@link SocketTimeoutException} occurs
	 * @throws SocketException           Delegated
	 */
	@SuppressWarnings("Duplicates")
	private Packet readPacket(ObjectInputStream reader, int timeoutMs) throws SocketException {
		Objects.requireNonNull(reader);

		try {
			// Totally weird workaround for a bug (if it is one) where the readInt method would wait forever
			int counter = 0;
			while (reader.available() <= 0) {
				if (counter >= timeoutMs / 1000) {
					throw new UncheckedTimeoutException("Socket timed out.");
				}
				Thread.sleep(1000);
				counter++;
			}
			int read = reader.readInt();

			if (read == -1) {
				throw new IllegalStateException("Reading packet ID: Input stream is empty...");
			}

			Class<? extends Packet> packetClass = packetMap.get(read);

			if (packetClass == null) {
				throw new IllegalArgumentException("Packet with id is '" + read + "' unknown.");
			}

			Constructor<?> constructor = ReflectionUtil.getConstructor(packetClass, ObjectInputStream.class);

			Objects.requireNonNull(constructor, "Subclass doesn't have the superclass constructor. Read the " +
					"Javadoc...");

			@SuppressWarnings("unchecked") // it is safe, as I instantiate a packet class.
					Packet packet = (Packet) ReflectionUtil.instantiate(constructor, reader);

			return packet;
		} catch (SocketException e) {
			throw e;
		} catch (SocketTimeoutException | InterruptedException e) {
			throw new UncheckedTimeoutException(e);
		} catch (IOException e) {
			throw new RuntimeException("Error while reading from the input stream", e);
		}
	}

	/**
	 * Writes the packet and flushes the stream.
	 *
	 * @param packet The packet to write
	 * @param writer The writer to write to
	 *
	 * @throws IllegalArgumentException If the packet is not registered
	 * @throws SocketException          Delegated. If a write error occurs
	 */
	@SuppressWarnings("Duplicates")
	public void writePacket(Packet packet, OutputStream writer, ObjectOutputStream outputStream) throws
			SocketException {
		try {
			boolean idWritten = false;
			for (Map.Entry<Integer, Class<? extends Packet>> entry : packetMap.entrySet()) {
				if (entry.getValue() == packet.getClass()) {
					outputStream.writeInt(entry.getKey());
					idWritten = true;
					break;
				}
			}

			if (!idWritten) {
				throw new IllegalArgumentException("Packet '" + packet + "' not registered");
			}

			packet.write(outputStream);

			// write everything
			outputStream.flush();

			// write it to the output stream
			writer.write(byteArrayOutputStream.toByteArray());
			writer.flush();

			// reset both and allow for reuse. Otherwise a ObjectXXStream may exist for too long and the header
			// get out of sync. (One expects one, the other has already sent one)
			byteArrayOutputStream.reset();
			outputStream.reset();

		} catch (SocketException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
