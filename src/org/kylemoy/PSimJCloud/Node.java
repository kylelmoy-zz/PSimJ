package org.kylemoy.PSimJCloud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import org.kylemoy.PSimJ.Communicator;
import org.kylemoy.PSimJ.NetworkCommunicator;
import org.kylemoy.PSimJ.PSimJClassLoader;
import org.kylemoy.PSimJ.PSimJRunnable;
import org.kylemoy.PSimJ.SerializeableClassDefinition;
import org.kylemoy.PSimJ.Topology;

public class Node {
	public static void main(String[] args) throws Exception {
		String hostAddress = args[0];
		int hostPort = Integer.parseInt(args[1]);
		String keyFile = args[2];
		
		// Not real authentication please fix me
		byte[] key = Files.readAllBytes(Paths.get(keyFile));
		
		// Try connecting to host
		while (true) {
			System.out.println("Attempting to connect to " + hostAddress + ".");
			while (true) {
				try {
					connect(hostAddress, hostPort, key);
				} catch (UnknownHostException e) {
					System.err.println("Unable to resolve host.");
				} catch (IOException e) {
					// Do nothing, try again later.
				}
				try {
					Thread.sleep(1000);
				} catch (Exception s) {}
			}
		}
	}
	
	public static void connect(String hostAddress, int hostPort, byte[] key) throws UnknownHostException, IOException, NotBoundException {
		Socket socket = new Socket(hostAddress, hostPort);
		DataInputStream is = new DataInputStream(socket.getInputStream());
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		System.out.println("Connection to " + hostAddress + " made.");
		
		// Submit key for "auth"
		os.write(key);
		
		try {
			while (true) {
				System.out.println("Waiting for work.");

				// Wait for directives from pool
				int rank;
				while (true) {
					rank = is.readInt();
					if (rank > 0) {
						// It's go time!
						break;
					} else if (rank < 0) {
						// Quit
						return;
					}
				}
				
				try {
					// Receive ips of all nodes
					List<String> ipList = new ArrayList<String>();
					int size = is.readInt();
					for (int i = 0; i < size; i++) {
						int bufSize = is.readInt();
						byte[] buf = new byte[bufSize];
						is.readFully(buf);
						ipList.add(new String(buf));
					}
					
					// Initialize communications with all nodes
					Communicator comm = new NetworkCommunicator(ipList, rank, new Topology.Switch());
					
					// Receive class definition from node 0
					SerializeableClassDefinition cls = null;
					cls = comm.one2all_broadcast(0, cls, SerializeableClassDefinition.class);
					
					// Build class from class definition
					PSimJClassLoader classLoader = new PSimJClassLoader(PSimJClassLoader.class.getClassLoader());
					Class type = classLoader.loadClass(cls);
					
					// Instantiate class
					PSimJRunnable instance = (PSimJRunnable) type.newInstance();
					
					// Go go go!
					instance.run(comm);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("Connection to " + hostAddress + " lost.");
			socket.close();
		}
	}	
}
