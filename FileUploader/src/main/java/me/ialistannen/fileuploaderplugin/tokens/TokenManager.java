package me.ialistannen.fileuploaderplugin.tokens;

import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages all the active tokens
 */
public class TokenManager {

	private final ConcurrentMap<String, Token> tokenIdMap;

	private TokenCreator tokenCreator;

	{
		new BukkitRunnable() {
			@Override
			public void run() {
				cleanExpired();
			}
		}.runTaskTimer(FileUploaderPlugin.getInstance(), 0, 20);

		tokenIdMap = new ConcurrentHashMap<>();
	}

	/**
	 * Adds a token
	 *
	 * @param token The token to add
	 */
	public void addToken(Token token) {
		Objects.requireNonNull(token);

		tokenIdMap.put(token.getTokenID(), token);
	}

	/**
	 * Removes the token with the given ID
	 *
	 * @param tokenID The ID of the token
	 */
	public void removeToken(String tokenID) {
		tokenIdMap.remove(tokenID);
	}

	/**
	 * Returns a token from its id
	 *
	 * @param tokenID The ID of the token
	 *
	 * @return The Token associated with the ID or an empty Optional if none
	 */
	public Optional<Token> getToken(String tokenID) {
		return Optional.ofNullable(tokenIdMap.get(tokenID));
	}

	/**
	 * Checks if it contains the token
	 *
	 * @param tokenID The ID of the token
	 *
	 * @return True if it contains the token
	 */
	public boolean containsTokenID(String tokenID) {
		return tokenIdMap.containsKey(tokenID);
	}

	/**
	 * Checks whether the player already has a token. Quite costly operation [O(n)]
	 *
	 * @param playerID The UUID of the player
	 *
	 * @return True if it contains a token for the player
	 */
	public boolean containsPlayer(UUID playerID) {
		return tokenIdMap.values().stream().anyMatch(token -> token.getPlayerID().equals(playerID));
	}

	/**
	 * Removes all tokens for the given player. Quite costly operation [O(n)]
	 *
	 * @param playerID The UUID of the player
	 */
	public void removePlayerToken(UUID playerID) {
		tokenIdMap.values().removeIf(token -> token.getPlayerID().equals(playerID));
	}

	/**
	 * Cleans expired tokens
	 */
	public void cleanExpired() {
		// explicit synchronizing probably unneeded
		// TODO: Check it
		synchronized (tokenIdMap) {
			tokenIdMap.values().removeIf(Token::isExpired);
		}
	}
}
