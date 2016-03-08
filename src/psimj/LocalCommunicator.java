package psimj;

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

import psimj.Topology.TopologyViolationException;
import psimj.network.NodeSocket;

/**
 * A Communicator for simulating communications across multiple machines by
 * using multithreading
 * 
 * @author Kyle Moy
 *
 */
public class LocalCommunicator implements Communicator {

	private static LocalCommunicator[] instances;
	private PipedInputStream[] is;
	private PipedOutputStream[] os;
	private int rank;
	private int nprocs;
	private Topology topology;

	/**
	 * Constructs n LocalCommunicators that simulate the specified network
	 * topology
	 * 
	 * @param numProcs
	 * @param topology
	 */
	public LocalCommunicator(int numProcs, Topology topology) {
		rank = 0;
		nprocs = numProcs;
		this.topology = topology;

		PipedOutputStream[][] outputStreams;
		PipedInputStream[][] inputStreams;

		// Create communication pipes
		try {
			outputStreams = new PipedOutputStream[numProcs][numProcs];
			inputStreams = new PipedInputStream[numProcs][numProcs];
			for (int i = 0; i < numProcs; i++) {
				for (int j = 0; j < numProcs; j++) {
					// Assert topology
					if (topology.valid(i, j)) {
						// Create pipes between valid nodes
						outputStreams[i][j] = new PipedOutputStream();
						inputStreams[j][i] = new PipedInputStream(outputStreams[i][j]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		instances = new LocalCommunicator[numProcs];
		is = inputStreams[0];
		os = outputStreams[0];
		instances[0] = this;
		for (int i = 0; i < numProcs; i++) {
			instances[i] = new LocalCommunicator(i, numProcs, topology, inputStreams[i], outputStreams[i]);
		}
	}

	/**
	 * Constructs a LocalCommunicator with the specified topology and
	 * connections
	 * 
	 * @param r
	 *            the rank of this Communicator
	 * @param n
	 *            the total number of connected Communicators
	 * @param t
	 *            the topology to simulate
	 * @param i
	 *            the InputStreams from all other connected Communicators
	 * @param o
	 *            the OutputStreams to all other connected Communicators
	 */
	private LocalCommunicator(int r, int n, Topology t, PipedInputStream[] i, PipedOutputStream[] o) {
		rank = r;
		nprocs = n;
		is = i;
		os = o;
		topology = t;
	}

	@Override
	public void runTask(final Class<? extends Task> task) {
		// Run task objects on separate threads
		ExecutorService executor = Executors.newFixedThreadPool(nprocs());
		
		for (int i = 0; i < nprocs(); i++) {
			final int j = i;
			executor.submit(new java.lang.Runnable() {
				@Override
				public void run() {
					try {
						task.newInstance().run(instances[j]);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			});
		}
		executor.shutdown();
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
	public void send(int dest, Serializable data) throws TopologyViolationException {
		if (!topology(rank, dest)) {
			throw new TopologyViolationException("Topology violation! " + rank + " cannot send to " + dest);
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
	public <T extends Serializable> T recv(int source, Class<T> type) throws TopologyViolationException {
		if (!topology(source, rank)) {
			throw new TopologyViolationException("Topology violation! " + rank + " cannot recv from " + source);
		}

		if (type.isPrimitive()) {
			type = PrimitiveBoxer.get(type);
		}

		try {
			ObjectInputStream i = new ObjectInputStream(is[source]);
			Object obj = i.readObject();
			if (type.isInstance(obj)) {
				// lol type safety
				return (T) obj;
			} else {
				throw new ClassNotFoundException("Object received was not of type " + type.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T one2all_broadcast(int source, Serializable data, Class<T> type)
			throws TopologyViolationException {
		if (rank() == source) {
			for (int i = 0; i < nprocs(); i++) {
				if (i != rank()) {
					send(i, data);
				}
			}
		} else {
			return recv(source, type);
		}
		return (T) data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> List<T> all2one_collect(int dest, Serializable data, Class<T> type)
			throws TopologyViolationException {
		if (rank() != dest) {
			send(dest, data);
		} else {
			List<T> list = new ArrayList<T>();
			for (int i = 0; i < nprocs(); i++) {
				if (i != rank()) {
					list.add(recv(i, type));
				} else {
					list.add((T) data);
				}
			}
			return list;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> List<T> all2all_broadcast(Serializable data, Class<T> type)
			throws TopologyViolationException {
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
				list.add((T) data);
			}
		}
		return list;
	}

	@Override
	public void finish() {
		is = null;
		os = null;
	}
	
	@Override
	public void close() {
		is = null;
		os = null;
	}
}
