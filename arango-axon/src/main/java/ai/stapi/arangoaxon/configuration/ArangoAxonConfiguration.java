package ai.stapi.arangoaxon.configuration;

import ai.stapi.arangoaxon.ArangoCommandMessageStore;
import ai.stapi.arangoaxon.ArangoTokenStore;
import com.arangodb.ArangoDB;
import org.axonframework.serialization.Serializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ArangoAxonConfiguration {
  
  @Bean
  public ArangoTokenStore arangoTokenStore(ArangoDB arangoDB, Serializer serializer) {
    return new ArangoTokenStore(arangoDB, serializer);
  }
  
  @Bean
  public ArangoCommandMessageStore arangoCommandMessageStore(ArangoDB arangoDB) {
    return new ArangoCommandMessageStore(arangoDB);
  }
}
