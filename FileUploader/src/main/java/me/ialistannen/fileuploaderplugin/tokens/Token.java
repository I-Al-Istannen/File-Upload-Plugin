package me.ialistannen.fileuploaderplugin.tokens;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A tokenID
 */
public class Token {
	private String tokenID;
	private UUID playerID;
	private LocalDateTime expireTime;

	private Set<Path> allowedPaths = new HashSet<>();

	/**
	 * @param tokenID      The tokenID
	 * @param playerID     The UUID of the player it belongs to
	 * @param allowedPaths The allowed paths
	 * @param expireTime   The time it expires.
	 */
	public Token(String tokenID, UUID playerID, Set<Path> allowedPaths, LocalDateTime expireTime) {
		Objects.requireNonNull(tokenID);
		Objects.requireNonNull(playerID);
		Objects.requireNonNull(allowedPaths);
		Objects.requireNonNull(expireTime);

		this.tokenID = tokenID;
		this.playerID = playerID;
		this.allowedPaths = new HashSet<>(allowedPaths);
		this.expireTime = expireTime;
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
	 * Returns the time this tokenID will expire.
	 *
	 * @return The time this token will expire
	 */
	public LocalDateTime getExpireTime() {
		return expireTime;
	}

	/**
	 * Checks whether the token is expired
	 *
	 * @return True if the token is expired
	 */
	public boolean isExpired() {
		return getExpireTime().isBefore(LocalDateTime.now());
	}

	/**
	 * Returns the {@link UUID} of the player this token belongs to
	 *
	 * @return The UUID of the player
	 */
	public UUID getPlayerID() {
		return playerID;
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
				", expireTime=" + expireTime +
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
