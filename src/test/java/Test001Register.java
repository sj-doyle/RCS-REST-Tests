package test.java;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
 

public class Test001Register {
	
	@Test 
	public void registerTestUser1() {
		Setup.initialise(); 
		Setup.majorTest("User Registration", "Registration/Login");
		
		Setup.startTest("Testing successful register call for User 1");
		
		RestAssured.authentication=RestAssured.basic(Setup.TestUser1, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().
	    statusCode(204).
	    when().
	    post(Setup.registerURL(Setup.TestUser1));

		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		
		Setup.endTest();
	}
	
	@Test 
	public void registerGSMAAcceptance2() {
		Setup.initialise(); 

		Setup.startTest("Testing successful register call for User 2");
		RestAssured.authentication=RestAssured.basic(Setup.TestUser2, Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().
	    statusCode(204).
	    when().
	    post(Setup.registerURL(Setup.TestUser2));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		
		Setup.endTest();
	}

	@Test 
	public void registerGSMAAcceptanceNonExist() {
		Setup.initialise(); 

		Setup.startTest("Testing erroneous register call - non existent user");
		RestAssured.authentication=RestAssured.basic("GSMAAcceptanceNonExist", Setup.applicationPassword);
		Response resp=RestAssured.expect().log().ifError().
	    statusCode(401).
	    /*body(
	    		"requestError.serviceException.messageId", equalTo("SVC001"),
	    		"requestError.serviceException.variables", hasItem("User not authorized")
	    ).*/when().
	    post(Setup.registerURL("GSMAAcceptanceNonExist"));
		
		System.out.println("Response = "+resp.getStatusCode()+" / "+resp.asString());
		
		Setup.endTest();
	}
	
}
