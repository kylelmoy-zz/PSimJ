package org.kylemoy.PSimJCloud;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Host {
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Invalid parameters. Usage: TO DO");
			return;
		}
		int port = Integer.parseInt(args[0]);
		String keyFile = args[1];
		int wwwPort = Integer.parseInt(args[2]);
		String rootDir = args[3];
		
		byte[] key = Files.readAllBytes(Paths.get(keyFile));
		
		NodePool pool = new NodePool(port, key);
		pool.start();

		System.out.println("Running web UI on port " + wwwPort);
		WebUI www = new WebUI(wwwPort, rootDir, pool);
		www.start();
		
		
		boolean run = true;
		while (run) {
			Thread.sleep(1000);
		}
		
		pool.stop();
		www.stop();
	}
}