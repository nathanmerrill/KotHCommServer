package repository;

import models.EntryVersion;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class EntryVersionRepository extends BaseRepository<EntryVersion>{

    @Inject
    public EntryVersionRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    @Override
    protected Class<EntryVersion> getModelClass() {
        return EntryVersion.class;
    }
}
