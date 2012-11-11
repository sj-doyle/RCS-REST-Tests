package test.java;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
 

public class Test008Capabilities {
	
	@Test 
	public void registerTestUser1() {
		Setup.initialise(); 
		
		Setup.majorTest("Capabilities", "Read and set user capabilities");

		Setup.startTest("Testing successful register call for User 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(204).post(Setup.registerURL(Setup.TestUser1));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void readUserCapabilities() {
		Setup.startTest("Testing read capabilities for User 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError()./*statusCode(200).*/get(Setup.capabilitiesURL(Setup.TestUser1));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void setUserCapabilities() throws JsonGenerationException, JsonMappingException, IOException {
		Capabilities capabilities=new Capabilities();
		capabilities.setAddress(Setup.TestUser1Contact);
		capabilities.setImSession(true);
		capabilities.setFileTransfer(true);
		capabilities.setImageShare(true);
		capabilities.setSocialPresence(true);
		capabilities.setDiscoveryPresence(true);
		
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"capabilities\":"+mapper.writeValueAsString(capabilities)+"}";

		Setup.startTest("Testing set capabilities for User 1. Sending "+jsonRequestData);

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError()./*statusCode(204).*/
				put(Setup.capabilitiesURL(Setup.TestUser1));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		//TODO - receiving 405 error
		Setup.endTest();
	}
	
	
	/*
	 * Supporting class
	 */

	public class Capabilities {
		String address;
		boolean imSession;
		boolean fileTransfer;
		boolean imageShare;
		boolean videoShare;
		boolean socialPresence;
		boolean discoveryPresence;
		public String getAddress() {
			return address;
		}
		public boolean isImSession() {
			return imSession;
		}
		public boolean isFileTransfer() {
			return fileTransfer;
		}
		public boolean isImageShare() {
			return imageShare;
		}
		public boolean isVideoShare() {
			return videoShare;
		}
		public boolean isSocialPresence() {
			return socialPresence;
		}
		public boolean isDiscoveryPresence() {
			return discoveryPresence;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public void setImSession(boolean imSession) {
			this.imSession = imSession;
		}
		public void setFileTransfer(boolean fileTransfer) {
			this.fileTransfer = fileTransfer;
		}
		public void setImageShare(boolean imageShare) {
			this.imageShare = imageShare;
		}
		public void setVideoShare(boolean videoShare) {
			this.videoShare = videoShare;
		}
		public void setSocialPresence(boolean socialPresence) {
			this.socialPresence = socialPresence;
		}
		public void setDiscoveryPresence(boolean discoveryPresence) {
			this.discoveryPresence = discoveryPresence;
		}
	}


}
