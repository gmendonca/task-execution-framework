package server.remote;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.Tables;

public class ClusterDynamoDB {
	private AWSCredentials credentials;
	private static AmazonDynamoDBClient dynamoDB;
	private static Map<String, AttributeValue> item;
	
	public ClusterDynamoDB(){
        credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
        	throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct location!",
                    e);
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
	}
	
	public void createTable(String tableName){
		if(!Tables.doesTableExist(dynamoDB, tableName)){
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
	                .withKeySchema(new KeySchemaElement().withAttributeName("taskid").withKeyType(KeyType.HASH))
	                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("taskid").withAttributeType(ScalarAttributeType.S))
	                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(20L).withWriteCapacityUnits(20L));
	        dynamoDB.createTable(createTableRequest);
	        Tables.waitForTableToBecomeActive(dynamoDB, tableName);
		}
	}
	
	public Boolean addItem(String tableName, String id){
		item = new HashMap<String, AttributeValue>();
		item.put("taskid", new AttributeValue(id));
		GetItemRequest getItemRequest = new GetItemRequest(tableName, item);
		if(dynamoDB.getItem(getItemRequest) == null) return false;
		PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
		dynamoDB.putItem(putItemRequest);
		return true;
	}

}
