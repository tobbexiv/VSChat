package controllers;

import helpers.ServerHelper;

import java.io.IOException;
import java.sql.Date;

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

public class Client extends Controller {
	private static Form<User> userForm = Form.form(User.class);
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static Result index() {
		if(session("loggedIn") == "true") {
			return redirect(controllers.routes.Client.chat());
        }
		
		return redirect(controllers.routes.Client.login());
    }
	
    public static Result register() {
    	if(session("loggedIn") != "true") {
        	return ok("TODO: Register Form");
        }
        
        return redirect(controllers.routes.Client.chat());
    }
    
    public static Result doRegister() throws JsonProcessingException {
    	if(session("loggedIn") == "true") {
    		redirect(controllers.routes.Client.chat());
        }
        
    	User user = userForm.bindFromRequest().get();
    	
    	if(Ebean.find(User.class).where().eq("username", user.getUsername()).findUnique() != null) {
    		return unauthorized("Schon erstellt!");
    	}
    	
    	Ebean.save(user);
    	
    	ServerHelper.sendToAll("/server/push/user", Json.parse(mapper.writeValueAsString(user)));
    	
    	session("loggedIn", "true");
    	session("username", user.getUsername());
    	
        return ok();
    }
    
    public static Result login() {
        if(session("loggedIn") != "true") {
        	return ok("TODO: Login Form");
        }
        
        return redirect(controllers.routes.Client.chat());
    }
    
    public static Result doLogin() throws JsonParseException, JsonMappingException, IOException {
    	User user = userForm.bindFromRequest().get();
    	
    	if(Ebean.find(User.class).where().eq("username", user.getUsername()).findUnique().getPassword() != user.getPassword()) {
    		return unauthorized("Fehlerhafte Logindaten!");
    	}
    	
    	session("loggedIn", "true");
    	session("username", user.getUsername());
    	
        return ok();
    }
    
    public static Result logout() {
    	session().clear();
    	return redirect(controllers.routes.Client.index());
    }
    
    public static Result chat() {
        return ok("TODO: Chat anzeigen");
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result sendMessage() throws JsonProcessingException {
    	if(session("loggedIn") != "true") {
    		return unauthorized("Not logged in!");
        }
    	
    	JsonNode json = request().body().asJson();
		
    	User sender = Ebean.find(User.class).where().eq("username", session("username")).findUnique();
    	String messageText = json.findPath("message").textValue();
		Message message = new Message(messageText, sender);
		Ebean.save(message);
		
		ServerHelper.sendToAll("/server/push/message", Json.parse(mapper.writeValueAsString(message)));
		
    	return ok();
    }
    
    public static Result getMessages(int lastXHours) throws JsonProcessingException {
    	if(session("loggedIn") != "true") {
    		return unauthorized("Not logged in!");
        }
    	
    	Date after = new Date(System.currentTimeMillis() - lastXHours * 3600000);
    	String jsonText = mapper.writeValueAsString(Ebean.find(Message.class).where().gt("sent", after));
    	
    	return ok(jsonText);
    }
}
