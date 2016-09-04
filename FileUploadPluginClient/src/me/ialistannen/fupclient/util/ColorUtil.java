package me.ialistannen.fupclient.util;

/**
 * Colors Strings for the terminal
 */
public class ColorUtil {

	/**
	 * Colors the text
	 *
	 * @param text  The text to color
	 * @param color The color
	 *
	 * @return The colored text
	 */
	public static String colorText(String text, TextColor color) {
		return color.getText() + text + TextColor.RESET.getText();
	}

	/**
	 * The text colors
	 */
	public enum TextColor {
		RESET("\u001B[0m"),
		BLACK("\u001B[30m"),
		RED("\u001B[31m"),
		GREEN("\u001B[32m"),
		YELLOW("\u001B[33m"),
		BLUE("\u001B[34m"),
		PURPLE("\u001B[35m"),
		CYAN("\u001B[36m"),
		WHITE("\u001B[37m");

		private String text;

		TextColor(String text) {
			this.text = text;
		}

		/**
		 * The color text
		 *
		 * @return The color text
		 */
		public String getText() {
			return text;
		}
	}
}
