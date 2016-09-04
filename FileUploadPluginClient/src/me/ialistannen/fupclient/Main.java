package me.ialistannen.fupclient;

import me.ialistannen.fupclient.network.packets.Packet;
import me.ialistannen.fupclient.network.packets.PacketManager;
import me.ialistannen.fupclient.network.packets.client.PacketPostFile;
import me.ialistannen.fupclient.network.packets.client.PacketRequestFile;
import me.ialistannen.fupclient.network.packets.client.PacketTokenTransmit;
import me.ialistannen.fupclient.network.packets.server.PacketAuthenticationStatus;
import me.ialistannen.fupclient.network.packets.server.PacketAuthenticationStatus.AuthenticationState;
import me.ialistannen.fupclient.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fupclient.network.packets.shared.PacketOperationSuccessful;
import me.ialistannen.fupclient.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fupclient.network.packets.shared.PacketWriteException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class Main {

	private static Path serverPath, clientPath;
	private static String token;
	private static boolean upload;

	private static int port;
	private static String hostName;

	public static void main(String[] args) throws IOException {
		List<String> list = Arrays.asList(args);
		if (!list.contains("--serverPath") || !list.contains("--clientPath") || !list.contains("--token")
				|| !list.contains("--port") || !list.contains("--host")
				|| (!list.contains("--download") && !list.contains("--upload"))
				|| list.size() != 11) {
			System.out.println("Usage:");
			System.out.println("\t--serverPath <path on server, relative to plugin dir>");
			System.out.println("\t--clientPath <path on your computer>");
			System.out.println("\t--token <your token>");
			System.out.println("\t--host <hostname>");
			System.out.println("\t--port <the port>");
			System.out.println("\t--download or --upload");
			return;
		}

		if (list.contains("--upload")) {
			upload = true;
		}

		token = list.get(list.indexOf("--token") + 1);

		serverPath = Paths.get(list.get(list.indexOf("--serverPath") + 1));
		clientPath = Paths.get(list.get(list.indexOf("--clientPath") + 1));

		port = Integer.parseInt(list.get(list.indexOf("--port") + 1));
		hostName = list.get(list.indexOf("--host") + 1);

		Socket socket = new Socket(hostName, port);
		socket.setSoTimeout(10000);

		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

		PacketManager manager = PacketManager.INSTANCE;

		manager.writePacket(new PacketTokenTransmit(token), outputStream, objectOutputStream);

		ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

		Packet packet = manager.readPacket(objectInputStream);

		if (!(packet instanceof PacketAuthenticationStatus)) {
			System.out.println("Wrong packet received: " + packet.getClass().getSimpleName());
			return;
		}
		AuthenticationState state = ((PacketAuthenticationStatus) packet).getAuthenticationState();

		if (state == AuthenticationState.INVALID_TOKEN) {
			System.out.println("INVALID TOKEN: '" + token + "'");
			return;
		}

		if (!upload) {
			PacketRequestFile packetRequestFile = new PacketRequestFile(serverPath);
			manager.writePacket(packetRequestFile, outputStream, objectOutputStream);
			Packet answer = manager.readPacket(objectInputStream);
			if (answer instanceof PacketPermissionDenied) {
				System.out.println("Permission to path: '" + serverPath + "' denied!");
				System.out.println(((PacketPermissionDenied) answer).getMessage());
				return;
			} else if (answer instanceof PacketTransmitFile) {
				if (!Files.exists(clientPath)) {
					Files.createFile(clientPath);
				}
				Files.write(clientPath, ((PacketTransmitFile) answer).getContents(),
						StandardOpenOption.TRUNCATE_EXISTING);
				System.out.println("Wrote the file to '" + clientPath.toAbsolutePath() + "'");
				return;
			}
		} else {
			PacketPostFile packetPostFile = new PacketPostFile(serverPath);
			manager.writePacket(packetPostFile, outputStream, objectOutputStream);
			Packet answer = manager.readPacket(objectInputStream);
			if (answer instanceof PacketPermissionDenied) {
				System.out.println("Permission to path: '" + serverPath + "' denied!");
				System.out.println(((PacketPermissionDenied) answer).getMessage());
				return;
			}
			byte[] bytes = Files.readAllBytes(clientPath);
			PacketTransmitFile packetTransmitFile = new PacketTransmitFile(bytes, StandardCharsets.UTF_8);
			manager.writePacket(packetTransmitFile, outputStream, objectOutputStream);

			answer = manager.readPacket(objectInputStream);
			if (answer instanceof PacketWriteException) {
				System.out.println("Error writing file to: '" + serverPath + "'");
				System.out.println("Source file is in: '" + clientPath.toAbsolutePath() + "'");
				System.out.println("Error: " + ((PacketWriteException) answer).getMessage());
				return;
			} else if (answer instanceof PacketOperationSuccessful) {
				System.out.println("Successfully uploaded a file from");
				System.out.println(clientPath.toAbsolutePath());
				System.out.println("to " + serverPath);
			}
		}
	}
}
