package server.local;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import server.SleepTask;

public class TaskQueue {
	private static Queue<SleepTask> scheduler;
	
	
	public TaskQueue(){
		scheduler = new ConcurrentLinkedQueue<SleepTask>();
	}
	
	public static void setTask(SleepTask st){
		scheduler.add(st);
	}
	
	public static String executeTask(){
		SleepTask st = scheduler.poll();
		if (st == null) return null;
		String result = st.executeTask();
		if(result != null) return result;
		return null;
	}
	
	public static SleepTask getTask(){
		return scheduler.poll();
	}
}
