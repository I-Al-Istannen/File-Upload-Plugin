package me.ialistannen.fupclient.javafx.view.login;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import me.ialistannen.fupclient.javafx.JavaFxMain;
import me.ialistannen.fupclient.javafx.logic.ServerConnection;
import me.ialistannen.fupclient.javafx.logic.ServerConnection.State;
import me.ialistannen.fupclient.javafx.view.main.MainActionChooseWindowController;
import me.ialistannen.fupclient.model.Token;

import java.io.IOException;

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

			ServerConnection connection = new ServerConnection(hostName, port, token);
			JavaFxMain.getInstance().setServerConnection(connection);
			connection.authenticate();

			if (connection.getState() == State.INVALID_TOKEN) {
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.ERROR);
					alert.initOwner(JavaFxMain.getInstance().getPrimaryStage());
					alert.setTitle("Invalid token");
					alert.setHeaderText("The token is invalid");
					alert.setContentText("The token you provided is expired or not valid at all. Same outcome.");
					alert.show();
				});
			} else if (connection.getState() == State.AUTHENTICATED) {
				connection.createToken();
				showMainActionWindow(connection.getToken());
			}
		}).start();
	}

	private void showMainActionWindow(Token token) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> showMainActionWindow(token));
			return;
		}
		try {
			FXMLLoader loader = new FXMLLoader(
					MainActionChooseWindowController.class
							.getResource("MainActionChooseWindow.fxml")
			);
			Pane pane = loader.load();

			MainActionChooseWindowController controller = loader.getController();
			controller.setToken(token);

			Scene scene = new Scene(pane);
			JavaFxMain.getInstance().getPrimaryStage().hide();
			JavaFxMain.getInstance().getPrimaryStage().setScene(scene);
			JavaFxMain.getInstance().getPrimaryStage().show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
