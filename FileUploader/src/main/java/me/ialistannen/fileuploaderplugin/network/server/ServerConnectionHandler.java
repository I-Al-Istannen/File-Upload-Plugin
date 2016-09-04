package me.ialistannen.fileuploaderplugin.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listens for connections and accepts them
 */
public class ServerConnectionHandler {

	private int port;
	private volatile AtomicBoolean started = new AtomicBoolean(false);

	private ListenerThread listener;

	/**
	 * Creates the handler.
	 * <p>
	 * You must invoke {@link #start()} yourself!
	 *
	 * @param port The port to listen on
	 */
	public ServerConnectionHandler(int port) {
		this.port = port;
	}

	/**
	 * Starts the listener, if it is running.
	 */
	public synchronized void start() {
		if (started.get()) {
			return;
		}
		started.set(true);

		createAndStartThread();
	}

	private void createAndStartThread() {
		// TODO: Make configurable
		listener = new ListenerThread(10000);
		listener.start();
	}

	/**
	 * Stops the listener, if it is currently running
	 */
	public synchronized void stop() {
		if (!started.get()) {
			return;
		}
		started.set(false);
		listener.cancel();
		listener = null;
	}

	/**
	 * Listens for client connections
	 */
	private class ListenerThread extends Thread {

		private ServerSocket serverSocket;
		private volatile AtomicBoolean running = new AtomicBoolean(true);
		private int timeout;

		private ExecutorService executorService;

		public ListenerThread(int timeout) {
			super("ServerSocketListener - FileUploaderPlugin");
			this.timeout = timeout;

			executorService = Executors.newFixedThreadPool(4);
		}

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(port);

				while (running.get()) {
					Socket clientConnection = serverSocket.accept();
					clientConnection.setSoTimeout(timeout);
					ClientServingRunnable runnable = new ClientServingRunnable(clientConnection, new NetHandler());
					new Thread(runnable, LocalDateTime.now().toString()).start();
				}

			} catch (SocketException ignore) {
				// Thrown by the cancel method.
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void cancel() {
			running.set(false);
			try {
				if(serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
