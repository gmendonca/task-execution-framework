import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import server.LocalScheduler;

public class LocalTaskExecutor {
	
	public static List<Integer> workLoad() throws IOException{
		List<Integer> tasks = new ArrayList<Integer>();
		
		BufferedReader br = null;
	    try {
	    	br = new BufferedReader(new FileReader("workload"));
	        String line;
			line = br.readLine();

	        while (line != null) {
	        	//System.out.println(line);
	        	tasks.add(Integer.parseInt(line.split("\\s+")[1]));
	            line = br.readLine();
	        }
	        
	    }catch (Exception e){
	    	e.printStackTrace();
	    } finally {
	        br.close();
	    }
		
		return tasks;
	}
	
	public static void main(String[] args) {
		
		List<Integer> tasks = null;
		
		try {
			 tasks = workLoad();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		for(Integer task : tasks){
			//System.out.println(task + " milliseconds");
		}
		Thread t1 = new Thread(new LocalScheduler(9003));
		Thread t2 = new Thread(new Client("localhost", 9003));
		t1.start();
		t2.start();

	}

}
