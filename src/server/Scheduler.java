package server;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.codec.binary.Base64;

import server.local.CompleteTaskQueue;
import server.local.LocalWorker;
import server.local.TaskQueue;
import server.remote.SimpleQueueService;

public class Scheduler implements Runnable{
	 private int port;
	 private ServerSocket socketServer;
	 private TaskQueue queue;
	 private CompleteTaskQueue complete;
	 private int numThreads;
	 private Socket client;
	 private Boolean remote;
	 
	 public Scheduler(int port, int numThreads, Boolean remote){
		 this.port = port;
		 this.numThreads = numThreads;
		 this.remote = remote;
	 }
	 
	 private void startLocalServer() throws IOException {
		 
			socketServer = new ServerSocket(port);
			queue = new TaskQueue();
			complete = new CompleteTaskQueue();
			
			Thread worker = new Thread(new LocalWorker(numThreads));
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
			
			
			String[] full;
			Queue<String> lane = new ConcurrentLinkedQueue<String>();
			
			while(true){
				
				DataInputStream isr = new DataInputStream(client.getInputStream());

				byte[] messageByte = new byte[1000*1024];
				while(isr.read(messageByte) != -1){
					System.out.println("message = " + new String(messageByte));
					full = new String(messageByte).split(";");
					lane.addAll(Arrays.asList(full));
					System.out.println(lane.size());
					messageByte = new byte[1000*1024];
					while(!lane.isEmpty()){
						String q = lane.poll();
						if(q.compareTo(" ") > 0) setNewTask(q); //System.out.println(q);
					}
				}
			}
		 }
	 
	 private void startRemoteServer() throws IOException {
		 
			socketServer = new ServerSocket(port);
			queue = new TaskQueue();
			complete = new CompleteTaskQueue();
			
			client = socketServer.accept();
			
			String[] full;
			Queue<String> lane = new ConcurrentLinkedQueue<String>();
			SimpleQueueService sqs = new SimpleQueueService();
			String taskConverted;
			SleepTask sts;
			ByteArrayOutputStream bo;
			ObjectOutputStream so;
			String queueUrl = sqs.createQueue("send-to-workers");
			//String queueUrl = "https://sqs.us-west-2.amazonaws.com/398157563435/queue-task-execution-framework";
			System.out.println(queueUrl);
			
			createWorkers(sqs);
			
			while(true){
				
				DataInputStream isr = new DataInputStream(client.getInputStream());

				byte[] messageByte = new byte[1000*1024];
				while(isr.read(messageByte) != -1){
					System.out.println("message = " + new String(messageByte));
					full = new String(messageByte).split(";");
					lane.addAll(Arrays.asList(full));
					System.out.println(lane.size());
					messageByte = new byte[1000*1024];
					while(!lane.isEmpty()){
						String task = lane.poll();
						if(task.compareTo(" ") > 0){
							sts = new SleepTask(task.split("\\s+")[0], Integer.parseInt(task.split("\\s+")[1].trim()));
							bo = new ByteArrayOutputStream();
							so = new ObjectOutputStream(bo);
							so.writeObject(sts);
							so.flush();
							//taskConverted = bo.toString();
							taskConverted = new String(Base64.encodeBase64(bo.toByteArray()));
							sqs.sendTask(taskConverted, queueUrl);
						}
					}
				}
			}
		 }
	 
	@SuppressWarnings("static-access")
	private void setNewTask(String task) {
		if(task.length() > 10) {
			SleepTask t = new SleepTask(task.split("\\s+")[0], Integer.parseInt(task.split("\\s+")[1].trim()));
			queue.setTask(t);
		}
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
	
	private void createWorkers(SimpleQueueService sqs){
		
		
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
				dos.write((st.getId()+";").getBytes());
			}
			try { Thread.sleep(100); } catch (Exception e) { }
		}
		
	}

	public void run() {
		try {
			//startServerLines();
			if(remote){
				startRemoteServer();
			}else{
				startLocalServer();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String [] args){
		int port = 9018;
		int numThreads = 5;
		Boolean remote = false;
		
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
		else if(args[2] != null && args[2].equals("-rw")){
			remote = true;
		}
		
		Thread thread = new Thread(new Scheduler(port, numThreads, remote));
		thread.start();
		
	}
	 
}
