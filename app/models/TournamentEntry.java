package models;

import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="tournamentEntry")
public class TournamentEntry extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @ManyToOne(cascade = CascadeType.ALL)
    public EntryVersion version;

    @Constraints.Required
    @ManyToOne(cascade = CascadeType.ALL)
    public Tournament tournament;

    public Long rank;

    @OneToMany(mappedBy = "tournamentEntry", cascade = CascadeType.ALL, targetEntity = Score.class)
    public List<Score> scores;

}

