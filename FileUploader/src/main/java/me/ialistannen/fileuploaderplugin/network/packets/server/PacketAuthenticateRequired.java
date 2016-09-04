package me.ialistannen.fileuploaderplugin.network.packets.server;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Send to notify the client they need to send a token.
 */
public class PacketAuthenticateRequired extends Packet {

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketAuthenticateRequired(ObjectInputStream reader) {
		super(reader);
	}

	/**
	 * Allows you to create "normal" instances.
	 */
	public PacketAuthenticateRequired() {
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
		return "PacketAuthenticateRequired{}";
	}
}
