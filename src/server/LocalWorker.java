package server;

public class LocalWorker implements Runnable{


	public void run() {
		while(true){
			String s = TaskQueue.executeTask();
			if(s != null) System.out.println(s);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	

}
