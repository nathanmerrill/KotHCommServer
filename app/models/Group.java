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
    public String scorerParameters;

    @Column(nullable = false)
    public boolean rankDescending;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, targetEntity = TournamentEntry.class)
    public List<TournamentEntry> entries;

    public enum Matchmaker {
        @EnumValue("Random Sample")
         RANDOM_SAMPLE,
        @EnumValue("Similar Score")
        SIMILAR_SCORE,
        @EnumValue("Sobol")
        SOBOL,
        @EnumValue("Tournament")
        TOURNAMENT,
        @EnumValue("Elitist Selection")
        ELITIST_SELETION,
    }

    public enum Scorer {
        @EnumValue("Arithmetic Mean")
        ARITHMETIC_MEAN,
        @EnumValue("Geometric Mean")
        GEOMETRIC_MEAN,
        @EnumValue("Median")
        MEDIAN,
        @EnumValue("Maximum")
        MAXIMUM,
        @EnumValue("Minimum")
        MINIMUM,
        @EnumValue("True Skill")
        TRUE_SKILL, //http://www.moserware.com/2010/03/computing-your-skill.html
        @EnumValue("Condorcet")
        CONDORCET,
        @EnumValue("Single Transferable Vote")
        SINGLE_TRANSFERRABLE_VOTE,
        @EnumValue("Rank Points")
        RANK_POINTS
    }

    public enum CondorcetTiebreakers {
        @EnumValue("Plain Condorcet")
        PLAIN_CONDORCET,
        @EnumValue("Schulze")
        SCHULZE,
        @EnumValue("Maximized Affirmed Majorities")
        MAXIMIZED_AFFIRMED_MAJORITIES
    }

}

