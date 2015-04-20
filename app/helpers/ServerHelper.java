package helpers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import models.Message;
import models.User;
import play.libs.ws.WS;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

public class ServerHelper {
	private static List<String>	servers	= new LinkedList<String>();
	
	public static boolean serverExists(String host) {
		return servers.contains(host);
	}
		
    public static void registerNewServer(String host) {
    	if(!ServerHelper.serverExists(host)) {
    		servers.add(host);
		}
    }
    
    public static void unregisterServer(String host) {
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
    
    public static void sendToAll(String path, JsonNode json) {
    	for(Iterator<String> iterator = servers.iterator(); iterator.hasNext();) {
			String serverHost = (String) iterator.next();

			ServerHelper.sendToServer(serverHost + path, json);
		}
    }
    
    public static void sendToServer(String url, JsonNode json) {
    	WS.url(url).post(json);
    }
    
    public static List<String> getServerList() {
    	return servers;
    }
}
