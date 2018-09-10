package models;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="tournament")
public class Tournament extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Challenge challenge;

    @Column(nullable = false)
    public String version;

    /**
     * The following parameters make a tournament unique.
     * If a user wants to change any one of them, it means we need to re-execute everything
     */

    @Column(nullable = false)
    public String gitHash;

    @Column(nullable = false)
    public Integer iterationGoal;

    @Column
    public Group primaryGroup;

    @Column
    public Integer configuration;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, targetEntity = TournamentEntry.class)
    public List<TournamentEntry> entries;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, targetEntity = Game.class)
    public List<Game> games;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, targetEntity = Group.class)
    public List<Group> groups;

}

