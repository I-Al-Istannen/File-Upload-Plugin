package me.ialistannen.fupclient.network.client;

import me.ialistannen.fupclient.model.Token;
import me.ialistannen.fupclient.network.packets.client.PacketHeartBeatResponse;
import me.ialistannen.fupclient.network.packets.server.PacketHeartbeatSend;
import me.ialistannen.fupclient.network.packets.server.PacketListAvailablePaths;
import me.ialistannen.fupclient.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fupclient.network.packets.shared.PacketOperationSuccessful;
import me.ialistannen.fupclient.network.packets.shared.PacketReadException;
import me.ialistannen.fupclient.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fupclient.network.packets.shared.PacketWriteException;

/**
 * The net handler implementation
 */
public class NetHandler implements INetHandler {

	@Override
	public void handlePacketHeartbeatSend(PacketHeartbeatSend packet, ServerConnection connection) {
		connection.sendPacket(new PacketHeartBeatResponse(packet.getId()));
	}

	@Override
	public Token handlePacketListAvailablePaths(PacketListAvailablePaths packet, String tokenID) {
		return new Token(tokenID, packet.getPaths());
	}

	@Override
	public void handlePacketPermissionDenied(PacketPermissionDenied packet, ServerConnection connection) {

	}

	@Override
	public void handlePacketOperationSuccessful(PacketOperationSuccessful packet, ServerConnection connection) {

	}

	@Override
	public void handlePacketTransmitFile(PacketTransmitFile packet, ServerConnection connection) {

	}

	@Override
	public void handlePacketReadException(PacketReadException packet, ServerConnection connection) {

	}

	@Override
	public void handlePacketWriteException(PacketWriteException packet, ServerConnection connection) {

	}
}
