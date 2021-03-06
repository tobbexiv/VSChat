package controllers;

import helpers.ServerHelper;

import java.io.IOException;
import java.sql.Timestamp;

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
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Client extends Controller {
	public static Result index() {
		if(!ServerHelper.isInitialized()) {
			return redirect(controllers.routes.Server.init());
		}
		
		if(Client.isLoggedIn()) {
			return redirect(controllers.routes.Client.chat());
        }
		
		return redirect(controllers.routes.Client.login());
    }
	
    public static Result register() {
    	if(!ServerHelper.isInitialized()) {
			return redirect(controllers.routes.Server.init());
		}
    	
    	if(!Client.isLoggedIn()) {
        	return ok(views.html.register.render());
        }
        
        return redirect(controllers.routes.Client.chat());
    }
    
    public static Result doRegister() throws JsonProcessingException {
    	if(!ServerHelper.isInitialized()) {
			return redirect(controllers.routes.Server.init());
		}
    	
    	if(Client.isLoggedIn()) {
    		redirect(controllers.routes.Client.chat());
        }
    	
    	String username = Form.form().bindFromRequest().get("username");
    	String password = Form.form().bindFromRequest().get("password");
    	
    	if(username.length() < 4 || password.length() < 4) {
    		return unauthorized("Nutername oder Passwort zu kurz (mindestlänge 4)!");
    	}
    	
    	if(Ebean.find(User.class).where().eq("username", username).findUnique() != null) {
    		return unauthorized("Nutzername schon vergeben!");
    	}
    	
    	User user = new User(username, password);
    	Ebean.save(user);
    	
    	ServerHelper.sendToAll(controllers.routes.Server.pushUser().url(), Json.toJson(user));
    	
    	session("loggedIn", "true");
    	session("username", username);
    	
        return redirect(controllers.routes.Client.index());
    }
    
    public static Result login() {
    	if(!ServerHelper.isInitialized()) {
			return redirect(controllers.routes.Server.init());
		}
    	
        if(!Client.isLoggedIn()) {
        	return ok(views.html.login.render());
        }
        
        return redirect(controllers.routes.Client.chat());
    }
    
    public static Result doLogin() throws JsonParseException, JsonMappingException, IOException {
    	if(!ServerHelper.isInitialized()) {
			return redirect(controllers.routes.Server.init());
		}
    	
    	String username = Form.form().bindFromRequest().get("username");
    	String password = Form.form().bindFromRequest().get("password");
    	
    	User user = Ebean.find(User.class).where().eq("username", username).findUnique();
    	
    	if(user == null || user.getPassword().compareTo(password) != 0) {
    		return unauthorized("Fehlerhafte Logindaten!");
    	}
    	
    	session("loggedIn", "true");
    	session("username", username);
    	
    	return redirect(controllers.routes.Client.index());
    }
    
    public static Result logout() {
    	session().clear();
    	return redirect(controllers.routes.Client.index().url());
    }
    
    public static Result chat() {
    	if(!ServerHelper.isInitialized()) {
			return redirect(controllers.routes.Server.init());
		}
    	
    	if(!Client.isLoggedIn()) {
			return redirect(controllers.routes.Client.login());
        }
		
        return ok(views.html.chat.render());
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result sendMessage() throws JsonProcessingException {
    	if(!Client.isLoggedIn()) {
    		return unauthorized("Not logged in!");
        }
    	
    	JsonNode json = request().body().asJson();
		
    	User sender = Ebean.find(User.class).where().eq("username", session("username")).findUnique();
    	String messageText = json.findPath("message").textValue().replaceAll("\"", "&quot;");
    	if(messageText.length() == 0) {
    		return unauthorized("No content!");
    	}
		Message message = new Message(messageText, sender);
		Ebean.save(message);
		
		ServerHelper.sendToAll(controllers.routes.Server.pushMessage().url(), Json.toJson(message));
		
    	return ok(Json.stringify(Json.toJson(message)));
    }
    
    public static Result getMessagesByTime(int lastXHours) throws JsonProcessingException {
    	if(!Client.isLoggedIn()) {
    		return unauthorized("Not logged in!");
        }
    	
    	JsonNode json;
    	
    	if(lastXHours >= 0) {
    		Timestamp after = new Timestamp(System.currentTimeMillis() - lastXHours * 3600000);
	    	json = Json.toJson(Ebean.find(Message.class).where().gt("sent", after).orderBy("sent").findList());
    	} else {
    		json = Json.toJson(Ebean.find(Message.class).orderBy("sent").findList());
    	}
    	
    	return ok(Json.stringify(json));
    }
    
    public static Result getMessagesById(long id) throws JsonProcessingException {
    	if(!Client.isLoggedIn()) {
    		return unauthorized("Not logged in!");
        }
    	
    	JsonNode json = Json.toJson(Ebean.find(Message.class).where().gt("id", id).orderBy("sent").findList());
    	
    	return ok(Json.stringify(json));
    }
    
    public static Result getLatestMessageId() {
    	if(!Client.isLoggedIn()) {
    		return unauthorized("Not logged in!");
        }
    	
    	long latestId = Ebean.find(Message.class).orderBy().desc("id").setMaxRows(1).findUnique().getId();
    	
    	ObjectNode result = Json.newObject();
		result.put("latestId", latestId);
		
		return ok(result);
    }
    
    private static boolean isLoggedIn() {
    	boolean sessionExists = session("loggedIn") != null && session("username") != null && session("loggedIn").compareTo("true") == 0;
    	
    	if(sessionExists) {
    		sessionExists = Ebean.find(User.class).where().eq("username", session("username")).findUnique() != null;
    	}
    	
    	if(!sessionExists) {
    		session().clear();
    	}
    	
    	return sessionExists;
    }
}
