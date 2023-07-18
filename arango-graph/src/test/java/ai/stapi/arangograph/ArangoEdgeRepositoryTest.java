package ai.stapi.arangograph;

import ai.stapi.arangograph.configuration.ArangoGraphRepositoryConfiguration;
import ai.stapi.graph.EdgeRepository;
import ai.stapi.graph.NodeRepository;
import com.arangodb.ArangoDB;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(ArangoGraphRepositoryConfiguration.class)
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
