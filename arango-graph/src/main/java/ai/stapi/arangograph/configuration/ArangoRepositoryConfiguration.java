package ai.stapi.arangograph.configuration;

import ai.stapi.arangograph.ArangoEdgeRepository;
import ai.stapi.arangograph.ArangoNodeRepository;
import ai.stapi.arangograph.repositorypruner.ArangoRepositoryPruner;
import ai.stapi.graph.repositorypruner.RepositoryPruner;
import com.arangodb.ArangoDB;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@AutoConfiguration
public class ArangoRepositoryConfiguration {
  
  @Bean
  public ArangoNodeRepository arangoNodeRepository(
      ArangoDB arangoDb,
      @Lazy ArangoEdgeRepository arangoEdgeRepository
  ) {
    return new ArangoNodeRepository(arangoDb, arangoEdgeRepository);
  }
  
  @Bean
  public ArangoEdgeRepository arangoEdgeRepository(
      ArangoDB arangoDB,
      ArangoNodeRepository arangoNodeRepository
  ) {
    return new ArangoEdgeRepository(arangoDB, arangoNodeRepository);
  }
  
  @Bean
  public RepositoryPruner arangoRepositoryPruner(ArangoDB arangoDB) {
    return new ArangoRepositoryPruner(arangoDB);
  }
}
