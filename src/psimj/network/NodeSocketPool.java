package psimj.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NodeSocketPool implements java.lang.Runnable {
	private final int port;
	private final byte[] key;
	
	private boolean run;
	private List<NodeSocket> nodePool;
	private Thread thread;
	private ServerSocket serverSocket;
	
	public NodeSocketPool(int port, byte[] key) {
		this.key = key;
		this.port = port;
		nodePool = new ArrayList<NodeSocket>();
	}
	
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			run = true;
			
			//This creates a new thread to call heartbeat() every 500 ms
			(new Thread() { public void run() { while(run){heartbeat(); try{Thread.sleep(500);}catch(Exception e){}}}}).start();
			
			// Listen for new nodes
			while (run) {
				try {
					Socket nodeSocket = serverSocket.accept();
					NodeSocket node = new NodeSocket(nodeSocket);
					
					// Get key from node
					node.socket.setSoTimeout(1000);
					byte[] readKey = new byte[key.length];
					node.is.read(readKey);
					node.socket.setSoTimeout(0);
					
					// Verify key before adding to pool
					if (verifyKey(readKey)) {
						nodePool.add(node);
					} else {
						node.socket.close();
					}
				} catch (IOException e) {
					// Timed out, try again
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			// Host port already in use, probably
			e.printStackTrace();
		}
	}
	
	public List<NodeSocket> getNodes() {
		heartbeat();
		return nodePool;
	}
	
	public void start() {
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void stop() {
		try {
			run = false;
			serverSocket.close();
			
			// Wait for closure
			thread.join();
		} catch (Exception e) {}
	}

	private boolean verifyKey(byte[] test) {
		if (key.length != test.length) {
			return false;
		}
		for (int i = 0; i < key.length; i++) {
			if (key[i] != test[i]) {
				return false;
			}
		}
		return true;
	}
	
	private void heartbeat() {
		synchronized (nodePool) {
			if (nodePool.size() > 0) {
				for (int i = 0; i < nodePool.size(); i++) {
					NodeSocket node = nodePool.get(i);
					try {
						new DataOutputStream(node.os).writeInt(0);
					} catch (Exception e) {
						nodePool.remove(i--);
					}
				}
			}
		}
	}
	
}