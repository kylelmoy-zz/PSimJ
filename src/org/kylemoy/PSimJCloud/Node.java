package org.kylemoy.PSimJCloud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import org.kylemoy.PSimJ.Communicator;
import org.kylemoy.PSimJ.NetworkCommunicator;
import org.kylemoy.PSimJ.PSimJRMIServer;
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

				// Wait for go time!
				int rank;
				while (true) {
					rank = is.readInt();
					if (rank > 0) {
						// IT'S GO TIME WOOOO
						break;
					} else if (rank < 0) {
						// Time to die :c
						return;
					}
				}

				// Receive ips of all nodes
				List<String> ipList = new ArrayList<String>();
				int size = is.readInt();
				for (int i = 0; i < size; i++) {
					int bufSize = is.readInt();
					byte[] buf = new byte[bufSize];
					is.readFully(buf);
					ipList.add(new String(buf));
				}
				
				System.out.println("I am rank " + rank);

				Communicator comm = new NetworkCommunicator(ipList, rank, new Topology.Switch());
				
				//Build RMI address
				String rmiAddress = "";
				
				//Broadcast to all nodes
				rmiAddress = comm.one2all_broadcast(0, rmiAddress, String.class);
				
				System.out.println(rmiAddress);
				
				System.out.println("Done!");
				/*
				// Receive RMI server name from host
				byte[] buf = new byte[size];
				is.readFully(buf);
				String rmiName = new String(buf);
				
				// Receive ips of other nodes
				List<String> ipList = new ArrayList<String>();
				size = is.readInt();
				for (int i = 0; i < size; i++) {
					int bufSize = is.readInt();
					buf = new byte[bufSize];
					is.readFully(buf);
					ipList.add(new String(buf));
				}
				
				// Get this node's rank, although technically this could be determined from the node list
				int rank = is.readInt();
				
				// Instantiate remote class via literal magic
				PSimJRMIServer rmiClass = (PSimJRMIServer)Naming.lookup(rmiName);

				// Instantiate communicator and establish links to other nodes
				Communicator comm = new NetworkCommunicator(ipList, hostPort, rank);
				
				// Execute parallel task
				rmiClass.run(comm);
				// Done
				*/
			}
		} catch (IOException e) {
			System.out.println("Connection to " + hostAddress + " lost.");
			socket.close();
		}
	}	
}
