package ai.stapi.arangograph.configuration;

import ai.stapi.graphoperations.serializableGraph.jackson.SerializableGraphConfigurer;
import ai.stapi.serialization.jackson.JavaTimeConfigurer;
import ai.stapi.serialization.jackson.SerializableObjectConfigurer;
import com.arangodb.ArangoDB;
import com.arangodb.ContentType;
import com.arangodb.serde.jackson.JacksonSerde;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties({ArangoConfigurationProperties.class})
public class ArangoDBConfiguration {

  private final ArangoConfigurationProperties arangoConfigurationProperties;

  public ArangoDBConfiguration(ArangoConfigurationProperties arangoConfigurationProperties) {
    this.arangoConfigurationProperties = arangoConfigurationProperties;
  }

  @Bean
  public ArangoDB arangoDB(JacksonSerde serde) {
    var port = this.arangoConfigurationProperties.getPort();
    return new ArangoDB.Builder()
        .serde(serde)
        .host(
            this.arangoConfigurationProperties.getHost(),
            Integer.parseInt(port)
        )
        .user(this.arangoConfigurationProperties.getUser())
        .password(this.arangoConfigurationProperties.getPassword())
        .build();
  }

  @Bean
  public JacksonSerde jacksonSerde(
      @Autowired SerializableObjectConfigurer serializableObjectConfigurer,
      @Autowired SerializableGraphConfigurer serializableGraphConfigurer
  ) {
    var serde = JacksonSerde.of(ContentType.JSON);
    serde.configure(
        mapper -> {
          serializableObjectConfigurer.configure(mapper);
          serializableGraphConfigurer.configure(mapper);
          JavaTimeConfigurer.configureJavaTimeModule(mapper);
        }
    );
    return serde;
  }
}
