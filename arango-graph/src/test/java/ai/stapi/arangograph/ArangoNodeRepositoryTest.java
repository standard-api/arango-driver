package ai.stapi.arangograph;

import com.arangodb.ArangoDB;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public class ArangoNodeRepositoryTest extends AbstractNodeRepositoryTest {

  @Autowired
  protected ArangoDB arangoDB;
  
  @Autowired
  private ArangoNodeRepository nodeRepository;
  
  @Autowired
  private ArangoEdgeRepository edgeRepository;

  @BeforeEach
  public void setUp() {
    arangoDB.db().getCollections().forEach(
        CollectionEntity -> {
          if (!CollectionEntity.getName().startsWith("_")) {
            arangoDB.db().collection(CollectionEntity.getName()).drop();
          }
        }
    );
  }

  @Override
  protected ArangoNodeRepository getNodeRepository() {
    return nodeRepository;
  }

  @Override
  protected ArangoEdgeRepository getEdgeRepository() {
    return edgeRepository;
  }

}
