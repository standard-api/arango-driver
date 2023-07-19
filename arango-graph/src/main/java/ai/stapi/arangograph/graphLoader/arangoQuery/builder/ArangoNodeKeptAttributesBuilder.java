package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

public class ArangoNodeKeptAttributesBuilder extends ArangoKeptAttributesBuilder {

  protected static final String[] DEFAULT_META_ATTRIBUTES = {"_id", "_rev", "_key", "_metaData"};

  public ArangoNodeKeptAttributesBuilder(
      StructureSchemaFinder structureSchemaFinder,
      AqlVariable documentName,
      String subQueryPostfix,
      String graphElementType
  ) {
    super(structureSchemaFinder, documentName, subQueryPostfix, graphElementType);
  }

  @Override
  protected String[] provideDefaultMetaAttributes() {
    return ArangoNodeKeptAttributesBuilder.DEFAULT_META_ATTRIBUTES;
  }

}
