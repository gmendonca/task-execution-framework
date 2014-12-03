import client.Client;
import server.Scheduler;
import server.remote.SimpleQueueService;

public class LocalTaskExecutor {
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 9017;
		String filename = "workload";
		int numThreads = 5;
		Boolean remote = false;
		int numWorkers = 4;
		
		Scheduler scheduler = new Scheduler(port, numThreads, remote, numWorkers);
		Client client = new Client(hostname, port, filename);
		
		Thread t1 = new Thread(scheduler);
		Thread t2 = new Thread(client);
		//t1.start();
		//t2.start();
		
		SimpleQueueService sqs = new SimpleQueueService();
		for(String s : sqs.listQueues()){
			System.out.println(s);
		}
	}
}
