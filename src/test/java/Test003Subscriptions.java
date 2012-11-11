package test.java;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class Test003Subscriptions {
	
	static {
		Setup.initialise(); 


		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response response = RestAssured.given().body(Setup.validLongPoll).post(Setup.notificationChannelURL(Setup.TestUser1));
		JsonPath jsonData=response.jsonPath();
		Setup.resourceURL[1]=jsonData.get("notificationChannel.resourceURL");
		Setup.channelURL[1]=jsonData.get("notificationChannel.channelData.channelURL");
		Setup.callbackURL[1]=jsonData.get("notificationChannel.callbackURL");
		System.out.println("resourceURL="+Setup.resourceURL[1]);
		System.out.println("channelURL="+Setup.channelURL[1]);
		System.out.println("callbackURL="+Setup.callbackURL[1]);	
	}
	
	@Test 
	public void testInitialNotification1() {
		Setup.majorTest("Notifications", "Notifications and subscriptions");
		
		Setup.startTest("Testing successful notification subscription for User 1");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().body(Setup.validLongPoll).expect().log().ifError().statusCode(201).
		body(
	    		"notificationChannel.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
	    		"notificationChannel.callbackURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
	    		"notificationChannel.channelData.channelURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1))	    		
	    ).when().post(Setup.notificationChannelURL(Setup.TestUser1));
//		System.out.println("channelURL="+resp.jsonPath().get("notificationChannel.channelData.channelURL"));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void testInitialNotification2() {
		
		Setup.startTest("Testing successful notification subscription for User 2");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.given().body(Setup.validLongPoll).expect().log().ifError().statusCode(201).
		body(
	    		"notificationChannel.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser2)),
	    		"notificationChannel.callbackURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser2)),
	    		"notificationChannel.channelData.channelURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser2))	    		
	    ).when().post(Setup.notificationChannelURL(Setup.TestUser2));
//		System.out.println("channelURL="+r2.jsonPath().get("notificationChannel.channelData.channelURL"));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();

	}

	@Test 
	public void testInitialNotificationNonExist() {
		Setup.startTest("Testing erroneous notification subscription for non existent user");

		RestAssured.authentication=RestAssured.basic("GSMAAcceptanceNonExist", Setup.applicationPassword);
		Response resp=RestAssured.given().body(Setup.validLongPoll).expect().log().ifError().statusCode(401).
			when().post(Setup.notificationChannelURL("GSMAAcceptanceNonExist"));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
	}
	
	@Test
	public void testSubscribeSessionNotifications1() {
		long clientCorrelator=System.currentTimeMillis();
		String requestData="{\"sessionSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[1]+"\",\"callbackData\":\"GSMA1\"},\"clientCorrelator\":\""+Long.toString(clientCorrelator)+"\", \"duration\":900}}";
		
		Setup.startTest("Testing successful session subscription for User 1");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().contentType("application/json").body(requestData).expect().log().ifError().statusCode(201).
		body(
				"sessionSubscription.clientCorrelator", IsEqual.equalTo(Long.toString(clientCorrelator)),
				"sessionSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"sessionSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),
				"sessionSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"sessionSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON"),
				"sessionSubscription.duration", IsEqual.equalTo(Integer.valueOf(900))
		).post(Setup.sessionSubscriptionURL(Setup.TestUser1));

		Setup.sessionSubscriptionURL[1]=resp.jsonPath().get("sessionSubscription.resourceURL");

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void testReadSessionSubscriptionDetails1() {
		Setup.startTest("Testing read session subscription for User 1");
		String[] parts=Setup.sessionSubscriptionURL[1].split(Setup.urlSplit);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		System.out.println("URL = "+parts[1]);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).body(
				"sessionSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"sessionSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),  
				"sessionSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"sessionSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).get(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void testReadAllSessionSubscriptions1() {
		Setup.startTest("Testing read all session subscriptions for User 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).body(
				"sessionSubscriptionList.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"sessionSubscriptionList.sessionSubscription.size()", Matchers.is(1),
				"sessionSubscriptionList.sessionSubscription[0].callbackReference.callbackData", IsEqual.equalTo("GSMA1"),  
				"sessionSubscriptionList.sessionSubscription[0].callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"sessionSubscriptionList.sessionSubscription[0].callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).get(Setup.sessionSubscriptionURL(Setup.TestUser1));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void testDeleteSessionSubscriptions1() {
		Setup.startTest("Testing delete session subscriptions for User 1");
		
		String[] parts=Setup.sessionSubscriptionURL[1].split(Setup.urlSplit);
		Response resp=RestAssured.expect().log().ifError().statusCode(204).delete(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void testSubscribeAbChangesNotifications1() {
		long clientCorrelator=System.currentTimeMillis();
		String requestData="{\"abChangesSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[1]+"\",\"callbackData\":\"GSMA1\"},\"clientCorrelator\":\""+Long.toString(clientCorrelator)+"\", \"duration\":900}}";

		Setup.startTest("Testing successful address book subscription for User 1");

		Response resp=RestAssured.given().contentType("application/json").body(requestData).expect().log().ifError().statusCode(201).
		body(
				"abChangesSubscription.clientCorrelator", IsEqual.equalTo(Long.toString(clientCorrelator)),
				"abChangesSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"abChangesSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),
				"abChangesSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"abChangesSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON")
		).post(Setup.abChangesSubscriptionURL(Setup.TestUser1));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());

		Setup.abChangesSubscriptionURL[1]=resp.jsonPath().get("abChangesSubscription.resourceURL");
		Setup.endTest();

	}

	@Test 
	public void testReadabChangesSubscriptionDetails1() {
		Setup.startTest("Testing read address book subscription for User 1");
		String[] parts=Setup.abChangesSubscriptionURL[1].split(Setup.urlSplit);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).body(
				"abChangesSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"abChangesSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"), 				
				"abChangesSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"abChangesSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).get(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void testReadAllabChangesSubscriptions1() {
		Setup.startTest("Testing read all address book subscriptions for User 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).body(
				"abChangesSubscriptionCollection.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"abChangesSubscriptionCollection.abChangesSubscription.size()", Matchers.is(1),
				"abChangesSubscriptionCollection.abChangesSubscription[0].resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"abChangesSubscriptionCollection.abChangesSubscription[0].callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"abChangesSubscriptionCollection.abChangesSubscription[0].callbackReference.callbackData", IsEqual.equalTo("GSMA1"),  
				"abChangesSubscriptionCollection.abChangesSubscription[0].callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).get(Setup.abChangesSubscriptionURL(Setup.TestUser1));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	
	@Test
	public void testExtendabChangesSubscriptions1() {
		Setup.startTest("Testing extend address book subscriptions for User 1");
		
		long clientCorrelator=System.currentTimeMillis();
		String requestData="{\"abChangesSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[1]+"\",\"callbackData\":\"GSMA1\"},\"clientCorrelator\":\""+Long.toString(clientCorrelator)+"\", \"duration\":2000}}";

		String[] parts=Setup.abChangesSubscriptionURL[1].split(Setup.urlSplit);
		
		System.out.println("URL = "+parts[1]);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().contentType("application/json").body(requestData).expect().log().ifError().statusCode(200).body(
				"abChangesSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"abChangesSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),				
				"abChangesSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"abChangesSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).put(Setup.prepareForTest(parts[1]));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void testDeleteabChangesSubscriptions1() {
		Setup.startTest("Testing delete address book subscriptions for User 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		String[] parts=Setup.abChangesSubscriptionURL[1].split(Setup.urlSplit);
		Response resp=RestAssured.expect().log().ifError().statusCode(204).delete(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}


	@Test
	public void testChatSessionNotifications1A() {
		long clientCorrelator=System.currentTimeMillis();
		String requestData="{\"chatNotificationSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[1]+"\",\"callbackData\":\"GSMA1\"},\"clientCorrelator\":\""+Long.toString(clientCorrelator)+
				"\", \"duration\":900, \"confirmedChatSupported\":false, \"adhocChatSupported\":true}}";

		Setup.startTest("Testing successful chat subscription for User 1 (adhoc)");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().contentType("application/json").body(requestData).expect().log().ifError().statusCode(201).
		body(
				"chatNotificationSubscription.clientCorrelator", IsEqual.equalTo(Long.toString(clientCorrelator)),
				"chatNotificationSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"chatNotificationSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),
				"chatNotificationSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"chatNotificationSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON"),
				"chatNotificationSubscription.duration", IsEqual.equalTo(Integer.valueOf(900))
		).post(Setup.chatSubscriptionURL(Setup.TestUser1));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.chatSubscriptionURL[1]=resp.jsonPath().get("chatNotificationSubscription.resourceURL");
		Setup.endTest();
	}

	@Test
	public void testChatSessionNotifications1B() {
		long clientCorrelator=System.currentTimeMillis();
		String requestData="{\"chatNotificationSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[1]+"\",\"callbackData\":\"GSMA1\"},\"clientCorrelator\":\""+Long.toString(clientCorrelator)+
				"\", \"duration\":900, \"confirmedChatSupported\":true, \"adhocChatSupported\":false}}";

		Setup.startTest("Testing successful chat subscription for User 1 (confirmed)");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().contentType("application/json").body(requestData).expect().log().ifError().statusCode(201).
		body(
				"chatNotificationSubscription.clientCorrelator", IsEqual.equalTo(Long.toString(clientCorrelator)),
				"chatNotificationSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"chatNotificationSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),
				"chatNotificationSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"chatNotificationSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON"),
				"chatNotificationSubscription.duration", IsEqual.equalTo(Integer.valueOf(900))
		).post(Setup.chatSubscriptionURL(Setup.TestUser1));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.chatSubscriptionURL[1]=resp.jsonPath().get("chatNotificationSubscription.resourceURL");
		Setup.endTest();
	}

	@Test 
	public void testReadChatSubscriptionDetails1() {
		Setup.startTest("Testing read chat subscription for User 1");
		String[] parts=Setup.chatSubscriptionURL[1].split(Setup.urlSplit);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).
				body(
				"chatNotificationSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"chatNotificationSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),  
				"chatNotificationSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"chatNotificationSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).
				get(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void testReadAllChatSubscriptions1() {
		Setup.startTest("Testing read all chat subscriptions for User 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).
				body(
				"chatSubscriptionList.chatNotificationSubscription.size()", Matchers.is(1),
				"chatSubscriptionList.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"chatSubscriptionList.chatNotificationSubscription[0].resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"chatSubscriptionList.chatNotificationSubscription[0].callbackReference.callbackData", IsEqual.equalTo("GSMA1"),
				"chatSubscriptionList.chatNotificationSubscription[0].callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"chatSubscriptionList.chatNotificationSubscription[0].callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).
				get(Setup.chatSubscriptionURL(Setup.TestUser1));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void testDeleteChatSubscriptions1() {
		Setup.startTest("Testing delete session subscriptions for User 1");
		
		String[] parts=Setup.chatSubscriptionURL[1].split(Setup.urlSplit);
		Response resp=RestAssured.expect().log().ifError().statusCode(204).delete(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void testSubscribeFileTransferNotifications1() {
		long clientCorrelator=System.currentTimeMillis();
		String requestData="{\"fileTransferSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[1]+"\",\"callbackData\":\"GSMA1\"}, \"clientCorrelator\":\""+Long.toString(clientCorrelator)+
				"\", \"duration\":900}}";
		
		Setup.startTest("Testing successful file transfer subscription for User 1");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().contentType("application/json").body(requestData).expect().log().ifError().statusCode(201).
		body(
				"fileTransferSubscription.clientCorrelator", IsEqual.equalTo(Long.toString(clientCorrelator)),
				"fileTransferSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"fileTransferSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),
				"fileTransferSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"fileTransferSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON"),
				"fileTransferSubscription.duration", Matchers.greaterThan(890)
		).post(Setup.fileTransferSubscriptionURL(Setup.TestUser1));

		Setup.fileTransferSubscriptionURL[1]=resp.jsonPath().get("fileTransferSubscription.resourceURL");

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void testReadFileTransferSubscriptionDetails1() {
		Setup.startTest("Testing read file transfer subscription for User 1");
		String[] parts=Setup.fileTransferSubscriptionURL[1].split(Setup.urlSplit);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).body(
				"fileTransferSubscription.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"fileTransferSubscription.callbackReference.callbackData", IsEqual.equalTo("GSMA1"),  
				"fileTransferSubscription.callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"fileTransferSubscription.callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).get(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void testReadAllFileTransferSubscriptions1() {
		Setup.startTest("Testing read all file transfer subscriptions for User 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).body(
				"fileTransferSubscriptionList.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
				"fileTransferSubscriptionList.fileTransferSubscription.size()", Matchers.is(1),
				"fileTransferSubscriptionList.fileTransferSubscription[0].callbackReference.callbackData", IsEqual.equalTo("GSMA1"),  
				"fileTransferSubscriptionList.fileTransferSubscription[0].callbackReference.notifyURL", IsEqual.equalTo(Setup.callbackURL[1]),
				"fileTransferSubscriptionList.fileTransferSubscription[0].callbackReference.notificationFormat", IsEqual.equalTo("JSON")
				).get(Setup.fileTransferSubscriptionURL(Setup.TestUser1));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void testDeleteFileTransferSubscriptions1() {
		Setup.startTest("Testing delete file transfer subscriptions for User 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		String[] parts=Setup.fileTransferSubscriptionURL[1].split(Setup.urlSplit);
		Response resp=RestAssured.expect().log().ifError().statusCode(204).delete(Setup.prepareForTest(parts[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	
}
