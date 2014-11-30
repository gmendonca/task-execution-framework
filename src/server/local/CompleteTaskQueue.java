package server.local;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import server.SleepTask;

public class CompleteTaskQueue {
private static Queue<SleepTask> tasksDone;
	
	
	public CompleteTaskQueue(){
		tasksDone = new ConcurrentLinkedQueue<SleepTask>();
	}
	
	public static void setTask(SleepTask st){
		tasksDone.add(st);
	}
	
	public static SleepTask getTask(){
		return tasksDone.poll();
	}
}
