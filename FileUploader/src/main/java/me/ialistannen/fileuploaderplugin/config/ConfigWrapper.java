package me.ialistannen.fileuploaderplugin.config;

import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;
import me.ialistannen.fileuploaderplugin.tokens.TokenCreator;
import me.ialistannen.fileuploaderplugin.util.DurationParser;
import org.bukkit.configuration.file.FileConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A small config wrapper
 */
public class ConfigWrapper {

	private static final Path PLUGINS_DIR = FileUploaderPlugin.getInstance()
			.getDataFolder()    // <server dir>/plugins/FileUploader
			.getParentFile()    // <server dir>/plugins
			.toPath();


	private Map<String, Set<Path>> permissionsPathMap;

	{
		initializePermissionsPathMap();
	}

	/**
	 * Gets the {@link TokenCreator} specified in the config
	 *
	 * @return The specified {@link TokenCreator} or an empty optional if not valid
	 */
	public Optional<TokenCreator> getTokenCreator() {
		String creatorName = FileUploaderPlugin.getInstance().getConfig().getString("token_creator");
		Optional<TokenCreator> tokenCreatorOptional = FileUploaderPlugin.getInstance()
				.getTokenFactory().getTokenCreator(creatorName);

		if (!tokenCreatorOptional.isPresent()) {
			FileUploaderPlugin.getInstance().getLogger().warning(String.format(
					"The TokenCreator '%s' is not known!",
					creatorName
			));
			return Optional.empty();
		}

		return tokenCreatorOptional;
	}


	/**
	 * @return The time until a token expires
	 */
	public Duration getTokenDuration() {
		String string = FileUploaderPlugin.getInstance().getConfig().getString("token_duration");
		try {
			return Duration.ofMillis(DurationParser.parseDuration(string));
		} catch (RuntimeException e) {
			FileUploaderPlugin.getInstance().getLogger().warning("The 'token_duration' in the config is not valid!");
			return Duration.ofMinutes(10);
		}
	}

	/**
	 * Parses the config and returns a map with all groups (permission and the paths they have access to)
	 *
	 * @return A Map with the permission and the Set of paths they can access. Unmodifiable
	 */
	public Map<String, Set<Path>> getPermissionPathsMap() {
		return Collections.unmodifiableMap(permissionsPathMap);
	}


	/**
	 * Initializes the map
	 */
	private void initializePermissionsPathMap() {
		permissionsPathMap = new HashMap<>();

		FileConfiguration config = FileUploaderPlugin.getInstance().getConfig();

		for (String key : config.getConfigurationSection("groups").getKeys(false)) {

			key = "groups." + key;

			if (!config.contains(key + ".permission") || !config.contains(key + ".paths")) {
				FileUploaderPlugin.getInstance().getLogger().warning("The config key '" + key + "' is missing " +
						"'permission' or 'paths'. Please fix it.");
				continue;
			}

			String permission = config.getString(key + ".permission");
			Set<Path> paths = new HashSet<>();
			for (String pathString : config.getStringList(key + ".paths")) {
				Path path = Paths.get(pathString);
				path = PLUGINS_DIR.resolve(path).toAbsolutePath();
				paths.add(path);
			}

			permissionsPathMap.put(permission, Collections.unmodifiableSet(paths));
		}
	}
}
