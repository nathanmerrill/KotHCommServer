package models;

import io.ebean.annotation.EnumValue;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="user")
public class User extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    @Lob
    public String authentication;

    @Column(nullable = false)
    public String name;

    public String stackExchangeID;

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

