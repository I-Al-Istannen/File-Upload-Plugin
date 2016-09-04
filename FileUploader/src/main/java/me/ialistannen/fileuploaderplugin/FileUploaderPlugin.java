package me.ialistannen.fileuploaderplugin;

import me.ialistannen.fileuploaderplugin.commands.GetTokenCommand;
import me.ialistannen.fileuploaderplugin.config.ConfigWrapper;
import me.ialistannen.fileuploaderplugin.network.server.ServerConnectionHandler;
import me.ialistannen.fileuploaderplugin.tokens.DefaultTokenCreator;
import me.ialistannen.fileuploaderplugin.tokens.TokenFactory;
import me.ialistannen.fileuploaderplugin.tokens.TokenManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The plugin side of the file uploader
 */
public class FileUploaderPlugin extends JavaPlugin {

	private static FileUploaderPlugin instance;


	private ConfigWrapper configWrapper;
	private TokenManager tokenManager;
	private TokenFactory tokenFactory;
	private ServerConnectionHandler handler;

	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		configWrapper = new ConfigWrapper();
		tokenManager = new TokenManager();
		tokenFactory = new TokenFactory();

		{
			tokenFactory.registerTokenCreator(DefaultTokenCreator.RANDOM);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				getConfigWrapper().getTokenCreator().ifPresent(creator -> getTokenFactory().setDefaultCreator(creator.getName()));
			}
		}.runTaskLater(this, 10);

		handler = new ServerConnectionHandler(10000);
		handler.start();

		getCommand("testf").setExecutor(new GetTokenCommand());
	}

	@Override
	public void onDisable() {
		handler.stop();
		// prevent the old instance from still being around.
		instance = null;
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
