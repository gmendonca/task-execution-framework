package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;



public class LocalScheduler implements Runnable{
	 private int port;
	 private ServerSocket socketServer;
	 
	 public LocalScheduler(int port){
		 this.port = port;
	 }
	 
	 public void startServer() throws IOException {
		 
		socketServer = new ServerSocket(port);

		Socket client = socketServer.accept();
		
		BufferedReader task = new BufferedReader(new InputStreamReader(client.getInputStream()));
		
		System.out.println(task.readLine());
	 }

	public void run() {
		try {
			startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	 
}
