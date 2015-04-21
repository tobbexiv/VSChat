package helpers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import models.Message;
import models.User;
import play.libs.Json;
import play.libs.ws.WS;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServerHelper {
	private static List<String>	servers	= new LinkedList<String>();
	private static String		ownHost = "";
	
	public static void setOwnHost(String host) {
		ownHost = host;
	}
	
	public static boolean isInitialized() {
		return ownHost.length() > 0;
	}
	
	public static boolean serverExists(String host) {
		return servers.contains(host);
	}
		
    public static void registerNewServer(String host) {
    	System.out.println("registerNewServer: " + host);
    	if(!ServerHelper.serverExists(host) && !ownHost.equals(host)) {
    		servers.add(host);
		}
    }
    
    public static void unregisterServer(String host) {
    	System.out.println("unregisterServer: " + host);
    	servers.remove(host);
    }
    
    public static void storeMessage(Message message) {
		if(Ebean.find(Message.class).where().eq("UUID", message.getUUID()).findUnique() == null) {
			Ebean.save(message);
		}
	}

    public static void storeUser(User user) {
		if(Ebean.find(User.class).where().eq("username", user.getUsername()).findUnique() == null) {
			Ebean.save(user);
		}
	}
    
    public static void unregisterFromAll() {
    	ObjectNode result = Json.newObject();
		result.put("host", ownHost);
		
    	ServerHelper.sendToAll(controllers.routes.Server.unregister().url(), Json.toJson(result));
    }
    
    public static void sendToAll(String path, JsonNode json) {
    	for(Iterator<String> iterator = servers.iterator(); iterator.hasNext();) {
			String serverHost = (String) iterator.next();

			ServerHelper.sendToServer(serverHost + path, json);
		}
    }
    
    public static void sendToServer(String url, JsonNode json) {
    	System.out.println("sendToServer: " + url);
    	WS.url(url).post(json);
    }
    
    public static List<String> getServerList() {
    	return servers;
    }
}
