package me.ialistannen.fileuploaderplugin.network.packets.server;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Send if the user tries to perform an illegal action
 */
public class PacketPermissionDenied extends Packet {

	private String message;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketPermissionDenied(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param message The explaining message
	 */
	public PacketPermissionDenied(String message) {
		this.message = message;
	}

	/**
	 * Returns the error message
	 *
	 * @return The message
	 */
	public String getMessage() {
		return message;
	}

	private void read(ObjectInputStream reader) {
		try {
			message = reader.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes this packet to an output stream. You do not need to write the ID.
	 *
	 * @param writer The writer to write to
	 */
	@Override
	public void write(ObjectOutputStream writer) {
		try {
			writer.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketPermissionDenied{" +
				"message='" + message + '\'' +
				'}';
	}
}
