package me.ialistannen.fileuploaderplugin.tokens;

import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Creates a token
 */
public interface TokenCreator {

	/**
	 * Creates a token
	 *
	 * @param player       The player to create it for. May be used to create a token ID
	 * @param allowedPaths The allowed paths
	 * @param expireTime   The time it expires
	 *
	 * @return A valid token, with an <i><b>unique</b></i> TokenID
	 */
	Token create(Player player, Set<Path> allowedPaths, LocalDateTime expireTime);

	/**
	 * Returns the getName of this creator
	 *
	 * @return The getName of this creator
	 */
	String getName();
}
