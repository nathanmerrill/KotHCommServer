package models;

import io.ebean.annotation.EnumValue;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="entry")
public class EntryVersion extends BaseModel {
    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL)
    public Entry entry;

    @Constraints.Required
    @Column(nullable = false)
    public String name;

    @Constraints.Required
    @Column(nullable = false)
    @Lob
    public String code;

    @Constraints.Required
    @Column(nullable = false)
    public Language language;

    public enum Language {
        @EnumValue("Java")
        JAVA
    }

    @Constraints.Required
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "version", targetEntity = TournamentEntry.class)
    public List<TournamentEntry> tournamentEntries;



}

