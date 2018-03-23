package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@Entity 
public class Challenge extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @ManyToOne
    public User user;

    public Long sePostId;

    public enum Matchmaker {
        ADJACENT_GROUP,
        SIMILAR_SCORE,
        RANDOM_SAMPLE
    }

}

