package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;



public class LocalScheduler implements Runnable{
	 private int port;
	 private ServerSocket socketServer;
	 private TaskQueue queue;
	 
	 public LocalScheduler(int port){
		 this.port = port;
	 }
	 
	 private void startServer() throws IOException {
		 
		socketServer = new ServerSocket(port);
		queue = new TaskQueue();
		Thread localWorker = new Thread(new LocalWorker());
		localWorker.start();
		
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
	 
	@SuppressWarnings("static-access")
	private void setNewTask(String task) {
		SleepTask t = new SleepTask(task.split("\\s+")[0], Integer.parseInt(task.split("\\s+")[1].trim()));
		queue.setTask(t);
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
