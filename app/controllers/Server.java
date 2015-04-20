package controllers;

import java.io.IOException;

import helpers.ServerHelper;
import models.Message;
import models.User;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server extends Controller {
	private static String secret = "asdfjklö";
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static boolean checkSecret(String secretToCheck) {
		return secret == secretToCheck;
	}
	
	@BodyParser.Of(BodyParser.Json.class)
    public static Result register() {
    	JsonNode json = request().body().asJson();
    	
    	if(checkSecret(json.findPath("secret").textValue())) {
    		String host = json.findPath("host").textValue();
    		
    		if(!ServerHelper.serverExists(host)) {
				ServerHelper.sendToAll("/server/push/server/", json);
    			
    			ServerHelper.registerNewServer(host);
    		}
    		
    		return ok();
    	}
    	
    	return unauthorized();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result unregister() {
		JsonNode json = request().body().asJson();
    	
    	if(checkSecret(json.findPath("secret").textValue())) {
    		String host = json.findPath("host").textValue();
    		
    		ServerHelper.unregisterServer(host);
    		
    		return ok();
    	}
    	
    	return unauthorized();
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
    	
    	return unauthorized();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushUser() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		if(checkSecret(json.findPath("secret").textValue())) {
			User user = mapper.readValue(json.toString(), User.class);
			
			if(Ebean.find(User.class).where().eq("username", user.getUsername()).findUnique() == null) {
				Ebean.save(user);
			}
			
			return ok();
		}
		
		return unauthorized();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushMessage() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		if(checkSecret(json.findPath("secret").textValue())) {
			Message message = mapper.readValue(json.toString(), Message.class);
			
			if(Ebean.find(Message.class).where().eq("UUID", message.getUUID()).findUnique() == null) {
				Ebean.save(message);
			}
			
			return ok();
		}
		
		return unauthorized();
    }
}