package org.kylemoy.PSimJCloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.kylemoy.PSimJ.NetworkCommunicator;

public class Host {
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Invalid parameters. Usage: TO DO");
			return;
		}
		int nodePort = Integer.parseInt(args[0]);
		int listenPort = Integer.parseInt(args[1]);
		String keyFile = args[2];
		int wwwPort = Integer.parseInt(args[3]);
		String rootDir = args[4];
		
		byte[] key = Files.readAllBytes(Paths.get(keyFile));

		System.out.println("Listening for nodes on port " + nodePort);
		NodePool pool = new NodePool(nodePort, key);
		pool.start();

		System.out.println("Running web UI on port " + wwwPort);
		WebUI www = new WebUI(wwwPort, rootDir, pool);
		www.start();

		System.out.println("Listening for tasks on port " + listenPort);
		boolean run = true;
		while (run) {
			try {
				ServerSocket serverSocket = new ServerSocket(listenPort);
				while (run) {
					try {
						Socket socket = serverSocket.accept();
						NodeHandle tasker = new NodeHandle(socket);

						List<NodeHandle> nodes = pool.getNodes();
						
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
						List<NodeHandle> requestPool = new ArrayList<NodeHandle>();
						requestPool.add(tasker);
						for (int i = 0; i < ncount; i++) {
							requestPool.add(nodes.get(i));
						}
						
						// Send node ips to tasker
						int poolSize = requestPool.size();
						for (int i = 0; i < poolSize; i++) {
							NodeHandle node = requestPool.get(i);
							node.os.writeInt(i);
							node.os.writeInt(poolSize);
						}
						for (int i = 0; i < poolSize; i++) {
							byte[] buf = requestPool.get(i).ip.getBytes();
							for (NodeHandle node : requestPool) {
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
}