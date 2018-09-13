package models;

import io.ebean.annotation.EnumValue;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="users")
public class User extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String authentication;

    @Column(nullable = false)
    public String name;

    public UserRole role;

    public enum UserRole {
        @EnumValue("Standard")
        STANDARD, //Can login, create challenges, view submissions
        @EnumValue("Creator")
        CREATOR, //Can submit challenges for execution
        @EnumValue("Admin")
        ADMIN //Can manage roles
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.MERGE, targetEntity = Challenge.class)
    public List<Challenge> challenges;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, targetEntity = Entry.class)
    public List<Entry> entries;


}

