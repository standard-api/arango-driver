package ai.stapi.arangograph.configuration;

import ai.stapi.arangograph.ArangoEdgeRepository;
import ai.stapi.arangograph.ArangoNodeRepository;
import ai.stapi.arangograph.graphLoader.ArangoGraphLoader;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoQueryBuilderProvider;
import ai.stapi.arangograph.repositorypruner.ArangoRepositoryPruner;
import ai.stapi.graph.EdgeRepository;
import ai.stapi.graph.NodeRepository;
import ai.stapi.graph.repositorypruner.RepositoryPruner;
import ai.stapi.graphoperations.graphLoader.GraphLoader;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import com.arangodb.ArangoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ArangoGraphRepositoryConfiguration {

    @Bean
    public ArangoNodeRepository arangoNodeRepository(
        ArangoDB arangoDB,
        @Lazy ArangoEdgeRepository edgeRepository
    ) {
        return new ArangoNodeRepository(arangoDB, edgeRepository);
    }

    @Bean
    public ArangoEdgeRepository arangoEdgeRepository(
        ArangoDB arangoDB,
        @Lazy ArangoNodeRepository arangoNodeRepository
    ) {
        return new ArangoEdgeRepository(arangoDB, arangoNodeRepository);
    }
    
    @Bean
    @Primary
    public NodeRepository interfaceNodeRepository(
        ArangoNodeRepository arangoNodeRepository
    ) {
        return arangoNodeRepository;
    }
    
    @Bean
    @Primary
    public EdgeRepository interfaceEdgeRepository(
        ArangoEdgeRepository arangoEdgeRepository
    ) {
        return arangoEdgeRepository;
    }

    @Bean
    public ArangoGraphLoader arangoGraphLoader(
        ArangoDB arangoDb,
        ArangoEdgeRepository arangoEdgeRepository,
        ArangoNodeRepository arangoNodeRepository,
        ArangoQueryBuilderProvider arangoQueryBuilderProvider,
        GenericSubQueryResolver genericSubQueryResolver,
        ObjectMapper objectMapper,
        GraphLoaderOgmFactory graphLoaderOgmFactory
    ) {
        return new ArangoGraphLoader(
            arangoDb,
            arangoEdgeRepository,
            arangoNodeRepository,
            arangoQueryBuilderProvider,
            genericSubQueryResolver,
            objectMapper,
            graphLoaderOgmFactory
        );
    }
    
    @Bean
    @Primary
    public GraphLoader interfaceGraphLoader(
        ArangoGraphLoader arangoGraphLoader
    ) {
        return arangoGraphLoader;
    }

    @Bean
    @Primary
    public RepositoryPruner interfaceRepositoryPruner(
        ArangoDB arangoDB
    ) {
        return new ArangoRepositoryPruner(arangoDB);
    }
}
