package me.ialistannen.fupclient.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import me.ialistannen.fupclient.javafx.logic.ServerConnection;
import me.ialistannen.fupclient.javafx.view.login.LoginWindowController;

/**
 * The JavaFx Gui main
 */
public class JavaFxMain extends Application {

	private static JavaFxMain instance;

	private Stage primaryStage;

	private volatile ServerConnection serverConnection;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		instance = this;

		FXMLLoader loader = new FXMLLoader(LoginWindowController.class.getResource("LoginWindow.fxml"));
		Pane pane = loader.load();

		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("File uploader plugin - Client");

		primaryStage.show();
	}

	/**
	 * @param serverConnection The new server connection. Null is permitted.
	 */
	public synchronized void setServerConnection(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	/**
	 * @return The current server connection. May be null
	 */
	public synchronized ServerConnection getServerConnection() {
		return serverConnection;
	}

	/**
	 * @return The primary stage
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * @return The Instance
	 */
	public static JavaFxMain getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
