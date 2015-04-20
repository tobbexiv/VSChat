package controllers;

import helpers.ServerHelper;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import models.Message;
import models.User;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Server extends Controller {
	private static ObjectMapper mapper = new ObjectMapper();
	
	@BodyParser.Of(BodyParser.Json.class)
    public static Result register() throws JsonProcessingException {
    	JsonNode json = request().body().asJson();
    	
		String host = json.findPath("host").textValue();
		
		if(!ServerHelper.serverExists(host)) {
			ServerHelper.sendToAll("/server/push/server/", json);
			
			ServerHelper.registerNewServer(host);
			
			Server.informServer(host);
		}
		
		return ok();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result unregister() {
		JsonNode json = request().body().asJson();
    	
		String host = json.findPath("host").textValue();
		
		ServerHelper.unregisterServer(host);
		
		return ok();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushServer() {
		JsonNode json = request().body().asJson();
    	
		String host = json.findPath("host").textValue();
		
		if(!ServerHelper.serverExists(host)) {
			ServerHelper.registerNewServer(host);
		}
		
		return ok();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushUser() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		User user = mapper.readValue(json.toString(), User.class);
		ServerHelper.storeUser(user);
		
		return ok();
    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public static Result pushMessage() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		Message message = mapper.readValue(json.toString(), Message.class);
		ServerHelper.storeMessage(message);
		
		return ok();
    }
	
	@BodyParser.Of(BodyParser.Json.class)
    public static Result getInformedMessages() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		Message message;
		
		for (Iterator iterator = json.iterator(); iterator.hasNext();) {
			JsonNode single = (JsonNode) iterator.next();
			
			message = mapper.readValue(single.toString(), Message.class); // evtl. setter missing
			ServerHelper.storeMessage(message);
		}
		
		return ok();
    }
	
	public static Result getInformedUsers() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		User user;
		
		for (Iterator iterator = json.iterator(); iterator.hasNext();) {
			JsonNode single = (JsonNode) iterator.next();
			
			user = mapper.readValue(single.toString(), User.class); // evtl. setter missing
			ServerHelper.storeUser(user);
		}
		
		return ok();
    }
	
	private static void informServer(String host) throws JsonProcessingException {
		List<String> serverList = ServerHelper.getServerList();
		
		for (Iterator iterator = serverList.iterator(); iterator.hasNext();) {
			String hostToPush = (String) iterator.next(); // TODO: Self ...?
			
			ObjectNode result = Json.newObject();
			result.put("host", hostToPush);
			
			ServerHelper.sendToServer(host + "/server/push/server", result);
		}
		
		
		ServerHelper.sendToServer(host + "/server/inform/users", Json.toJson(Ebean.find(User.class).findList()));
		ServerHelper.sendToServer(host + "/server/inform/messages", Json.toJson(Ebean.find(Message.class).orderBy("sent").findList()));
	}
}