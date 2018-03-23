package repository;

import models.Score;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;


/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class ScoreRepository extends BaseRepository<Score> {

    @Inject
    public ScoreRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    @Override
    protected Class<Score> getModelClass() {
        return Score.class;
    }
}
