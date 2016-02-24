package org.kylemoy.JPSim;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public interface JPSim {
	
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
	boolean topology();
	
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
	 * @param source
	 * @param data
	 * @return
	 */
	<T extends Serializable> T one2all_broadcast(int source, T data, Class<T> type);
	
	/**
	 * @param dest
	 * @param data
	 * @return
	 */
	<T extends Serializable> List<T> all2one_collect(int dest, T data, Class<T> type);
	
	/**
	 * @param data
	 * @return
	 */
	<T extends Serializable> List<T> all2all_broadcast(T data, Class<T> type);
	//all2one_reduce
	//all2all_reduce
	
	public class JPSimRunnable {
		void run(JPSim communicator){
			System.out.println("Forget something?");
		}
	}
}
