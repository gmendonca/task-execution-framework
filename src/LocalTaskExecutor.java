import client.Client;
import server.local.LocalScheduler;

public class LocalTaskExecutor {
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 9018;
		String filename = "workload";
		int numThreads = 5;
		
		LocalScheduler scheduler = new LocalScheduler(port, numThreads);
		Client client = new Client(hostname, port, filename);
		
		Thread t1 = new Thread(scheduler);
		Thread t2 = new Thread(client);
		t1.start();
		t2.start();
	}
}
