package me.ialistannen.fileuploaderplugin.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;
import me.ialistannen.fileuploaderplugin.tokens.Token;
import me.ialistannen.fileuploaderplugin.tokens.TokenCreator;
import me.ialistannen.fileuploaderplugin.tokens.TokenManager;
import me.ialistannen.fileuploaderplugin.util.Util;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A command to get a token
 */
class CommandGetToken extends DefaultCommand {

	CommandGetToken() {
		super(FileUploaderPlugin.getInstance().getLanguage(), "command_get_token",
				FileUploaderPlugin.getInstance().getLanguage().tr("command_get_token_permission"),
				sender -> sender instanceof Player);
	}

	/**
	 * Tab-Completes the command
	 *
	 * @param sender             The {@link CommandSender} who tab-completed
	 * @param alias              The alias used
	 * @param wholeUserChat      Everything he wrote
	 * @param indexRelativeToYou The index of the argument he completed, relative to you.
	 *
	 * @return A list with valid completions. Empty for none, null for all online, visible players
	 */
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		return Collections.emptyList();
	}

	/**
	 * Called when a user executes a command
	 *
	 * @param sender The sender of the command
	 * @param args   The arguments of the command
	 *
	 * @return False if the usage should be send
	 */
	@Override
	public CommandResultType execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		// all the valid permissions
		Map<String, Set<Path>> permissionPathsMap = FileUploaderPlugin.getInstance()
				.getConfigWrapper()
				.getPermissionPathsMap();

		// check if he has one
		Optional<String> first = player.getEffectivePermissions().stream()
				.filter(PermissionAttachmentInfo::getValue)
				.map(PermissionAttachmentInfo::getPermission)
				.filter(permissionPathsMap::containsKey)
				.findFirst();

		if (!first.isPresent()) {
			sender.sendMessage(Util.tr("no_permission_set_on_you"));
			return CommandResultType.SUCCESSFUL;
		}

		TokenManager tokenManager = FileUploaderPlugin.getInstance().getTokenManager();
		if (tokenManager.containsPlayer(player.getUniqueId())) {
			player.sendMessage(Util.tr("removed_last_token"));
			tokenManager.removePlayerToken(player.getUniqueId());
		}

		Duration duration = FileUploaderPlugin.getInstance().getConfigWrapper().getTokenDuration();

		Optional<TokenCreator> tokenCreator = FileUploaderPlugin.getInstance().getTokenFactory().getDefaultCreator();
		if (!tokenCreator.isPresent()) {
			player.sendMessage(Util.tr("no_token_creator_registered"));
			return CommandResultType.SUCCESSFUL;
		}

		Token token = tokenCreator.get().create(player, permissionPathsMap.get(first.get()), LocalDateTime.now().plus
				(duration));

		tokenManager.addToken(token);

		player.sendMessage(Util.tr("created_token",
				token.getTokenID(),
				DurationFormatUtils.formatDurationWords(duration.toMillis(), true, true)));

		return CommandResultType.SUCCESSFUL;
	}
}
