package server.remote;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import server.SleepTask;

public class TaskDoneQueue {
	private static Queue<SleepTask> tasksDone;
	
	
	public TaskDoneQueue(){
		tasksDone = new ConcurrentLinkedQueue<SleepTask>();
	}
	
	public static void setTask(SleepTask st){
		tasksDone.add(st);
	}
	
	public static SleepTask getTask(){
		return tasksDone.poll();
	}
}
