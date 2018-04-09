package models;


import javax.persistence.*;


@Entity
@Table(name="score")
public class Score extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Game game;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public TournamentEntry tournamentEntry;

    @Column(nullable = false)
    public Double score;

}

