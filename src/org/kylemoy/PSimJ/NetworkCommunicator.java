package org.kylemoy.PSimJ;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import org.kylemoy.PSimJCloud.Node;
import org.kylemoy.PSimJCloud.NodeHandle;

public class NetworkCommunicator implements Communicator {
	public static final int ALL_NODES = Integer.MIN_VALUE;
	public static final int PORT_RANGE_START = 8195;
	private NodeHandle[] nodes;
	private int port;
	private int rank;
	private int nprocs;
	private Topology topology;
	public static void init(String poolHost, int poolPort, int n, Topology topology, Class<? extends PSimJRMIServer> type) throws IOException, NotBoundException {
		
		//Connect to pool host
		NodeHandle host = new NodeHandle(poolHost, poolPort);
		while (!host.isReady()) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
		System.out.println("Connected to pool.");
		
		//Request n nodes
		host.os.writeInt(n);
		
		int rank = host.is.readInt();
		if (rank != 0) {
			System.err.println("Insufficient nodes for pool size " + host);
			return;
		}

		System.out.println("Pool accepted request.");
		//Receive list of node ips from pool
		List<String> ipList = new ArrayList<String>();
		int size = host.is.readInt();
		for (int i = 0; i < size; i++) {
			int bufSize = host.is.readInt();
			byte[] buf = new byte[bufSize];
			host.is.readFully(buf);
			ipList.add(new String(buf));
		}

		System.out.println("Creating communicator.");
		//Create communicator
		Communicator comm = new NetworkCommunicator(ipList, rank, topology);
		
		//Build RMI address
		String rmiAddress = "rmi://" + ipList.get(0) + "/" + type.getName();

		System.out.println("Broadcasting.");
		//Broadcast to all nodes
		rmiAddress = comm.one2all_broadcast(0, rmiAddress, String.class);
		
		System.out.println(rmiAddress);
		
		System.out.println("Done!");
	}
	public NetworkCommunicator(List<String> ips, int rank, Topology topology) {
		nodes = new NodeHandle[ips.size()];
		this.port = PORT_RANGE_START + rank;
		this.rank = rank;
		nprocs = ips.size();
		this.topology = topology;
		
		//Create dummy connection with self
		nodes[rank] = new NodeHandle();
		
		//Initiate connections with all ranks below me
		for (int i = rank + 1; i < ips.size(); i++) {
			nodes[i] = new NodeHandle(ips.get(i), PORT_RANGE_START + i, rank);
		}
		
		//Accept connections from all ranks above me
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(500);
			// Listen for new nodes
			while (!isReady()) {
				try {
					Socket nodeSocket = serverSocket.accept();
					NodeHandle node = new NodeHandle(nodeSocket);
					int nRank = node.is.readInt();
					nodes[nRank] = node;
					System.out.println("Successfully connected to node " + nRank);
				} catch (IOException e) {
					// Timed out, try again
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}

	public boolean isReady() {
		for (NodeHandle node : nodes) {
			if (node == null || !node.isReady()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int rank() {
		return rank;
	}

	@Override
	public int nprocs() {
		return nprocs;
	}

	@Override
	public boolean topology(int i, int j) {
		return topology.valid(i, j);
	}

	@Override
	public void send(int dest, Serializable data) {
		if (!topology(rank, dest)) {
			throw new Error("Topology violation! " + rank + " cannot send to " + dest);
		}
		try {
			ObjectOutputStream o = new ObjectOutputStream(nodes[dest].os);
			o.writeObject(data);
			o.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T extends Serializable> T recv(int source, Class<T> type) {
		if (!topology(source, rank)) {
			throw new Error("Topology violation! " + rank + " cannot recv from " + source);
		}
		try {
			ObjectInputStream i = new ObjectInputStream(nodes[source].is);
			Object obj = i.readObject();
			if (type.isInstance(obj)) {
				//lol type safety
				return (T) obj;
			} else {
				throw new ClassNotFoundException("Object received was not of type " + type.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public <T extends Serializable> T one2all_broadcast(int source, T data, Class<T> type) {
		if (rank() == source) {
			for (int i = 0; i < nprocs(); i++) {
				if (i != rank()) {
					send(i, data);
				}
			}
		} else {
			return recv(source, type);
		}
		return data;
	}

	@Override
	public <T extends Serializable> List<T> all2one_collect(int dest, T data, Class<T> type) {
		if (rank() != dest) {
			send(dest, data);
		} else {
			List<T> list = new ArrayList<T>();
			for (int i = 0; i < nprocs(); i++) {
				if (i != rank()) {
					list.add(recv(i, type));
				} else {
					list.add(data);
				}
			}
			return list;
		}
		return null;
	}

	@Override
	public <T extends Serializable> List<T> all2all_broadcast(T data, Class<T> type) {
		for (int i = 0; i < nprocs(); i++) {
			if (i != rank()) {
				send(i, data);
			}
		}
		List<T> list = new ArrayList<T>();
		for (int i = 0; i < nprocs(); i++) {
			if (i != rank()) {
				list.add(recv(i, type));
			} else {
				list.add(data);
			}
		}
		return list;
	}

}
