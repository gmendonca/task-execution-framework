import client.Client;
import server.LocalScheduler;

public class LocalTaskExecutor {
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 9015;
		String filename = "workload";
		
		LocalScheduler scheduler = new LocalScheduler(port);
		Client client = new Client(hostname, port, filename);
		
		Thread t1 = new Thread(scheduler);
		Thread t2 = new Thread(client);
		t1.start();
		t2.start();

	}

}
