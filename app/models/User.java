package models;

import io.ebean.annotation.EnumValue;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="user")
public class User extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Column(unique = true, nullable = false)
    public String username;

    @Constraints.Required
    @Column(nullable = false)
    @Lob
    public String authentication;

    @Constraints.Required
    @Column(nullable = false)
    public String name;

    public String stackExchangeID;

    @Constraints.Required
    public UserRole role;

    public enum UserRole {
        @EnumValue("Banned")
        BANNED,
        @EnumValue("Standard")
        STANDARD,
        @EnumValue("Creator")
        CREATOR,
        @EnumValue("Admin")
        ADMIN
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.MERGE, targetEntity = Challenge.class)
    public List<Challenge> challenges;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, targetEntity = Entry.class)
    public List<Entry> entries;


}

