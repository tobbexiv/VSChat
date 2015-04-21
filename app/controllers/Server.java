package controllers;

import helpers.ServerHelper;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import models.Message;
import models.User;
import play.data.Form;
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
	
	public static Result init() {
		return ok(views.html.init.render());
	}
	
	public static Result doInit() {
		String ownHost = Form.form().bindFromRequest().get("ownHost");
    	String connectTo = Form.form().bindFromRequest().get("connectTo");
    	
    	ObjectNode result = Json.newObject();
		result.put("host", ownHost);
    	
    	ServerHelper.setOwnHost(ownHost);
    	
    	if(connectTo.length() > 0) {
    		ServerHelper.registerNewServer(connectTo);
    		ServerHelper.sendToServer(connectTo + controllers.routes.Server.register(), Json.toJson(result));
    	}
    	
		return redirect(controllers.routes.Client.index());
	}
	
	@BodyParser.Of(BodyParser.Json.class)
    public static Result register() throws JsonProcessingException {
    	JsonNode json = request().body().asJson();
    	
		String host = json.findPath("host").textValue();
		
		ServerHelper.sendToAll(controllers.routes.Server.pushServer().url(), json);
		
		ServerHelper.registerNewServer(host);
		
		Server.informServer(host);
		
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
		
		
		int		UUID		= json.findPath("uuid").intValue();
		String	messageText	= json.findPath("message").textValue();
		String	username	= json.findPath("sender").textValue();
		User	sender		= Ebean.find(User.class).where().eq("username", username).findUnique();
		long	sentTime	= json.findPath("sentTimeAsLong").longValue();
		Timestamp	sent	= new Timestamp(sentTime);
		
		Message message = new Message(UUID, messageText, sender, sent);
		ServerHelper.storeMessage(message);
		
		return ok();
    }
	
	@BodyParser.Of(BodyParser.Json.class)
    public static Result getInformedMessages() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		Message message;
		
		for (Iterator iterator = json.iterator(); iterator.hasNext();) {
			JsonNode single = (JsonNode) iterator.next();
			
			int		UUID		= single.findPath("uuid").intValue();
			String	messageText	= single.findPath("message").textValue();
			String	username	= single.findPath("sender").textValue();
			User	sender		= Ebean.find(User.class).where().eq("username", username).findUnique();
			long	sentTime	= single.findPath("sentTimeAsLong").longValue();
			Timestamp	sent	= new Timestamp(sentTime);
			
			message = new Message(UUID, messageText, sender, sent);
			ServerHelper.storeMessage(message);
		}
		
		return ok();
    }
	
	public static Result getInformedUsers() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		
		User user;
		
		for (Iterator iterator = json.iterator(); iterator.hasNext();) {
			JsonNode single = (JsonNode) iterator.next();
			
			user = mapper.readValue(single.toString(), User.class);
			ServerHelper.storeUser(user);
		}
		
		return ok();
    }
	
	private static void informServer(String host) throws JsonProcessingException {
		List<String> serverList = ServerHelper.getServerList();
		
		for (Iterator iterator = serverList.iterator(); iterator.hasNext();) {
			String hostToPush = (String) iterator.next();
			
			ObjectNode result = Json.newObject();
			result.put("host", hostToPush);
			
			ServerHelper.sendToServer(host + controllers.routes.Server.pushServer().url(), result);
		}
		
		ServerHelper.sendToServer(host + controllers.routes.Server.getInformedUsers().url(), Json.toJson(Ebean.find(User.class).findList()));
		ServerHelper.sendToServer(host + controllers.routes.Server.getInformedMessages().url(), Json.toJson(Ebean.find(Message.class).orderBy("sent").findList()));
	}
}