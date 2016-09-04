package me.ialistannen.fupclient.network.packets.server;


import me.ialistannen.fupclient.network.packets.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

/**
 * Send by the server to express the authentication status
 */
public class PacketAuthenticationStatus extends Packet {

	private AuthenticationState authenticationState;

	/**
	 * Reads this packet from an input stream. ID will not be part of it.
	 *
	 * @param reader The reader to read from
	 */
	public PacketAuthenticationStatus(ObjectInputStream reader) {
		read(reader);
	}

	/**
	 * @param authenticationState The current {@link AuthenticationState}
	 */
	public PacketAuthenticationStatus(AuthenticationState authenticationState) {
		this.authenticationState = authenticationState;
	}

	/**
	 * Returns the status
	 *
	 * @return The {@link AuthenticationState}
	 */
	@SuppressWarnings("unused")
	public AuthenticationState getAuthenticationState() {
		return authenticationState;
	}

	@SuppressWarnings("Duplicates")
	private void read(ObjectInputStream reader) {
		try {
			String stateName = reader.readUTF();
			authenticationState = AuthenticationState.match(stateName);
			Objects.requireNonNull(authenticationState, "Unknown state: '" + stateName + "'");
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
			writer.writeUTF(authenticationState.name());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "PacketAuthenticationStatus{" +
				"authenticationState=" + authenticationState +
				'}';
	}

	/**
	 * The different authentication states
	 */
	public enum AuthenticationState {
		/**
		 * Successfully authenticated
		 */
		SUCCESSFUL,
		/**
		 * The token is invalid
		 */
		INVALID_TOKEN;

		/**
		 * Matches the name to the constant.
		 *
		 * @param input The input String
		 *
		 * @return The {@link AuthenticationState} or null if it is invalid
		 */
		private static AuthenticationState match(String input) {
			Objects.requireNonNull(input);
			try {
				return valueOf(input.toUpperCase().replace(" ", "_"));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}
}
