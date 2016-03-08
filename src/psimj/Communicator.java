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
	 * @return true if the specified topology would allow the specified nodes to
	 *         communicate
	 */
	boolean topology(int i, int j);

	/**
	 * Runs a task, distributing code if necessary
	 * 
	 * @param task
	 *            the Class of the task to be run
	 */
	void runTask(Class<? extends Task> task);

	/**
	 * Sends data to another node.
	 * 
	 * @param dest
	 *            the destination node rank
	 * @throws TopologyViolationException
	 *             if this operation would violate the network topology
	 */
	void send(int dest, Serializable data) throws TopologyViolationException;

	/**
	 * Receives data from a source node.
	 * 
	 * @param source
	 *            the source node rank
	 * @return the data received
	 * @throws TopologyViolationException
	 *             if this operation would violate the network topology
	 */
	<T extends Serializable> T recv(int source, Class<T> type) throws TopologyViolationException;

	/**
	 * Broadcast data from a source node to all nodes
	 * 
	 * @param source
	 *            the source node to send from
	 * @param data
	 *            the data Object to be sent
	 * @param type
	 *            the Class of the data
	 * @return the data Object
	 * @throws TopologyViolationException
	 *             if this operation would violate the network topology
	 */
	<T extends Serializable> T one2all_broadcast(int source, Serializable data, Class<T> type)
			throws TopologyViolationException;

	/**
	 * Broadcast data from all nodes to a destination node
	 * 
	 * @param dest
	 *            the destination node to send to
	 * @param data
	 *            the data Object to be sent
	 * @param type
	 *            the Class of the data
	 * @return a List of all Objects sent, if executed by the destination node
	 * @throws TopologyViolationException
	 *             if this operation would violate the network topology
	 */
	<T extends Serializable> List<T> all2one_collect(int dest, Serializable data, Class<T> type)
			throws TopologyViolationException;

	/**
	 * Broadcast data from all nodes to all other nodes
	 * 
	 * @param data
	 *            the data Object to be broadcast
	 * @param type
	 *            the Class of the data being broadcast
	 * @return a List of all Objects sent
	 * @throws TopologyViolationException
	 *             if this operation would violate the network topology
	 */
	<T extends Serializable> List<T> all2all_broadcast(Serializable data, Class<T> type)
			throws TopologyViolationException;

	/**
	 * Blocks until all nodes are ready, then closes communications.
	 */
	void finish();
	
	/**
	 * Forces connections to close.
	 */
	void close();
}
