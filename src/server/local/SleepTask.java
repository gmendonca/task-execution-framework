package server.local;

import java.util.concurrent.Callable;

public class SleepTask implements Callable<SleepTask>{
	private String id;
	private int time;
	
	public SleepTask(String id, int time){
		this.id = id;
		this.time = time;
	}
	
	public String getId(){
		return id;
	}
	
	public String executeTask(){
		try {
			Thread.sleep(time);
			return "I slept for " + time + " ms.";
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SleepTask call() throws Exception {
		System.out.println("----- Task " + id + " started -----");
		String result = executeTask();
		if(result == null){
			System.out.println("----- Task " + id + " Interrupted -----");
			return null;
		}
		else {
			System.out.println("----- Task " + id + " Completed -----");
			return new SleepTask(id, time);
		}
	}

}
