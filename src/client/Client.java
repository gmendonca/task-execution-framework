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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class Client implements Runnable{
	private String hostname;
	private int port;
    private Socket socketClient;
    private String filename;
    private List<String> sendTasks;
    
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
				Thread.sleep(1000);
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
    	//String id;
    	String [] full;
    	Queue<String> lane = new LinkedList<String>();
    	for(;;){
    		DataInputStream dis = new DataInputStream(socketClient.getInputStream());
    		byte[] messageByte = new byte[1000*1024];
			while(dis.read(messageByte) != -1){
				full = new String(messageByte).split(";");
				//System.out.println(id);
				//Boolean b = sendTasks.remove(id.trim());
				//if(b) System.out.println(sendTasks.size());
				//if(sendTasks.isEmpty()) System.out.println("All tasks done");
				lane.addAll(Arrays.asList(full));
				messageByte = new byte[1000*1024];
				//try { Thread.sleep(100); } catch (InterruptedException e) { }
				while(!lane.isEmpty()){
					String q = lane.poll();
					Boolean b = sendTasks.remove(q);
					if(b) System.out.println(sendTasks.size());
					if(sendTasks.isEmpty()) System.out.println("All tasks done");
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
		
		final Client c = new Client(hostname, port, filename);
		try {
			c.connectToServer();
		} catch (Exception e){
			System.out.println("Couldn't connect");
		}
		
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
	}
}
