import java.io.IOException;

import client.Client;
import server.LocalScheduler;

public class LocalTaskExecutor {
	
	public static void main(String[] args) {
		
		Thread t1 = new Thread(new LocalScheduler(9003));
		Thread t2 = new Thread(new Client("localhost", 9003));
		t1.start();
		t2.start();

	}

}
