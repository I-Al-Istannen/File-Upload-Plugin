package me.ialistannen.fileuploaderplugin.tokens;


import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Manages the {@link TokenCreator}s
 */
public class TokenFactory {

	private Map<String, TokenCreator> tokenCreatorMap = new HashMap<>();

	private String defaultCreator;

	/**
	 * Registers a token creator
	 *
	 * @param creator The tokenCreator to add
	 *
	 * @throws IllegalArgumentException If there is already a {@link TokenCreator} registered with that getName
	 */
	public void registerTokenCreator(TokenCreator creator) {
		if (contains(creator.getName())) {
			throw new IllegalArgumentException("There is already a TokenCreator with the name '"
					+ creator.getName() + "' registered.");
		}

		tokenCreatorMap.put(creator.getName(), creator);

		if (defaultCreator == null) {
			setDefaultCreator(creator.getName());
		}
	}

	/**
	 * Registers the default token creator
	 *
	 * @param creatorName The getName of the default token creator
	 *
	 * @throws IllegalStateException If there is no {@link TokenCreator} with that getName registered
	 */
	public void setDefaultCreator(String creatorName) {
		if (!contains(creatorName)) {
			throw new IllegalStateException("There is no TokenCreator with the name '"
					+ creatorName + "' registered.");
		}

		defaultCreator = creatorName;

		TokenCreator tokenCreator = getTokenCreator(creatorName).get();
		FileUploaderPlugin.getInstance().getLogger().log(Level.INFO, String.format(
				"Using token creator of class: '%s' with the name '%s'.",
				tokenCreator.getClass().getName(), tokenCreator.getName()
				)
		);
	}

	/**
	 * Gets a {@link TokenCreator}
	 *
	 * @param name The getName of the token creator
	 *
	 * @return The {@link TokenCreator} or an empty optional if not registered
	 */
	public Optional<TokenCreator> getTokenCreator(String name) {
		return Optional.ofNullable(tokenCreatorMap.get(name));
	}


	/**
	 * The default token creator
	 *
	 * @return The default creator (if there is any)
	 */
	public Optional<TokenCreator> getDefaultCreator() {
		return getTokenCreator(defaultCreator);
	}

	/**
	 * Checks if it contains the token creator
	 *
	 * @param name The getName of the {@link TokenCreator}
	 *
	 * @return True if it contains the token creator
	 */
	public boolean contains(String name) {
		return tokenCreatorMap.containsKey(name);
	}
}
