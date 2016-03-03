package org.kylemoy.PSimJ;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public interface PSimJRMIServer extends Remote {
	void run(Communicator communicator) throws RemoteException;
	
	public static class ParallelTask extends UnicastRemoteObject implements PSimJRMIServer {
		private static final long serialVersionUID = -6901285525890598488L;
		public ParallelTask() throws RemoteException { }
		@Override
		public void run(Communicator communicator) throws RemoteException { }
	}
}