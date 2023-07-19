package ai.stapi.arangograph;

import ai.stapi.graph.EdgeRepository;
import ai.stapi.graph.NodeRepository;
import com.arangodb.ArangoDB;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public class ArangoEdgeRepositoryTest extends AbstractEdgeRepositoryTest {

  @Autowired
  private ArangoEdgeRepository arangoEdgeRepository;
  
  @Autowired
  private ArangoNodeRepository arangoNodeRepository;
  
  @Autowired
  private ArangoDB arangoDB;

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
  protected EdgeRepository getEdgeRepository() {
    return this.arangoEdgeRepository;
  }

  @Override
  protected NodeRepository getNodeRepository() {
    return this.arangoNodeRepository;
  }
}
