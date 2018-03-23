package repository;

import models.Tournament;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;


/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class UserRepository extends BaseRepository<Tournament> {

    @Inject
    public UserRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    @Override
    protected Class<Tournament> getModelClass() {
        return Tournament.class;
    }

    public CompletionStage<Optional<Tournament>> view(Long id) {
        return get(id, query -> query
                .select("id,name,stackExchangeId")
                .fetch("entries", "id,currentName")
                .fetch("entries.challenge", "name")
                .fetch("challenges", "id,name"));
    }
}
