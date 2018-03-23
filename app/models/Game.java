package models;

import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;


@Entity
public class Game extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @ManyToOne
    public ChallengeVersion version;

    @Constraints.Required
    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    public Date startTime = new Date();

    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    public Date finishTime;

}

