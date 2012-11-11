package test.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class Test005FileTransfer {

	static {
		Setup.initialise(); 
		
		System.out.println("Creating subscriptions for test accounts");
		Setup.subscriptions(Setup.TestUser1, 1);
		Setup.subscriptions(Setup.TestUser2, 2);
		Setup.subscriptions(Setup.TestUser3, 3);
		Setup.subscriptions(Setup.TestUser4, 4);
		
		System.out.println("Clearing pending notifications for test accounts");
		Setup.clearPendingNotifications(Setup.TestUser1, 1);
		Setup.clearPendingNotifications(Setup.TestUser2, 2);
		Setup.clearPendingNotifications(Setup.TestUser3, 3);
		Setup.clearPendingNotifications(Setup.TestUser4, 4);
	}
	
	static final String INVALID="INVALID";
	
	static String senderSessionId=INVALID;
	static String recipientSessionId=INVALID;
	static String attachmentURL1=INVALID;
	static String savedResourceURL=INVALID;

	private static void restart() {
		senderSessionId=INVALID;
		recipientSessionId=INVALID;
		attachmentURL1=INVALID;
		savedResourceURL=INVALID;
	}
	
//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Test
//	public void testSendFileURL1() {
//		Setup.majorTest("File Transfer", "Send via URL (confirmed accept)");
//
//		restart();
//		
//		String rqdata="{\"fileTransferSessionInformation\": {\"fileInformation\": {\"fileDescription\": \"This is my latest picture\",\"fileDisposition\": \"Render\","+
//					"\"fileSelector\": {\"name\": \"tux.png\",\"size\": 56320,\"type\": \"image/png\"},\"fileURL\": \"http://tux.crystalxp.net/png/brightknight-doraemon-tux-3704.png\"},"+
//					"\"originatorAddress\": \""+Setup.TestUser3Contact+"\",\"originatorName\": \"G3\",\"receiverAddress\": \""+Setup.TestUser4Contact+"\",\"receiverName\": \"G4\"}}";
//
//		Setup.startTest("Testing file send via URL from User 3 to User 4");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
//		Response resp = RestAssured.given().body(rqdata).expect().statusCode(201).body(
//				"resourceReference.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser3))
//		).post(Setup.sendFileURL(Setup.TestUser3));
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		
//		String resourceURL=resp.jsonPath().getString("resourceReference.resourceURL");
//		String[] parts=resourceURL.split("/sessions/");
//		senderSessionId=parts[1];
//		System.out.println("Sender sessionId="+senderSessionId+" from resourceURL="+resourceURL);
//
//		System.out.println("Sleeping ...");
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}	
//	@Test
//	public void testSendFileURL2(){
//		Setup.startTest("Seeing if any notifications for user 4");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		
//		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
//		System.out.println("Sending request to "+url);
//		JsonPath jsonData=null;
//		try {
//			Response notifications = RestAssured.given().urlEncodingEnabled(true).expect().statusCode(200).body(
//					"notificationList.ftSessionInvitationNotification[0].fileInformation.resourceURL", Matchers.notNullValue(),
//					"notificationList.ftSessionInvitationNotification[0].sessionId", Matchers.notNullValue(),
//					"notificationList.ftSessionInvitationNotification[0].originatorAddress", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser3)),
//					"notificationList.ftSessionInvitationNotification[0].receiverName", Matchers.equalTo(Setup.TestUser4),
//					"notificationList.ftSessionInvitationNotification[0].fileInformation.fileSelector.name",  Matchers.notNullValue(),
//					"notificationList.ftSessionInvitationNotification[0].fileInformation.fileSelector.type",  Matchers.equalTo("image/png")
//					).post(url);
//			System.out.println("response = "+notifications.getStatusCode()+" / "+notifications.asString());
//			
//			jsonData=notifications.jsonPath();
//		} catch (Exception e) {
//			System.out.println("Exception "+e.getMessage());
//		}
//		recipientSessionId=jsonData.get("notificationList.ftSessionInvitationNotification[0].sessionId");
//		attachmentURL1=jsonData.get("notificationList.ftSessionInvitationNotification[0].fileInformation.fileURL");
//
//		System.out.println("Receiver session = "+recipientSessionId);
//		System.out.println("File name = "+jsonData.get("notificationList.ftSessionInvitationNotification[0].fileInformation.fileSelector.name"));
//		System.out.println("attachmentURL1 = "+attachmentURL1);
//		Setup.endTest();
//	}	
//	@Test
//	public void testSendFileURL3(){
//		
//		String acceptData="{\"receiverSessionStatus\": {\"status\": \"Connected\"}}";
//		
//		Setup.startTest("Sending accept for User 4");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
//		Response accept=RestAssured.given().body(acceptData).urlEncodingEnabled(true).expect().statusCode(204).post(Setup.fileTransferStatusURL(Setup.TestUser4,recipientSessionId));
//		System.out.println("Accept response = "+accept.getStatusCode()+" / "+accept.asString());
//
//		System.out.println("Sleeping ...");
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileURL4(){
//		Setup.startTest("Checking notifications for user 3");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
//		System.out.println("Sending request to "+url);
//		Response notifications = RestAssured.given().urlEncodingEnabled(true).expect().statusCode(200).body(
//				"notificationList.receiverAcceptanceNotification[0].receiverAddress",  Matchers.equalTo(Setup.TestUser4Contact),
//				"notificationList.receiverAcceptanceNotification[0].receiverSessionStatus.status",  Matchers.equalTo("Connected"),
//				"notificationList.receiverAcceptanceNotification[0].sessionId",  Matchers.equalTo(senderSessionId)
//				).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileURL4A(){
//		Setup.startTest("Ensuring no outstanding notifications for user 3");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
//		Response notifications = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileURL5(){
//		Setup.startTest("Getting file URL notification");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
//		Response notifications = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.notNullValue()).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		JsonPath jsonData=notifications.jsonPath();
//		attachmentURL1=jsonData.get("notificationList.fileNotification[0].fileInformation.fileURL");
//		System.out.println("attachmentURL1 = "+attachmentURL1);
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileURL6(){
//		Setup.startTest("Receiving attachment");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response fileResponse=RestAssured.given().urlEncodingEnabled(false).expect().statusCode(200).get(attachmentURL1);
//		System.out.println("Content type = "+fileResponse.getContentType());
//		System.out.println("Content disposition = "+fileResponse.getHeader("Content-Disposition"));
//		System.out.println("Content length = "+fileResponse.getHeader("Content-Length"));
//		Setup.endTest();
//	}
//	
//	@Test
//	public void testSendFileURL7(){
//		Setup.startTest("Ensuring no outstanding notifications for user 3");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[3].split("username\\=")[0])+"username="+Setup.TestUser3;
//		Response notifications = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//
//	@Test
//	public void testSendFileURL8(){
//		Setup.startTest("Ensuring no outstanding notifications for user 4");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[4].split("username\\=")[0])+"username="+Setup.TestUser4;
//		Response notifications = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//
//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Test
//	public void testSendFileAutoURL1() {
//		Setup.majorTest("File Transfer", "Send via URL (auto accept)");
//
//		restart();
//		
//		String rqdata="{\"fileTransferSessionInformation\": {\"fileInformation\": {\"fileDescription\": \"This is my latest picture\",\"fileDisposition\": \"Render\","+
//					"\"fileSelector\": {\"name\": \"tux.png\",\"size\": 56320,\"type\": \"image/png\"},\"fileURL\": \"http://tux.crystalxp.net/png/brightknight-doraemon-tux-3704.png\"},"+
//					"\"originatorAddress\": \""+Setup.TestUser1Contact+"\",\"originatorName\": \"G1\",\"receiverAddress\": \""+Setup.TestUser2Contact+"\",\"receiverName\": \"G2\"}}";
//
//		Setup.startTest("Testing file send via URL from User 1 to User 2");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response resp = RestAssured.given().body(rqdata).expect().statusCode(201).body(
//				"resourceReference.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1))
//		).post(Setup.sendFileURL(Setup.TestUser1));
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		
//		String resourceURL=resp.jsonPath().getString("resourceReference.resourceURL");
//		String[] parts=resourceURL.split("/sessions/");
//		senderSessionId=parts[1];
//		System.out.println("Sender sessionId="+senderSessionId+" from resourceURL="+resourceURL);
//
//		System.out.println("Sleeping ...");
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}	
//	@Test
//	public void testSendFileAutoURL2(){
//		Setup.startTest("Seeing if any notifications for user 2");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		
//		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
//		System.out.println("Sending request to "+url);
//		JsonPath jsonData=null;
//		Response notifications = RestAssured.given().urlEncodingEnabled(true).expect().statusCode(200).body(
//				"notificationList.fileNotification[0].fileInformation.resourceURL", Matchers.notNullValue(),
//				"notificationList.fileNotification[0].sessionId", Matchers.notNullValue(),
//				"notificationList.fileNotification[0].fileInformation.fileSelector.name",  Matchers.notNullValue(),
//				"notificationList.fileNotification[0].fileInformation.fileSelector.type",  Matchers.equalTo("image/png")
//				).post(url);
////TODO - not sending originator address		
//		/*
//		 * {"notificationList":[{
//		 * "fileNotification": {"callbackData":"+15554000002",
//		 * "fileInformation":{"fileURL":"http://api.oneapi-gw.gsma.com/repo/user/ObtainUserFile?username=%2B15554000002&filename=tux1352636678534.png",
//		 * "fileDisposition":"Render","fileSelector":{"size":55620,"type":"image/png","name":"tux1352636678534.png"},
//		 * "resourceURL":"http://api.oneapi-gw.gsma.com/filetransfer/0.1/%2B15554000002/sessions/-137432193"},"sessionId":"-137432193"}}]}
//		 */
//		System.out.println("response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		jsonData=notifications.jsonPath();
//
//		recipientSessionId=jsonData.get("notificationList[0].fileNotification.sessionId");
//		System.out.println("Receiver session = "+recipientSessionId);
//		attachmentURL1=jsonData.get("notificationList[0].fileNotification.fileInformation.fileURL");
//		System.out.println("attachmentURL1 = "+attachmentURL1);
//		System.out.println("File name = "+jsonData.get("notificationList[0].fileNotification.fileInformation.fileSelector.name"));
//		
//		Setup.endTest();
//	}	
//	@Test
//	public void testSendFileAutoURL4(){
//		Setup.startTest("Checking notifications for user 1");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
//		System.out.println("Sending request to "+url);
//		Response notifications = RestAssured.given().urlEncodingEnabled(true).expect().statusCode(200).body(
//				"notificationList.receiverAcceptanceNotification[0].receiverAddress",  Matchers.equalTo(Setup.TestUser2Contact),
//				"notificationList.receiverAcceptanceNotification[0].receiverSessionStatus.status",  Matchers.equalTo("Connected"),
//				"notificationList.receiverAcceptanceNotification[0].sessionId",  Matchers.equalTo(senderSessionId)
//				).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileAutoURL5(){
//		
//		Setup.startTest("Checking no further notifications for user 2");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response notifications = RestAssured.given().expect().statusCode(200).body(
//				"notificationList",  Matchers.nullValue()
//				).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileAutoURL6(){
//		
//		Setup.startTest("Receiving attachment");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response fileResponse=RestAssured.given().urlEncodingEnabled(false).expect().statusCode(200).get(attachmentURL1);
//		System.out.println("Content type = "+fileResponse.getContentType());
//		System.out.println("Content disposition = "+fileResponse.getHeader("Content-Disposition"));
//		System.out.println("Content length = "+fileResponse.getHeader("Content-Length"));
////TODO - returning text/plain content type		
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileAutoURL7(){
//		Setup.startTest("Ensuring no outstanding notifications for user 1");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[1].split("username\\=")[0])+"username="+Setup.TestUser1;
//		Response notifications = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileAutoURL8(){
//		Setup.startTest("Ensuring no outstanding notifications for user 2");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+Setup.TestUser2;
//		Response notifications = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(url);
//		System.out.println("Response = "+notifications.getStatusCode()+" / "+notifications.asString());
//		Setup.endTest();
//	}
//
//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Test
//	public void testSendFileURLMismatchedSender() {
//		Setup.majorTest("File Transfer", "Checking mismatched sender is not allowed");
//
//		restart();
//		String rqdata="{\"fileTransferSessionInformation\": {\"fileInformation\": {\"fileDescription\": \"This is my latest picture\",\"fileDisposition\": \"Render\","+
//					"\"fileSelector\": {\"name\": \"tux.png\",\"size\": 56320,\"type\": \"image/png\"},\"fileURL\": \"http://tux.crystalxp.net/png/brightknight-doraemon-tux-3704.png\"},"+
//					"\"originatorAddress\": \""+Setup.TestUser1Contact+"\",\"originatorName\": \"G1\",\"receiverAddress\": \""+Setup.TestUser2Contact+"\",\"receiverName\": \"G2\"}}";
//
//		Setup.startTest("Testing file send does not happen User 1 to User 2 as requested by User 4");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
//		Response resp = RestAssured.given().body(rqdata).expect().statusCode(400).body(
//				"requestError.serviceException.messageId", Matchers.equalTo("SVC002"),
//				"requestError.serviceException.variables", Matchers.hasItem("Originator's Address is wrong")
//		).post(Setup.sendFileURL(Setup.TestUser4));
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileURLMismatchedSender1(){
//		Setup.startTest("Ensuring no outstanding notifications for user 1");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response notifications1 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[1]);
//		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendFileURLMismatchedSender2(){
//		Setup.startTest("Ensuring no outstanding notifications for user 2");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		Setup.endTest();
//	}

////	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////	@Test
////	public void testReadFileTransferSession1() {
////		Setup.majorTest("File Transfer", "Reading file transfer");
////		restart();
////		Setup.startTest("Testing read file transfer session information");
////		String rqdata="{\"fileTransferSessionInformation\": {\"fileInformation\": {\"fileDescription\": \"This is my latest picture\",\"fileDisposition\": \"Render\","+
////					"\"fileSelector\": {\"name\": \"tux.png\",\"size\": 56320,\"type\": \"image/png\"},\"fileURL\": \"http://tux.crystalxp.net/png/brightknight-doraemon-tux-3704.png\"},"+
////					"\"originatorAddress\": \""+Setup.TestUser1Contact+"\",\"originatorName\": \"G1\",\"receiverAddress\": \""+Setup.TestUser2Contact+"\",\"receiverName\": \"G2\"}}";
////		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
////		Response resp = RestAssured.given().body(rqdata).expect().statusCode(201).body(
////				"resourceReference.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1))
////		).post(Setup.sendFileURL(Setup.TestUser1));
////		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
////		JsonPath jsonData=resp.jsonPath();
////		savedResourceURL=jsonData.get("resourceReference.resourceURL");
////		System.out.println("Message resourceURL="+savedResourceURL);
////		try {
////			Thread.sleep(2500);
////		} catch (InterruptedException ie) {}
////		Setup.endTest();
////	}
////	@Test
////	public void testReadFileTransferSession2() {		
////		String[] parts=Setup.prepareForTest(savedResourceURL).split(Setup.urlSplit);
////		Setup.startTest("File transfer session info URL="+parts[1]);
////		
////		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
////		Response resp = RestAssured.expect().statusCode(200).body(
////				"fileTransferSessionInformation.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)),
////				"fileTransferSessionInformation.fileInformation.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)),
////				"fileTransferSessionInformation.status", Matchers.equalTo("Invited"),
////				"fileTransferSessionInformation.receiverAddress",  Matchers.equalTo(Setup.TestUser2Contact)
////		).get(parts[1]);		
////		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
////		Setup.endTest();
////	}
////	@Test
////	public void testReadFileTransferSession3() {
////		String[] parts=Setup.prepareForTest(savedResourceURL).split(Setup.urlSplit);
////		Setup.startTest("File transfer session info URL="+parts[1]);
////		
////		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
////		Response resp = RestAssured.expect().statusCode(204).delete(parts[1]);
////		//TODO - this delete request is accepted, it's not clear from the documentation if it should be
////		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
////		Setup.endTest();
////	}
////	@Test
////	public void testReadFileTransferSession4(){
////		Setup.startTest("Checking that the notification is still received");
////		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
////		Response notifications2 = RestAssured.given().expect().statusCode(200)./*body(
////				"notificationList.ftSessionInvitationNotification.sessionId", Matchers.notNullValue(),
////				"notificationList.ftSessionInvitationNotification.originatorAddress", Matchers.hasItem(Setup.TestUser1Contact),
////				"notificationList.ftSessionInvitationNotification.receiverName", Matchers.hasItem(Setup.TestUser2)
////				).*/post(Setup.channelURL[2]);
////		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
////		
////		JsonPath jsonData=notifications2.jsonPath();
////		
////		recipientSessionId=jsonData.get("notificationList.ftSessionInvitationNotification[0].sessionId");
////		System.out.println("Receiver session = "+recipientSessionId);
////		Setup.endTest();
////	}	
////	@Test
////	public void testReadFileTransferSession6(){
////		Setup.startTest("Sending decline for User 2");
////		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
////		Response accept=RestAssured.given().expect()./*statusCode(204).*/delete(Setup.fileTransferSessionURL(Setup.TestUser2,recipientSessionId));
////		System.out.println("Decline response = "+accept.getStatusCode()+" / "+accept.asString());
////		System.out.println("Sleeping ...");
////		try {
////			Thread.sleep(1000);
////		} catch (InterruptedException ie) {}
////		Setup.endTest();
////	}
////	@Test
////	public void testReadFileTransferSession7(){
////		Setup.startTest("Checking for declined file transfer notifications for user 1");
////		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
////		Response notifications1 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[1]);
//////TODO - should there be a notification even though originator deleted
////		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
////		Setup.endTest();
////	}
////	@Test
////	public void testReadFileTransferSession8(){
////		Setup.startTest("Ensuring no outstanding notifications for user 2");
////		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
////		Response notifications2 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[2]);
////		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
////		Setup.endTest();
////	}
//	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testSendMultipartSingleAttachment() {
		Setup.majorTest("File Transfer", "Sending using multipart attachment");
		restart();
		Setup.startTest("Testing send multipart with single attachment");
		String rqdata="{\"fileTransferSessionInformation\": {\"fileInformation\": {\"fileDescription\": \"Logo\",\"fileDisposition\": \"Render\","+
				"\"fileSelector\": {\"name\": \"rclogo.png\",\"type\": \"image/png\"}},"+
				"\"originatorAddress\": \""+Setup.TestUser1Contact+"\",\"originatorName\": \"G1\",\"receiverAddress\": \""+Setup.TestUser2Contact+"\",\"receiverName\": \"G2\"}}";
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp = RestAssured.given().multiPart("root-fields", rqdata, "application/json").multiPart("attachments", new File("misc/rclogo.png")).expect().statusCode(201).body(
				"resourceReference.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1))
		).post(Setup.sendFileURL(Setup.TestUser1));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		JsonPath jsonData=resp.jsonPath();
		savedResourceURL=jsonData.get("resourceReference.resourceURL");
		System.out.println("Message resourceURL="+savedResourceURL);
		String[] parts=savedResourceURL.split("/sessions/");
		senderSessionId=parts[1];
		System.out.println("senderSessionId="+senderSessionId);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException ie) {}
		Setup.endTest();
	}
	@Test
	public void testSendMultipartSingleAttachment2(){
		Setup.startTest("Get the notification and recipient sessionID");
		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
		Response notifications2 = RestAssured.given().expect().statusCode(200)./*body(
				"notificationList.ftSessionInvitationNotification[0].sessionId", Matchers.notNullValue(),
				"notificationList.ftSessionInvitationNotification[0].originatorAddress", Matchers.containsString(Setup.cleanPrefix(Setup.TestUser1Contact)),
				"notificationList.ftSessionInvitationNotification[0].receiverName", Matchers.equalTo(Setup.TestUser2)
				).*/post(Setup.channelURL[2]);
		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
		
		JsonPath jsonData=notifications2.jsonPath();
		
		recipientSessionId=jsonData.get("notificationList.ftSessionInvitationNotification[0].sessionId");
		System.out.println("Receiver session = "+recipientSessionId);
		Setup.endTest();
	}	
//	@Test
//	public void testSendMultipartSingleAttachment3(){
//		String acceptData="{\"receiverSessionStatus\": {\"status\": \"Connected\"}}";
//		
//		Setup.startTest("Sending accept for User 2");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response accept=RestAssured.given().body(acceptData).expect().statusCode(204).post(Setup.fileTransferStatusURL(Setup.TestUser2,recipientSessionId));
//		System.out.println("Accept response = "+accept.getStatusCode()+" / "+accept.asString());
//
//		System.out.println("Sleeping ...");
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartSingleAttachment4(){
//		Setup.startTest("Checking notifications for user 1");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response notifications1 = RestAssured.given().expect().statusCode(200).body(
//				"notificationList.receiverAcceptanceNotification.receiverAddress",  Matchers.hasItem(Setup.TestUser2Contact),
//				"notificationList.receiverAcceptanceNotification.receiverSessionStatus.status",  Matchers.hasItem("Connected"),
//				"notificationList.receiverAcceptanceNotification.sessionId",  Matchers.hasItem(senderSessionId),
//				"notificationList.fileTransferEventNotification.eventType",  Matchers.hasItem("Successful"),
//				"notificationList.fileTransferEventNotification.sessionId",  Matchers.hasItem(senderSessionId)
//				).post(Setup.channelURL[1]);
//		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartSingleAttachment5(){
//		Setup.startTest("Checking notifications for user 2");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body(
//				"notificationList.fileNotification.sessionId",  Matchers.hasItem(recipientSessionId),
//				"notificationList.fileNotification.fileInformation.fileSelector.name",  Matchers.hasItem("rclogo.png")
//				).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		
//		attachmentURL1=notifications2.jsonPath().get("notificationList.fileNotification[0].fileInformation.fileURL");
//		System.out.println("attachmentURL1 = "+attachmentURL1);
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartSingleAttachment6(){
//		Setup.startTest("Receiving attachment");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response fileResponse=RestAssured.expect().statusCode(200).get(attachmentURL1);
//		System.out.println("Content type = "+fileResponse.getContentType());
//		System.out.println("Content disposition = "+fileResponse.getHeader("Content-Disposition"));
//		System.out.println("Content length = "+fileResponse.getHeader("Content-Length"));
////TODO content type is not returned
////TODO it could be possible for a badly behaved app to scan for well known file names - storage path could be stronger	
////TODO if two file transfers send the same file name simultaneously they could presumably clash as there is no sender ID identified in the file		
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartSingleAttachment7(){
//		Setup.startTest("Ensuring no outstanding notifications for user 1");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response notifications1 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[1]);
//		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartSingleAttachment8(){
//		Setup.startTest("Ensuring no outstanding notifications for user 2");
//		RestAssured.authentication=RestAssured.DEFAULT_AUTH;
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		Setup.endTest();
//	}
	
	
//
//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Test
//	public void testSendMultipartDualAttachments() throws IOException {
//		Setup.majorTest("File Transfer", "Sending using multipart - with 2 attachments (only 1 should be received)");
//		restart();
//		Setup.startTest("Testing send multipart with two attachments");
//		String rqdata="{\"fileTransferSessionInformation\": {\"fileInformation\": {"+
//				"\"fileSelector\": {\"name\": \"rclogo.png\",\"type\": \"image/png\"}},"+
//				"\"originatorAddress\": \""+Setup.TestUser1Contact+"\",\"originatorName\": \"G1\",\"receiverAddress\": \""+Setup.TestUser2Contact+"\",\"receiverName\": \"G2\"}}";
//
//		StringBody att1=new StringBody("hello world!", "text/plain", null);
//		FileBody att2=new FileBody(new File("misc/rclogo.png"), "image/png");
//		MultipartEntity entity = new MultipartEntity();
//		entity.addPart("hello", att1);
//		entity.addPart("rclogo.png", att2);
//		
//		Header contentType=entity.getContentType();
//		String ct=contentType.toString().replaceFirst("Content-Type: ", "");
//		System.out.println("contentType = "+ct);
//		ByteArrayOutputStream baos=new ByteArrayOutputStream();
//		entity.writeTo(baos);
//		
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response resp = RestAssured.given().multiPart("root-fields", rqdata, "application/json").multiPart("attachments", "attachments", baos.toByteArray(), ct).expect().statusCode(201).body(
//				"resourceReference.resourceURL", StringContains.containsString(Setup.TestUser1)
//		).post(Setup.sendFileURL(Setup.TestUser1));
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		JsonPath jsonData=resp.jsonPath();
//		savedResourceURL=jsonData.get("resourceReference.resourceURL");
//		String[] parts=savedResourceURL.split("/sessions/");
//		senderSessionId=parts[1];
//		System.out.println("Message resourceURL="+savedResourceURL);
//		try {
//			Thread.sleep(2500);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartDualAttachments2(){
//		Setup.startTest("Get the notification and recipient sessionID");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body(
//				"notificationList.ftSessionInvitationNotification.sessionId", Matchers.notNullValue(),
//				"notificationList.ftSessionInvitationNotification.originatorAddress", Matchers.hasItem(Setup.TestUser1Contact),
//				"notificationList.ftSessionInvitationNotification.receiverName", Matchers.hasItem(Setup.TestUser2)
//				).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		
//		JsonPath jsonData=notifications2.jsonPath();
//		
//		recipientSessionId=jsonData.get("notificationList.ftSessionInvitationNotification[0].sessionId");
//		System.out.println("Receiver session = "+recipientSessionId);
//		Setup.endTest();
//	}	
//	@Test
//	public void testSendMultipartDualAttachments3(){
//		String acceptData="{\"receiverSessionStatus\": {\"status\": \"Connected\"}}";
//		
//		Setup.startTest("Sending accept for User 2");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response accept=RestAssured.given().body(acceptData).expect().statusCode(204).post(Setup.fileTransferStatusURL(Setup.TestUser2,recipientSessionId));
//		System.out.println("Accept response = "+accept.getStatusCode()+" / "+accept.asString());
//
//		System.out.println("Sleeping ...");
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartDualAttachments4(){
//		Setup.startTest("Checking notifications for user 1");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response notifications1 = RestAssured.given().expect().statusCode(200).body(
//				"notificationList.receiverAcceptanceNotification.receiverAddress",  Matchers.hasItem(Setup.TestUser2Contact),
//				"notificationList.receiverAcceptanceNotification.receiverSessionStatus.status",  Matchers.hasItem("Connected"),
//				"notificationList.receiverAcceptanceNotification.sessionId",  Matchers.hasItem(senderSessionId),
//				"notificationList.fileTransferEventNotification.eventType",  Matchers.hasItem("Successful"),
//				"notificationList.fileTransferEventNotification.sessionId",  Matchers.hasItem(senderSessionId)
//				).post(Setup.channelURL[1]);
//		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartDualAttachments5(){
//		Setup.startTest("Checking notifications for user 2");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body(
//				"notificationList.fileNotification.sessionId",  Matchers.hasItem(recipientSessionId),
//				"notificationList.fileNotification.fileInformation.fileSelector.name",  Matchers.hasItem("rclogo.png")
//				).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		
//		attachmentURL1=notifications2.jsonPath().get("notificationList.fileNotification[0].fileInformation.fileURL");
//		System.out.println("attachmentURL1 = "+attachmentURL1);
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartDualAttachments6(){
//		Setup.startTest("Receiving attachment");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response fileResponse=RestAssured.expect().statusCode(200).get(attachmentURL1);
//		System.out.println("Content type = "+fileResponse.getContentType());
//		System.out.println("Content disposition = "+fileResponse.getHeader("Content-Disposition"));
//		System.out.println("Content length = "+fileResponse.getHeader("Content-Length"));
////TODO content type is not returned
////TODO it could be possible for a badly behaved app to scan for well known file names - storage path could be stronger	
////TODO if two file transfers send the same file name simultaneously they could presumably clash as there is no sender ID identified in the file		
////TODO actually transferred two attachments - but not clear how to collect them 
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartDualAttachments7(){
//		Setup.startTest("Ensuring no outstanding notifications for user 1");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response notifications1 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[1]);
//		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testSendMultipartDualAttachments8(){
//		Setup.startTest("Ensuring no outstanding notifications for user 2");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		Setup.endTest();
//	}
//
//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Test
//	public void testDeclineSingleAttachment() {
//		Setup.majorTest("File Transfer", "Decline file transfer");
//		restart();
//		Setup.startTest("Initiating send multipart with single attachment");
//		String rqdata="{\"fileTransferSessionInformation\": {\"fileInformation\": {\"fileDescription\": \"Logo\",\"fileDisposition\": \"Render\","+
//				"\"fileSelector\": {\"name\": \"rclogo.png\",\"type\": \"image/png\"}},"+
//				"\"originatorAddress\": \""+Setup.TestUser1Contact+"\",\"originatorName\": \"G1\",\"receiverAddress\": \""+Setup.TestUser2Contact+"\",\"receiverName\": \"G2\"}}";
//		
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response resp = RestAssured.given().multiPart("root-fields", rqdata, "application/json").multiPart("attachments", new File("misc/rclogo.png")).expect().statusCode(201).body(
//				"resourceReference.resourceURL", StringContains.containsString(Setup.TestUser1)
//		).post(Setup.sendFileURL(Setup.TestUser1));
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		JsonPath jsonData=resp.jsonPath();
//		savedResourceURL=jsonData.get("resourceReference.resourceURL");
//		String[] parts=savedResourceURL.split("/sessions/");
//		senderSessionId=parts[1];
//		System.out.println("Message resourceURL="+savedResourceURL);
//		try {
//			Thread.sleep(2500);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}
//	@Test
//	public void testDeclineSingleAttachment2(){
//		System.out.println("Get the notification and recipient sessionID");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body(
//				"notificationList.ftSessionInvitationNotification.sessionId", Matchers.notNullValue(),
//				"notificationList.ftSessionInvitationNotification.originatorAddress", Matchers.hasItem(Setup.TestUser1Contact),
//				"notificationList.ftSessionInvitationNotification.receiverName", Matchers.hasItem(Setup.TestUser2)
//				).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		
//		JsonPath jsonData=notifications2.jsonPath();
//		
//		recipientSessionId=jsonData.get("notificationList.ftSessionInvitationNotification[0].sessionId");
//		System.out.println("Receiver session = "+recipientSessionId);
//		Setup.endTest();
//	}	
//	@Test
//	public void testDeclineSingleAttachment3(){
//		Setup.startTest("Sending deline file transfer for User 2");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response accept=RestAssured.given().expect().statusCode(204).delete(Setup.fileTransferSessionURL(Setup.TestUser2,recipientSessionId));
//		System.out.println("Decline response = "+accept.getStatusCode()+" / "+accept.asString());
//
//		System.out.println("Sleeping ...");
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException ie) {}
//		Setup.endTest();
//	}
//	@Test
//	public void testDeclineSingleAttachment4(){
//		Setup.startTest("Ensuring User 1 is informed of file transfer declined");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response notifications1 = RestAssured.given().expect().statusCode(200).body(
//				"notificationList.fileTransferEventNotification.sessionId", Matchers.hasItem(senderSessionId),
//				"notificationList.fileTransferEventNotification.eventType", Matchers.hasItem("Declined")
//				).post(Setup.channelURL[1]);
//		System.out.println("Response = "+notifications1.getStatusCode()+" / "+notifications1.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testDeclineSingleAttachment5(){
//		Setup.startTest("Ensuring no outstanding notifications for user 2");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
//		Response notifications2 = RestAssured.given().expect().statusCode(200).body("notificationList", Matchers.nullValue()).post(Setup.channelURL[2]);
//		System.out.println("Response = "+notifications2.getStatusCode()+" / "+notifications2.asString());
//		Setup.endTest();
//	}
//
//	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Test
//	public void testFileTransferNotifications() {
//		Setup.majorTest("File Transfer", "Read file transfer notification / subscription info");
//		
//		Setup.startTest("Getting information about file transfer notification subscription");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response resp=RestAssured.expect().statusCode(200).body(
//				"fileTransferSubscription.callbackReference.callbackData", Matchers.equalTo(Setup.TestUser1),
//				"fileTransferSubscription.callbackReference.notificationFormat", Matchers.equalTo("JSON"),
//				"fileTransferSubscription.callbackReference.notifyURL", Matchers.equalTo(Setup.channelURL[1])
//				).get(Setup.fileTransferSubscriptionURL[1]);
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testFileTransferNotifications2() {		
//		Setup.startTest("Getting information about all file transfer notification subscriptions");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response resp=RestAssured.expect().statusCode(200).body(
//				"fileTransferSubscriptionList.fileTransferSubscription[0].callbackReference.callbackData", Matchers.equalTo(Setup.TestUser1),
//				"fileTransferSubscriptionList.fileTransferSubscription[0].callbackReference.notificationFormat", Matchers.equalTo("JSON"),
//				"fileTransferSubscriptionList.fileTransferSubscription[0].callbackReference.notifyURL", Matchers.equalTo(Setup.channelURL[1])
//				).get(Setup.fileTransferSubscriptionURL(Setup.TestUser1));
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		Setup.endTest();
//	}
//	@Test
//	public void testFileTransferNotifications3() {
//		Setup.startTest("Deleting file transfer notification subscription");
//		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
//		Response resp=RestAssured.expect().statusCode(204).delete(Setup.fileTransferSubscriptionURL[1]);
//		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
//		Setup.endTest();
//	}

	
	
}

