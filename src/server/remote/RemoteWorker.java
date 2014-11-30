package server.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;

import server.SleepTask;

import com.amazonaws.services.sqs.model.Message;

public class RemoteWorker implements Runnable{
	private ExecutorService executor;
	private CompletionService<SleepTask> pool;
	
	public RemoteWorker(){
		executor = Executors.newSingleThreadExecutor();
		pool = new ExecutorCompletionService<SleepTask>(executor);
	}
	
	public void sumitWork(SleepTask st){
		pool.submit(st);
	}
	
	public static void main(String[] args){
		SimpleQueueService sqs = new SimpleQueueService();
		String queueUrl = "https://sqs.us-west-2.amazonaws.com/398157563435/send-to-workers";
		
		Message message = null;
		SleepTask st = null;
		byte task[];
	    ByteArrayInputStream bi;
	    ObjectInputStream si;
	    
	    RemoteWorker rt = new RemoteWorker();
	    
	    Thread remoteworker = new Thread(rt);
	    remoteworker.start();
		
		for(;;){
			message = sqs.getTask(queueUrl);
			
			if(message == null){
				try{ Thread.sleep(100); } catch(Exception e) { }
				continue;
			}
			task = Base64.decodeBase64(message.getBody());
			sqs.deleteTask(queueUrl, message);
		    bi = new ByteArrayInputStream(task);
		    try {
				si = new ObjectInputStream(bi);
				st = (SleepTask) si.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(st != null){
				System.out.println(st.getId());
				rt.sumitWork(st);
			}
			
		}
	}
	public void getTasksDone(){
		String backQueueUrl = "https://sqs.us-west-2.amazonaws.com/398157563435/send-to-scheduler";
		SimpleQueueService sqs = new SimpleQueueService();
		String taskConverted;
		ByteArrayOutputStream bo;
		ObjectOutputStream so;
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		for(;;){
            try {
                //System.out.println("Looking for Complete Tasks");
                Future<SleepTask> result = pool.take();
                //System.out.println("Found. Trying to get the Task:"  );

                SleepTask st = result.get();
                //System.out.println("Task " + st.getId() + " Completed - " + st.getResult());
                bo = new ByteArrayOutputStream();
				so = new ObjectOutputStream(bo);
				so.writeObject(st);
				so.flush();
				taskConverted = new String(Base64.encodeBase64(bo.toByteArray()));
                sqs.sendTask(taskConverted, backQueueUrl);
                

            } catch (InterruptedException e) {
            	//e.printStackTrace();
                System.out.println("Task Interrupted!");
            } catch (ExecutionException e) {
                //e.printStackTrace();
                System.out.println("Error getting the task!");
            } catch (Exception e) {
            	System.out.println("Error sending back to SQS");
            }
        }
	}
	
	public void run(){
		getTasksDone();
	}

}
