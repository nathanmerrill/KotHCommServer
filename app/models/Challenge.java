package models;

import io.ebean.annotation.EnumValue;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="challenge")
public class Challenge extends BaseModel {  //TODO: look into Anorm, Slick, Doobie, Squeryl

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public User owner;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public Source source;

    public enum Source {
        @EnumValue("StackExchange")
        STACK_EXCHANGE
    }

    public String refId;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, targetEntity = Entry.class)
    public List<Entry> entries;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, targetEntity = Tournament.class)
    public List<Tournament> versions;


}

