package me.ialistannen.fileuploaderplugin.network.packets.client;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * A response to the heartBeat
 */
public class PacketHeartBeatResponse extends Packet {

	private int id;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketHeartBeatResponse(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param id The ID of the received heartbeat packet
	 */
	public PacketHeartBeatResponse(int id) {
		this.id = id;
	}

	/**
	 * Returns the id of the heartbeat
	 *
	 * @return The ID of the heartbeat
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
			writer.writeInt(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketHeartBeatResponse{" +
				"id=" + id +
				'}';
	}
}