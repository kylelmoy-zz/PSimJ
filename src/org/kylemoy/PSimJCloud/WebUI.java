package org.kylemoy.PSimJCloud;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Simple web server.
 * 
 * @author Kyle Moy
 *
 */
public class WebUI implements java.lang.Runnable {
	private static volatile boolean run = true;
	public int port;
	public String root;
	
	ServerSocket serverSocket;
	Thread thread;
	NodePool pool;
	
	public WebUI(int port, String root, NodePool pool) {
		this.port = port;
		this.root = root;
		this.pool = pool;
		try {
			serverSocket = new ServerSocket(port, 6);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		//Accept incoming connections
		while (run) {
			try {
				Socket socket = serverSocket.accept();
				//Start a new Worker for each connection
				new ClientHandler(socket, root, pool).start();
			} catch (Exception e) {}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void stop() {
		try {
			run = false;
			serverSocket.close();
			thread.join();
		} catch (Exception e) {}
	}
}
class ClientHandler extends Thread {
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private String root;
	private NodePool pool;
	ClientHandler (Socket socket, String root, NodePool pool) throws IOException {
		this.socket = socket;
		this.root = root;
		this.pool = pool;
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}
	@Override
	public void run() {
		try {
			//Make things easier
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			PrintStream out = new PrintStream(new BufferedOutputStream(os));

			//Get GET
			String request = in.readLine();
			
			//Keepin' Aliiiiivvee
			if (request == null) return;

			while (true) {
				String line = in.readLine();
				if (line.length() == 0) break;
			}
			
			//Test for GET request
			if (request.startsWith("GET")) {
				String req = request.substring(4, request.length()-9).trim();
				
				//Check for baddies
				if (req.indexOf("..") !=-1 || req.indexOf("/.ht")!=-1 || req.endsWith("~")) {
					out.println(buildResponseHeader(403, "Forbidden", "text/html"));
					out.println("<html><body><h1>403 FORBIDDEN</h1></body></html>");
				} else if (req.contains(".json")){
					//Print some JSON
					List<NodeHandle> nodes = pool.getNodes();
					String json = "";
					json += "{";
					json += "\"nodes\":";
					json += "[";
					String nodelist = "";
					for (NodeHandle node : nodes) {
						nodelist += ",\"" + node.socket.getRemoteSocketAddress() + "\"";
					}
					if (nodelist.length() > 0) json += nodelist.substring(1);
					json += "]";
					json += "}";

					out.print(buildResponseHeader(200, "OK", "application/json"));
					out.print(json);
				} else {
					//Trim trailing file separator
					if (req.endsWith("/")) req.substring(0, req.length() - 1);
					
					//Get request location
					File f = new File(root + req);
					File fIndex =  new File(root + req + "/index.html");
					
					//If index.html exists, point to that instead
					if (f.isDirectory() && fIndex.exists()) f = fIndex;
					
					//If it's a directory, print an index listing
					if (f.isDirectory()) { 
						
						//Build the HTML string
						StringBuilder body = new StringBuilder();
						body.append("<html><body style=\"background-color:#EEE\">\n");

						File[] fileList = f.listFiles();
						ArrayList<String> dirPaths = new ArrayList<String>();
						ArrayList<String> filePaths = new ArrayList<String>();
						
						//Separate directory names from file names
						for (File file : fileList) {
							if (file.isDirectory()) dirPaths.add(file.getPath());
							else filePaths.add(file.getPath());
						}
						
						//Sort by file name
						Collections.sort(dirPaths);
						Collections.sort(filePaths);
						
						//Title
						body.append("<div style=\"margin: 5% 15% 15%;padding:5% 5%;background-color:#FFF\">");
						body.append("<h1>Index</h1>");
						
						//Remove ../ option from root dir
						if (!f.getAbsolutePath().equals(root)) body.append("<a href=\"../\">../</a><br />\n");
						
						//Print dirs first, files next
						for (String path : dirPaths) {
							//Clean up the path; remove root dir, swap file separators
							//I'd HTML-escape the string but I'm lazy
							String relativePath = path.replace(f.getPath() + "\\", "./").replaceAll("\\\\", "/") + "/";
							body.append("<a href=\"" + relativePath + "\">" + relativePath + "</a><br />\n");
						}
						for (String path : filePaths) {
							String relativePath = path.replace(f.getPath() + "\\", "./").replaceAll("\\\\", "/");
							body.append("<a href=\"" + relativePath + "\">" + relativePath + "</a><br />\n");
						}
						body.append("</div></body></html>");
						
						//Write header, then body
						out.print(buildResponseHeader(200, "OK", "text/html"));
						out.print(body.toString());
					} else {
						try {
							InputStream file = new FileInputStream(f);
							//Write header
							out.print(buildResponseHeader(200, "OK", f.length(), getContentType(f.getPath())));
							//Flush because we're about to write directly to the OutputStream
							out.flush();
				            byte[] buf = new byte[4096];
				            while (file.available() > 0) os.write(buf, 0, file.read(buf));
						} catch (FileNotFoundException e) {
							//Write 404 Error Page
							out.println(buildResponseHeader(404, "Not Found", "text/html"));
							out.println("<html><body><h1>404 NOT FOUND</h1></body></html>");
						}
					}
				}
			} else {
				//Write 400 Error Page
				out.println(buildResponseHeader(400, "Bad Request", "text/html"));
				out.println("<html><body><h1>400 BAD REQUEST</h1></body></html>");
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			//I really don't care.
		} finally {
			//Clean up
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Builds a response header
	 * @param code The response code
	 * @param title The response title
	 * @param contentType The MIME type
	 * @return The response header
	 */
	public static String buildResponseHeader(int code, String title, String contentType) {
		return "HTTP/1.0 " + code + " " + title + " \r\n" +
				"Content-Type: " + contentType + "\r\n" +
				"Date: " + new Date() + "\r\n" +
				"Server: Kyle's Web Server 1.0\r\n\r\n";
	}
	/**
	 * Builds a response header, with Content-Length
	 * @param code The response code
	 * @param title The response title
	 * @param contentLength The content length
	 * @param contentType The MIME type
	 * @return The response header
	 */
	public static String buildResponseHeader(int code, String title, long contentLength, String contentType) {
		return "HTTP/1.0 " + code + " " + title + " \r\n" +
				"Content-Length: " + contentLength + "\r\n" +
				"Content-Type: " + contentType + "\r\n" +
				"Date: " + new Date() + "\r\n" +
				"Server: Kyle's Web Server 1.0\r\n\r\n";
	}
	/**
	 * Guesses the content type of a file
	 * @param path The file path
	 * @return The guessed content type
	 * @throws IOException
	 */
	public static String getContentType(String path) throws IOException
    {
        return Files.probeContentType(Paths.get(path));
    }
}