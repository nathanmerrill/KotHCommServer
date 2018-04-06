package controllers;

import io.ebean.Ebean;
import io.ebean.text.json.JsonIOException;
import models.User;
import play.mvc.Controller;

import java.util.Optional;

public class BaseController extends Controller {

    public static Optional<User> getUser() {
        try {
            return Optional.ofNullable(session().get("user"))
                    .map(json -> Ebean.json().toBean(User.class, json));
        } catch (JsonIOException e){
            return Optional.empty();
        }
    }


    public static boolean loggedIn() {
        return getUser().isPresent();
    }
}
