package psimj;

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
 * Holds miscellaneous information about connected machine.
 * Provides different connection methods.
 * 
 * @author Kyle Moy
 *
 */
class NodeSocket implements Runnable {
	public Socket socket;
	public DataOutputStream os;
	public DataInputStream is;
	
	public String ip;
	public int port;
	public int rank = -1;
	
	/**
	 * Localhost connection constructor
	 */
	public NodeSocket() {
		try {
			PipedInputStream pis = new PipedInputStream();
			PipedOutputStream pos = new PipedOutputStream(pis);
			is = new DataInputStream(pis);
			os = new DataOutputStream(pos);
			
			ip = "localhost";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor for wrapping a connected Socket
	 * @param socket
	 * @throws IOException
	 */
	public NodeSocket(Socket socket) throws IOException {
		this.socket = socket;
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());
		
		ip = socket.getInetAddress().getHostAddress();
	}
	
	/**
	 * Constructor for connection attempt on a separate thread
	 * @param ip
	 * @param port
	 */
	public NodeSocket(String ip, int port) {
		this.ip = ip;
		this.port = port;
		(new Thread(this)).start();
	}
	
	/**
	 * Constructor for connection attempt on a separate thread, with rank label
	 * @param ip
	 * @param port
	 * @param rank
	 */
	public NodeSocket(String ip, int port, int rank) {
		this.ip = ip;
		this.port = port;
		this.rank = rank;
		(new Thread(this)).start();
	}
	
	@Override
	public void run() {
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
			if (socket != null) socket.close();
			socket = null;
			is = null;
			os = null;
		} catch (IOException e) {}
	}
}