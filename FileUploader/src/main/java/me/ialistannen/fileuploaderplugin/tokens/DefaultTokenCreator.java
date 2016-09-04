package me.ialistannen.fileuploaderplugin.tokens;

import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

/**
 * The default {@link TokenCreator} implementation
 */
public enum DefaultTokenCreator implements TokenCreator {
	RANDOM {
		public Token create(Player player, Set<Path> allowedPaths, LocalDateTime expireTime) {
			byte[] bytes = new byte[5];
			SECURE_RANDOM_INSTANCE.nextBytes(bytes);
			String tokenID = Base64.getEncoder().encodeToString(bytes);

			return new Token(tokenID, player.getUniqueId(), allowedPaths, expireTime);
		}

		@Override
		public String getName() {
			return name();
		}
	};

	private static SecureRandom SECURE_RANDOM_INSTANCE = new SecureRandom();

}
