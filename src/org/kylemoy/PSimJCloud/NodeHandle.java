package org.kylemoy.PSimJCloud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NodeHandle {
	public final Socket socket;
	public final DataOutputStream os;
	public final DataInputStream is;
	public NodeHandle(Socket socket) throws IOException {
		this.socket = socket;
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());
	}
}