package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.login;

import javax.inject.Inject;

public class AuthenticationController extends Controller {

    @Inject
    public AuthenticationController(
    ) {
    }

    public Result auth() {
        return play.mvc.Results.TODO;
    }

    public Result logout() {
        return play.mvc.Results.TODO;
    }

    public Result register() {
        return play.mvc.Results.TODO;
    }

    public Result registerSubmit() {
        return play.mvc.Results.TODO;
    }

    public Result login() {
        return ok(
            login.render()
        );
    }

}
            
