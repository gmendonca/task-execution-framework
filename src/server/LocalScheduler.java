package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;



public class LocalScheduler implements Runnable{
	 private int port;
	 private ServerSocket socketServer;
	 private Queue<SleepTask> scheduler;
	 
	 public LocalScheduler(int port){
		 this.port = port;
		 scheduler = new LinkedList<SleepTask>();
	 }
	 
	 private void startServer() throws IOException {
		 
		socketServer = new ServerSocket(port);
		
		while(true){
			Socket client = socketServer.accept();
			
			DataInputStream isr = new DataInputStream(client.getInputStream());
			byte[] messageByte = new byte[1024];
			while(isr.read(messageByte) != -1){
				setNewTask(new String(messageByte));
				messageByte = new byte[1024];
			}
		}
	 }
	 
	private void setNewTask(String task) {
		SleepTask t = new SleepTask(task.split("\\s+")[0], Integer.parseInt(task.split("\\s+")[1].trim()));
		scheduler.add(t);
	}

	@SuppressWarnings("unused")
	private void startServerLines() throws IOException { 
		socketServer = new ServerSocket(port);	
		while(true){
			Socket client = socketServer.accept();
			BufferedReader task = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String str;
			while((str = task.readLine()) != null) {
				System.out.println(str);
			}
		}
	}

	public void run() {
		try {
			//startServerLines();
			startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	 
}
