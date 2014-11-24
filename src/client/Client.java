package client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable{
	private String hostname;
	private int port;
    private Socket socketClient;
    
    public Client(String hostname, int port){
    	this.hostname = hostname;
    	this.port = port;
    }
    
    public void connectToServer() throws UnknownHostException, IOException{
    	socketClient = new Socket(hostname, port);
    }
    
    public void sendTask(String task) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socketClient.getOutputStream()));
    	writer.write(task);
    	writer.flush();
    	writer.close();
    }


	public void run() {
		try {
			connectToServer();
			sendTask("testing");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
