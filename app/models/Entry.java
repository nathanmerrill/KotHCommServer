package models;


import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="entry")
public class Entry extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL)
    public User owner;

    @ManyToOne(optional = false)
    public Challenge challenge;

    @Column(nullable = false)
    public String currentName;

    public String refId;

    @Column(nullable = false)
    public EntryVersion latestVersion;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, targetEntity = EntryVersion.class)
    public List<EntryVersion> versions;

}

