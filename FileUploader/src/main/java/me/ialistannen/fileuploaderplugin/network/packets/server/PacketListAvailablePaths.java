package me.ialistannen.fileuploaderplugin.network.packets.server;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains all the paths the user can access
 */
public class PacketListAvailablePaths extends Packet {

	private final Set<Path> paths;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketListAvailablePaths(ObjectInputStream reader) {
		paths = new HashSet<>();
		read(reader);
	}

	/**
	 * @param paths The allowed paths
	 */
	public PacketListAvailablePaths(Collection<Path> paths) {
		this.paths = new HashSet<>(paths);
	}

	/**
	 * Returns the paths
	 *
	 * @return The paths
	 */
	@SuppressWarnings("unused")
	public Set<Path> getPaths() {
		return paths;
	}

	private void read(ObjectInputStream reader) {
		try {
			int size = reader.readInt();
			for (int i = 0; i < size; i++) {
				paths.add(Paths.get(reader.readUTF()));
			}
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
			writer.writeInt(paths.size());
			for (Path path : paths) {
				writer.writeUTF(path.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketListAvailablePaths{" +
				"paths=" + paths +
				'}';
	}
}
