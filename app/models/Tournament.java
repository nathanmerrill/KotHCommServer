package models;

import io.ebean.annotation.EnumValue;
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
    public String version;

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
    @Lob
    @Column(nullable = false)
    public String scoringParameters;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, targetEntity = TournamentEntry.class)
    public List<TournamentEntry> entries;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, targetEntity = Game.class)
    public List<Game> games;


    public enum Matchmaker {
        @EnumValue("SimilarScore")
        SIMILAR_SCORE,
        @EnumValue("RandomSample")
        RANDOM_SAMPLE,
    }

    public enum Scorer {
        @EnumValue("ArithmeticMean")
        ARITHMETIC_MEAN,
        @EnumValue("GeometricMean")
        GEOMETRIC_MEAN,
        @EnumValue("Maximum")
        MAXIMUM,
        @EnumValue("Minimum")
        MINIMUM,
        @EnumValue("Median")
        MEDIAN,
        @EnumValue("Elo")
        ELO,
        @EnumValue("Condorcet")
        CONDORCET,
        @EnumValue("SingleTransferableVote")
        SINGLE_TRANSFERRABLE_VOTE,
        @EnumValue("RankPoints")
        RANK_POINTS
    }

}

