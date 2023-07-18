package ai.stapi.arangograph.repositorypruner;

import ai.stapi.graph.repositorypruner.RepositoryPruner;
import com.arangodb.ArangoDB;
import com.arangodb.model.CollectionsReadOptions;

public class ArangoRepositoryPruner implements RepositoryPruner {
    
    private final ArangoDB arangoDB;

    public ArangoRepositoryPruner(ArangoDB arangoDB) {
        this.arangoDB = arangoDB;
    }

    @Override
    public void prune() {
        this.arangoDB.db().getCollections(new CollectionsReadOptions().excludeSystem(true)).forEach(
            collectionEntity -> arangoDB.db().collection(collectionEntity.getName()).drop()
        );
    }
}
