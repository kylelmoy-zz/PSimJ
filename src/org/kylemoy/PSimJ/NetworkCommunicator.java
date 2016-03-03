package org.kylemoy.PSimJ;

import java.io.Serializable;
import java.util.List;

import org.kylemoy.PSimJCloud.NodeHandle;

public class NetworkCommunicator implements Communicator {

	public NetworkCommunicator(List<NodeHandle> nodes) {
		
	}
	
	@Override
	public int rank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int nprocs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean topology() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void send(int dest, Serializable data) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends Serializable> T recv(int source, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> T one2all_broadcast(int source, T data, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> List<T> all2one_collect(int dest, T data, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Serializable> List<T> all2all_broadcast(T data, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

}
