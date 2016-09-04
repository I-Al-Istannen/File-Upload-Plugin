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
interface INetHandler {

	/**
	 * Called when a heartbeat was sent. For you to handle the response packet
	 * <p>
	 * You need to stop the client serving thread, if no answer follows, using the {@link ClientServingRunnable#stop()}
	 *
	 * @param heartbeatId The ID of the heartbeat
	 * @param runnable    The {@link ClientServingRunnable} which sent the request
	 */
	void heartbeatSend(int heartbeatId, ClientServingRunnable runnable);

	/**
	 * Called when the client sends a {@link PacketRequestFile}
	 *
	 * @param packet   The Packet
	 * @param runnable The {@link ClientServingRunnable} which sent the request
	 */
	void handleRequestFile(PacketRequestFile packet, ClientServingRunnable runnable);

	/**
	 * Called when the client sends a {@link PacketPostFile}
	 *
	 * @param packet   The Packet
	 * @param runnable The {@link ClientServingRunnable} which sent the request
	 */
	void handlePostFile(PacketPostFile packet, ClientServingRunnable runnable);

	/**
	 * Called when the client sends a {@link PacketTransmitFile} packet
	 *
	 * @param packet   The packet
	 * @param runnable The {@link ClientServingRunnable} which sent the request
	 */
	void handleTransmitFile(PacketTransmitFile packet, ClientServingRunnable runnable);

	/**
	 * Called when the client sends a {@link PacketTokenTransmit} packet
	 *
	 * @param packet   The packet
	 * @param runnable The {@link ClientServingRunnable} which sent the request
	 */
	void handlePacketTokenTransmit(PacketTokenTransmit packet, ClientServingRunnable runnable);

	/**
	 * Called when the client sends a {@link PacketRequestAvailablePaths} packet
	 *
	 * @param packet   The packet
	 * @param runnable The {@link ClientServingRunnable} which sent the request
	 */
	@SuppressWarnings("UnusedParameters")
	void handlePacketRequestAvailablePaths(PacketRequestAvailablePaths packet, ClientServingRunnable runnable);

	/**
	 * Called when the client sends a {@link PacketHeartBeatResponse} packet
	 *
	 * @param packet   The packet
	 * @param runnable The {@link ClientServingRunnable} which sent the request
	 */
	void handlePacketHeartbeatResponse(PacketHeartBeatResponse packet, ClientServingRunnable runnable);
}
