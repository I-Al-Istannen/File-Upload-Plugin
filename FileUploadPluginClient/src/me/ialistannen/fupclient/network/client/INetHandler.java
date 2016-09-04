package me.ialistannen.fupclient.network.client;

import me.ialistannen.fupclient.model.Token;
import me.ialistannen.fupclient.network.packets.server.PacketHeartbeatSend;
import me.ialistannen.fupclient.network.packets.server.PacketListAvailablePaths;
import me.ialistannen.fupclient.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fupclient.network.packets.shared.PacketOperationSuccessful;
import me.ialistannen.fupclient.network.packets.shared.PacketReadException;
import me.ialistannen.fupclient.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fupclient.network.packets.shared.PacketWriteException;

/**
 * A handler for the server packets
 */
public interface INetHandler {

	void handlePacketHeartbeatSend(PacketHeartbeatSend packet, ServerConnection connection);

	Token handlePacketListAvailablePaths(PacketListAvailablePaths packet, String tokenID);

	void handlePacketPermissionDenied(PacketPermissionDenied packet, ServerConnection connection);

	void handlePacketOperationSuccessful(PacketOperationSuccessful packet, ServerConnection connection);

	void handlePacketTransmitFile(PacketTransmitFile packet, ServerConnection connection);

	void handlePacketReadException(PacketReadException packet, ServerConnection connection);

	void handlePacketWriteException(PacketWriteException packet, ServerConnection connection);

}
