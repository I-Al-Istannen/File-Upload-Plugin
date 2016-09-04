package me.ialistannen.fileuploaderplugin.util;

import me.ialistannen.fileuploaderplugin.FileUploaderPlugin;

/**
 * Some static Utility functions
 */
public class Util {

	/**
	 * Translates a string
	 *
	 * @param key     The key
	 * @param objects The Formatting objects
	 *
	 * @return The translated string
	 */
	public static String tr(String key, Object... objects) {
		return FileUploaderPlugin.getInstance().getLanguage().tr(key, objects);
	}
}
