package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlString;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlConcatSeparator;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlDocument;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLet;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import org.jetbrains.annotations.NotNull;

public class ArangoEdgeGetSubQueryBuilder extends ArangoEdgeSubQueryBuilder {

  private final UniqueIdentifier graphElementId;

  public ArangoEdgeGetSubQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String graphElementType,
      String subQueryPostfix,
      boolean isOutgoing,
      UniqueIdentifier graphElementId
  ) {
    super(searchOptionResolver, structureSchemaFinder, graphElementType, subQueryPostfix,
        isOutgoing);
    this.graphElementId = graphElementId;
  }

  public static ArangoEdgeGetSubQueryBuilder ingoing(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      UniqueIdentifier graphElementUuid,
      String graphElementType,
      String subQueryPostfix
  ) {
    return new ArangoEdgeGetSubQueryBuilder(
        searchOptionResolver,
        structureSchemaFinder,
        graphElementType,
        subQueryPostfix,
        false,
        graphElementUuid
    );
  }

  public static ArangoEdgeGetSubQueryBuilder outgoing(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      UniqueIdentifier graphElementUuid,
      String graphElementType,
      String subQueryPostfix
  ) {
    return new ArangoEdgeGetSubQueryBuilder(
        searchOptionResolver,
        structureSchemaFinder,
        graphElementType,
        subQueryPostfix,
        true,
        graphElementUuid
    );
  }

  @NotNull
  @Override
  protected ArangoQuery createAqlBody(
      ArangoQuery keptAttributes,
      AqlObject graph,
      GraphLoaderReturnType... returnTypes
  ) {
    var collectionVariable = this.createCollectionVariable(this.subQueryPostfix);
    var idVariable = this.createIdVariable(this.subQueryPostfix);
    var aqlFor = new AqlLet(
        this.createEdgeDocumentName(this.subQueryPostfix),
        new AqlDocument(
            new AqlConcatSeparator(
                new AqlString("/"),
                collectionVariable.markAsBindParameter(),
                idVariable.markAsBindParameter()
            )
        )
    );
    var baseAqlBody = this.createBaseAqlBody(keptAttributes, graph, aqlFor, returnTypes);
    baseAqlBody.getBindParameters()
        .put(collectionVariable.getVariableName(), this.graphElementType);
    baseAqlBody.getBindParameters().put(idVariable.getVariableName(), this.graphElementId.getId());
    return baseAqlBody;
  }
}
