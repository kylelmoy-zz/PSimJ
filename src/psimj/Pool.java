package psimj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Pool {
	public static void host(int nodePort, int listenPort, byte[] key, int wwwPort, String rootDir) throws Exception {
		System.out.println("Listening for nodes on port " + nodePort);
		NodeSocketPool pool = new NodeSocketPool(nodePort, key);
		pool.start();

		System.out.println("Running web UI on port " + wwwPort);
		WebServer www = new WebServer(wwwPort, rootDir, pool);
		www.start();

		System.out.println("Listening for tasks on port " + listenPort);
		boolean run = true;
		while (run) {
			try {
				ServerSocket serverSocket = new ServerSocket(listenPort);
				while (run) {
					try {
						Socket socket = serverSocket.accept();
						NodeSocket tasker = new NodeSocket(socket);

						List<NodeSocket> nodes = pool.getNodes();
						
						//Receive node count request, exclude tasker themselves
						int ncount = tasker.is.readInt() - 1;
						
						if (ncount > nodes.size()) {
							//Send failure
							ncount = -1;
							tasker.os.writeInt(-1);
							socket.close();
							continue;
						} else if (ncount < 0) {
							//Assume all nodes
							ncount = nodes.size();
						}
						
						
						//Build a pool to meet ncount nodes
						List<NodeSocket> requestPool = new ArrayList<NodeSocket>();
						requestPool.add(tasker);
						for (int i = 0; i < ncount; i++) {
							requestPool.add(nodes.get(i));
						}
						
						// Send node ips to tasker
						int poolSize = requestPool.size();
						for (int i = 0; i < poolSize; i++) {
							NodeSocket node = requestPool.get(i);
							node.os.writeInt(i);
							node.os.writeInt(poolSize);
						}
						for (int i = 0; i < poolSize; i++) {
							byte[] buf = requestPool.get(i).ip.getBytes();
							for (NodeSocket node : requestPool) {
								node.os.writeInt(buf.length);
								node.os.write(buf);
							}
						}
						
						System.out.println("Done!");
						//Our job is over
						socket.close();
					} catch (IOException e) {
						// Timed out, try again
					}
				}
				
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		pool.stop();
		www.stop();
	}
	

	@SuppressWarnings("rawtypes")
	public static void node(String hostAddress, int hostPort, byte[] key) throws Exception {
		// Try connecting to host
		while (true) {
			System.out.println("Attempting to connect to " + hostAddress + ".");
			while (true) {
				try {
					Socket socket = new Socket(hostAddress, hostPort);
					DataInputStream is = new DataInputStream(socket.getInputStream());
					DataOutputStream os = new DataOutputStream(socket.getOutputStream());
					
					System.out.println("Connection to " + hostAddress + " made.");
					
					// Submit key for "auth"
					os.write(key);
					
					try {
						while (true) {
							System.out.println("Waiting for work.");

							// Wait for directives from pool
							int rank;
							while (true) {
								rank = is.readInt();
								if (rank > 0) {
									// It's go time!
									break;
								} else if (rank < 0) {
									// Quit
									return;
								}
							}
							
							try {
								// Receive ips of all nodes
								List<String> ipList = new ArrayList<String>();
								int size = is.readInt();
								for (int i = 0; i < size; i++) {
									int bufSize = is.readInt();
									byte[] buf = new byte[bufSize];
									is.readFully(buf);
									ipList.add(new String(buf));
								}
								
								// Initialize communications with all nodes
								Communicator comm = new NetworkCommunicator(ipList, rank, new Topology.Switch());
								
								// Receive class definition from node 0
								SerializedClass cls = null;
								cls = comm.one2all_broadcast(0, cls, SerializedClass.class);
								
								// Build class from class definition
								SerializedClassLoader classLoader = new SerializedClassLoader(SerializedClassLoader.class.getClassLoader());
								Class type = classLoader.loadClass(cls);
								
								// Instantiate class
								Task instance = (Task) type.newInstance();
								
								// Go go go!
								instance.run(comm);
								
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					} catch (IOException e) {
						System.out.println("Connection to " + hostAddress + " lost.");
					}
					socket.close();
				} catch (UnknownHostException e) {
					System.err.println("Unable to resolve host.");
				} catch (IOException e) {
					// Do nothing, try again later.
				}
				try {
					Thread.sleep(1000);
				} catch (Exception s) {}
			}
		}
	}
}
