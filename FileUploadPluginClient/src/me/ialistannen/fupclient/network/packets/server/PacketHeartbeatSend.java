package me.ialistannen.fupclient.network.packets.server;


import me.ialistannen.fupclient.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sends a Heartbeat
 */
public class PacketHeartbeatSend extends Packet {
	private int id;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketHeartbeatSend(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param id The ID of the heartbeat
	 */
	private PacketHeartbeatSend(int id) {
		this.id = id;
	}

	/**
	 * Creates a random id.
	 *
	 * @see #PacketHeartbeatSend(int)
	 */
	public PacketHeartbeatSend() {
		this(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE - 1));
	}

	/**
	 * Returns the heartbeat id
	 *
	 * @return The ID of this heartbeat
	 */
	public int getId() {
		return id;
	}

	private void read(ObjectInputStream reader) {
		try {
			id = reader.readInt();
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
			writer.writeInt(getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketHeartbeatSend{" +
				"id=" + id +
				'}';
	}
}
