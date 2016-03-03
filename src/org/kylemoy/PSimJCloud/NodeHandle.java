package org.kylemoy.PSimJCloud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class NodeHandle implements Runnable {
	public Socket socket;
	public DataOutputStream os;
	public DataInputStream is;
	
	public String ip;
	public int port;
	public int rank = -1;
	public NodeHandle() {
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
	
	public NodeHandle(Socket socket) throws IOException {
		this.socket = socket;
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());
		
		ip = socket.getInetAddress().getHostAddress();
	}
	
	public NodeHandle(String ip, int port) {
		this.ip = ip;
		this.port = port;
		(new Thread(this)).start();
	}
	
	public NodeHandle(String ip, int port, int rank) {
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
	
	public boolean isReady() {
		return os != null;
	}
}