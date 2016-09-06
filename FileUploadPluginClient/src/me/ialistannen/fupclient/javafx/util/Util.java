package me.ialistannen.fupclient.javafx.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import me.ialistannen.fupclient.network.packets.Packet;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Some static Util functions
 */
public class Util {

	/**
	 * Returns the stacktrace of a throwable
	 *
	 * @param throwable The throwable
	 *
	 * @return The Stacktrace
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);

		throwable.printStackTrace(writer);
		writer.close();

		return stringWriter.toString();
	}

	/**
	 * Shows a dialog that states it got a wrong packet
	 *
	 * @param expected The expected packet
	 * @param actual   The actual packet
	 */
	public static void showWrongPacketReceivedAlert(Class<? extends Packet> expected, Packet actual) {
		showAlert(AlertType.ERROR,
				"Wrong packet received",
				"Received a packet of wrong type.",
				"Expected " + expected.getSimpleName() + " got "
						+ (actual == null ? "null" : actual.getClass().getSimpleName()));
	}

	/**
	 * Shows a dialog. Can be called from any thread.
	 *
	 * @param type        The type of th alert
	 * @param title       The title of the alert
	 * @param header      The header text
	 * @param contentText The content text
	 */
	public static void showAlert(AlertType type, String title, String header, String contentText) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> {
				showAlert(type, title, header, contentText);
			});
			return;
		}
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(contentText);
		alert.show();
	}
}
