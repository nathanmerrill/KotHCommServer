package models;

import io.ebean.annotation.EnumValue;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="challenge")
public class Challenge extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public User owner;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String repoUrl;

    @Column(nullable = false)
    public Language language;

    public enum Language {
        @EnumValue("Java")
        JAVA,
        @EnumValue("Gradle")
        GRADLE,
        @EnumValue("Npm")
        NPM,
        @EnumValue("Python 2")
        PYTHON_2,
        @EnumValue("Python 3")
        PYTHON_3
    }

    @Column()
    public String buildParameters;

    @Column(nullable = false)
    public Status status;

    public enum Status {
        Pending,
        Active,
        Finished,
        Inactive
    }

    @Column(nullable = false)
    public String refId;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, targetEntity = Entry.class)
    public List<Entry> entries;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, targetEntity = Tournament.class)
    public List<Tournament> versions;

}

