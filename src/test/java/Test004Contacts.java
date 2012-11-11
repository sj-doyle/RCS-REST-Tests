package test.java;

import java.util.List;

import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.exception.ParsePathException;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class Test004Contacts {

	
	static {
		Setup.initialise(); 
	}
	
	@Test 
	public void clearContacts1() {
		Setup.majorTest("Address book", "List, Add, Delete, Update contacts");
		
		Setup.startTest("Deleting prior contacts for User 1");
		deleteAnyContactsFor(Setup.TestUser1);
		
		System.out.println("Checking no contacts for User 1");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).
		body(
	    		"contactCollection.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser1)),
	    		"contactCollection.contact", Matchers.nullValue()	    		
	    ).when().get(Setup.contactsURL(Setup.TestUser1));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void clearContacts2() {
		Setup.startTest("Deleting prior contacts for User 2");
		deleteAnyContactsFor(Setup.TestUser2);
		
		System.out.println("Checking no contacts for User 2");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).
		body(
	    		"contactCollection.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser2)),
	    		"contactCollection.contact", Matchers.nullValue()	    		
	    ).when().get(Setup.contactsURL(Setup.TestUser2));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void clearContacts3() {
		Setup.startTest("Deleting prior contacts for User 3");
		deleteAnyContactsFor(Setup.TestUser3);
		
		System.out.println("Checking no contacts for User 3");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).
		body(
	    		"contactCollection.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser3)),
	    		"contactCollection.contact", Matchers.nullValue()	    		
	    ).when().get(Setup.contactsURL(Setup.TestUser3));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test 
	public void clearContacts4() {
		Setup.startTest("Deleting prior contacts for User 4");
		deleteAnyContactsFor(Setup.TestUser4);
		
		System.out.println("Checking no contacts for User 4");

		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().statusCode(200).
		body(
	    		"contactCollection.resourceURL", StringContains.containsString(Setup.cleanPrefix(Setup.TestUser4)),
	    		"contactCollection.contact", Matchers.nullValue()	    		
	    ).when().get(Setup.contactsURL(Setup.TestUser4));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	


	@Test
	public void addContactInfo1() {
		Setup.startTest("Adding 1 contact for User 1");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser2Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA2\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)+"/contacts/"+Setup.encodedValue(Setup.TestUser2Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser2Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA2")
				).when().put(Setup.contactURL(Setup.TestUser1,Setup.TestUser2Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void addContactInfo2() {
		Setup.startTest("Getting contact for User 1 / 2");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)+"/contacts/"+Setup.encodedValue(Setup.TestUser2Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser2Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA2"),
						"contact.attributeList.attribute.name", Matchers.hasItem("display-name")
				).when().get(Setup.contactURL(Setup.TestUser1,Setup.TestUser2Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactUnknownUserAndContact() {
		Setup.startTest("Testing failure case - adding contact unknown user and unknown contact");
		String rqdata="{\"contact\": {\"contactId\":\"sip:GSMAAcceptanceNotExisting@solaiemes.com\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMANONE\"}]}}}";
		
		RestAssured.authentication=RestAssured.basic("GSMAAcceptanceNotAUser", Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(401).
				/*body(
						"requestError.serviceException.variables", Matchers.hasItem("User not authorized"),		
						"requestError.serviceException.messageId", Matchers.equalTo("SVC002")
				).*/when().put(Setup.contactURL("GSMAAcceptanceNotAUser","sip:GSMAAcceptanceNotExisting@solaiemes.com"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
		//TODO - returns HTML page not JSON
	}

	@Test
	public void addContactUnknownUserValidContact() {
		Setup.startTest("Testing failure case - adding contact unknown user but valid contact");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser1Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA1\"}]}}}";
		
		RestAssured.authentication=RestAssured.basic("GSMAAcceptanceNotAUser", Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(401).
				/*body(
						"requestError.serviceException.variables", Matchers.hasItem("User not authorized"),		
						"requestError.serviceException.messageId", Matchers.equalTo("SVC002")
				).*/when().put(Setup.contactURL("GSMAAcceptanceNotAUser",Setup.TestUser1Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactKnownUserUnknownContact() {
		Setup.startTest("Testing case - adding contact known user but unknown contact");
		String rqdata="{\"contact\": {\"contactId\":\"sip:GSMAAcceptanceNotExisting@solaiemes.com\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMANONE\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)+"/contacts/"+Setup.encodedValue("sip:GSMAAcceptanceNotExisting@solaiemes.com")),		
						"contact.contactId", Matchers.equalTo("sip:GSMAAcceptanceNotExisting@solaiemes.com"),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMANONE")
				).when().put(Setup.contactURL(Setup.TestUser1,"sip:GSMAAcceptanceNotExisting@solaiemes.com"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactMismatchedContact() {
		Setup.startTest("Testing failure case - adding contact mismatched contact IDs ");
		String rqdata="{\"contact\": {\"contactId\":\"sip:GSMAAcceptanceNotExisting@solaiemes.com\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMANONE\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(403).
				body(
						"requestError.serviceException.variables", Matchers.hasItem("Parameters are not valid (contactIds are differents)"),		
						"requestError.serviceException.messageId", Matchers.equalTo("SVC002")
				).when().put(Setup.contactURL(Setup.TestUser1,"sip:GSMAAcceptanceNonExisting@solaiemes.com"));
		//TODO spelling error in the response		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactInfo2A() {
		Setup.startTest("Adding 2 contacts for User 2");
		listCurrentContactsFor(Setup.TestUser2);
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser1Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA1\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser2)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA1")
				).when().put(Setup.contactURL(Setup.TestUser2,Setup.TestUser1Contact));
				
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void addContactInfo2B() {
		Setup.startTest("Adding Contact 2 for User 2");
		listCurrentContactsFor(Setup.TestUser2);
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser3Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA3\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser2)+"/contacts/"+Setup.encodedValue(Setup.TestUser3Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser3Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA3")
				).when().put(Setup.contactURL(Setup.TestUser2,Setup.TestUser3Contact));
			
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactInfo3A() {
		Setup.startTest("Adding 3 contacts for User 3");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser1Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA1\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA1")
				).when().put(Setup.contactURL(Setup.TestUser3,Setup.TestUser1Contact));
				
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	@Test
	public void addContactInfo3B() {
		Setup.startTest("Adding contact 2 for User 3");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser2Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA2\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/contacts/"+Setup.encodedValue(Setup.TestUser2Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser2Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA2")
				).when().put(Setup.contactURL(Setup.TestUser3,Setup.TestUser2Contact));
			
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void addContactInfo3C() {
		Setup.startTest("Addign Contact 3 for User 3");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser4Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA4\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/contacts/"+Setup.encodedValue(Setup.TestUser4Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser4Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA4")
				).when().put(Setup.contactURL(Setup.TestUser3,Setup.TestUser4Contact));
			
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactInfo4A() {
		Setup.startTest("Adding 4 contacts for User 4");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser1Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA1\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA1")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser1Contact));
				
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactInfo4B() {
		Setup.startTest("Adding Contact 2 for User 4");

		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser2Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA2\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser2Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser2Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA2")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser2Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactInfo4C() {
		Setup.startTest("Adding Contact 3 for User 4");
		
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser3Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA3\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser3Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser3Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA3")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser3Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void addContactInfo4D() {
		Setup.startTest("Contact 4 for User 4");
		
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser4Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"GSMA4\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(201).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser4Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser4Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("GSMA4")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser4Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	
	@Test
	public void updateContactInfo1() {
		Setup.startTest("Updating 1 contact for User 1");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser2Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G12\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser1)+"/contacts/"+Setup.encodedValue(Setup.TestUser2Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser2Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G12")
				).when().put(Setup.contactURL(Setup.TestUser1,Setup.TestUser2Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void updateContactInfo2A() {
		Setup.startTest("Updating 2 contacts User 2");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser1Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G21\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser2)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G21")
				).when().put(Setup.contactURL(Setup.TestUser2,Setup.TestUser1Contact));
				
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void updateContactInfo2B() {
		Setup.startTest("Updating Contact 2 for User 2");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser3Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G23\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser2)+"/contacts/"+Setup.encodedValue(Setup.TestUser3Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser3Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G23")
				).when().put(Setup.contactURL(Setup.TestUser2,Setup.TestUser3Contact));
			
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void updateContactInfo3A() {
		Setup.startTest("Updating 3 contacts for User 3");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser1Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G31\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G31")
				).when().put(Setup.contactURL(Setup.TestUser3,Setup.TestUser1Contact));
				
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void udpateContactInfo3B() {
		Setup.startTest("Updating Contact 2 for User 3");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser2Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G32\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/contacts/"+Setup.encodedValue(Setup.TestUser2Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser2Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G32")
				).when().put(Setup.contactURL(Setup.TestUser3,Setup.TestUser2Contact));
			
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void updateContactInfo3C() {
		Setup.startTest("Updating Contact 3 for User 3");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser4Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G34\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser3, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser3)+"/contacts/"+Setup.encodedValue(Setup.TestUser4Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser4Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G34")
				).when().put(Setup.contactURL(Setup.TestUser3,Setup.TestUser4Contact));
			
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void updateContactInfo4A() {
		Setup.startTest("Updating 4 contacts for User 4");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser1Contact+"\","+
					  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G41\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G41")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser1Contact));
				
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void updateContactInfo4B() {
		Setup.startTest("Updating Contact 2 for User 4");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser2Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G42\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser2Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser2Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G42")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser2Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void updateContactInfo4C() {
		Setup.startTest("Updating Contact 3 for User 4");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser3Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G43\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser3Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser3Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G43")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser3Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void updateContactInfo4D() {
		Setup.startTest("Updating Contact 4 for User 4");
		String rqdata="{\"contact\": {\"contactId\":\""+Setup.TestUser4Contact+"\","+
				  "\"attributeList\":{\"attribute\":[{\"name\": \"display-name\", \"value\":\"G44\"}]}}}";
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp=RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).
				body(
						"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser4Contact)),		
						"contact.contactId", Matchers.equalTo(Setup.TestUser4Contact),	
						"contact.attributeList.attribute.value", Matchers.hasItem("G44")
				).when().put(Setup.contactURL(Setup.TestUser4,Setup.TestUser4Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void deleteContact4D() {
		Setup.startTest("Deleting contact 4 for User 4");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(204).delete(Setup.contactURL(Setup.TestUser4, Setup.TestUser4Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void deleteNonExistentContact4() {
		Setup.startTest("Verifying error case deleting contact sip:GSMAAcceptanceNonExist@solaiemes.com for User 4");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(400).body(
				"requestError.serviceException.variables", Matchers.hasItem("Contact doesn't exist: sip:GSMAAcceptanceNonExist@solaiemes.com"),		
				"requestError.serviceException.messageId", Matchers.equalTo("SVC001")
		).delete(Setup.contactURL(Setup.TestUser4, "sip:GSMAAcceptanceNonExist@solaiemes.com"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void testContactAttributesURL() {
		Setup.startTest("Retrieving contact attributes for User 4 / 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(200).body(
				"contact.attributeList.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)+"/attributes"),
				"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),
				"contact.attributeList.attribute.value", Matchers.hasItem("G41"),
				"contact.attributeList.attribute.name", Matchers.hasItem("display-name"),
				"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact)
		).get(Setup.contactURL(Setup.TestUser4, Setup.TestUser1Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void testNonExistingContactAttributesURL() {
		Setup.startTest("Checking failure case - contact attributes for User 4 / sip:DoesNotExist@solaiemes.com");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(400).body(
				"requestError.serviceException.variables", Matchers.hasItem("Contact not found (sip:DoesNotExist@solaiemes.com)"),		
				"requestError.serviceException.messageId", Matchers.equalTo("SVC001")
		).get(Setup.contactURL(Setup.TestUser4, "sip:DoesNotExist@solaiemes.com"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test
	public void testNonExistingUserContactAttributesURL() {
		Setup.startTest("Checking failure case - contact attributes for GSMAAcceptanceNonExisting / sip:DoesNotExist@solaiemes.com");
		RestAssured.authentication=RestAssured.basic("GSMAAcceptanceNonExisting", Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(401)./*body(
				"requestError.serviceException.variables", Matchers.hasItem("User not authorized"),		
				"requestError.serviceException.messageId", Matchers.equalTo("SVC001")
		).*/get(Setup.contactURL("GSMAAcceptanceNonExisting", "sip:DoesNotExist@solaiemes.com"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}


	@Test
	public void testUpdateContactAttribute() {
		String rqdata="{\"attribute\":{\"name\": \"display-name\", \"value\":\"G41NEW\"}}";

		Setup.startTest("Updating display-name attribute for User 4 / 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().body(rqdata).expect().log().ifError().statusCode(200).body(
				"attribute.value", Matchers.equalTo("G41NEW"),
				"attribute.name", Matchers.equalTo("display-name")
		).put(Setup.contactAttributeURL(Setup.TestUser4, Setup.TestUser1Contact, "display-name"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void testRetrieveDisplayNameAttribute() {
		Setup.startTest("Retrieving display-name attribute for User 4 / 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(200).body(
				"attribute.value", Matchers.equalTo("G41NEW"),
				"attribute.name", Matchers.equalTo("display-name")
		).get(Setup.contactAttributeURL(Setup.TestUser4, Setup.TestUser1Contact, "display-name"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}


	@Test
	public void testUpdateNonExistingAttribute() {
		String rqdata="{\"attribute\":{\"name\": \"ZZZ-NON-EXISTING\", \"value\":\"NOTSET\"}}";

		Setup.startTest("Failure case - updating non existent attribute for User 4 / 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().body(rqdata).expect().log().ifError().statusCode(400).body(
				"requestError.serviceException.variables", Matchers.hasItem("Attribute is not valid (only 'display-name' can be modified). Received 'ZZZ-NON-EXISTING'")						
		).put(Setup.contactAttributeURL(Setup.TestUser4, Setup.TestUser1Contact, "ZZZ-NON-EXISTING"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void testDeleteContactAttribute() {
		Setup.startTest("Deleting display-name attribute for User 4 / 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(204)./*body(
				"attribute.value", Matchers.equalTo("G41NEW"),
				"attribute.name", Matchers.equalTo("display-name")
		).*/delete(Setup.contactAttributeURL(Setup.TestUser4, Setup.TestUser1Contact, "display-name"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());

		System.out.println("Checking display name reverts to contactId for User 4 / 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		resp = RestAssured.expect().log().ifError().statusCode(200).body(
				"contact.attributeList.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)+"/attributes"),
				"contact.resourceURL", StringContains.containsString(Setup.encodedValue(Setup.TestUser4)+"/contacts/"+Setup.encodedValue(Setup.TestUser1Contact)),
				"contact.attributeList.attribute.value", Matchers.hasItem(Setup.TestUser1Contact),
				"contact.attributeList.attribute.name", Matchers.hasItem("display-name"),
				"contact.contactId", Matchers.equalTo(Setup.TestUser1Contact)
		).get(Setup.contactURL(Setup.TestUser4, Setup.TestUser1Contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}

	@Test
	public void testUpdateContactAttributeMismatchedParamName() {
		String rqdata="{\"attribute\":{\"name\": \"display-name\", \"value\":\"G41NEW\"}}";

		Setup.startTest("Failure case - updating display-name attribute for User 4 / 1 for mismatched attr name");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser4, Setup.applicationPassword);
		Response resp = RestAssured.given().body(rqdata).expect().log().ifError().statusCode(400).body(
				"requestError.serviceException.variables", Matchers.hasItem("Attribute ('display-name')and ResourceRelPath  ('capabilities') do not have the same value"),		
				"requestError.serviceException.messageId", Matchers.equalTo("SVC002")
		).put(Setup.contactAttributeURL(Setup.TestUser4, Setup.TestUser1Contact, "capabilities"));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	
	/*
	 * Support functions for the contact testing
	 */

	public void deleteContact(String uid, String contact) {
		System.out.println("Deleting contact "+contact+" for "+uid);
		RestAssured.authentication=RestAssured.basic(uid, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(204).delete(Setup.contactURL(uid, contact));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
	}
	
	public void deleteAnyContactsFor(String uid) {
		System.out.println("Request to "+Setup.contactsURL(uid));
		RestAssured.authentication=RestAssured.basic(uid, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(200).when().get(Setup.contactsURL(uid));
		JsonPath jsonData=resp.jsonPath();
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		if (jsonData.get("contactCollection.contact")!=null) {
			List<String> contacts=jsonData.get("contactCollection.contact.contactId");
			if (contacts!=null) {
				for (String contact:contacts) {
					System.out.println("contactId = "+contact);
					deleteContact(uid, contact);
				}
			}
		}
	}

	public void listCurrentContactsFor(String uid) {
		System.out.println("Getting contacts for "+uid);
		RestAssured.authentication=RestAssured.basic(uid, Setup.applicationPassword);
		Response resp = RestAssured.expect().log().ifError().statusCode(200).get(Setup.contactsURL(uid));
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		try {
			System.out.println("Getting json ");
			JsonPath jsonData=resp.jsonPath();
			System.out.println("Checking for contact records");
			if (jsonData.get("contactCollection.contact")!=null) {
				Object o=jsonData.get("contactCollection.contact.contactId*");
				System.out.println("Class is "+o.getClass().getName());
				List<String> contacts=jsonData.get("contactCollection.contact.contactId");
				if (contacts!=null) {
					for (String contact:contacts) {
						System.out.println("contactId = "+contact);
					}
				}
			}
		} catch (ParsePathException pe) {
			System.out.println("PARSE EXCEPTION "+pe.getMessage());
		}
	}


}

