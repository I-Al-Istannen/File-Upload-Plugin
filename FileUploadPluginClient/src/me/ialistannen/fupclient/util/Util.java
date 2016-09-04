package me.ialistannen.fupclient.util;

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
}
