package me.ialistannen.fileuploaderplugin;

import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommandExecutor;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultTabCompleter;
import me.ialistannen.fileuploaderplugin.commands.RelayCommandFileUploaderPlugin;
import me.ialistannen.fileuploaderplugin.config.ConfigWrapper;
import me.ialistannen.fileuploaderplugin.network.server.ServerConnectionHandler;
import me.ialistannen.fileuploaderplugin.tokens.DefaultTokenCreator;
import me.ialistannen.fileuploaderplugin.tokens.TokenFactory;
import me.ialistannen.fileuploaderplugin.tokens.TokenManager;
import me.ialistannen.languageSystem.I18N;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The plugin side of the file uploader
 */
public class FileUploaderPlugin extends JavaPlugin {

	private static FileUploaderPlugin instance;

	private I18N language;
	private CommandTree commandTree;

	private ConfigWrapper configWrapper;
	private TokenManager tokenManager;
	private TokenFactory tokenFactory;
	private ServerConnectionHandler handler;

	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		configWrapper = new ConfigWrapper();

		{
			Path savePath = getDataFolder().toPath().resolve("language");
			try {
				Files.createDirectories(savePath);
			} catch (IOException e) {
				e.printStackTrace();
			}

			I18N.copyDefaultFiles("language", savePath, false, this.getClass());

			language = new I18N("language", savePath, Locale.ENGLISH, getLogger(), getClassLoader(), "Messages");
		}
		initializeCommands();

		tokenManager = new TokenManager();
		tokenFactory = new TokenFactory();

		{
			tokenFactory.registerTokenCreator(DefaultTokenCreator.RANDOM);
		}

		// set the token creator, now that all are registered
		new BukkitRunnable() {
			@Override
			public void run() {
				getConfigWrapper().getTokenCreator()
						.ifPresent(creator -> getTokenFactory().setDefaultCreator(creator.getName()));
			}
		}.runTaskLater(this, 10);

		handler = new ServerConnectionHandler(getConfigWrapper().getPort());
		handler.start();
	}

	private void initializeCommands() {
		commandTree = new CommandTree(language);
		DefaultCommandExecutor commandExecutor = new DefaultCommandExecutor(commandTree, language);
		DefaultTabCompleter tabCompleter = new DefaultTabCompleter(commandTree);

		RelayCommandFileUploaderPlugin topLevelNode = new RelayCommandFileUploaderPlugin();
		commandTree.addTopLevelChild(topLevelNode, true, this, commandExecutor, tabCompleter);
	}

	@Override
	public void onDisable() {
		handler.stop();
		// prevent the old instance from still being around.
		instance = null;
	}

	/**
	 * @return The {@link I18N} language
	 */
	public I18N getLanguage() {
		return language;
	}

	/**
	 * @return The {@link TokenFactory}
	 */
	public TokenFactory getTokenFactory() {
		return tokenFactory;
	}

	/**
	 * @return The {@link TokenManager}
	 */
	public TokenManager getTokenManager() {
		return tokenManager;
	}

	/**
	 * @return The {@link ConfigWrapper}
	 */
	public ConfigWrapper getConfigWrapper() {
		return configWrapper;
	}

	/**
	 * Returns the plugins instance
	 *
	 * @return The plugin instance
	 */
	public static FileUploaderPlugin getInstance() {
		return instance;
	}
}
