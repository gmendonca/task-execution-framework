package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Client implements Runnable{
	private String hostname;
	private int port;
    private Socket socketClient;
    private String filename;
    
    public Client(String hostname, int port, String filename){
    	this.hostname = hostname;
    	this.port = port;
    	this.filename = filename;
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
    	String task;
		for(String time : workLoad()){
			task = UUID.randomUUID().toString() + " " + time;
			dos.write(task.getBytes());
			try { Thread.sleep(10); } catch (InterruptedException e) { }
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


	public void run() {
		try {
			connectToServer();
			//sendTask(workLoad());
			sendOneTask(workLoad());
			
		} catch (Exception e) {
			System.out.println("Couldn't connect");
		}	
	}
}
