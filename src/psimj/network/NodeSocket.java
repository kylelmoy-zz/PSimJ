package psimj.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Wrapper for sockets.
 * 
 * Holds miscellaneous information about connected machine. Provides different
 * connection methods.
 * 
 * @author Kyle Moy
 *
 */
public class NodeSocket implements Runnable {
	public Socket socket;
	public DataOutputStream os;
	public DataInputStream is;

	public String ip;
	public int port;
	public final int rank;
	
	public String osName;
	public int cores;

	/**
	 * Localhost connection constructor
	 * 
	 * @throws IOException
	 */
	public NodeSocket() throws IOException {
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);
		is = new DataInputStream(pis);
		os = new DataOutputStream(pos);
		ip = "localhost";
		rank = -1;
	}

	/**
	 * Constructor for wrapping a connected Socket
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public NodeSocket(Socket socket) throws IOException {
		this.socket = socket;
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());
		ip = socket.getInetAddress().getHostAddress();
		rank = -1;
	}

	/**
	 * Constructor for connection attempt on a separate thread, with rank label
	 * 
	 * @param ip
	 * @param port
	 * @param rank
	 */
	private NodeSocket(String ip, int port, int rank) {
		this.ip = ip;
		this.port = port;
		this.rank = rank;
	}

	/**
	 * Open a new socket, block while connecting
	 * 
	 * @param ip
	 * @param port
	 * @param rank
	 * @return
	 */
	public static NodeSocket openNow(String ip, int port, int rank) {
		NodeSocket node = new NodeSocket(ip, port, rank);
		node.run();
		return node;
	}

	public static NodeSocket openNow(String ip, int port) {
		return openNow(ip, port, -1);
	}

	/**
	 * Open a new socket, return immediately
	 * 
	 * @param ip
	 * @param port
	 * @param rank
	 * @return
	 */
	public static NodeSocket openLater(String ip, int port, int rank) {
		NodeSocket node = new NodeSocket(ip, port, rank);
		(new Thread(node)).start();
		return node;
	}

	public static NodeSocket openLater(String ip, int port) {
		return openLater(ip, port, -1);
	}

	@Override
	public void run() {
		if (ip == null || socket != null) {
			return;
		}
		try {
			socket = new Socket(ip, port);
			os = new DataOutputStream(socket.getOutputStream());
			is = new DataInputStream(socket.getInputStream());
			ip = socket.getInetAddress().getHostAddress();
			if (rank != -1) {
				os.writeInt(rank);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check that a connection is available
	 * 
	 * @return
	 */
	public boolean isReady() {
		return os != null;
	}

	/**
	 * Close connection
	 */
	public void close() {
		try {
			if (socket != null)
				socket.close();
			socket = null;
			is = null;
			os = null;
		} catch (IOException e) {
		}
	}
}