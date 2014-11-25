package server;

import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue {
	private static Queue<SleepTask> scheduler;
	
	
	public TaskQueue(){
		scheduler = new LinkedList<SleepTask>();
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
}
