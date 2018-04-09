package models;

import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public class BaseModel extends Model {
    @Id
    public Long id;

    @Column(nullable = false)
    @CreatedTimestamp
    public LocalDateTime createdAt;
}
