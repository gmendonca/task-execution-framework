import client.Client;

public class LocalTaskExecutor {
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 9018;
		String filename = "workload";
		int numThreads = 5;
		Boolean remote = false;
		
		Scheduler scheduler = new Scheduler(port, numThreads, remote);
		Client client = new Client(hostname, port, filename);
		
		Thread t1 = new Thread(scheduler);
		Thread t2 = new Thread(client);
		t1.start();
		t2.start();
	}
}
