package models;

import play.data.validation.Constraints;

import javax.persistence.*;


@Entity
@Table(name="score")
public class Score extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Game game;

    @Constraints.Required
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public TournamentEntry tournamentEntry;

    @Constraints.Required
    @Column(nullable = false)
    public Double score;

}

