package repository;

import models.Entry;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class EntryRepository extends BaseRepository<Entry> {

    @Inject
    public EntryRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    @Override
    protected Class<Entry> getModelClass() {
        return Entry.class;
    }

    public CompletionStage<Optional<Entry>> view(Long id) {
        return get(id, query -> query
                .fetch("versions", "id,name,createdAt")
                .fetch("owner", "id,name")
                .fetch("versions","id,createdAt,versionId"));
    }

    public CompletionStage<Optional<Entry>> update(Entry data) {
        Entry toSave = new Entry();
        toSave.id = data.id;
        toSave.currentName = data.currentName;
        toSave.refId = data.refId;
        toSave.owner = data.owner;
        return updateModel(data);
    }
}
