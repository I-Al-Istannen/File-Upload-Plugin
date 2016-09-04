package me.ialistannen.fupclient.network.packets.shared;

import me.ialistannen.fupclient.network.packets.Packet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Says if an operation was successful. Send in place of an error packet.
 */
public class PacketOperationSuccessful extends Packet {

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketOperationSuccessful(ObjectInputStream reader) {

	}

	/**
	 * Just an empty packet.
	 */
	public PacketOperationSuccessful() {
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
