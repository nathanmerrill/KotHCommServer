package models;

import io.ebean.annotation.EnumValue;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="entry_version")
public class EntryVersion extends BaseModel {
    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Entry entry;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    @Lob
    public String code;

    @Column(nullable = false)
    public Language language;

    public enum Language {
        @EnumValue("Java")
        JAVA
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "version", targetEntity = TournamentEntry.class)
    public List<TournamentEntry> tournamentEntries;



}

