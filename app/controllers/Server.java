package controllers;

import helpers.ServerHelper;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.fasterxml.jackson.databind.JsonNode;

public class Server extends Controller {
	private static String secret = "asdfjklö";
	
	private static boolean checkSecret(String secretToCheck) {
		return secret == secretToCheck;
	}
	
	@BodyParser.Of(BodyParser.Json.class)
    public static Result register() {
    	JsonNode json = request().body().asJson();
    	
    	if(checkSecret(json.findPath("secret").textValue())) {
    		String host = json.findPath("host").textValue();
    		
    		if(!ServerHelper.serverExists(host)) {
				ServerHelper.sendToAll("/push/server/", json);
    			
    			ServerHelper.registerNewServer(host);
    		}
    		
    		return ok();
    	}
    	
    	return internalServerError();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result unregister() {
		JsonNode json = request().body().asJson();
    	
    	if(checkSecret(json.findPath("secret").textValue())) {
    		String host = json.findPath("host").textValue();
    		
    		ServerHelper.unregisterServer(host);
    		
    		return ok();
    	}
    	
    	return internalServerError();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushServer() {
		JsonNode json = request().body().asJson();
    	
    	if(checkSecret(json.findPath("secret").textValue())) {
    		String host = json.findPath("host").textValue();
    		
    		if(!ServerHelper.serverExists(host)) {
    			ServerHelper.registerNewServer(host);
    		}
    		
    		return ok();
    	}
    	
    	return internalServerError();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushUser() {
        return ok("Push User");
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushMessage() {
        return ok("Push Message");
    }
}