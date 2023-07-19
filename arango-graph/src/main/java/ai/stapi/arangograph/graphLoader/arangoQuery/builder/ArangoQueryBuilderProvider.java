package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

public class ArangoQueryBuilderProvider {

  private final ArangoGenericSearchOptionResolver searchOptionResolver;
  private final StructureSchemaFinder structureSchemaFinder;

  public ArangoQueryBuilderProvider(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder
  ) {
    this.searchOptionResolver = searchOptionResolver;
    this.structureSchemaFinder = structureSchemaFinder;
  }

  public ArangoQueryBuilder provide() {
    return new ArangoQueryBuilder(this.searchOptionResolver, this.structureSchemaFinder);
  }
}
