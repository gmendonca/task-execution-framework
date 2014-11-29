package server.remote;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SimpleQueueService {
	private AWSCredentials credentials;
	private AmazonSQS sqs;
	
	public SimpleQueueService(){
		credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct location!",
                    e);
        }

        sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        sqs.setRegion(usWest2);
	}
	
	public List<String> listQueues(){
		return sqs.listQueues().getQueueUrls();
	}
	
	public String createQueue(String name){
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(name);
		return sqs.createQueue(createQueueRequest).getQueueUrl();
	}
	
	public void sendTask(String task, String queueUrl){
		sqs.sendMessage(new SendMessageRequest(queueUrl, task));
	}
	
	public Message getTask(String queueUrl){
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		return messages.get(0);
	}
	
	public void deleteTask(String queueUrl){
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		String messageRecieptHandle = messages.get(0).getReceiptHandle();
		sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
	}
	
	public void deteleQueue(String queueUrl){
		sqs.deleteQueue(new DeleteQueueRequest(queueUrl));
	}

}
