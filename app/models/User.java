package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;


@Entity
public class User extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String authentication;

    @Constraints.Required
    public String name;

}

