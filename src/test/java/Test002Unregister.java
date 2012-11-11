package test.java;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
 

public class Test002Unregister {
	
	@Test 
	public void unregisterTestUser1() {
		Setup.initialise(); 
		Setup.majorTest("User Unregistration", "Unregistration/Logout");

		Setup.startTest("Testing successful unregister call for User 1");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().
	    statusCode(204).
	    when().
	    delete(Setup.unregisterURL(Setup.TestUser1));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void unregisterGSMAAcceptance2() {
		Setup.initialise();
		
		Setup.startTest("Testing successful unregister call for User 2");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().
	    statusCode(204).
	    when().
	    delete(Setup.unregisterURL(Setup.TestUser2));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
	@Test 
	public void unregisterGSMAAcceptanceNonExist() {
		Setup.initialise(); 
		
		Setup.startTest("Testing erroneous unregister call - non existent user");
		RestAssured.authentication=RestAssured.basic("GSMAAcceptanceNonExist", Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().
	    statusCode(401).
	    /*body(
	    		"requestError.serviceException.messageId", equalTo("SVC001"),
	    		"requestError.serviceException.variables", hasItem("User not authorized")
	    ).*/when().
	    delete(Setup.unregisterURL("GSMAAcceptanceNonExist"));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		Setup.endTest();
	}
	
}
