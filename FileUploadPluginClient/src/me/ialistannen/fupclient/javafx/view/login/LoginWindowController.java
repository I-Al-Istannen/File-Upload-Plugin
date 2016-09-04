package me.ialistannen.fupclient.javafx.view.login;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import me.ialistannen.fupclient.javafx.JavaFxMain;
import me.ialistannen.fupclient.util.Util;

import java.io.IOException;
import java.net.Socket;

/**
 * The controller for the Login window
 */
public class LoginWindowController {

	@FXML
	private TextField tokenTextField;

	@FXML
	private TextField hostTextField;

	@FXML
	private TextField portTextField;

	@FXML
	private void initialize() {
		TextFormatter<Integer> portFormatter = new TextFormatter<>(new StringConverter<Integer>() {
			@Override
			public String toString(Integer object) {
				return Integer.toString(object);
			}

			@Override
			public Integer fromString(String string) {
				return Integer.parseInt(string);
			}
		}, 10000, change -> {
			String newText = change.getControlNewText();

			try {
				int port = Integer.parseInt(newText);
				if (port < 0 || port > 65535) {
					return null;
				}
			} catch (NumberFormatException e) {
				return null;
			}
			return change;
		});
		portTextField.setTextFormatter(portFormatter);
	}


	@FXML
	void onExit(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	void onLogin(ActionEvent event) {
		String token = tokenTextField.getText();

		if (token.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(JavaFxMain.getInstance().getPrimaryStage());
			alert.setTitle("No token entered");
			alert.setHeaderText("Invalid token");
			alert.setContentText("Please enter a token.");
			alert.show();
			return;
		}

		String hostName = hostTextField.getText();

		if (hostName.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(JavaFxMain.getInstance().getPrimaryStage());
			alert.setTitle("No hostname entered");
			alert.setHeaderText("Invalid host");
			alert.setContentText("Please enter a hostname.");
			alert.show();
			return;
		}

		String portString = portTextField.getText();

		if (portString.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(JavaFxMain.getInstance().getPrimaryStage());
			alert.setTitle("No port entered");
			alert.setHeaderText("Invalid port");
			alert.setContentText("Please enter a port.");
			alert.show();
			return;
		}

		int port = Integer.parseInt(portString);

		new Thread(() -> {
			try (Socket socket = new Socket(hostName, port)) {
				socket.setSoTimeout(10000);
				boolean validToken = false;     // TODO: This!
				if (!validToken) {
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.ERROR);
						alert.initOwner(JavaFxMain.getInstance().getPrimaryStage());
						alert.setTitle("Invalid token");
						alert.setHeaderText("The token is invalid");
						alert.setContentText("The token you provided is expired or not valid at all. Same outcome.");
						alert.show();
					});
				} else {
					// Valid token
				}
			} catch (IOException e) {
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.ERROR);
					String expandable = Util.getStackTrace(e);
					TextArea textArea = new TextArea(expandable);
					textArea.setFont(Font.font("monospaced"));
					alert.getDialogPane().setExpandableContent(textArea);

					alert.setTitle("Exception while connecting");
					alert.setHeaderText("An error occurred while trying to connect to the host.");
					alert.setContentText("Please see the expandable content for a more detailed error message.");
					alert.show();
				});
			}
		}).start();
	}

}
