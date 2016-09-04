package me.ialistannen.fupclient.network.packets;

import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A packet.
 * <p>
 * <i><b>You must provide a public constructor with an {@link InputStreamReader} as only parameter.</b></i>
 */
public abstract class Packet {

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	@SuppressWarnings({"UnusedParameters", "unused"}) // needed to fulfil the contract
	public Packet(ObjectInputStream reader) {
	}

	/**
	 * Allows you to create "normal" instances.
	 */
	protected Packet() {
	}

	/**
	 * Writes this packet to an output stream. You do not need to write the ID.
	 *
	 * @param writer The writer to write to
	 */
	public abstract void write(ObjectOutputStream writer);
}
