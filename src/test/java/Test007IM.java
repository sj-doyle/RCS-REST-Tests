package test.java;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class Test007IM {

	static {
		Setup.initialise(); 
		
		System.out.println("Creating subscriptions for test accounts");
		Setup.subscriptions(Setup.TestUser1, 1);
		Setup.subscriptions(Setup.TestUser2, 2);
		Setup.subscriptions(Setup.TestUser3, 3);
		Setup.subscriptions(Setup.TestUser4, 4);
//		
		System.out.println("Clearing pending notifications for test accounts");
		Setup.clearPendingNotifications(Setup.TestUser1, 1);
		Setup.clearPendingNotifications(Setup.TestUser2, 2);
		Setup.clearPendingNotifications(Setup.TestUser3, 3);
		Setup.clearPendingNotifications(Setup.TestUser4, 4);
	}

	static final String INVALID="INVALID";

	public static String sendMessageStatusURL=INVALID;
	public static String receiveMessageStatusURL=INVALID;
	public static String sentMessageID=INVALID;
	public static String receiveMessageID=INVALID;
	public static String sessionID=INVALID;
	public static String receiveSessionID=INVALID;
	public static String sendSessionURL=INVALID;
	public static String receiveSessionURL=INVALID;
	
	private static void restart() {
		sendMessageStatusURL=INVALID;
		receiveMessageStatusURL=INVALID;
		sentMessageID=INVALID;
		receiveMessageID=INVALID;
		sessionID=INVALID;
		receiveSessionID=INVALID;
		sendSessionURL=INVALID;
		receiveSessionURL=INVALID;
	}
	

	
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void sessionIMChatAccept() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.majorTest("Chat/IM", "Confirmed chat between users 3 and 4");

		restart();
		Setup.startTest("Testing initiating IM chat session between User 3 and User 4");

		String requestData3="{\"chatNotificationSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[3]+"\",\"callbackData\":\"GSMA3\"},\"clientCorrelator\":\""+UUID.randomUUID().toString()+
				"\", \"duration\":900, \"confirmedChatSupported\":true, \"adhocChatSupported\":false}}";
		String requestData4="{\"chatNotificationSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
				Setup.callbackURL[4]+"\",\"callbackData\":\"GSMA3\"},\"clientCorrelator\":\""+UUID.randomUUID().toString()+
				"\", \"duration\":900, \"confirmedChatSupported\":true, \"adhocChatSupported\":false}}";

		System.out.println("Setting chat subscription for confirmed session for user 3");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.given().contentType("application/json").body(requestData3).expect().log().ifError().statusCode(201).post(Setup.chatSubscriptionURL(Setup.TestUser3));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());

		System.out.println("Setting chat subscription for confirmed session for user 4");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		resp=RestAssured.given().contentType("application/json").body(requestData4).expect().log().ifError().statusCode(201).post(Setup.chatSubscriptionURL(Setup.TestUser4));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());

		ChatSessionInformation chatSessionInformation=new ChatSessionInformation("Session based IM", Setup.TestUser3Contact, "MO", Setup.TestUser4Contact, "MT");
			
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatSessionInformation\":"+mapper.writeValueAsString(chatSessionInformation)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"chatSessionInformation.status", Matchers.equalTo("Invited"),
				"chatSessionInformation.originatorAddress", Matchers.equalTo(Setup.TestUser3Contact),
				"chatSessionInformation.tParticipantAddress", Matchers.equalTo(Setup.TestUser4Contact),
				"chatSessionInformation.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/oneToOne/"+Setup.encodedValue(Setup.TestUser4Contact))
		).post(Setup.createIMChatSessionURL(Setup.TestUser3, Setup.TestUser4Contact));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());

		JsonPath jsonData=resp.jsonPath();
		
		sendSessionURL=jsonData.getString("chatSessionInformation.resourceURL");
		System.out.println("sendSessionURL = "+sendSessionURL);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept1A(){
		Setup.startTest("Checking IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications4 = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.chatSessionInvitationNotification", Matchers.notNullValue(),
				"notificationList.messageNotification.dateTime", Matchers.notNullValue(),
				"notificationList.messageNotification.messageId", Matchers.notNullValue(),
				"notificationList.messageNotification.senderAddress[0]", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact))
				).post(url);
		System.out.println("Response = "+notifications4.getStatusCode()+" / "+notifications4.asString());
		
		JsonPath jsonData=notifications4.jsonPath();
		
		receiveSessionURL=jsonData.getString("notificationList.chatSessionInvitationNotification[0].link[0].href");
		System.out.println("Extracted receiveSessionURL="+receiveSessionURL);
		
		receiveSessionID=jsonData.getString("notificationList.messageNotification.sessionId[0]");
		System.out.println("Extracted receiveSessionID="+receiveSessionID);
		
		receiveMessageID=jsonData.getString("notificationList.messageNotification.messageId[0]");
		System.out.println("Extracted receiveMessageID="+receiveMessageID);

		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept1B(){
		Setup.startTest("Sending accept chat for user 4");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(204).
				put(Setup.chatSessionIMStatusURL(Setup.TestUser4, Setup.TestUser3Contact, receiveSessionID));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());

		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {}
		
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept2(){
		Setup.startTest("Checking IM notifications for user 3");
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		Response notifications3 = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList", Matchers.notNullValue()).post(url);
		System.out.println("Response = "+notifications3.getStatusCode()+" / "+notifications3.asString());
		
		/*
		 * {"notificationList":[{"messageStatusNotification": {"callbackData":"GSMA3","link": [{"rel":"ChatMessage","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000003/oneToOne/tel%3A%2B15554000004//messages/1352666437776-470267184"}],
		 * "status":"Delivered","messageId":"1352666437776-470267184"}},
		 * {"messageStatusNotification": {"callbackData":"GSMA3","link": [{"rel":"ChatMessage","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000003/oneToOne/tel%3A%2B15554000004//messages/1352666437776-470267184"}],
		 * "status":"Displayed","messageId":"1352666437776-470267184"}},
		 * {"chatEventNotification": {"callbackData":"GSMA3","link": [{"rel":"ChatSessionInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000003/oneToOne/tel%3A%2B15554000004/373305033"}],
		 * "eventType":"Accepted","sessionId":"373305033"}}]}

		 */
		JsonPath jsonData=notifications3.jsonPath();
		sentMessageID=jsonData.getString("notificationList.messageStatusNotification[0].messageId");
		System.out.println("Extracted messageId="+sentMessageID);
		sendSessionURL=jsonData.getString("notificationList.chatEventNotification.link[0].href[0]");
		System.out.println("Extracted sendSessionURL="+sendSessionURL);
		sessionID=jsonData.getString("notificationList.chatEventNotification.sessionId[0]");
		System.out.println("Extracted sessionId="+sessionID);
		
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept2A(){
		Setup.startTest("Checking chat session status for User 3");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		String url=Setup.chatSessionIMURL(Setup.TestUser3, Setup.TestUser4Contact, sessionID);
		System.out.println("URL = "+url);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(201).body(
				"chatSessionInformation.status", Matchers.equalTo("Connected"),
				"chatSessionInformation.tParticipantAddress", Matchers.equalTo(Setup.TestUser4Contact)
				).get(url);
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept3(){
		Setup.startTest("Checking no further IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications4 = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList", Matchers.nullValue()
				).post(url);
		System.out.println("Response = "+notifications4.getStatusCode()+" / "+notifications4.asString());

		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept4() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Sending IM chat from User 3 to User 4");
		
		ChatMessage chatMessage=new ChatMessage("hello user4", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", 
					StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/oneToOne/"+Setup.encodedValue(Setup.TestUser4Contact)+"/"+sessionID+"/messages/")
		).post(Setup.sendIMURL(Setup.TestUser3, Setup.TestUser4Contact, sessionID));
		
		sendMessageStatusURL=resp.jsonPath().getString("resourceReference.resourceURL");
		String[] parts=sendMessageStatusURL.split("/messages/");
		sentMessageID=parts[1].replaceAll("/status", "");
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		System.out.println("resourceURL = "+sendMessageStatusURL);
		System.out.println("sentMessageID = "+sentMessageID);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept5(){
		Setup.startTest("Checking IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications4 = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderAddress", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact)),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("hello user4")
				).post(url);
		System.out.println("Response = "+notifications4.getStatusCode()+" / "+notifications4.asString());
		/*
		 * {"notificationList":[{"messageNotification": 
		 * {"callbackData":"GSMA3","link": 
		 * [{"rel":"MessageStatusReport","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000004/oneToOne/tel%3A%2B15554000003/-797939895/messages/1352475373833-1790113032/status"}],
		 * "dateTime":"2012-11-09T15:36:13Z","chatMessage":{"text":"hello user4"},"sessionId":"-797939895","messageId":"1352475373833-1790113032",
		 * "senderAddress":"tel:+15554000003"}}]}
		 */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept6(){
		Setup.startTest("Checking IM notifications for user 3");
		System.out.println("Expecting messageId="+sentMessageID);
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications3 = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+notifications3.getStatusCode()+" / "+notifications3.asString());
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept7() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Reply message IM chat between User 3 and User 4");
		
		sendMessageStatusURL=INVALID;
		
		ChatMessage chatMessage=new ChatMessage("I am good", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/oneToOne/"+Setup.encodedValue(Setup.TestUser3Contact))
		).post(Setup.sendIMURL(Setup.TestUser4, Setup.TestUser3Contact, receiveSessionID));
		
		sendMessageStatusURL=resp.jsonPath().getString("resourceReference.resourceURL");
		String[] parts=sendMessageStatusURL.split("/messages/");
		sentMessageID=parts[1].replaceAll("/status", "");
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		System.out.println("resourceURL = "+sendMessageStatusURL);
		System.out.println("sentMessageID = "+sentMessageID);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept8() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 3 has received a chat message");
		
		receiveMessageStatusURL=INVALID;
		
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification.chatMessage.text",  Matchers.hasItem("I am good"),
				"notificationList.messageNotification.sessionId",  Matchers.hasItem(sessionID),
				"notificationList.messageNotification.messageId",  Matchers.notNullValue(),
				"notificationList.messageNotification.senderAddress",  Matchers.hasItem(Setup.TestUser4Contact),
				"notificationList.messageNotification.link",  Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());
		
		receiveMessageStatusURL=response.jsonPath().getString("notificationList.messageNotification[0].link[0].href");
		System.out.println("message status URL = "+receiveMessageStatusURL);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept9(){
		Setup.startTest("Checking the sender status");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response status = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"messageStatusReport.status", Matchers.equalTo("DISPLAYED")
				).get(Setup.prepareForTest(sendMessageStatusURL));
		System.out.println("Response = "+status.getStatusCode()+" / "+status.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept10(){
		Setup.startTest("Checking the receiver status");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response status = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"messageStatusReport.status", Matchers.equalTo("DISPLAYED")
				).get(Setup.prepareForTest(receiveMessageStatusURL));
		System.out.println("Response = "+status.getStatusCode()+" / "+status.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept11(){
		Setup.startTest("Checking IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications2 = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageStatusNotification[0].status",  Matchers.equalTo("Delivered")
				).post(url);
		/*
		 * {"notificationList":[{"messageStatusNotification": {"callbackData":"GSMA3","link": [{"rel":"ChatMessage","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000004/oneToOne/sip%3A%2B15554000003%40rcstestconnect.net/637088086/messages/1352667703567--1091968228"}],
		 * "status":"Delivered","messageId":"1352667703567--1091968228"}},{"messageStatusNotification": {"callbackData":"GSMA3","link": [{"rel":"ChatMessage","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000004/oneToOne/sip%3A%2B15554000003%40rcstestconnect.net/637088086/messages/1352667703567--1091968228"}],"status":"Displayed","messageId":"1352667703567--1091968228"}}]}
		 */
		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept12(){
		Setup.startTest("Checking no further IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications3 = RestAssured.given().expect().log().ifError().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
		System.out.println("Response = "+notifications3.getStatusCode()+" / "+notifications3.asString());
		Setup.endTest();
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void sessionIMChatAccept13() throws JsonGenerationException, JsonMappingException, IOException{
		Setup.majorTest("Chat/IM", "isComposing functionality between users 3 and 4");
		
		Setup.startTest("Send isComposing from User 3");
		IsComposing isComposing=new IsComposing("active", new java.util.Date(), "text/plain", 60);
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"isComposing\":"+mapper.writeValueAsString(isComposing)+"}";

		System.out.println("Sending "+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(204).
				post(Setup.sendIMURL(Setup.TestUser3, Setup.TestUser4Contact, sessionID));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept14() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 4 has received composing notification");
		
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].isComposing.refresh",  Matchers.equalTo(60),
				"notificationList.messageNotification[0].isComposing.status",  Matchers.equalTo("active"),
				"notificationList.messageNotification[0].sessionId",  Matchers.equalTo(receiveSessionID),
				"notificationList.messageNotification[0].senderAddress",  Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact))
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept15() throws JsonGenerationException, JsonMappingException, IOException{
		Setup.startTest("Send isComposing from User 4");
		IsComposing isComposing=new IsComposing("active", new java.util.Date(), "text/plain", 60);
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"isComposing\":"+mapper.writeValueAsString(isComposing)+"}";

		System.out.println("Sending "+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(204).
				post(Setup.sendIMURL(Setup.TestUser4, Setup.TestUser3Contact, receiveSessionID));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept16() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 3 has received composing notification");
		
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].isComposing.refresh",  Matchers.equalTo(60),
				"notificationList.messageNotification[0].isComposing.status",  Matchers.equalTo("active"),
				"notificationList.messageNotification[0].sessionId",  Matchers.equalTo(sessionID),
				"notificationList.messageNotification[0].senderAddress",  Matchers.containsString(Setup.cleanPrefix(Setup.TestUser4Contact))
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept17() throws JsonGenerationException, JsonMappingException, IOException{
		Setup.startTest("Send isComposing idle from User 3");
		IsComposing isComposing=new IsComposing("idle", new java.util.Date(), "text/plain", 60);
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"isComposing\":"+mapper.writeValueAsString(isComposing)+"}";

		System.out.println("Sending "+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(204).
				post(Setup.sendIMURL(Setup.TestUser3, Setup.TestUser4Contact, sessionID));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept18() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 4 has received composing notification");
		
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].isComposing.refresh",  Matchers.equalTo(60),
				"notificationList.messageNotification[0].isComposing.status",  Matchers.equalTo("idle"),
				"notificationList.messageNotification[0].sessionId",  Matchers.equalTo(receiveSessionID),
				"notificationList.messageNotification[0].senderAddress",  Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact))
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept19() throws JsonGenerationException, JsonMappingException, IOException{
		Setup.startTest("Send isComposing from User 4");
		IsComposing isComposing=new IsComposing("idle", new java.util.Date(), "text/plain", 60);
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"isComposing\":"+mapper.writeValueAsString(isComposing)+"}";

		System.out.println("Sending "+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(204).
				post(Setup.sendIMURL(Setup.TestUser4, Setup.TestUser3Contact, receiveSessionID));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept20() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 3 has received composing notification");
		
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].isComposing.refresh",  Matchers.equalTo(60),
				"notificationList.messageNotification[0].isComposing.status",  Matchers.equalTo("idle"),
				"notificationList.messageNotification[0].sessionId",  Matchers.equalTo(sessionID),
				"notificationList.messageNotification[0].senderAddress",  Matchers.containsString(Setup.cleanPrefix(Setup.TestUser4Contact))
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());
		Setup.endTest();
	}
	@Test
	public void sessionIMChatAccept21() throws JsonGenerationException, JsonMappingException, IOException{
		Setup.startTest("Send invalid isComposing from User 4");
		IsComposing isComposing=new IsComposing("rubbish", new java.util.Date(), "text/plain", 60);
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"isComposing\":"+mapper.writeValueAsString(isComposing)+"}";

		System.out.println("Sending "+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(400).
				post(Setup.sendIMURL(Setup.TestUser4, Setup.TestUser3Contact, receiveSessionID));
		/*
		 * 400 / {"requestError": {"serviceException": {
		 * "messageId":"SVC002","variables": ["State is not valid. Received 'rubbish'"],"text":"Invalid input value for message. Part %0"}}}
		 */
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept22() throws JsonGenerationException, JsonMappingException, IOException{
		Setup.startTest("Close chat by User 4");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(204).
				delete(Setup.chatSessionIMURL(Setup.TestUser4, Setup.TestUser3Contact, receiveSessionID));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}	
	@Test
	public void sessionIMChatAccept23() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 3 has received chat closed notification");
		
		System.out.println("sessionID="+sessionID);
		System.out.println("receiveSessionID="+receiveSessionID);
		
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.chatEventNotification.eventType",  Matchers.hasItem("SessionEnded"),
				"notificationList.chatEventNotification.sessionId",  Matchers.hasItem(sessionID),
				"notificationList.chatEventNotification.link[0].rel",  Matchers.hasItem("ChatSessionInformation")
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());		
		Setup.endTest();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void adhocIMChat() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.majorTest("Chat/IM", "Adhoc chat between users 1 and 2");
		
		restart();
		Setup.startTest("Testing initiating IM chat between User 1 and User 2");
		
		ChatMessage chatMessage=new ChatMessage("hello", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)+"/oneToOne/"+Setup.encodedValue(Setup.TestUser2Contact))
		).post(Setup.sendIMURL(Setup.TestUser1, Setup.TestUser2Contact, "adhoc"));
		
		sendMessageStatusURL=resp.jsonPath().getString("resourceReference.resourceURL");
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		System.out.println("resourceURL = "+sendMessageStatusURL);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void adhocIMChat1() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 2 has received a chat message");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		System.out.println("URL "+url);
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification.chatMessage.text",  Matchers.hasItem("hello"),
				"notificationList.messageNotification.sessionId",  Matchers.hasItem("adhoc"),
				"notificationList.messageNotification.messageId",  Matchers.notNullValue(),
				"notificationList.messageNotification.senderAddress",  Matchers.hasItem(Setup.TestUser1Contact),
				"notificationList.messageNotification.link",  Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());
		
		receiveMessageStatusURL=response.jsonPath().getString("notificationList.messageNotification[0].link.href[0]");
		System.out.println("message status URL = "+receiveMessageStatusURL);
		Setup.endTest();
	}
	@Test
	public void adhocIMChat2(){
		Setup.startTest("Checking the sender status");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response status = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"messageStatusReport.status", Matchers.is("DISPLAYED")
				).get(sendMessageStatusURL);
		System.out.println("Response = "+status.getStatusCode()+" / "+status.asString());
		Setup.endTest();
	}	
	@Test
	public void adhocIMChat3(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications1 = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.chatEventNotification.eventType", Matchers.hasItem("Accepted"),
				"notificationList.chatEventNotification.sessionId", Matchers.hasItem("adhoc")
				).post(url);
		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
		Setup.endTest();
	}
	@Test
	public void adhocIMChat4(){
		Setup.startTest("Checking no further IM notifications for user 2 ");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications2 = RestAssured.given().expect().log().ifError().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
		Setup.endTest();
	}
	@Test
	public void adhocIMChat5() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Reply message IM chat between User 1 and User 2");
		
		sendMessageStatusURL=INVALID;
		
		ChatMessage chatMessage=new ChatMessage("how are you", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser2)+"/oneToOne/"+Setup.encodedValue(Setup.TestUser1Contact))
		).post(Setup.sendIMURL(Setup.TestUser2, Setup.TestUser1Contact, "adhoc"));
		
		sendMessageStatusURL=resp.jsonPath().getString("resourceReference.resourceURL");
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		System.out.println("resourceURL = "+sendMessageStatusURL);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void adhocIMChat6() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Checking that User 1 has received a chat message");
		
		receiveMessageStatusURL=INVALID;
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response response = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification.chatMessage.text",  Matchers.hasItem("how are you"),
				"notificationList.messageNotification.sessionId",  Matchers.hasItem("adhoc"),
				"notificationList.messageNotification.messageId",  Matchers.notNullValue(),
				"notificationList.messageNotification.senderAddress",  Matchers.hasItem(Setup.TestUser2Contact),
				"notificationList.messageNotification.link",  Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+response.getStatusCode()+" / "+response.asString());
		
		receiveMessageStatusURL=response.jsonPath().getString("notificationList.messageNotification[0].link[0].href");
		System.out.println("message status URL = "+receiveMessageStatusURL);
		Setup.endTest();
	}
	@Test
	public void adhocIMChat7(){
		Setup.startTest("Checking the sender status");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response status = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"messageStatusReport.status", Matchers.is("DISPLAYED")
				).get(sendMessageStatusURL);
		System.out.println("Response = "+status.getStatusCode()+" / "+status.asString());
		Setup.endTest();
	}	
	@Test
	public void adhocIMChat8(){
		Setup.startTest("Checking IM notifications for user 2");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		Response notifications2 = RestAssured.given().expect().log().ifError().statusCode(200)./*body(
				"notificationList.chatEventNotification.eventType", Matchers.hasItem("Accepted"),
				"notificationList.chatEventNotification.sessionId", Matchers.hasItem("adhoc")
				).*/post(url);
		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
		Setup.endTest();
	}
	@Test
	public void adhocIMChat9(){
		Setup.startTest("Checking no further IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications1 = RestAssured.given().expect().log().ifError().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
		Setup.endTest();
		
	}
	
	public class ChatMessage {
		String text;
		String reportRequest;
		String resourceURL;
		
		public String getText() {
			return text;
		}

		public String getReportRequest() {
			return reportRequest;
		}

		@JsonIgnore
		public String getResourceURL() {
			return resourceURL;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setReportRequest(String reportRequest) {
			this.reportRequest = reportRequest;
		}

		public void setResourceURL(String resourceURL) {
			this.resourceURL = resourceURL;
		}

		public ChatMessage(String text, String reportRequest) {
			this.text=text;
			this.reportRequest=reportRequest;
		}
	}

	public class ChatSessionInformation {
		String subject;
		String originatorAddress;
		String originatorName;
		String tParticipantAddress;
		String tParticipantName;
		String status;
		String resourceURL;
		public String getSubject() {
			return subject;
		}
		public String getOriginatorAddress() {
			return originatorAddress;
		}
		public String getOriginatorName() {
			return originatorName;
		}
		public String gettParticipantAddress() {
			return tParticipantAddress;
		}
		public String gettParticipantName() {
			return tParticipantName;
		}
		@JsonIgnore
		public String getStatus() {
			return status;
		}
		@JsonIgnore
		public String getResourceURL() {
			return resourceURL;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public void setOriginatorAddress(String originatorAddress) {
			this.originatorAddress = originatorAddress;
		}
		public void setOriginatorName(String originatorName) {
			this.originatorName = originatorName;
		}
		public void settParticipantAddress(String tParticipantAddress) {
			this.tParticipantAddress = tParticipantAddress;
		}
		public void settParticipantName(String tParticipantName) {
			this.tParticipantName = tParticipantName;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public void setResourceURL(String resourceURL) {
			this.resourceURL = resourceURL;
		}
		
		public ChatSessionInformation(String subject, String originatorAddress, String originatorName, String tParticipantAddress, String tParticipantName) {
			this.subject=subject;
			this.originatorAddress=originatorAddress;
			this.originatorName=originatorName;
			this.tParticipantAddress=tParticipantAddress;
			this.tParticipantName=tParticipantName;
		}
		
	}
	
	public class IsComposing {
		String state;
		java.util.Date lastActive;
		String contentType;
		int refresh;
		boolean outputISO8601=false;
		public String getState() {
			return state;
		}
//		@JsonIgnore
		public String getLastActive() {
			String dt=null;
			if (lastActive!=null) {
				TimeZone zone=TimeZone.getTimeZone("UTC");
				Calendar c=Calendar.getInstance(zone);
				c.setTime(lastActive);
				dt=DatatypeConverter.printDateTime(c).substring(0,19)+"Z";
			}
			return dt;
		}
		public String getContentType() {
			return contentType;
		}
		public int getRefresh() {
			return refresh;
		}
		public void setState(String state) {
			this.state = state;
		}
		public void setLastActive(java.util.Date lastActive) {
			this.lastActive = lastActive;
		}
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}
		public void setRefresh(int refresh) {
			this.refresh = refresh;
		}
		
		public IsComposing(String state, java.util.Date lastActive, String contentType, int refresh) {
			this.state=state;
			this.lastActive=lastActive;
			this.contentType=contentType;
			this.refresh=refresh;
		}
	}
	
	
}
