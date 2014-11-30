package server.remote;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.commons.codec.binary.Base64;

import server.SleepTask;

import com.amazonaws.services.sqs.model.Message;

public class RemoteWorker{
	
	public static void main(String[] args){
		SimpleQueueService sqs = new SimpleQueueService();
		String queueUrl = "https://sqs.us-west-2.amazonaws.com/398157563435/send-to-workers";
		//String queueUrl = "https://sqs.us-west-2.amazonaws.com/398157563435/queue-task-execution-framework";
		
		Message message = null;
		SleepTask st = null;
		byte task[];
	    ByteArrayInputStream bi;
	    ObjectInputStream si;
	    String result = null;
		
		for(;;){
			message = sqs.getTask(queueUrl);
			
			if(message == null){
				try{ Thread.sleep(100); } catch(Exception e) { }
				continue;
			}
			sqs.deleteTask(queueUrl, message);
			task = Base64.decodeBase64(message.getBody()); 
		    bi = new ByteArrayInputStream(task);
		    try {
				si = new ObjectInputStream(bi);
				st = (SleepTask) si.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(st != null){
				//System.out.println(st.getId());
				result = st.executeTask();
				System.out.println(result);
			}
			
		}
	}

}
