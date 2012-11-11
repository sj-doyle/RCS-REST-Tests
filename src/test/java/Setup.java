package test.java;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class Setup {
	public static final String baseURI="http://api.oneapi-gw.gsma.com";
	
	public static final String apiVersion="0.1";
	
	private static final String dateFormat="yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat dateFormatter=new SimpleDateFormat(dateFormat);
	
	public static final String applicationPassword="3Kvm4\"DD";
	public static final String urlSplit="/api.oneapi-gw.gsma.com"; // /rcsbox-servlet-oma
	
	private static boolean initialised=false;
	public static synchronized void initialise() {
		if (!initialised) {
			RestAssured.baseURI = baseURI;
			RestAssured.port = 80;
			RestAssured.basePath = ""; //"/rcsgw/rcsbox-servlet-oma";
			RestAssured.urlEncodingEnabled=true;
			initialised=true;
		}
	}
	
	public static final String TestUser1="+15554000001";
	public static final String TestUser2="+15554000002";
	public static final String TestUser3="+15554000003";
	public static final String TestUser4="+15554000004";
	public static final String TestUser5="+15554000005";
	public static final String TestUser6="+15554000006";
	public static final String TestUser7="+15554000007";
	public static final String TestUser8="+15554000008";
	
	public static final String TestUser1Contact="tel:+15554000001";
	public static final String TestUser2Contact="tel:+15554000002";
	public static final String TestUser3Contact="tel:+15554000003";
	public static final String TestUser4Contact="tel:+15554000004";
	public static final String TestUser5Contact="tel:+15554000005";
	public static final String TestUser6Contact="tel:+15554000006";
	public static final String TestUser7Contact="tel:+15554000007";
	public static final String TestUser8Contact="tel:+15554000008";


	public static String encode(String n) {
		return n;
//		String rv="";
//		if (n!=null) {
//			try {
//				rv=URLEncoder.encode(n, "UTF-8");
//			} catch (UnsupportedEncodingException e) {
//			}
//		}
//		System.out.println("Encoded "+n+" to "+rv);
//		return rv;
	}
	
	public static String cleanPrefix(String n) {
		return n.replaceAll("tel\\:\\+", "").replaceAll("tel\\:","").replaceAll("\\+", "").replaceAll("\\:", "");
	}
	
	public static String prepareForTest(String n) {
		return n.replaceAll("\\%2B", "+").replaceAll("\\%3A",":");
	}
	
	public static String encodedValue(String n) {
		String rv="";
		if (n!=null) {
			try {
				rv=URLEncoder.encode(n, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		return rv;	
	}

	
	public static String registerURL(String uid) {
		return "/register/"+Setup.apiVersion+"/"+encode(uid)+"/sessions";
	}

	public static String unregisterURL(String uid) {
		return "/register/"+Setup.apiVersion+"/"+encode(uid)+"/sessions";
	}
	
	public static String notificationChannelURL(String uid) {
		return "/notificationchannel/"+Setup.apiVersion+"/"+encode(uid)+"/channels";
	}

	public static String sessionSubscriptionURL(String uid) {
		return "/register/"+Setup.apiVersion+"/"+encode(uid)+"/subscriptions";
	}

	public static String abChangesSubscriptionURL(String uid) {
		return "/addressbook/"+Setup.apiVersion+"/"+encode(uid)+"/subscriptions/abChanges";
	}

	public static String chatSubscriptionURL(String uid) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/subscriptions";
	}

	public static String fileTransferSubscriptionURL(String uid) {
		return "/filetransfer/"+Setup.apiVersion+"/"+encode(uid)+"/subscriptions";
	}

	public static String contactsURL(String uid) {
		return "/addressbook/"+Setup.apiVersion+"/"+encode(uid)+"/contacts";
	}

	public static String contactURL(String uid, String contact) {
		return "/addressbook/"+Setup.apiVersion+"/"+encode(uid)+"/contacts/"+encode(contact);
	}
	
	public static String contactAttributesURL(String uid, String contact) {
		return "/addressbook/"+Setup.apiVersion+"/"+encode(uid)+"/contacts/"+encode(contact)+"/attributes";
	}

	public static String contactAttributeURL(String uid, String contact, String attribute) {
		return "/addressbook/"+Setup.apiVersion+"/"+encode(uid)+"/contacts/"+encode(contact)+"/attributes/"+encode(attribute);
	}

	public static String sendFileURL(String uid) {
		return "/filetransfer/"+Setup.apiVersion+"/"+encode(uid)+"/sessions";
	}
	
	public static String fileTransferStatusURL(String uid, String sessionId) {
		return "/filetransfer/"+Setup.apiVersion+"/"+encode(uid)+"/sessions/"+encode(sessionId)+"/status";
	}

	public static String fileTransferSessionURL(String uid, String sessionId) {
		return "/filetransfer/"+Setup.apiVersion+"/"+encode(uid)+"/sessions/"+encode(sessionId);
	}

	public static String groupChatURL(String uid) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/group";
	}

	public static String groupChatSessionURL(String uid, String sessionId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/group/"+encode(sessionId);
	}

	public static String groupChatMessageURL(String uid, String sessionId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/group/"+encode(sessionId)+"/messages";
	}

	//http://{serverRoot}/chat/{apiVersion}/{userId}/group/{sessionId}/participants
	public static String groupChatParticipantsURL(String uid, String sessionId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/group/"+encode(sessionId)+"/participants";
	}
	
	public static String groupChatParticipantURL(String uid, String sessionId, String participantId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/group/"+encode(sessionId)+"/participants/"+encode(participantId);
	}

	public static String groupChatParticipantStatusURL(String uid, String sessionId, String participantId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/group/"+encode(sessionId)+"/participants/"+encode(participantId)+"/status";
	}

	public static String sendIMURL(String uid, String otheruid, String sessionId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/oneToOne/"+encode(otheruid)+"/"+encode(sessionId)+"/messages";
	}

	public static String createIMChatSessionURL(String uid, String otheruid) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/oneToOne/"+encode(otheruid);
	}

	public static String chatSessionIMURL(String uid, String otheruid, String sessionId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/oneToOne/"+encode(otheruid)+"/"+encode(sessionId);
	}

	public static String chatSessionIMStatusURL(String uid, String otheruid, String sessionId) {
		return "/chat/"+Setup.apiVersion+"/"+encode(uid)+"/oneToOne/"+encode(otheruid)+"/"+encode(sessionId)+"/status";
	}

	public static String capabilitiesURL(String uid) {
		return "/capabilities/"+Setup.apiVersion+"/"+encode(uid);
	}

	public static String[] resourceURL=new String[10];
	public static String[] channelURL=new String[10];
	public static String[] callbackURL=new String[10];
	public static String[] sessionSubscriptionURL=new String[10];
	public static String[] abChangesSubscriptionURL=new String[10];
	public static String[] chatSubscriptionURL=new String[10];
	public static String[] fileTransferSubscriptionURL=new String[10];
	public static String validLongPoll="{\"notificationChannel\": { \"channelData\": { \"maxNotifications\": 1000 }, \"channelLifetime\": 0, \"channelType\": \"LongPolling\" }}";
	
	public static void subscriptions(String uid, int index) {
		try {
			initialise();
			System.out.println("Creating subscription for "+uid);
			RestAssured.authentication=RestAssured.digest(uid, Setup.applicationPassword);
			Response resp=RestAssured.given().expect().statusCode(204).when().post(Setup.registerURL(uid));
	
			RestAssured.authentication=RestAssured.digest(uid, Setup.applicationPassword);
			Response response = RestAssured.given().body(Setup.validLongPoll).expect().statusCode(201).post(Setup.notificationChannelURL(uid));
			JsonPath jsonData=response.jsonPath();
			resourceURL[index]=jsonData.get("notificationChannel.resourceURL");
			channelURL[index]=jsonData.get("notificationChannel.channelData.channelURL");
			callbackURL[index]=jsonData.get("notificationChannel.callbackURL");
			System.out.println(uid+" resourceURL["+index+"] = "+resourceURL[index]);
			System.out.println(uid+" channelURL["+index+"] = "+channelURL[index]);
			System.out.println(uid+" callbackURL["+index+"] = "+callbackURL[index]);
			
			String requestData="{\"sessionSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
					Setup.callbackURL[index]+"\",\"callbackData\":\""+uid+"\"}, \"duration\":900}}";
			
			System.out.println("Sending "+requestData+" to "+Setup.sessionSubscriptionURL(uid));
			
			RestAssured.authentication=RestAssured.digest(uid, Setup.applicationPassword);
			resp=RestAssured.given().contentType("application/json").body(requestData).expect().statusCode(201).post(Setup.sessionSubscriptionURL(uid));
			jsonData=resp.jsonPath();
			
			sessionSubscriptionURL[index]=jsonData.get("sessionSubscription.resourceURL");
			System.out.println("sessionSubscriptionURL="+sessionSubscriptionURL[index]);
	
			requestData="{\"abChangesSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
					Setup.callbackURL[index]+"\",\"callbackData\":\""+uid+"\"}, \"duration\":900}}";
			
			System.out.println("Sending "+requestData+" to "+Setup.abChangesSubscriptionURL(uid));

			RestAssured.authentication=RestAssured.digest(uid, Setup.applicationPassword);
			resp=RestAssured.given().contentType("application/json").body(requestData).expect().statusCode(201).post(Setup.abChangesSubscriptionURL(uid));
			jsonData=resp.jsonPath();
			
			abChangesSubscriptionURL[index]=jsonData.get("abChangesSubscription.resourceURL");
			System.out.println("abChangesSubscriptionURL="+abChangesSubscriptionURL[index]);
	
			requestData="{\"chatNotificationSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
					Setup.callbackURL[index]+"\",\"callbackData\":\""+uid+"\"}, \"duration\":900}}";
			
			System.out.println("Sending "+requestData+" to "+Setup.chatSubscriptionURL(uid));

			RestAssured.authentication=RestAssured.digest(uid, Setup.applicationPassword);
			resp=RestAssured.given().contentType("application/json").body(requestData).expect().statusCode(201).post(Setup.chatSubscriptionURL(uid));
			jsonData=resp.jsonPath();
			
			chatSubscriptionURL[index]=jsonData.get("chatNotificationSubscription.resourceURL");
			System.out.println("chatSubscriptionURL="+chatSubscriptionURL[index]);
	
			requestData="{\"fileTransferSubscription\":{ \"callbackReference\":{\"notifyURL\":\""+
					Setup.callbackURL[index]+"\",\"callbackData\":\""+uid+"\"}, \"duration\":900}}";
			
			System.out.println("Sending "+requestData+" to "+Setup.fileTransferSubscriptionURL(uid));

			RestAssured.authentication=RestAssured.digest(uid, Setup.applicationPassword);
			resp=RestAssured.given().contentType("application/json").body(requestData).expect().statusCode(201).post(Setup.fileTransferSubscriptionURL(uid));
			jsonData=resp.jsonPath();
			
			fileTransferSubscriptionURL[index]=jsonData.get("fileTransferSubscription.resourceURL");
			System.out.println("fileTransferSubscriptionURL="+fileTransferSubscriptionURL[index]);
		} catch (Exception e) {
			System.out.println("Exception "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void clearPendingNotifications(String uid, int index) {
		try {
			if (channelURL[index]!=null) {
				RestAssured.authentication=RestAssured.DEFAULT_AUTH;
				
				String url=(Setup.channelURL[2].split("username\\=")[0])+"username="+uid;

				Response response = RestAssured.given().post(url);
				//System.out.println("["+index+"] Response = "+response.getStatusCode()+" / "+response.asString());
			}
		} catch (Exception e) {
			System.out.println("Exception "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void addContact(String uid, String contactId) {
		try {
			String rqdata="{\"contact\": {\"contactId\":\""+contactId+"\"}}";
			RestAssured.authentication=RestAssured.basic(uid, Setup.applicationPassword);
			Response resp=RestAssured.given().body(rqdata).
					put(Setup.contactURL(uid,contactId));
		} catch (Exception e) {
			System.out.println("Exception "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static String lastTest=null;
	
	public static void majorTest(String function, String sequence) {
		if (lastTest!=null) {
			System.out.println(dateFormatter.format(new java.util.Date())+" ---------- Previous test ended prematurely : "+lastTest+" ----------");
		}
		System.out.println("********************************************************************************************************************************");
		System.out.println("* Testing "+function+" : "+sequence);
		System.out.println("********************************************************************************************************************************");
	}
	
	public static void startTest(String name) {
		if (lastTest!=null) {
			System.out.println(dateFormatter.format(new java.util.Date())+" ---------- Previous test ended prematurely : "+lastTest+" ----------");
		}
		lastTest=name;
		System.out.println(dateFormatter.format(new java.util.Date())+" ########## Started test sequence part : "+name+" ###########");
	}

	public static void endTest() {
		System.out.println(dateFormatter.format(new java.util.Date())+" ++++++++++ Ended test sequence part : "+lastTest+" ++++++++++");
		lastTest=null;
	}
	

}
