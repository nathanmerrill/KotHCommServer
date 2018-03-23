package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;
import models.BaseModel;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public abstract class BaseRepository<T extends BaseModel> {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public BaseRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext){
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    protected abstract Class<T> getModelClass();

    protected Query<T> query(){
        return ebeanServer.find(getModelClass());
    }

    protected <U> CompletionStage<U> get(Function<Query<T>, U> queryBuilder){
        return supplyAsync(() -> queryBuilder.apply(query()), executionContext);
    }

    protected CompletionStage<Optional<T>> get(Long id, Function<Query<T>, Query<T>> queryBuilder){
        return get(queryBuilder.andThen(query -> query.setId(id).findOneOrEmpty()));
    }

    protected CompletionStage<Optional<T>> updateModel(T data){
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                T saved = ebeanServer.find(getModelClass()).setId(data.id).findOne();
                if (saved != null) {
                    saved.update();
                    txn.commit();
                }
                return Optional.ofNullable(saved);
            } finally {
                txn.end();
            }
        }, executionContext);
    }

    public CompletionStage<Optional<T>> get(Long id) {
        return get(id, query->query);
    }

    public CompletionStage<T> insert(T data) {
        return supplyAsync(() -> {
            data.id = null;
            ebeanServer.insert(data);
            return data;
        }, executionContext);
    }

}
