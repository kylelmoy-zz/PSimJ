package org.kylemoy.JPSim;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class Local implements JPSim {
	
	/**
	 * Creates and runs simulated parallel processes
	 * @param n the number of parallel processes
	 * @param topology the simulated network topology of the system
	 * @param type the Java class containing the code to be run
	 */
	public static void instantiate(int n, JPSimTopology topology, Class<? extends JPSimRunnable> type) {
		PipedOutputStream[][] os;
		PipedInputStream[][] is;
		
		//Instantiate n objects of Type type
		List<JPSimRunnable> procs = new ArrayList<JPSimRunnable>();
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
			JPSimRunnable runnable = procs.get(i);
			Local comm = new Local(i, n, is[i], os[i]);
			executor.submit(new Runnable() {
	            @Override
	            public void run() {
	            	runnable.run(comm);
	            }
	        });
		}
		executor.shutdown();
	}
	
	private PipedInputStream[] is;
	private PipedOutputStream[] os;
	private int rank;
	private int nprocs;
	
	private Local(int r, int n, PipedInputStream[] _is, PipedOutputStream[] _os) {
		rank = r;
		nprocs = n;
		is = _is;
		os = _os;
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
	public boolean topology() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void send(int dest, Serializable data) {
		if (os[dest] == null) {
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
		if (is[source] == null) {
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
	public <T extends Serializable> T all2one_collect(int dest, T data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> T all2all_broadcast(T data) {
		// TODO Auto-generated method stub
		return null;
	}



}
