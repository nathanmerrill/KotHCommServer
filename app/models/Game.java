package models;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Table(name="game")
public class Game extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    public Tournament tournament;

    @Column(nullable = false)
    public Date startTime = new Date();

    public Date endTime;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, targetEntity = Score.class)
    public List<Score> scores;

}

