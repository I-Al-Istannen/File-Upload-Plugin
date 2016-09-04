package me.ialistannen.fileuploaderplugin.network.packets.client;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Send to indicate that the client wants to terminate the connection
 */
public class PacketEndConnection extends Packet {

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketEndConnection(ObjectInputStream reader) {

	}

	/**
	 * An empty packet
	 */
	public PacketEndConnection() {
	}

	/**
	 * Writes this packet to an output stream. You do not need to write the ID.
	 *
	 * @param writer The writer to write to
	 */
	@Override
	public void write(ObjectOutputStream writer) {

	}
}
