package repository;

import models.Challenge;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class ChallengeRepository extends BaseRepository<Challenge> {

    @Inject
    public ChallengeRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    @Override
    protected Class<Challenge> getModelClass() {
        return Challenge.class;
    }

    /**
     * Return a list of all challenges
     */
    public CompletionStage<List<Challenge>> all() {
        return get(query -> query
                .orderBy("createdAt")
                .select("name,createdAt")
                .fetch("owner", "id,name")
                .findList());
    }

    public CompletionStage<Optional<Challenge>> view(Long id) {
        return get(id, query -> query
                .fetch("entries", "id,currentName")
                .fetch("owner", "id,name")
                .fetch("versions", "id,createdAt,versionId"));
    }

    public CompletionStage<Optional<Challenge>> update(Challenge data) {
        Challenge toSave = new Challenge();
        toSave.id = data.id;
        toSave.name = data.name;
        toSave.refId = data.refId;
        toSave.owner = data.owner;
        return updateModel(data);
    }

}
