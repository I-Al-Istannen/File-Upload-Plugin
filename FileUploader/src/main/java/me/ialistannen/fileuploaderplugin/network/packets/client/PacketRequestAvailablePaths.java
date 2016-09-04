package me.ialistannen.fileuploaderplugin.network.packets.client;


import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Send by the client to indicate it wants the current path list for the given token
 */
public class PacketRequestAvailablePaths extends Packet {

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketRequestAvailablePaths(ObjectInputStream reader) {

	}

	/**
	 * An empty packet.
	 */
	public PacketRequestAvailablePaths() {
	}

	/**
	 * Writes this packet to an output stream. You do not need to write the ID.
	 *
	 * @param writer The writer to write to
	 */
	@Override
	public void write(ObjectOutputStream writer) {

	}

	@Override
	public String toString() {
		return "PacketRequestAvailablePaths{}";
	}
}
