package repository;

import models.TournamentEntry;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class TournamentEntryRepository extends BaseRepository<TournamentEntry> {


    @Inject
    public TournamentEntryRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    @Override
    protected Class<TournamentEntry> getModelClass() {
        return TournamentEntry.class;
    }

    public CompletionStage<Optional<TournamentEntry>> view(Long id) {
        return get(id, query -> query
                .fetch("version", "name,code,language")
                .fetch("version.entry.owner", "id,name")
                .fetch("tournament", "id")
                .fetch("tournament.challenge", "id,name")
                .fetch("scores", "score")
                .fetch("scores.game", "id"));
    }

}
