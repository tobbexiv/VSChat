package controllers;

import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

    public static Result index() {
    	RequestBody body = request().body();
    	
    	return ok(body.asJson());
    }

}
