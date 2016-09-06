package me.ialistannen.fupclient.javafx.view.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import me.ialistannen.fupclient.javafx.JavaFxMain;
import me.ialistannen.fupclient.javafx.logic.ServerConnection;
import me.ialistannen.fupclient.javafx.util.Util;
import me.ialistannen.fupclient.model.Token;
import me.ialistannen.fupclient.network.packets.Packet;
import me.ialistannen.fupclient.network.packets.client.PacketEndConnection;
import me.ialistannen.fupclient.network.packets.client.PacketPostFile;
import me.ialistannen.fupclient.network.packets.client.PacketRequestFile;
import me.ialistannen.fupclient.network.packets.server.PacketPermissionDenied;
import me.ialistannen.fupclient.network.packets.shared.PacketOperationSuccessful;
import me.ialistannen.fupclient.network.packets.shared.PacketReadException;
import me.ialistannen.fupclient.network.packets.shared.PacketTransmitFile;
import me.ialistannen.fupclient.network.packets.shared.PacketWriteException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The controller for the main action window, where you can choose what to do.
 */
public class MainActionChooseWindowController {

	@FXML
	private TreeView<String> treeView;

	private Token token;

	@FXML
	private void initialize() {
		TreeItem<String> root = new TreeItem<>("Error :(");
		treeView.setShowRoot(true);
		treeView.setRoot(root);
	}


	/**
	 * Sets the token and therefore structures the {@link TreeView}
	 *
	 * @param token The token
	 *
	 * @throws NullPointerException If token is null
	 */
	public void setToken(Token token) {
		Objects.requireNonNull(token);

		this.token = token;
		buildFromToken();
	}

	private void buildFromToken() {
		TreeItem<String> root = new TreeItem<>("plugins");
		treeView.setRoot(root);
		treeView.setShowRoot(true);

		// initialize paths
		for (Path path : token.getPaths()) {
			insert(pathToStringList(path, 1), root);
		}
	}

	private List<String> pathToStringList(Path path, int startIndex) {
		List<String> list = new ArrayList<>(path.getNameCount());
		for (int i = startIndex, max = path.getNameCount(); i < max; i++) {
			list.add(path.getName(i).toString());
		}

		return list;
	}

	private void insert(List<String> stringList, TreeItem<String> root) {
		if (stringList.isEmpty()) {
			return;
		}

		Optional<TreeItem<String>> child = getStringChild(stringList.get(0), root);
		TreeItem<String> newRoot;
		if (child.isPresent()) {
			newRoot = child.get();
		} else {
			newRoot = new TreeItem<>(stringList.get(0));
			root.getChildren().add(newRoot);
		}

		int fromIndex = stringList.size() == 1 ? 0 : 1;
		int toIndex = stringList.size() > 1 ? stringList.size() : 0;
		insert(stringList.subList(fromIndex, toIndex), newRoot);
	}

	private Optional<TreeItem<String>> getStringChild(String string, TreeItem<String> root) {
		return root.getChildren()
				.stream()
				.filter(stringTreeItem -> stringTreeItem.getValue().equals(string))
				.findAny();
	}

	@FXML
	void onDownload(ActionEvent event) {
		if (!ensureIndexIsSelected()) {
			return;
		}

		boolean isFile = treeView.getSelectionModel().getSelectedItem().getChildren().isEmpty();
		Path path = getPathOfSelectedTreeItem();

		if (isFile) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose a file to save to");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("All files", "*.*"));
			fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
			File file = fileChooser.showSaveDialog(JavaFxMain.getInstance().getPrimaryStage());

			if (file != null) {
				handleDownloadFile(path, file.toPath());
			}
		} else {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Choose a directory to save to");
			File file = directoryChooser.showDialog(JavaFxMain.getInstance().getPrimaryStage());

			if (file != null) {
				handleDownloadDirectory(path, file.toPath());
			}
		}
	}

	private void handleDownloadDirectory(Path directoryPath, Path savePath) {
		try {
			if (!Files.exists(savePath)) {
				Files.createDirectories(savePath);
			}

			List<Path> paths = new ArrayList<>();
			{
				List<String> strings = pathToStringList(directoryPath, 0);
				TreeItem<String> item = treeView.getRoot();
				for (String string : strings) {
					Optional<TreeItem<String>> childOptional = getStringChild(string, item);
					if (childOptional.isPresent()) {
						item = childOptional.get();
					}
				}
				for (TreeItem<String> stringTreeItem : item.getChildren()) {
					if (!stringTreeItem.getChildren().isEmpty()) {
						// skip directories
						continue;
					}
					Path childPath = directoryPath.resolve(stringTreeItem.getValue());
					paths.add(childPath);
				}
			}

			for (Path path : paths) {
				handleDownloadFile(path, savePath.resolve(path));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleDownloadFile(Path file, Path savePath) {
		if (Platform.isFxApplicationThread()) {
			new Thread(() -> handleDownloadFile(file, savePath)).start();
			return;
		}

		try {
			ServerConnection serverConnection = JavaFxMain.getInstance().getServerConnection();
			serverConnection.writePacket(new PacketRequestFile(file));

			serverConnection.downCounter();

			Packet packet = serverConnection.pollQueue();

			if (packet instanceof PacketPermissionDenied) {
				Util.showAlert(AlertType.ERROR,
						"Permission denied",
						"You don't have permission to request this file!",
						((PacketPermissionDenied) packet).getMessage());
				return;
			}

			if (packet instanceof PacketReadException) {
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("An error happened trying to download a file.");

					Text contentText = new Text(((PacketReadException) packet).getMessage());
					contentText.setFont(Font.font("monospaced"));
					alert.getDialogPane().setContent(contentText);

					alert.show();
				});
				return;
			}

			if (!(packet instanceof PacketTransmitFile)) {
				Util.showWrongPacketReceivedAlert(PacketTransmitFile.class, packet);
				return;
			}

			if (!Files.exists(savePath)) {
				Files.createDirectories(savePath.getParent());
				Files.createFile(savePath);
			}
			Files.write(savePath, ((PacketTransmitFile) packet).getContents(), StandardOpenOption.TRUNCATE_EXISTING);

			Util.showAlert(AlertType.INFORMATION,
					"Wrote file",
					"Wrote \n\t'" + file.toString() + "'",
					"Wrote \n\t'" + file.toString() + "'\nto\n\t'" + savePath.toString() + "'");
		} catch (InterruptedException | IOException e) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("An error happened trying to download a file.");
				alert.setContentText(e.getMessage());
				TextArea textArea = new TextArea(Util.getStackTrace(e));
				textArea.setFont(Font.font("monospaced"));
				alert.getDialogPane().setExpandableContent(textArea);
				alert.show();
			});
			e.printStackTrace();
		}
	}

	private Path getPathOfSelectedTreeItem() {
		TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();

		List<String> parents = new ArrayList<>();

		TreeItem<String> parent = item.getParent();
		// ignore the root
		while (parent != null && parent.getParent() != null) {
			parents.add(parent.getValue());
			parent = parent.getParent();
		}

		// reorder them, as they are currently (child -> parent) not (parent -> child)
		Collections.reverse(parents);

		// add the selected item
		parents.add(item.getValue());

		return Paths.get(parents.stream().collect(Collectors.joining("/")));
	}

	@FXML
	void onUpload(ActionEvent event) {
		if (!ensureIndexIsSelected()) {
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("All files", "*.*"));
		fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
		fileChooser.setTitle("Choose a file to upload");
		File uploadFile = fileChooser.showOpenDialog(JavaFxMain.getInstance().getPrimaryStage());

		if (uploadFile == null) {
			return;
		}

		boolean isFile = treeView.getSelectionModel().getSelectedItem().getChildren().isEmpty();

		if (!isFile) {
			Util.showAlert(AlertType.ERROR,
					"Error while uploading",
					"You can only upload to a file, not to a directory!",
					"");
			return;
		}

		Path targetPath = getPathOfSelectedTreeItem();

		// ==== UPLOAD IT ====
		try {
			ServerConnection connection = JavaFxMain.getInstance().getServerConnection();
			connection.writePacket(new PacketPostFile(targetPath));

			connection.downCounter();

			Packet packet = connection.pollQueue();

			if (packet instanceof PacketPermissionDenied) {
				Util.showAlert(AlertType.ERROR,
						"Permission denied",
						"You don't have permission to upload to this path!",
						((PacketPermissionDenied) packet).getMessage());
				return;
			}

			if (!(packet instanceof PacketOperationSuccessful)) {
				Util.showWrongPacketReceivedAlert(PacketOperationSuccessful.class, packet);
				return;
			}

			byte[] fileContent = Files.readAllBytes(uploadFile.getAbsoluteFile().toPath());
			connection.writePacket(new PacketTransmitFile(fileContent, StandardCharsets.UTF_8));

			connection.downCounter();

			packet = connection.pollQueue();

			if (packet instanceof PacketWriteException) {
				final PacketWriteException finalPacket = (PacketWriteException) packet;
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("An error happened trying to upload a file.");

					Text contentText = new Text(finalPacket.getMessage());
					contentText.setFont(Font.font("monospaced"));
					alert.getDialogPane().setContent(contentText);

					alert.show();
				});
				return;
			}

			if (!(packet instanceof PacketOperationSuccessful)) {
				Util.showWrongPacketReceivedAlert(PacketOperationSuccessful.class, packet);
			} else {
				Util.showAlert(AlertType.INFORMATION,
						"Uploaded file",
						"Wrote \n\t'" + targetPath.toString() + "'",
						"Wrote \n\t'" + uploadFile.getAbsolutePath() + "'\nto\n\t'" + targetPath.toString() + "'");
			}

		} catch (InterruptedException | IOException e) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("An error happened trying to upload a file.");
				alert.setContentText(e.getMessage());
				TextArea textArea = new TextArea(Util.getStackTrace(e));
				textArea.setFont(Font.font("monospaced"));
				alert.getDialogPane().setExpandableContent(textArea);
				alert.show();
			});
			e.printStackTrace();
		}
	}

	private boolean ensureIndexIsSelected() {
		if (treeView.getSelectionModel().getSelectedItem() == null) {
			Util.showAlert(AlertType.ERROR,
					"User error",
					"Please select an item in the left file tree first",
					"");
			return false;
		}
		return true;
	}

	@FXML
	void onExit(ActionEvent event) {
		try {
			JavaFxMain.getInstance().getServerConnection().writePacket(new PacketEndConnection());
		} catch (Exception e) {
			// just for me. Shouldn't affect the program in any way
			e.printStackTrace();
		}
		System.exit(0);
	}
}
