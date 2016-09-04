package me.ialistannen.fileuploaderplugin.commands;

import me.ialistannen.bukkitutil.commandsystem.implementation.RelayCommandNode;
import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;
import org.bukkit.entity.Player;

/**
 * The top level command
 */
public class RelayCommandFileUploaderPlugin extends RelayCommandNode {

	/**
	 * The main command
	 */
	public RelayCommandFileUploaderPlugin() {
		super(FileUploaderPlugin.getInstance().getLanguage(), "command_fup",
				FileUploaderPlugin.getInstance().getLanguage().tr("command_fup_permission"),
				sender -> sender instanceof Player);

		addChild(new CommandGetToken());
	}
}
