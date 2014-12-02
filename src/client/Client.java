package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client implements Runnable{
	private String hostname;
	private int port;
    private Socket socketClient;
    private String filename;
    private List<String> sendTasks;
    private static Boolean done;
    private static Queue<String> lane;
    
    public Client(String hostname, int port, String filename){
    	this.hostname = hostname;
    	this.port = port;
    	this.filename = filename;
    	sendTasks = new ArrayList<String>();
    }
    
    private void connectToServer() throws UnknownHostException, IOException{
    	socketClient = new Socket(hostname, port);
    }
    
    @SuppressWarnings("unused")
	private void sendTask(List<String> tasks) throws IOException {
    	//BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream()));
    	
    	PrintWriter pw = new PrintWriter(socketClient.getOutputStream());
    	for(String task : tasks){
    		pw.println(task);
    		try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	pw.close();
    }
    
    private void sendOneTask(List<String> tasks) throws IOException {
    	DataOutputStream dos = new DataOutputStream(socketClient.getOutputStream());
    	String task, id;
    	//int i = 0;
		for(String time : tasks){
			id = UUID.randomUUID().toString();
			//id = "Thread" + i;
			//i++;
			//System.out.println(id);
			task =  id + " " + time + ";";
			sendTasks.add(id);
			//if(sendTasks.add(id)) System.out.println(id);
			//System.out.println(sendTasks.get(i));
			dos.write(task.getBytes());
			try { Thread.sleep(100); } catch (InterruptedException e) { }
		}
    }
    
    private List<String> workLoad() throws IOException{
		List<String> tasks = new ArrayList<String>();
		
		BufferedReader br = null;
	    try {
	    	br = new BufferedReader(new FileReader(filename));
	        String line;
			line = br.readLine();

	        while (line != null) {
	        	tasks.add(line.split("\\s+")[1]);
	            line = br.readLine();
	        }
	        
	    }catch (Exception e){
	    	e.printStackTrace();
	    } finally {
	        br.close();
	    }
		
		return tasks;
	}
    
    private void waitForResults() throws IOException{
    	String [] full;
    	lane = new ConcurrentLinkedQueue<String>();
    	
    	for(;;){
    	    DataInputStream dis = new DataInputStream(socketClient.getInputStream());
    	    byte[] messageByte = new byte[1000*1024];
    		while(dis.read(messageByte) != -1){
    		full = new String(messageByte).split(";");
    		lane.addAll(Arrays.asList(full));
    		messageByte = new byte[1000*1024];
    		}
    	}
    }
    	
    private void retrieveResults() throws IOException{	
    	for(;;){
				while(!lane.isEmpty()){
					String q = lane.poll();
					Boolean b = sendTasks.remove(q);
					if(b) System.out.println(sendTasks.size());
					if(sendTasks.isEmpty()){
						System.out.println("All tasks done");
						done = true;
					}
				}
			}
    }


	public void run() {
		try {
			//sendTask(workLoad());
			sendOneTask(workLoad());
		} catch (Exception e) {
			System.out.println("Couldn't send messages");
		}	
	}
	
	public static void main(String [] args){
		
		String hostname = "localhost";
		int port = 9018;
		String filename = "workload";
		long startTime, stopTime;
		
		if(args[0] != null && args[0].equals("-s")){
			if(args[1] != null){
				hostname = args[1].split(":")[0];
				port = Integer.parseInt(args[1].split(":")[1]);
			}
		}
		
		if(args[2] != null && args[2].equals("-w")){
			if(args[3] != null){
				filename = args[3];
			}
		}
		
		done = false;
		
		final Client c = new Client(hostname, port, filename);
		try {
			c.connectToServer();
		} catch (Exception e){
			System.out.println("Couldn't connect");
		}
		
		startTime = System.currentTimeMillis();
		Thread thread = new Thread(c);
		thread.start();
		
		Thread results = new Thread(new Runnable(){
			public void run(){
				try {
					c.waitForResults();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		results.start();
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		Thread retrieves = new Thread(new Runnable(){
			public void run(){
				try {
					c.retrieveResults();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		retrieves.start();
		while(!done){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		stopTime = System.currentTimeMillis();
		System.out.println("Total time = " + (stopTime - startTime));
	}
}
