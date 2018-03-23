package models;

import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Table(name="game")
public class Game extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Tournament tournament;

    @Constraints.Required
    @Column(nullable = false)
    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    public Date startTime = new Date();

    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    public Date endTime;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, targetEntity = Score.class)
    public List<Score> scores;

}

