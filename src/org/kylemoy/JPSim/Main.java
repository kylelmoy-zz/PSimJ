package org.kylemoy.JPSim;

import org.kylemoy.JPSim.JPSim.JPSimRunnable;

public class Main {
	public static class MyJPSimRunnable extends JPSimRunnable {
		@Override
		void run(JPSim comm) {
			/*
			if (comm.rank() == 0) {
				System.out.println("Receiving..." + comm.nprocs());
				for (int i = 1; i < comm.nprocs(); i++) {
					System.out.println(i);
					ArrayList<String> list  = comm.recv(i, ArrayList.class);
					for (String s : list) {
						System.out.println("From rank " + i + ": " + s);
					}
				}
			} else {
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < comm.rank(); i++) {
					list.add((i + comm.rank()) + " derps");
				}
				comm.send(0, list);
			}
			*/
			String str = "";
			if (comm.rank() == 0) str = "Hello World!";
			str = comm.one2all_broadcast(0, str, String.class);
			System.out.println(comm.rank() +": " + str);
		}
	}
	public static void main(String[] args) {
		Local.instantiate(4, new JPSimTopology.Default(), MyJPSimRunnable.class);
	}
}