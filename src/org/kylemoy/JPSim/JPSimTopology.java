package org.kylemoy.JPSim;

public interface JPSimTopology {
	boolean valid(int i, int j);
	
	public static class Default implements JPSimTopology {
		@Override
		public boolean valid(int i, int j) {
			return true;
		}
	}
	
	public static class Bus implements JPSimTopology {
		@Override
		public boolean valid(int i, int j) {
			return true;
		}
	}
	
	public static class Switch implements JPSimTopology {
		@Override
		public boolean valid(int i, int j) {
			return true;
		}
	}
	
	public static class Tree implements JPSimTopology {
		@Override
		public boolean valid(int i, int j) {
			return i==(int)((j-1)/2) || j==(int)((i-1)/2);
		}
	}
}
