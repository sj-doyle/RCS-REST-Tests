package test.java;

import java.io.IOException;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import test.java.Test007IM.ChatMessage;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class Test006GroupChat {

	static {
		Setup.initialise(); 
		
		System.out.println("Creating subscriptions for test accounts");
		Setup.subscriptions(Setup.TestUser1, 1);
		Setup.subscriptions(Setup.TestUser2, 2);
		Setup.subscriptions(Setup.TestUser3, 3);
		Setup.subscriptions(Setup.TestUser4, 4);
		Setup.subscriptions(Setup.TestUser5, 5);
		Setup.subscriptions(Setup.TestUser6, 6);
		Setup.subscriptions(Setup.TestUser7, 7);
		Setup.subscriptions(Setup.TestUser8, 8);

		System.out.println("Creating contacts for test accounts");
		Setup.addContact(Setup.TestUser1, Setup.TestUser2Contact);
		Setup.addContact(Setup.TestUser1, Setup.TestUser3Contact);
		Setup.addContact(Setup.TestUser1, Setup.TestUser4Contact);
		Setup.addContact(Setup.TestUser1, Setup.TestUser5Contact);
		Setup.addContact(Setup.TestUser1, Setup.TestUser6Contact);
		Setup.addContact(Setup.TestUser1, Setup.TestUser7Contact);
		Setup.addContact(Setup.TestUser1, Setup.TestUser8Contact);

		//		
		System.out.println("Clearing pending notifications for test accounts");
		Setup.clearPendingNotifications(Setup.TestUser1, 1);
		Setup.clearPendingNotifications(Setup.TestUser2, 2);
		Setup.clearPendingNotifications(Setup.TestUser3, 3);
		Setup.clearPendingNotifications(Setup.TestUser4, 4);
		Setup.clearPendingNotifications(Setup.TestUser5, 5);
		Setup.clearPendingNotifications(Setup.TestUser6, 6);
		Setup.clearPendingNotifications(Setup.TestUser7, 7);
		Setup.clearPendingNotifications(Setup.TestUser8, 8);
		
	}

	static String[] sessionId=new String[9];
	static String[] sessionUrl=new String[9];
	static String[] participantId=new String[9];
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void groupChatSession1() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.majorTest("Group Chat", "Simple 3 way group chat");

		ParticipantInformation originator=new ParticipantInformation(Setup.TestUser1Contact, "Mark", true, UUID.randomUUID().toString());
		ParticipantInformation party2=new ParticipantInformation(Setup.TestUser2Contact, "Sally", false, UUID.randomUUID().toString());
		ParticipantInformation party3=new ParticipantInformation(Setup.TestUser3Contact, "Jane", false, UUID.randomUUID().toString());
		GroupChatSessionInformation chatSession=new GroupChatSessionInformation("Welcome", new ParticipantInformation[]{originator, party2, party3}, UUID.randomUUID().toString());
		Setup.startTest("Testing group chat between Users 1, 2 and 3");
		
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"groupChatSessionInformation\":"+mapper.writeValueAsString(chatSession)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);

		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1))
		).post(Setup.groupChatURL(Setup.TestUser1));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		
		try { Thread.sleep(5000); } catch (InterruptedException ie) {}
		Setup.endTest();
	}
	@Test
	public void groupChatSession2(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;//RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.chatEventNotification[0].eventType", Matchers.equalTo("Successful"),
				"notificationList.chatEventNotification[0].link.rel[0]", Matchers.equalTo("GroupChatSessionInformation"),
				"notificationList.chatEventNotification[0].sessionId", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		JsonPath jsonData=resp.jsonPath();
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		sessionId[1]=jsonData.getString("notificationList.chatEventNotification[0].sessionId");
		sessionUrl[1]=jsonData.getString("notificationList.chatEventNotification[0].link.href[0]");
		/*
		 * {"notificationList":[{"chatEventNotification": {"link": [{"rel":"GroupChatSessionInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/176893339"}],
		 * "eventType":"Successful","sessionId":"176893339"}},
		 * {"participantStatusNotification": {"link": [{"rel":"GroupChatSessionInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/176893339"}],
		 * "participant": [{"link": [{"rel":"ParticipantInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/176893339/participants/sip%3A%2B15554000003%40rcstestconnect.net"},
		 * {"rel":"ParticipantInformationStatus","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/176893339/participants/sip%3A%2B15554000003%40rcstestconnect.net/status"}],"yourown":true,"status":"Connected","name":"tel:+15554000003","address":"sip:+15554000003@rcstestconnect.net"},{"link": [{"rel":"ParticipantInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/176893339/participants/tel%3A%2B15554000002"},{"rel":"ParticipantInformationStatus","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/176893339/participants/tel%3A%2B15554000002/status"}],"yourown":true,"status":"Connected","name":"tel:+15554000002","address":"tel:+15554000002"}],"sessionId":"176893339"}}]}
		 */
		System.out.println("SessionId = "+sessionId[1]+" SessionUrl="+sessionUrl[1]);
		Setup.endTest();
	}
	@Test
	public void groupChatSession3(){
		Setup.startTest("Checking IM notifications for user 2");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;//RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification[0].link.rel[0]", Matchers.equalTo("GroupChatSessionInformation"),
				"notificationList.participantStatusNotification[0].sessionId", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		JsonPath jsonData=resp.jsonPath();
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		sessionId[2]=jsonData.getString("notificationList.participantStatusNotification[0].sessionId");
		sessionUrl[2]=jsonData.getString("notificationList.participantStatusNotification[0].link.href[0]");
		/*
		 * {"notificationList":[{"participantStatusNotification": {"link": [{"rel":"GroupChatSessionInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000002/group/1730723508"}],
		 * "sessionId":"1730723508"}},
		 * {"participantStatusNotification": {"link": [{"rel":"GroupChatSessionInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000002/group/1730723508"}],"participant": [{"link": [{"rel":"ParticipantInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000002/group/1730723508/participants/tel%3A%2B15554000003"},{"rel":"ParticipantInformationStatus","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000002/group/1730723508/participants/tel%3A%2B15554000003/status"}],"yourown":true,"status":"Connected","name":"G23","address":"tel:+15554000003"},{"link": [{"rel":"ParticipantInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000002/group/1730723508/participants/tel%3A%2B15554000001"},{"rel":"ParticipantInformationStatus","href":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000002/group/1730723508/participants/tel%3A%2B15554000001/status"}],"yourown":true,"status":"Connected","name":"G21","address":"tel:+15554000001"}],"sessionId":"1730723508"}}]}
		 */
		System.out.println("SessionId = "+sessionId[2]+" SessionUrl="+sessionUrl[2]);
		Setup.endTest();
	}
	@Test
	public void groupChatSession4(){
		Setup.startTest("Checking IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;//RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification[0].link.rel[0]", Matchers.equalTo("GroupChatSessionInformation"),
				"notificationList.participantStatusNotification[0].sessionId", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		JsonPath jsonData=resp.jsonPath();
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		sessionId[3]=jsonData.getString("notificationList.participantStatusNotification[0].sessionId");
		sessionUrl[3]=jsonData.getString("notificationList.participantStatusNotification[0].link.href[0]");
		/*
		 * {"notificationList":[
		 * {"participantStatusNotification": {"link": [{"rel":"GroupChatSessionInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/+15554000003/group/341472929"}],
		 * "sessionId":"341472929"}},
		 * {"participantStatusNotification": {"link": [{"rel":"GroupChatSessionInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/+15554000003/group/341472929"}],
		 * "participant": [
		 * {"link": [{"rel":"ParticipantInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/+15554000003/group/341472929/participants/tel:+15554000001"},
		 * {"rel":"ParticipantInformationStatus","href":"http://api.oneapi-gw.gsma.com/chat/0.1/+15554000003/group/341472929/participants/tel:+15554000001/status"}],
		 * "yourown":true,"status":"Connected","name":"G31","address":"tel:+15554000001"},
		 * {"link": [{"rel":"ParticipantInformation","href":"http://api.oneapi-gw.gsma.com/chat/0.1/+15554000003/group/341472929/participants/tel:+15554000002"},
		 * {"rel":"ParticipantInformationStatus","href":"http://api.oneapi-gw.gsma.com/chat/0.1/+15554000003/group/341472929/participants/tel:+15554000002/status"}],
		 * "yourown":true,"status":"Connected","name":"G32","address":"tel:+15554000002"}],"sessionId":"341472929"}}]}
		 */
		System.out.println("SessionId = "+sessionId[3]+" SessionUrl="+sessionUrl[3]);
		Setup.endTest();
	}
	@Test
	public void groupChatSession5() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Getting session information for user 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(201).body(
				"groupChatSessionInformation.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)+"/group/"+sessionId[1]),
				"groupChatSessionInformation.subject", Matchers.equalTo("Welcome")
		).get(Setup.groupChatSessionURL(Setup.TestUser1, sessionId[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		/*
		 * {"groupChatSessionInformation":
		 * {"subject":"Welcome","participant":[{"resourceURL":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/-872154352/participants/sip%3A%2B15554000003%40rcstestconnect.net",
		 * "status":"Connected","address":"sip:+15554000003@rcstestconnect.net"},
		 * {"resourceURL":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/-872154352/participants/tel%3A%2B15554000002",
		 * "status":"Connected","address":"tel:+15554000002"}],"resourceURL":"http://api.oneapi-gw.gsma.com/chat/0.1/%2B15554000001/group/-872154352"}}
		 */
		Setup.endTest();
	}
	@Test
	public void groupChatSession6() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Getting session information for user 2");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(201).body(
				"groupChatSessionInformation.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser2)+"/group/"+sessionId[2]),
				"groupChatSessionInformation.subject", Matchers.equalTo("Welcome")
				).get(Setup.groupChatSessionURL(Setup.TestUser2, sessionId[2]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession7() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Getting session information for user 3");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(201).body(
				"groupChatSessionInformation.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser3)+"/group/"+sessionId[3]),
				"groupChatSessionInformation.subject", Matchers.equalTo("Welcome")
				).get(Setup.groupChatSessionURL(Setup.TestUser3, sessionId[3]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession8() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Getting participants information for user 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(201).body(
				"participantList", Matchers.notNullValue(),
				"participantList.participant", Matchers.notNullValue(),
				"participantList.participant[0].status", Matchers.equalTo("Connected")
		).get(Setup.groupChatParticipantsURL(Setup.TestUser1, sessionId[1]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession9() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Getting participants information for user 2");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(201).body(
				"participantList", Matchers.notNullValue(),
				"participantList.participant", Matchers.notNullValue(),
				"participantList.participant[0].status", Matchers.equalTo("Connected")
		).get(Setup.groupChatParticipantsURL(Setup.TestUser2, sessionId[2]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession10() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Getting participants information for user 3");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(201).body(
				"participantList", Matchers.notNullValue(),
				"participantList.participant", Matchers.notNullValue(),
				"participantList.participant[0].status", Matchers.equalTo("Connected")
		).get(Setup.groupChatParticipantsURL(Setup.TestUser3, sessionId[3]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	
	@Test
	public void groupChatSession11() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Sending message from User 1");
		
		ChatMessage chatMessage=new ChatMessage("Welcome", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)+"/group/")
		).post(Setup.groupChatMessageURL(Setup.TestUser1, sessionId[1]));
		JsonPath jsonData=resp.jsonPath();

		String sendMessageStatusURL=jsonData.getString("resourceReference.resourceURL");
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		System.out.println("resourceURL = "+sendMessageStatusURL);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void groupChatSession12(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList", Matchers.nullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession13(){
		Setup.startTest("Checking IM notifications for user 2");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.equalTo(Setup.TestUser1Contact),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("Welcome")
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession14(){
		Setup.startTest("Checking IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.equalTo(Setup.TestUser1Contact),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("Welcome")
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession15() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Sending message from User 2");
		
		ChatMessage chatMessage=new ChatMessage("User 2 here", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser2)+"/group/")
		).post(Setup.groupChatMessageURL(Setup.TestUser2, sessionId[2]));
		JsonPath jsonData=resp.jsonPath();

		String sendMessageStatusURL=jsonData.getString("resourceReference.resourceURL");
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		System.out.println("resourceURL = "+sendMessageStatusURL);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void groupChatSession16(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.equalTo(Setup.TestUser2Contact),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("User 2 here")
				).post(url);
		//JsonPath jsonData=resp.jsonPath();
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession17(){
		Setup.startTest("Checking IM notifications for user 2");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList", Matchers.nullValue()
				).post(url);
		//JsonPath jsonData=resp.jsonPath();
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession18(){
		Setup.startTest("Checking IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.equalTo(Setup.TestUser2Contact),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("User 2 here")
				).post(url);
		//JsonPath jsonData=resp.jsonPath();
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void groupChatSession19() throws JsonGenerationException, JsonMappingException, IOException {
		ParticipantInformation party4=new ParticipantInformation(Setup.TestUser4Contact, "Mike", false, UUID.randomUUID().toString());
		ParticipantInformation party5=new ParticipantInformation(Setup.TestUser5Contact, "Ed", false, UUID.randomUUID().toString());
		ParticipantInformation party6=new ParticipantInformation(Setup.TestUser6Contact, "Jen", false, UUID.randomUUID().toString());
		ParticipantInformation party7=new ParticipantInformation(Setup.TestUser7Contact, "Bill", false, UUID.randomUUID().toString());
		
		Setup.majorTest("Group Chat", "Addition of users to existing group chat");

		ParticipantInformation[] participants=new ParticipantInformation[]{party4, party5, party6, party7};
		Setup.startTest("Adding User 4, 5, 6, 7");
		
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"participantList\":{\"participant\":"+mapper.writeValueAsString(participants)+"}}";
		
		System.out.println("Sending json="+jsonRequestData);
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);

		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1))
		).post(Setup.groupChatParticipantsURL(Setup.TestUser1, sessionId[1]));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		
		try { Thread.sleep(5000); } catch (InterruptedException ie) {}
		Setup.endTest();
	}
	@Test
	public void groupChatSession20(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession21(){
		Setup.startTest("Checking IM notifications for user 2");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession22(){
		Setup.startTest("Checking IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession23(){
		Setup.startTest("Checking IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification[0].link[0].rel", Matchers.equalTo("GroupChatSessionInformation"),
				"notificationList.participantStatusNotification[0].sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		JsonPath jsonData=resp.jsonPath();
		sessionId[4]=jsonData.getString("notificationList.participantStatusNotification[0].sessionId");
		System.out.println("SessionId = "+sessionId[4]);
		Setup.endTest();
	}

	@Test
	public void groupChatSession23A(){
		Setup.startTest("Sending accept session for user 4");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		String jsonRequestData="{\"participantSessionStatus\":{\"status\":\"Connected\"}}";
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError()./*statusCode(204).*/
				put(Setup.groupChatParticipantStatusURL(Setup.TestUser4, sessionId[4], Setup.TestUser4Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void groupChatSession24(){
		Setup.startTest("Checking IM notifications for user 5 - should be an invite");
		String url=(Setup.channelURL[5].split("username\\=")[0])+"username="+Setup.TestUser5;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification[0].link[0].rel", Matchers.equalTo("GroupChatSessionInformation"),
				"notificationList.participantStatusNotification[0].sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		JsonPath jsonData=resp.jsonPath();
		sessionId[5]=jsonData.getString("notificationList.participantStatusNotification[0].sessionId");
		System.out.println("SessionId = "+sessionId[5]);
		Setup.endTest();
	}
	@Test
	public void groupChatSession24A(){
		Setup.startTest("Sending accept session for user 5");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser5, Setup.applicationPassword);
		String jsonRequestData="{\"participantSessionStatus\":{\"status\":\"Connected\"}}";
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError()./*statusCode(204).*/
				put(Setup.groupChatParticipantStatusURL(Setup.TestUser5, sessionId[5], Setup.TestUser5Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession24B(){
		Setup.startTest("Checking IM notifications for user 6 - should be an invite");
		String url=(Setup.channelURL[6].split("username\\=")[0])+"username="+Setup.TestUser6;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification[0].link[0].rel", Matchers.equalTo("GroupChatSessionInformation"),
				"notificationList.participantStatusNotification[0].sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		JsonPath jsonData=resp.jsonPath();
		sessionId[6]=jsonData.getString("notificationList.participantStatusNotification[0].sessionId");
		System.out.println("SessionId = "+sessionId[6]);
		Setup.endTest();
	}
	@Test
	public void groupChatSession24C(){
		Setup.startTest("Sending accept session for user 6");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser6, Setup.applicationPassword);
		String jsonRequestData="{\"participantSessionStatus\":{\"status\":\"Connected\"}}";
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError()./*statusCode(204).*/
				put(Setup.groupChatParticipantStatusURL(Setup.TestUser6, sessionId[6], Setup.TestUser6Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession24D(){
		Setup.startTest("Checking IM notifications for user 7 - should be an invite");
		String url=(Setup.channelURL[7].split("username\\=")[0])+"username="+Setup.TestUser7;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification[0].link[0].rel", Matchers.equalTo("GroupChatSessionInformation"),
				"notificationList.participantStatusNotification[0].sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		JsonPath jsonData=resp.jsonPath();
		sessionId[7]=jsonData.getString("notificationList.participantStatusNotification[0].sessionId");
		System.out.println("SessionId = "+sessionId[7]);
		Setup.endTest();
	}
	@Test
	public void groupChatSession24E(){
		Setup.startTest("Sending decline session for user 7");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser7, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(204).
				delete(Setup.groupChatParticipantURL(Setup.TestUser7, sessionId[7], Setup.TestUser7Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession25() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Sending message from User 4");
		
		ChatMessage chatMessage=new ChatMessage("Thanks for including me", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/group/")
		).post(Setup.groupChatMessageURL(Setup.TestUser4, sessionId[4]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		JsonPath jsonData=resp.jsonPath();

		String sendMessageStatusURL=jsonData.getString("resourceReference.resourceURL");
		System.out.println("resourceURL = "+sendMessageStatusURL);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void groupChatSession26(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue(),
				"notificationList.messageNotification.chatMessage.text", Matchers.notNullValue(),
				"notificationList.messageNotification.sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession27(){
		Setup.startTest("Checking IM notifications for user 2");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue(),
				"notificationList.messageNotification.chatMessage.text", Matchers.notNullValue(),
				"notificationList.messageNotification.sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession28(){
		Setup.startTest("Checking IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue(),
				"notificationList.messageNotification.chatMessage.text", Matchers.notNullValue(),
				"notificationList.messageNotification.sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession29(){
		Setup.startTest("Checking IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0]", Matchers.nullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession30(){
		Setup.startTest("Checking IM notifications for user 5");
		String url=(Setup.channelURL[5].split("username\\=")[0])+"username="+Setup.TestUser5;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue(),
				"notificationList.messageNotification.chatMessage.text", Matchers.notNullValue(),
				"notificationList.messageNotification.sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession30A(){
		Setup.startTest("Checking IM notifications for user 6");
		String url=(Setup.channelURL[6].split("username\\=")[0])+"username="+Setup.TestUser6;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification", Matchers.notNullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue(),
				"notificationList.messageNotification.chatMessage.text", Matchers.notNullValue(),
				"notificationList.messageNotification.sessionId", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession30B(){
		Setup.startTest("Checking IM notifications for user 7");
		String url=(Setup.channelURL[7].split("username\\=")[0])+"username="+Setup.TestUser7;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0]", Matchers.nullValue(),
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void groupChatSession31() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.startTest("Sending message from User 3");
		
		ChatMessage chatMessage=new ChatMessage("I am here too", "Displayed");
		ObjectMapper mapper=new ObjectMapper();	
		String jsonRequestData="{\"chatMessage\":"+mapper.writeValueAsString(chatMessage)+"}";
		
		System.out.println("Sending json="+jsonRequestData);
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp = RestAssured.given().contentType("application/json").body(jsonRequestData).expect().log().ifError().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/group/")
		).post(Setup.groupChatMessageURL(Setup.TestUser3, sessionId[3]));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		JsonPath jsonData=resp.jsonPath();

		String sendMessageStatusURL=jsonData.getString("resourceReference.resourceURL");
		System.out.println("resourceURL = "+sendMessageStatusURL);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void groupChatSession32(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact)),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("I am here too")
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession33(){
		Setup.startTest("Checking IM notifications for user 2");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact)),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("I am here too")
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession34(){
		Setup.startTest("Checking IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList", Matchers.nullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession35(){
		Setup.startTest("Checking IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact)),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("I am here too")
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession36(){
		Setup.startTest("Checking IM notifications for user 5");
		String url=(Setup.channelURL[5].split("username\\=")[0])+"username="+Setup.TestUser5;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact)),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("I am here too")
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession36A(){
		Setup.startTest("Checking IM notifications for user 6");
		String url=(Setup.channelURL[6].split("username\\=")[0])+"username="+Setup.TestUser6;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.messageNotification[0].senderName", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3Contact)),
				"notificationList.messageNotification[0].chatMessage.text", Matchers.equalTo("I am here too")
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession36B(){
		Setup.startTest("Checking IM notifications for user 7");
		String url=(Setup.channelURL[5].split("username\\=")[0])+"username="+Setup.TestUser5;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList", Matchers.nullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void groupChatSession37() throws JsonGenerationException, JsonMappingException, IOException {
		Setup.majorTest("Group Chat", "User leaves session");
		Setup.startTest("User 3 leaves the session");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp = RestAssured.given().expect().log().ifError().statusCode(204).
				delete(Setup.groupChatParticipantURL(Setup.TestUser3, sessionId[3], Setup.TestUser3Contact));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {}
		Setup.endTest();
	}
	@Test
	public void groupChatSession38(){
		Setup.startTest("Checking IM notifications for user 1");
		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession39(){
		Setup.startTest("Checking IM notifications for user 2");
		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession40(){
		Setup.startTest("Checking IM notifications for user 3");
		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession41(){
		Setup.startTest("Checking IM notifications for user 4");
		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession42(){
		Setup.startTest("Checking IM notifications for user 5");
		String url=(Setup.channelURL[5].split("username\\=")[0])+"username="+Setup.TestUser5;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession43(){
		Setup.startTest("Checking IM notifications for user 6");
		String url=(Setup.channelURL[6].split("username\\=")[0])+"username="+Setup.TestUser6;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void groupChatSession44(){
		Setup.startTest("Checking IM notifications for user 7");
		String url=(Setup.channelURL[7].split("username\\=")[0])+"username="+Setup.TestUser7;
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response resp = RestAssured.given().expect().log().ifError().statusCode(200).body(
				"notificationList.participantStatusNotification", Matchers.notNullValue()
				).post(url);
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	//
	
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

	static class ParticipantInformation {
		String address;
		String name;
		boolean isOriginator;
		String clientCorrelator;
		String resourceURL;
		public String getAddress() {
			return address;
		}
		public String getName() {
			return name;
		}
		public boolean getIsOriginator() {
			return isOriginator;
		}
		public String getClientCorrelator() {
			return clientCorrelator;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setIsOriginator(boolean isOriginator) {
			this.isOriginator = isOriginator;
		}
		public void setClientCorrelator(String clientCorrelator) {
			this.clientCorrelator = clientCorrelator;
		}
		@JsonIgnore
		public String getResourceURL() {
			return resourceURL;
		}
		public void setResourceURL(String resourceURL) {
			this.resourceURL = resourceURL;
		}
		public ParticipantInformation(String address, String name, boolean isOriginator, String clientCorrelator) {
			this.address=address;
			this.name=name;
			this.isOriginator=isOriginator;
			this.clientCorrelator=clientCorrelator;
		}
	}
	static class GroupChatSessionInformation {
		String subject;
		ParticipantInformation[] participant;
		String clientCorrelator;
		String resourceURL;
		public String getSubject() {
			return subject;
		}
		public ParticipantInformation[] getParticipant() {
			return participant;
		}
		public String getClientCorrelator() {
			return clientCorrelator;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public void setParticipant(ParticipantInformation[] participant) {
			this.participant = participant;
		}
		public void setClientCorrelator(String clientCorrelator) {
			this.clientCorrelator = clientCorrelator;
		}
		@JsonIgnore
		public String getResourceURL() {
			return resourceURL;
		}
		public void setResourceURL(String resourceURL) {
			this.resourceURL = resourceURL;
		}
		public GroupChatSessionInformation(String subject, ParticipantInformation[] participant, String clientCorrelator) {
			this.subject=subject;
			this.participant=participant;
			this.clientCorrelator=clientCorrelator;
		}
		
	}
	

}
