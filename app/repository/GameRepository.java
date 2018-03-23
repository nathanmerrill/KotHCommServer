package repository;

import models.Game;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;


/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class GameRepository extends BaseRepository<Game> {

    @Inject
    public GameRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    @Override
    protected Class<Game> getModelClass() {
        return Game.class;
    }

    public CompletionStage<Optional<Game>> view(Long id) {
        return get(id, query -> query
                .fetch("tournament", "")
                .fetch("scores", "score")
                .fetch("scores.tournamentEntry.version", "name"));
    }

}
