package ai.stapi.arangograph.configuration;

import ai.stapi.arangograph.ArangoEdgeRepository;
import ai.stapi.arangograph.ArangoNodeRepository;
import ai.stapi.arangograph.graphLoader.ArangoGraphLoader;
import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.ArangoEdgeCollectionSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.ArangoEdgeGetSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.ArangoGraphTraversalSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.ArangoNodeCollectionSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.ArangoNodeGetSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.ArangoSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoQueryBuilderProvider;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoAllMatchFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoAndFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoAnyMatchFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoAscendingSortOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoContainsFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoDescendingSortOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoEndsWithFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoEqualsFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGreaterThanFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGreaterThanOrEqualFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoLowerThanFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoLowerThanOrEqualFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoNoneMatchFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoNotEqualsFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoNotFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoOffsetPaginationOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoOrFilterOptionResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoStartsWithFilterOptionResolver;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.GenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.SearchOptionResolver;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.arangodb.ArangoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@AutoConfiguration
public class ArangoGraphLoaderConfiguration {
  
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
  @ConditionalOnBean(ArangoGraphLoader.class)
  public ArangoQueryBuilderProvider arangoQueryBuilderProvider(
      ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder
  ) {
    return new ArangoQueryBuilderProvider(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder
    );
  }
  
  @Bean
  @ConditionalOnBean(ArangoGraphLoader.class)
  public GenericSubQueryResolver genericSubQueryResolver(
      List<ArangoSubQueryResolver> subQueryResolvers
  ) {
    return new GenericSubQueryResolver(subQueryResolvers);
  }
  
  @Bean
  @ConditionalOnBean(ArangoGraphLoader.class)
  public ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver(
      List<SearchOptionResolver<ArangoQuery>> searchOptionResolvers
  ) {
    return new ArangoGenericSearchOptionResolver(searchOptionResolvers);
  }
  
  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoAllMatchFilterOptionResolver arangoAllMatchFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      @Lazy GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    return new ArangoAllMatchFilterOptionResolver(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoAnyMatchFilterOptionResolver arangoAnyMatchFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      @Lazy GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    return new ArangoAnyMatchFilterOptionResolver(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoNoneMatchFilterOptionResolver arangoNoneMatchFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      @Lazy GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    return new ArangoNoneMatchFilterOptionResolver(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoAndFilterOptionResolver arangoAndFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      @Lazy GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    return new ArangoAndFilterOptionResolver(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoOrFilterOptionResolver arangoOrFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      @Lazy GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    return new ArangoOrFilterOptionResolver(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoNotFilterOptionResolver arangoNotFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      @Lazy GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    return new ArangoNotFilterOptionResolver(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoAscendingSortOptionResolver arangoAscendingSortOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoAscendingSortOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder, 
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoContainsFilterOptionResolver arangoContainsFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoContainsFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder, 
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoDescendingSortOptionResolver arangoDescendingSortOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoDescendingSortOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoEndsWithFilterOptionResolver arangoEndsWithFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoEndsWithFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoEqualsFilterOptionResolver arangoEqualsFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoEqualsFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoGreaterThanFilterOptionResolver arangoGreaterThanFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoGreaterThanFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoGreaterThanOrEqualFilterOptionResolver arangoGreaterThanOrEqualFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoGreaterThanOrEqualFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoLowerThanFilterOptionResolver arangoLowerThanFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoLowerThanFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoLowerThanOrEqualFilterOptionResolver arangoLowerThanOrEqualFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoLowerThanOrEqualFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoStartsWithFilterOptionResolver arangoStartsWithFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoStartsWithFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoNotEqualsFilterOptionResolver arangoNotEqualsFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    return new ArangoNotEqualsFilterOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Bean
  @ConditionalOnBean(ArangoGenericSearchOptionResolver.class)
  public ArangoOffsetPaginationOptionResolver arangoOffsetPaginationOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOgmFactory
  ) {
    return new ArangoOffsetPaginationOptionResolver(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOgmFactory
    );
  }
  
  @Bean
  @ConditionalOnBean(GenericSubQueryResolver.class)
  public ArangoEdgeCollectionSubQueryResolver arangoEdgeCollectionSubQueryResolver(
      @Lazy GenericSubQueryResolver genericSubQueryResolver
  ) {
    return new ArangoEdgeCollectionSubQueryResolver(genericSubQueryResolver);
  }

  @Bean
  @ConditionalOnBean(GenericSubQueryResolver.class)
  public ArangoNodeCollectionSubQueryResolver arangoNodeCollectionSubQueryResolver(
      @Lazy GenericSubQueryResolver genericSubQueryResolver
  ) {
    return new ArangoNodeCollectionSubQueryResolver(genericSubQueryResolver);
  }

  @Bean
  @ConditionalOnBean(GenericSubQueryResolver.class)
  public ArangoEdgeGetSubQueryResolver arangoEdgeGetSubQueryResolver(
      @Lazy GenericSubQueryResolver genericSubQueryResolver
  ) {
    return new ArangoEdgeGetSubQueryResolver(genericSubQueryResolver);
  }

  @Bean
  @ConditionalOnBean(GenericSubQueryResolver.class)
  public ArangoGraphTraversalSubQueryResolver arangoGraphTraversalSubQueryResolver(
      @Lazy GenericSubQueryResolver genericSubQueryResolver
  ) {
    return new ArangoGraphTraversalSubQueryResolver(genericSubQueryResolver);
  }

  @Bean
  @ConditionalOnBean(GenericSubQueryResolver.class)
  public ArangoNodeGetSubQueryResolver arangoNodeGetSubQueryResolver(
      @Lazy GenericSubQueryResolver genericSubQueryResolver
  ) {
    return new ArangoNodeGetSubQueryResolver(genericSubQueryResolver);
  }
}
