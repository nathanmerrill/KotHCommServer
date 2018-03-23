package models;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@Entity
public class ChallengeVersion extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    public String versionNumber;

    @Constraints.Required
    @ManyToOne
    public Challenge challenge;

    @Constraints.Required
    public String gitHash;


}

