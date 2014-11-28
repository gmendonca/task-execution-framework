package server.local;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class LocalScheduler implements Runnable{
	 private int port;
	 private ServerSocket socketServer;
	 private TaskQueue queue;
	 private CompleteTaskQueue complete;
	 private int numThreads;
	 private Socket client;
	 
	 public LocalScheduler(int port, int numThreads){
		 this.port = port;
		 this.numThreads = numThreads;
		 complete = new CompleteTaskQueue();
	 }
	 
	 private void startServer() throws IOException {
		 
		socketServer = new ServerSocket(port);
		queue = new TaskQueue();
		complete = new CompleteTaskQueue();
		//Thread localWorker = new Thread(new LocalWorker());
		//localWorker.start();
		
		Thread worker = new Thread (new LocalWorker(numThreads));
		worker.start();
		
		client = socketServer.accept();
		
		Thread tasksDone = new Thread(new Runnable(){
			public void run() {
				try {
					sendCompletedTasksBack();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		tasksDone.start();
		
		Thread getTasksDone = new Thread(new Runnable(){
			public void run(){
				LocalWorker.getTasksDone();
			}
		});
		
		getTasksDone.start();
		
		while(true){
			//client = socketServer.accept();
			String id;
			
			DataInputStream isr = new DataInputStream(client.getInputStream());
			byte[] messageByte = new byte[1024];
			while(isr.read(messageByte) != -1){
				id = new String(messageByte);
				//System.out.println(id);
				setNewTask(id);
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
	
	@SuppressWarnings("static-access")
	private void sendCompletedTasksBack() throws IOException{
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		SleepTask st = null;
		while(true){
			st = complete.getTask();
			if(st != null){
				//System.out.println(st.getId());
				dos.write(st.getId().getBytes());
			}
			try { Thread.sleep(100); } catch (Exception e) { }
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
	
	public static void main(String [] args){
		int port = 9018;
		int numThreads = 5;
		
		if(args[0] != null && args[0].equals("-s")){
			if(args[1] != null){
				port = Integer.parseInt(args[1]);
			}
		}
		
		if(args[2] != null && args[2].equals("-lw")){
			if(args[3] != null){
				numThreads = Integer.parseInt(args[3]);
			}
		}
		
		
		Thread thread = new Thread(new LocalScheduler(port, numThreads));
		thread.start();
		
	}
	 
}
