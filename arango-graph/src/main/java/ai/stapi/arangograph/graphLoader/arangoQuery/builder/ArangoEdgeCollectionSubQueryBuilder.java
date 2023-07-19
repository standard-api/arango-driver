package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlFor;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import org.jetbrains.annotations.NotNull;

public class ArangoEdgeCollectionSubQueryBuilder extends ArangoEdgeSubQueryBuilder {

  private ArangoEdgeCollectionSubQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String graphElementType,
      String subQueryPostfix,
      boolean isOutgoing
  ) {
    super(searchOptionResolver, structureSchemaFinder, graphElementType, subQueryPostfix,
        isOutgoing);
  }

  public static ArangoEdgeCollectionSubQueryBuilder ingoing(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String graphElementType,
      String subQueryPostfix
  ) {
    return new ArangoEdgeCollectionSubQueryBuilder(
        searchOptionResolver,
        structureSchemaFinder,
        graphElementType,
        subQueryPostfix,
        false
    );
  }

  public static ArangoEdgeCollectionSubQueryBuilder outgoing(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String graphElementType,
      String subQueryPostfix
  ) {
    return new ArangoEdgeCollectionSubQueryBuilder(
        searchOptionResolver,
        structureSchemaFinder,
        graphElementType,
        subQueryPostfix,
        true
    );
  }

  @Override
  @NotNull
  protected ArangoQuery createAqlBody(
      ArangoQuery keptAttributes,
      AqlObject graph,
      GraphLoaderReturnType... returnTypes
  ) {
    var collectionVariable =
        this.createCollectionVariable(this.subQueryPostfix).markAsBindParameter();
    var aqlFor = new AqlFor(
        this.createEdgeDocumentName(this.subQueryPostfix),
        collectionVariable.markAsBindParameter()
    );
    var aqlBody = this.createBaseAqlBody(keptAttributes, graph, aqlFor, returnTypes);
    aqlBody.getBindParameters().put(collectionVariable.getVariableName(), this.graphElementType);
    return aqlBody;
  }

}
