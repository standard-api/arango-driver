package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlString;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlConcatSeparator;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlDocument;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLet;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArangoNodeGetSubQueryBuilder extends ArangoNodeSubQueryBuilder {

  @Nullable
  protected AqlVariable nodeDocumentKeyVariable;
  @Nullable
  private UniqueIdentifier graphElementId;

  public ArangoNodeGetSubQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String graphElementType,
      String subQueryPostfix,
      UniqueIdentifier graphElementId
  ) {
    super(searchOptionResolver, structureSchemaFinder, graphElementType, subQueryPostfix);
    this.graphElementId = graphElementId;
  }

  public ArangoNodeGetSubQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder graphElementSchemaProvider,
      String graphElementType,
      String subQueryPostfix,
      AqlVariable nodeDocumentKeyVariable
  ) {
    super(searchOptionResolver, graphElementSchemaProvider, graphElementType, subQueryPostfix);
    this.nodeDocumentKeyVariable = nodeDocumentKeyVariable;
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
    var documentKey = new AqlConcatSeparator(
        new AqlString("/"),
        collectionVariable.markAsBindParameter(),
        idVariable.markAsBindParameter()
    );
    var aqlFor = new AqlLet(
        this.createNodeDocumentName(this.subQueryPostfix),
        new AqlDocument(
            this.nodeDocumentKeyVariable != null ? this.nodeDocumentKeyVariable : documentKey)
    );
    var baseAqlBody = this.createBaseAqlBody(keptAttributes, graph, aqlFor, returnTypes);
    if (this.graphElementId != null) {
      baseAqlBody.getBindParameters()
          .put(collectionVariable.getVariableName(), this.graphElementType);
      baseAqlBody.getBindParameters()
          .put(idVariable.getVariableName(), this.graphElementId.getId());
    }
    return baseAqlBody;
  }
}
