package models;


import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="tournament_entry")
public class TournamentEntry extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL)
    public EntryVersion version;

    @ManyToOne(cascade = CascadeType.ALL)
    public Tournament tournament;

    @ManyToOne(cascade = CascadeType.ALL)
    public Group group;

    public Long rank;

    @OneToMany(mappedBy = "tournamentEntry", cascade = CascadeType.ALL, targetEntity = Score.class)
    public List<Score> scores;

}

