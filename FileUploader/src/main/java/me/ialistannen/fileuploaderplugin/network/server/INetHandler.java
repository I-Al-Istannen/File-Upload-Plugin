package me.ialistannen.fileuploaderplugin.network.server;

import me.ialistannen.fileuploaderplugin.network.packets.client.PacketHeartBeatResponse;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketPostFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestAvailablePaths;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketRequestFile;
import me.ialistannen.fileuploaderplugin.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fileuploaderplugin.network.packets.shared.PacketTransmitFile;

/**
 * Handles the incoming packets
 */
public interface INetHandler {

	/**
	 * Called when a heartbeat was sent. For you to handle the response packet
	 * <p>
	 * You need to stop the client serving thread, if no answer follows, using the {@link ClientServingRunnable#stop()}
	 *
	 * @param heartbeatId The ID of the heartbeat
	 * @param runnable    The {@link ClientServingRunnable} which sent the request
	 */
	void heartbeatSend(int heartbeatId, ClientServingRunnable runnable);

	void handleRequestFile(PacketRequestFile packet, ClientServingRunnable runnable);

	void handlePostFile(PacketPostFile packet, ClientServingRunnable runnable);

	void handleTransmitFile(PacketTransmitFile packet, ClientServingRunnable runnable);

	void handlePacketTokenTransmit(PacketTokenTransmit packet, ClientServingRunnable runnable);

	void handlePacketRequestAvailablePaths(PacketRequestAvailablePaths packet, ClientServingRunnable runnable);

	void handlePacketHeartbeatResponse(PacketHeartBeatResponse packet, ClientServingRunnable runnable);
}
