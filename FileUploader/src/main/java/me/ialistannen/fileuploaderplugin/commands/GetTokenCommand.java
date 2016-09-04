package me.ialistannen.fileuploaderplugin.commands;

import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;
import me.ialistannen.fileuploaderplugin.network.packets.PacketManager;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketHeartBeatResponse;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketPostFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketAuthenticateRequired;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketAuthenticationStatus;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketHeartbeatSend;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketListAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketReadException;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketWriteException;
import me.ialistannen.fileuploaderplugin.tokens.Token;
import me.ialistannen.fileuploaderplugin.tokens.TokenManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates a token
 */
public class GetTokenCommand implements CommandExecutor {

	/**
	 * Executes the given command, returning its success
	 *
	 * @param sender  Source of the command
	 * @param command Command which was executed
	 * @param label   Alias of the command which was used
	 * @param args    Passed command arguments
	 *
	 * @return true if a valid command, otherwise false
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Map<String, Set<Path>> permissionPathsMap = FileUploaderPlugin.getInstance()
				.getConfigWrapper()
				.getPermissionPathsMap();

		Optional<String> first = sender.getEffectivePermissions().stream()
				.filter(PermissionAttachmentInfo::getValue)
				.map(PermissionAttachmentInfo::getPermission)
				.filter(permission -> permissionPathsMap.containsKey(permission))
				.findFirst();

		if(!first.isPresent()) {
			sender.sendMessage("Nope");
			return true;
		}

		TokenManager tokenManager = FileUploaderPlugin.getInstance().getTokenManager();
		if(tokenManager.containsPlayer(((Player) sender).getUniqueId())) {
			sender.sendMessage("Removed your last token");
			tokenManager.removePlayerToken(((Player) sender).getUniqueId());
		}

		Duration duration = FileUploaderPlugin.getInstance().getConfigWrapper().getTokenDuration();

		Token token = FileUploaderPlugin.getInstance().getTokenFactory().getDefaultCreator().get()
				.create((Player) sender, permissionPathsMap.get(first.get()), LocalDateTime.now().plus(duration));

		tokenManager.addToken(token);

		sender.sendMessage("Added token: " + token.getTokenID());

		System.out.println(token.getTokenID());

		if("".isEmpty()) {
			return true;
		}

		PacketManager manager = PacketManager.INSTANCE;
		try {
			Socket socket = new Socket("localhost", 10000);
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

			manager.registerPacket(0, PacketAuthenticateRequired.class);
			manager.registerPacket(1, PacketTokenTransmit.class);
			manager.registerPacket(2, PacketListAvailablePaths.class);
			manager.registerPacket(3, PacketRequestFile.class);
			manager.registerPacket(4, PacketTransmitFile.class);
			manager.registerPacket(5, PacketPostFile.class);
			manager.registerPacket(6, PacketHeartbeatSend.class);
			manager.registerPacket(7, PacketHeartBeatResponse.class);
			manager.registerPacket(8, PacketRequestAvailablePaths.class);
			manager.registerPacket(9, PacketAuthenticationStatus.class);
			manager.registerPacket(10, PacketPermissionDenied.class);
			manager.registerPacket(11, PacketReadException.class);
			manager.registerPacket(12, PacketWriteException.class);

			PacketAuthenticateRequired packetAuthenticateRequired = new PacketAuthenticateRequired();
			manager.writePacket(packetAuthenticateRequired, outputStream, objectOutputStream);

			PacketTokenTransmit tokenTransmit = new PacketTokenTransmit(token.getTokenID());
			manager.writePacket(tokenTransmit, outputStream, objectOutputStream);

			PacketListAvailablePaths packetListAvailablePaths = new PacketListAvailablePaths(token.getPaths());
			manager.writePacket(packetListAvailablePaths, outputStream, objectOutputStream);

			PacketRequestFile packetRequestFile = new PacketRequestFile(token.getPaths().iterator().next());
			manager.writePacket(packetRequestFile, outputStream, objectOutputStream);

			byte[] bytes = "Test string".getBytes(StandardCharsets.UTF_8);
			PacketTransmitFile packetTransmitFile = new PacketTransmitFile(bytes, StandardCharsets.UTF_8);
			manager.writePacket(packetTransmitFile, outputStream, objectOutputStream);

			PacketPostFile packetPostFile = new PacketPostFile(token.getPaths().iterator().next());
			manager.writePacket(packetPostFile, outputStream, objectOutputStream);

			PacketHeartbeatSend packetHeartbeatSend = new PacketHeartbeatSend();
			manager.writePacket(packetHeartbeatSend, outputStream, objectOutputStream);

			PacketHeartBeatResponse packetHeartBeatResponse = new PacketHeartBeatResponse(packetHeartbeatSend.getId());
			manager.writePacket(packetHeartBeatResponse, outputStream, objectOutputStream);

			PacketRequestAvailablePaths packetRequestAvailablePaths = new PacketRequestAvailablePaths();
			manager.writePacket(packetRequestAvailablePaths, outputStream, objectOutputStream);

			PacketAuthenticationStatus packetAuthenticationStatus = new PacketAuthenticationStatus(
					PacketAuthenticationStatus.AuthenticationState.values()[ThreadLocalRandom.current().nextInt(2)]);
			manager.writePacket(packetAuthenticationStatus, outputStream, objectOutputStream);

			PacketPermissionDenied packetPermissionDenied = new PacketPermissionDenied("Permission denied!");
			manager.writePacket(packetPermissionDenied, outputStream, objectOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
}
