package me.ialistannen.fileuploaderplugin.network.packets.client;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sends the request to download a file
 */
public class PacketRequestFile extends Packet {

	private Path path;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketRequestFile(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param path The path. Relative to the plugins folder.
	 */
	public PacketRequestFile(Path path) {
		this.path = path;
	}

	/**
	 * The path it wants to read
	 *
	 * @return The Path it wants to read
	 */
	public Path getPath() {
		return path;
	}

	private void read(ObjectInputStream reader) {
		try {
			path = Paths.get(reader.readUTF());
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
		return "PacketRequestFile{" +
				"path=" + path +
				'}';
	}
}
