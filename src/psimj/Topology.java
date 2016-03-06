package psimj;

/**
 * A class for asserting simulated network topologies
 * 
 * @author Kyle Moy
 *
 */
public interface Topology {
	/**
	 * Checks if the specified nodes could communicate in this topology
	 * 
	 * @param i
	 * @param j
	 * @return true if the nodes could communicate
	 */
	boolean valid(int i, int j);

	public static class Default implements Topology {
		@Override
		public boolean valid(int i, int j) {
			return true;
		}
	}

	public static class Bus implements Topology {
		@Override
		public boolean valid(int i, int j) {
			return true;
		}
	}

	public static class Switch implements Topology {
		@Override
		public boolean valid(int i, int j) {
			return true;
		}
	}

	public static class Tree implements Topology {
		@Override
		public boolean valid(int i, int j) {
			return i == (int) ((j - 1) / 2) || j == (int) ((i - 1) / 2);
		}
	}

	public static class TopologyViolationException extends Exception {
		private static final long serialVersionUID = -4632786047092150620L;

		public TopologyViolationException(String message) {
			super(message);
		}
	}
}
