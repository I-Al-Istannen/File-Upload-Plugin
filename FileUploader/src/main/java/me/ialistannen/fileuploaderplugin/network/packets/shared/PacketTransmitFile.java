package me.ialistannen.fileuploaderplugin.network.packets.shared;

import me.ialistannen.fileuploaderplugin.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A packet containing the contents of a file
 */
public class PacketTransmitFile extends Packet {
	private byte[] contents;
	private Charset encoding;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketTransmitFile(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param contents The contents
	 * @param encoding The encoding. Null for binary data, without encoding
	 */
	@SuppressWarnings("SameParameterValue")
	public PacketTransmitFile(byte[] contents, Charset encoding) {
		this.contents = contents;
		this.encoding = encoding;
	}


	private void read(ObjectInputStream reader) {
		try {
			String encodingName = reader.readUTF();
			if (encodingName.equals("NONE")) {
				encoding = null;
			} else {
				encoding = Charset.forName(encodingName);
			}
			contents = (byte[]) reader.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks whether an encoding was specified
	 *
	 * @return True if there was an encoding specified
	 */
	private boolean hasEncoding() {
		return encoding != null;
	}

	/**
	 * Interprets the byte array as a String
	 *
	 * @return The resulting String.
	 *
	 * @throws IllegalStateException If {@link #hasEncoding()} returns false
	 */
	private String interpretAsString() {
		if (!hasEncoding()) {
			throw new IllegalStateException("No encoding specified.");
		}
		return new String(contents, encoding);
	}

	/**
	 * Returns the raw bytes
	 *
	 * @return The byte array of the data. <i><b>MUTABLE.</b></i>
	 */
	public byte[] getContents() {
		return contents;
	}

	/**
	 * Writes this packet to an output stream. You do not need to write the ID.
	 *
	 * @param writer The writer to write to
	 */
	@Override
	public void write(ObjectOutputStream writer) {
		try {
			if (encoding != null) {
				writer.writeUTF(encoding.name());
			} else {
				writer.writeUTF("NONE");
			}
			writer.writeObject(contents);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketTransmitFile{" +
				"contents=" + Arrays.toString(contents) +
				", encoding=" + encoding +
				", hasEncoding=" + hasEncoding() +
				", interpretAsString='" + (hasEncoding() ? interpretAsString() : "NULL") + '\'' +
				'}';
	}
}