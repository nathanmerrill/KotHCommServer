package models;

import play.data.validation.Constraints;
import validators.JsonValidator;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name="tournament")
public class Tournament extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Challenge challenge;

    @Constraints.Required
    @Column(nullable = false)
    @Version
    public String versionId;

    /**
     * The following parameters make a tournament unique.
     * If a user wants to change any one of them, it means we need to re-execute everything
     */

    @Constraints.Required
    @Column(nullable = false)
    public String gitHash;

    @Constraints.Required
    @Column(nullable = false)
    public Matchmaker matchmaker;

    @Constraints.Required
    @Column(nullable = false)
    public Integer gameSize;

    @Constraints.Required
    @Column(nullable = false)
    public Scorer scorer;

    @Constraints.ValidateWith(JsonValidator.class)
    @Constraints.Required
    @Column(nullable = false)
    public String scoringParameters;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, targetEntity = TournamentEntry.class)
    public List<TournamentEntry> entries;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, targetEntity = Game.class)
    public List<Game> games;


    public enum Matchmaker {
        SIMILAR_SCORE,
        RANDOM_SAMPLE,
        //TODO: Figure out if it's possible to do round-robin
    }

    public enum Scorer {
        AVERAGE,
        MAXIMUM,
        MINIMUM,
        MEDIAN,
        ELO,
        MAXIMIZE_AFFIRMED_MAJORITIES,
        SINGLE_TRANSFERRABLE_VOTE,
        RANK_POINTS
    }

}

