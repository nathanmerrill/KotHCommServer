package models;

import io.ebean.annotation.EnumValue;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "group")
public class Group extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Tournament tournament;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public Integer size;

    @Column(nullable = false)
    public Matchmaker matchmaker;

    @Column
    public String matchmakerParameters;

    @Column(nullable = false)
    public Scorer scorer;

    @Column
    public String scoringParameters;

    @Column(nullable = false)
    public boolean rankDescending;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, targetEntity = TournamentEntry.class)
    public List<TournamentEntry> entries;

    public enum Matchmaker {
        @EnumValue("Random Sample")  // Randomly selects players.  Balances individual games, but not combinations of players
         RANDOM_SAMPLE,
        @EnumValue("Similar Score")  // Pairs according to similar score.  Useful to de-clump similar-scoring players
        SIMILAR_SCORE,
        @EnumValue("Sobol")  // Pairs according to Sobol (similar to round robin).  Attempts to balance individual games, and combinations of players.  Does not support groups
        SOBOL,
        @EnumValue("Tournament")  // N-elimination tournament.  Does not balance individual games, nor combinations of players.  Game size must be 2.  Does not support groups.
        TOURNAMENT,
        @EnumValue("Elitist Selection")  // Only the best players will survive
        ELITIST_SELETION,
    }

    public enum Scorer {
        @EnumValue("Arithmetic Mean") // Sum score, divide by N.  Emphasizes outlier games: Particularly bad or good games will have a larger effect on the score
        ARITHMETIC_MEAN,
        @EnumValue("Geometric Mean")  // Product of scores all to the power of 1/N.  Demphasizes outlier games:  Particularly bad or good games will have a smaller effect on the score
        GEOMETRIC_MEAN,
        @EnumValue("Median") // Select the median score from each player.  Completely ignores outlier games
        MEDIAN,
        @EnumValue("Maximum") // Select the player's highest score.  You can optionally choose to remove the top N% of games from each players.
        MAXIMUM,
        @EnumValue("Minimum") // Select the player's lowest score.  You can optionally choose to remove the lowest N% of games from each player
        MINIMUM,
        @EnumValue("True Skill") // Calculates the ELO for each player, where winning against a better opponent increases your ELO by more
        TRUE_SKILL,
        @EnumValue("Condorcet") // Uses the rank of a player within a game, instead of their score.  Selects the player that would beat all other players in a head-to-head. Can select a variety of methods as a tiebreaker
        CONDORCET,
        @EnumValue("Single Transferable Vote") // Uses the rank of a player within a game, instead of the score.  Consists of a series of rounds, where each round throws out the lowest ranked player, and the games that player has won are given to the next ranked player
        SINGLE_TRANSFERRABLE_VOTE,
        @EnumValue("Rank Points") // Each rank within a game is given a fixed number of points (1st place is 5 points, 2nd place is 3 points, etc), and then scores are summed
        RANK_POINTS
    }

}

