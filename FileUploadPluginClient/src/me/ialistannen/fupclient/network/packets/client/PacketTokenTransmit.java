package me.ialistannen.fupclient.network.packets.client;

import me.ialistannen.fupclient.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Transmits the token
 */
public class PacketTokenTransmit extends Packet {

	private String tokenID;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketTokenTransmit(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param tokenID The ID of the token
	 */
	public PacketTokenTransmit(String tokenID) {
		this.tokenID = tokenID;
	}

	/**
	 * Returns the tokenId of this packet
	 *
	 * @return The tokenID
	 */
	public String getTokenID() {
		return tokenID;
	}

	private void read(ObjectInputStream reader) {
		try {
			tokenID = reader.readUTF();
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
			writer.writeUTF(tokenID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketTokenTransmit{" +
				"tokenID='" + tokenID + '\'' +
				'}';
	}
}
