package org.kylemoy.PSimJCloud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Node {
	public static void main(String[] args) throws Exception {
		String hostAddress = args[0];
		int hostPort = Integer.parseInt(args[1]);
		String keyFile = args[2];
		
		// Not real authentication please fix me
		byte[] key = Files.readAllBytes(Paths.get(keyFile));
		
		// Connect to host
		Socket socket;
		DataInputStream is;
		DataOutputStream os;

		while (true) {
			System.out.println("Attempting to connect to " + hostAddress + ".");
			while (true) {
				try {
					socket = new Socket(hostAddress, hostPort);
					is = new DataInputStream(socket.getInputStream());
					os = new DataOutputStream(socket.getOutputStream());
					
					System.out.println("Connection to " + hostAddress + " made.");
					
					// Submit key for "auth"
					os.write(key);
					
					try {
						while (true) {
							System.out.println("Waiting for work.");

							// Wait for go time!
							int size;
							while (true) {
								size = is.readInt();
								if (size > 0) {
									// IT'S GO TIME WOOOO
									System.out.println("Incoming data length: " + size);
									break;
								} else if (size < 0) {
									// Time to die :c
									return;
								}
							}
							// Done
						}
					} catch (IOException e) {
						System.out.println("Connection to " + hostAddress + " lost.");
						socket.close();
					}
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
