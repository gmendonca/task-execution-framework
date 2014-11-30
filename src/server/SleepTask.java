package server;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SuppressWarnings("serial")
public class SleepTask implements Callable<SleepTask>, Serializable{
	private String id;
	private int time;
	private String result;
	
	public SleepTask(String id, int time){
		this.id = id;
		this.time = time;
	}
	
	public SleepTask(String id, int time, String result){
		this.id = id;
		this.time = time;
		this.result = result;
	}
	
	public String getId(){
		return id;
	}
	
	public String getResult(){
		return (result == null) ? "I didn't sleep!" : result;
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
		//System.out.println("----- Task " + id + " started -----");
		String result = executeTask();
		if(result == null){
			//System.out.println("----- Task " + id + " Interrupted -----");
			return null;
		}
		else {
			//System.out.println("----- Task " + id + " Completed -----");
			return new SleepTask(id, time, result);
		}
	}

}
