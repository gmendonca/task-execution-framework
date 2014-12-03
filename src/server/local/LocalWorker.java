package server.local;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import server.SleepTask;

public class LocalWorker implements Runnable{
	private int numThreads;
	private static ExecutorService executor;
	private static CompletionService<SleepTask> pool;
	
	public LocalWorker(int numThreads){
		this.numThreads = numThreads;
		executor = Executors.newFixedThreadPool(numThreads);
		pool = new ExecutorCompletionService<SleepTask>(executor);
	}

	public void startworker(int numThreads){
		
		SleepTask st = null;
		
		for(;;){
			st = TaskQueue.getTask();
			if(st != null) pool.submit(st);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void getTasksDone(){
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		for(;;){
            try {
                System.out.println("Looking for Complete Tasks");
                Future<SleepTask> result = pool.take();
                System.out.println("Found. Trying to get the Task:"  );

                SleepTask st = result.get();
                System.out.println("Task " + st.getId() + " Completed - " + st.getResult());
                CompleteTaskQueue.setTask(st);

            } catch (InterruptedException e) {
            	//e.printStackTrace();
                System.out.println("Task Interrupted!");
            } catch (ExecutionException e) {
                //e.printStackTrace();
                System.out.println("Error getting the task!");
            }
        }
	}
	
	public void run() {
		startworker(numThreads);
	}
	
	

}
