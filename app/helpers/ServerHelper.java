package helpers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import play.libs.ws.WS;

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
    
    public static void sendToAll(String path, JsonNode json) {
    	for(Iterator<String> iterator = servers.iterator(); iterator.hasNext();) {
			String serverHost = (String) iterator.next();

			WS.url(serverHost + path).post(json);
		}
    }
}
