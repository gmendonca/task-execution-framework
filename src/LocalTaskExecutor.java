import client.Client;
import server.local.LocalScheduler;
import server.local.LocalWorker;

public class LocalTaskExecutor {
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 9015;
		String filename = "workload";
		int numThreads = 5;
		
		LocalScheduler scheduler = new LocalScheduler(port, numThreads);
		Client client = new Client(hostname, port, filename);
		
		Thread t1 = new Thread(scheduler);
		Thread t2 = new Thread(client);
		t1.start();
		t2.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		LocalWorker.getTasksDone();
	}
}
