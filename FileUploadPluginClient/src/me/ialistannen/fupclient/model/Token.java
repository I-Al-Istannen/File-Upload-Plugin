package me.ialistannen.fupclient.model;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A tokenID
 */
public class Token {
	private String tokenID;

	private Set<Path> allowedPaths = new HashSet<>();

	/**
	 * @param tokenID      The tokenID
	 * @param allowedPaths The allowed paths
	 */
	public Token(String tokenID, Set<Path> allowedPaths) {
		this.tokenID = tokenID;
		this.allowedPaths = new HashSet<>(allowedPaths);
	}

	/**
	 * Returns the tokenID
	 *
	 * @return The tokenID
	 */
	public String getTokenID() {
		return tokenID;
	}


	/**
	 * Checks whether a path is allowed or not
	 *
	 * @param path The path to check
	 *
	 * @return True if the path is allowed
	 */
	public boolean isAllowed(Path path) {
		return allowedPaths.contains(path.toAbsolutePath());
	}

	/**
	 * Returns all the allowed paths
	 *
	 * @return An unmodifiable collection of the allowed paths
	 */
	public Collection<Path> getPaths() {
		return Collections.unmodifiableCollection(allowedPaths);
	}

	@Override
	public String toString() {
		return "Token{" +
				"tokenID='" + tokenID + '\'' +
				", allowedPaths=" + allowedPaths +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Token)) {
			return false;
		}
		Token token1 = (Token) o;
		return Objects.equals(tokenID, token1.tokenID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokenID);
	}
}
