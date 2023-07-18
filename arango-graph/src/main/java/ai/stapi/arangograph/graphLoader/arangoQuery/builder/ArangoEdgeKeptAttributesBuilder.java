package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

public class ArangoEdgeKeptAttributesBuilder extends ArangoKeptAttributesBuilder {

  protected static final String[] DEFAULT_META_ATTRIBUTES = {
      "_id",
      "_rev",
      "_key",
      "_from",
      "_to",
      "_metaData"
  };

  public ArangoEdgeKeptAttributesBuilder(
      StructureSchemaFinder structureSchemaProvider,
      AqlVariable documentName,
      String subQueryPostfix,
      String graphElementType
  ) {
    super(structureSchemaProvider, documentName, subQueryPostfix, graphElementType);
  }

  @Override
  protected String[] provideDefaultMetaAttributes() {
    return ArangoEdgeKeptAttributesBuilder.DEFAULT_META_ATTRIBUTES;
  }

}
