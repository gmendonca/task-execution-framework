package server;

public class SleepTask {
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

}
