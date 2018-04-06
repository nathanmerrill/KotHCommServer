package models;

import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public class BaseModel extends Model {
    @Id
    public Long id;

    @Constraints.Required
    @Column(nullable = false)
    @CreatedTimestamp
    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createdAt;
}
