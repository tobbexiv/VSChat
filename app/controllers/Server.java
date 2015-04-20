package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import views.html.index;

public class Server extends Controller {
	private static String secret = "asdfjklö";
	
    public static Result register() {
    	RequestBody body = request().body();
        return ok("Got json: " + body.asJson());
    }
    
    public static Result unregister() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result pushServer() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result pushUser() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result pushMessage() {
        return ok(index.render("Your new application is ready."));
    }
}