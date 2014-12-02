package server.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;

import org.apache.commons.codec.binary.Base64;

public class RequestWorker {
	private AmazonEC2 ec2;
	private AWSCredentials credentials;
	private String instanceType;
    private String amiID;
    private String bidPrice;
    private String securityGroup;
    private boolean deleteOnTermination;
    private ArrayList<String> spotInstanceRequestIds;
    private String userData;
	
	public RequestWorker(String instanceType, String amiID, String bidPrice, String securityGroup){
		credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct location!",
                    e);
        }
        
        ec2 = new AmazonEC2Client(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        ec2.setRegion(usWest2);
        
        this.instanceType = instanceType;
        this.amiID = amiID;
        this.bidPrice = bidPrice;
        this.securityGroup = securityGroup;
        this.deleteOnTermination = true;
        this.userData = getUserDataScript();
	}
	
	 private static String getUserDataScript(){
	        ArrayList<String> lines = new ArrayList<String>();
	        lines.add("#! /bin/bash");
	        lines.add("java -jar /usr/local/bin/RemoteWorker.jar");
	        String str = new String(Base64.encodeBase64(join(lines, "\n").getBytes()));
	        return str;
	    }
	 
	 private static String join(Collection<String> s, String delimiter) {
	        StringBuilder builder = new StringBuilder();
	        Iterator<String> iter = s.iterator();
	        while (iter.hasNext()) {
	            builder.append(iter.next());
	            if (!iter.hasNext()) {
	                break;
	            }
	            builder.append(delimiter);
	        }
	        return builder.toString();
	    }
	
	public void submitRequests(Integer numWorkers) {
		RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
		
        requestRequest.setSpotPrice(bidPrice);
        requestRequest.setInstanceCount(numWorkers);
        
        LaunchSpecification launchSpecification = new LaunchSpecification();
        launchSpecification.setImageId(amiID);
        launchSpecification.setInstanceType(instanceType);
        ArrayList<String> securityGroups = new ArrayList<String>();
        securityGroups.add(securityGroup);
        launchSpecification.setSecurityGroups(securityGroups);
        launchSpecification.setUserData(userData);
        
        if (!deleteOnTermination) {
            BlockDeviceMapping blockDeviceMapping = new BlockDeviceMapping();
            blockDeviceMapping.setDeviceName("/dev/sda1");

            EbsBlockDevice ebs = new EbsBlockDevice();
            ebs.setDeleteOnTermination(Boolean.FALSE);
            blockDeviceMapping.setEbs(ebs);

            ArrayList<BlockDeviceMapping> blockList = new ArrayList<BlockDeviceMapping>();
            blockList.add(blockDeviceMapping);

            launchSpecification.setBlockDeviceMappings(blockList);
        }
        
        requestRequest.setLaunchSpecification(launchSpecification);

        RequestSpotInstancesResult requestResult = ec2.requestSpotInstances(requestRequest);
        List<SpotInstanceRequest> requestResponses = requestResult.getSpotInstanceRequests();

        spotInstanceRequestIds = new ArrayList<String>();

        for (SpotInstanceRequest requestResponse : requestResponses) {
            System.out.println("Created Spot Request: "+requestResponse.getSpotInstanceRequestId());
            spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
        }
	}
	
	public void cleanup () {
        try {
            System.out.println("Cancelling requests.");
            CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(spotInstanceRequestIds);
            ec2.cancelSpotInstanceRequests(cancelRequest);
        } catch (AmazonServiceException e) {
            System.out.println("Error cancelling instances");
            System.out.println("Caught Exception: " + e.getMessage());
            System.out.println("Reponse Status Code: " + e.getStatusCode());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Request ID: " + e.getRequestId());
        }
        
        spotInstanceRequestIds.clear();
	}
	
	public Boolean anyOpen(){
		DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
        describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);
        
		try
        {
            DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
            List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();

            for (SpotInstanceRequest describeResponse : describeResponses) {
                System.out.println(" " +describeResponse.getSpotInstanceRequestId() +
                                   " is in the "+describeResponse.getState() + " state.");
                if (describeResponse.getState().equals("open")) {
                    return true;
                }

            }
        } catch (AmazonServiceException e) {
            System.out.println("Error when calling describeSpotInstances");
            System.out.println("Caught Exception: " + e.getMessage());
            System.out.println("Reponse Status Code: " + e.getStatusCode());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Request ID: " + e.getRequestId());
            return true;
        }

        return false;
	}

}
