package me.ialistannen.fupclient.network.packets.client;

import me.ialistannen.fupclient.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Send to inform the server that the client is now going to transmit a file.
 */
public class PacketPostFile extends Packet {

	private Path path;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketPostFile(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param path The path of the file you are uploading
	 */
	public PacketPostFile(Path path) {
		this.path = path;
	}


	/**
	 * Returns the path.
	 *
	 * @return The path
	 */
	@SuppressWarnings("unused")
	public Path getPath() {
		return path;
	}

	private void read(ObjectInputStream reader) {
		try {
			String pathString = reader.readUTF();
			path = Paths.get(pathString);
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
			writer.writeUTF(path.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketPostFile{" +
				"path=" + path +
				'}';
	}
}
