package psimj;

/**
 * A simple interface for code to be parallelized
 * 
 * @author Kyle Moy
 *
 */
public interface Task {
	/**
	 * Executes the code associated with this task
	 * @param comm the Communicator to be passed
	 */
	void run(Communicator comm);
}