package psimj;

import java.io.Serializable;
import java.util.List;

import psimj.Topology.TopologyViolationException;

/**
 *
 */
public interface Communicator {
	/**
	 * @return the rank of this node
	 */
	int rank();

	/**
	 * @return the total number of nodes
	 */
	int nprocs();

	/**
	 * @return ???
	 */
	boolean topology(int i, int j);

	/**
	 * Runs a task, distributing code if necessary
	 * 
	 * @param task
	 */
	void runTask(Class<? extends Task> task);

	/**
	 * Sends data to another node.
	 * 
	 * @param dest
	 *            the destination node rank
	 * @throws TopologyViolationException
	 */
	void send(int dest, Serializable data) throws TopologyViolationException;

	/**
	 * Receives data from a source node.
	 * 
	 * @param source
	 *            the source node rank
	 * @return the data received
	 * @throws TopologyViolationException
	 */
	<T extends Serializable> T recv(int source, Class<T> type) throws TopologyViolationException;

	/**
	 * Broadcast data from a source node to all nodes
	 * 
	 * @param source
	 * @param data
	 * @return
	 * @throws TopologyViolationException
	 */
	<T extends Serializable> T one2all_broadcast(int source, T data, Class<T> type) throws TopologyViolationException;

	/**
	 * Broadcast data from all nodes to a destination node
	 * 
	 * @param dest
	 * @param data
	 * @return
	 * @throws TopologyViolationException
	 */
	<T extends Serializable> List<T> all2one_collect(int dest, T data, Class<T> type) throws TopologyViolationException;

	/**
	 * Broadcast data from all nodes to all other nodes
	 * 
	 * @param data
	 * @return
	 * @throws TopologyViolationException
	 */
	<T extends Serializable> List<T> all2all_broadcast(T data, Class<T> type) throws TopologyViolationException;

	/**
	 * Close/finalize all communications
	 */
	void close();
}
