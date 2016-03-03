package org.kylemoy.PSimJ;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kylemoy.PSimJ.PSimJRMIServer.ParallelTask;

/**
 *
 */
public class LocalCommunicator implements Communicator {
	
	/**
	 * Creates and runs simulated parallel processes
	 * @param n the number of parallel processes
	 * @param topology the simulated network topology of the system
	 * @param type the Java class containing the code to be run
	 */
	public static void init(int n, Topology topology, Class<? extends PSimJRMIServer> type) {
		PipedOutputStream[][] os;
		PipedInputStream[][] is;

		//Instantiate n objects of Type type
		List<PSimJRMIServer> procs = new ArrayList<PSimJRMIServer>();
		for (int i = 0; i < n; i++) {
			try {
				procs.add(type.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//Create communication pipes
		try {
			os = new PipedOutputStream[n][n];
			is = new PipedInputStream[n][n];
			
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					//Assert topology
					if (topology.valid(i, j)) {
						//Create pipes between valid nodes
						os[i][j] = new PipedOutputStream();
						is[j][i] = new PipedInputStream(os[i][j]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//Run type objects on separate threads
		ExecutorService executor = Executors.newFixedThreadPool(n);
		for (int i = 0; i < n; i++) {
			PSimJRMIServer runnable = procs.get(i);
			LocalCommunicator comm = new LocalCommunicator(i, n, topology, is[i], os[i]);
			executor.submit(new java.lang.Runnable() {
	            @Override
	            public void run() {
	            	try {
						runnable.run(comm);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
	            }
	        });
		}
		executor.shutdown();
		
		//Wait for completion
		while (!executor.isTerminated()) {
			try{ Thread.sleep(1000); } catch (Exception e) {}
		}
	}
	
	private PipedInputStream[] is;
	private PipedOutputStream[] os;
	private int rank;
	private int nprocs;
	private Topology topology;
	private LocalCommunicator(int r, int n, Topology t, PipedInputStream[] i, PipedOutputStream[] o) {
		rank = r;
		nprocs = n;
		is = i;
		os = o;
		topology = t;
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
		if (topology(rank, dest)) {
			throw new Error("Topology violation! " + rank + " cannot send to " + dest);
		}
		try {
			ObjectOutputStream o = new ObjectOutputStream(os[dest]);
			o.writeObject(data);
			o.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T recv(int source, Class<T> type) {
		if (topology(source, rank)) {
			throw new Error("Topology violation! " + rank + " cannot recv from " + source);
		}
		try {
			ObjectInputStream i = new ObjectInputStream(is[source]);
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
