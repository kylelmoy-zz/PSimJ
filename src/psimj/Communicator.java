package psimj;

import java.io.Serializable;
import java.util.List;

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
	 * Sends data to another node.
	 * @param dest the destination node rank
	 */
	void send(int dest, Serializable data);
	
	/**
	 * Receives data from a source node.
	 * @param source the source node rank
	 * @return the data received
	 */
	<T extends Serializable> T recv(int source, Class<T> type);
	
	/**
	 * Broadcast data from a source node to all nodes
	 * @param source
	 * @param data
	 * @return
	 */
	<T extends Serializable> T one2all_broadcast(int source, T data, Class<T> type);
	
	/**
	 * Broadcast data from all nodes to a destination node
	 * @param dest
	 * @param data
	 * @return
	 */
	<T extends Serializable> List<T> all2one_collect(int dest, T data, Class<T> type);
	
	/**
	 * Broadcast data from all nodes to all other nodes
	 * @param data
	 * @return
	 */
	<T extends Serializable> List<T> all2all_broadcast(T data, Class<T> type);

	/**
	 * Close/finalize all communications
	 */
	void close();
}
