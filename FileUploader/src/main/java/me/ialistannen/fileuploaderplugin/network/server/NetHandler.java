package me.ialistannen.fileuploaderplugin.network.server;

import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketHeartBeatResponse;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketPostFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketAuthenticationStatus;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketAuthenticationStatus.AuthenticationState;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketListAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketOperationSuccessful;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketReadException;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketWriteException;
import me.ialistannen.fileuploaderplugin.tokens.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The NetHandler for the server side
 */
public class NetHandler implements INetHandler {

	private static final Path PLUGINS_DIR = FileUploaderPlugin.getInstance()
			.getDataFolder()    // <server dir>/plugins/FileUploader
			.getParentFile()    // <server dir>/plugins
			.toPath();

	private int heartBeatId;
	private volatile AtomicBoolean timedOut = new AtomicBoolean(false);

	private Token token;

	private Path postFilePointer = null;

	/**
	 * Called when a heartbeat was sent. For you to handle the response packet
	 * <p>
	 * You need to stop the client serving thread, if no answer follows, using the {@link ClientServingRunnable#stop()}
	 *
	 * @param heartbeatId The ID of the heartbeat
	 */
	@Override
	public void heartbeatSend(int heartbeatId, ClientServingRunnable runnable) {
		// second missed heartbeat
		if(timedOut.get()) {
			runnable.stop();
			return;
		}
		this.heartBeatId = heartbeatId;
		this.timedOut.set(true);
	}

	@Override
	public void handleRequestFile(PacketRequestFile packet, ClientServingRunnable runnable) {
		if(!isAuthenticated()) {
			return;
		}

		Path path = resolvePath(packet.getPath());
		if(!hasPermission(path)) {
			runnable.sendPacket(new PacketPermissionDenied("No right to request '" + path + "'"));
			return;
		}

		if(!Files.exists(path)) {
			runnable.sendPacket(new PacketReadException("The file '" + path + "' doesn't exist"));
			return;
		}
		if(Files.isDirectory(path)) {
			runnable.sendPacket(new PacketReadException("The file '" + path + "' is a directory."));
			return;
		}

		try {
			byte[] bytes = Files.readAllBytes(path);
			runnable.sendPacket(new PacketTransmitFile(bytes, StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
			String message = "I/O error reading '" + path + "': " + e.getMessage();
			runnable.sendPacket(new PacketReadException(message));
		}
	}

	@Override
	public void handlePostFile(PacketPostFile packet, ClientServingRunnable runnable) {
		if(!isAuthenticated()) {
			return;
		}

		Path path = resolvePath(packet.getPath());
		if(!hasPermission(path)) {
			runnable.sendPacket(new PacketPermissionDenied("No right to post '" + path + "'"));
			return;
		}

		postFilePointer = path;

		runnable.sendPacket(new PacketOperationSuccessful());
	}

	@Override
	public void handleTransmitFile(PacketTransmitFile packet, ClientServingRunnable runnable) {
		if(!isAuthenticated()) {
			return;
		}

		if(postFilePointer == null) {
			// TODO: Create a field for this.
			runnable.sendPacket(new PacketWriteException("No post packet send before."));
			return;
		}

		try {
			Files.write(postFilePointer, packet.getContents(),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			runnable.sendPacket(new PacketOperationSuccessful());
		} catch (IOException e) {
			e.printStackTrace();
			String message = "I/O error writing '" + postFilePointer + "': " + e.getMessage();
			runnable.sendPacket(new PacketWriteException(message));
		}
	}

	@Override
	public void handlePacketTokenTransmit(PacketTokenTransmit packet, ClientServingRunnable runnable) {
		String tokenID = packet.getTokenID();
		Optional<Token> tokenOptional = FileUploaderPlugin.getInstance().getTokenManager().getToken(tokenID);
		if(!tokenOptional.isPresent()) {
			runnable.sendPacket(new PacketAuthenticationStatus(AuthenticationState.INVALID_TOKEN));
			runnable.stop();
			return;
		}

		token = tokenOptional.get();
		runnable.sendPacket(new PacketAuthenticationStatus(AuthenticationState.SUCCESSFUL));
	}

	@Override
	public void handlePacketRequestAvailablePaths(PacketRequestAvailablePaths packet, ClientServingRunnable runnable) {
		if(!isAuthenticated()) {
			return;
		}

		runnable.sendPacket(new PacketListAvailablePaths(token.getPaths()));
	}

	@Override
	public void handlePacketHeartbeatResponse(PacketHeartBeatResponse packet, ClientServingRunnable runnable) {
		if(heartBeatId != packet.getId()) {
			runnable.stop();
			return;
		}

		// prevent the stop command from executing
		timedOut.set(false);
	}

	/**
	 * Checks whether the user is authenticated
	 *
	 * @return True if this object is valid
	 */
	private boolean isAuthenticated() {
		return token != null;
	}

	private Path resolvePath(Path path) {
		return PLUGINS_DIR.resolve(path).toAbsolutePath();
	}

	private boolean hasPermission(Path path) {
		return token.getPaths().contains(path);
	}
}
