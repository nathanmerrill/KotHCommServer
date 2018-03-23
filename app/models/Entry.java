package models;

import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="challenge_entry")
public class Entry extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL)
    public User owner;

    @ManyToOne(optional = false)
    public Challenge challenge;

    @Constraints.Required
    @Column(nullable = false)
    public String currentName;

    public String refId;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, targetEntity = EntryVersion.class)
    public List<EntryVersion> versions;

}
