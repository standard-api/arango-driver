package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlString;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlDocument;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlPush;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLet;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class ArangoEdgeSubQueryBuilder implements ArangoMainQueryBuilder {

  protected final boolean isOutgoing;
  protected final ArangoGenericSearchOptionResolver searchOptionResolver;
  protected final StructureSchemaFinder structureSchemaFinder;
  protected final ArangoQueryByNodeTypeBuilder arangoQueryByNodeTypeBuilder;
  protected final ArangoEdgeKeptAttributesBuilder arangoEdgeKeptAttributesBuilder;
  protected final ArangoMappedObjectBuilder arangoMappedObjectBuilder;
  protected final ArangoSearchOptionsBuilder arangoSearchOptionsBuilder;
  protected final String graphElementType;
  protected final String subQueryPostfix;
  private boolean isNodeFieldMapped = false;

  public ArangoEdgeSubQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String graphElementType,
      String subQueryPostfix,
      boolean isOutgoing
  ) {
    this.searchOptionResolver = searchOptionResolver;
    this.structureSchemaFinder = structureSchemaFinder;
    this.graphElementType = graphElementType;
    this.subQueryPostfix = subQueryPostfix;
    this.isOutgoing = isOutgoing;
    this.arangoQueryByNodeTypeBuilder = new ArangoQueryByNodeTypeBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        this.createChildSubQueryPostfix(this.subQueryPostfix, 0),
        this.buildJoinedNodeGetter()
    );
    var edgeDocumentName = this.createEdgeDocumentName(this.subQueryPostfix);
    this.arangoEdgeKeptAttributesBuilder = new ArangoEdgeKeptAttributesBuilder(
        this.structureSchemaFinder,
        edgeDocumentName,
        this.subQueryPostfix,
        this.graphElementType
    );
    this.arangoMappedObjectBuilder = new ArangoMappedObjectBuilder(
        this.arangoEdgeKeptAttributesBuilder,
        this.structureSchemaFinder,
        edgeDocumentName,
        this.subQueryPostfix,
        this.graphElementType
    );
    this.arangoSearchOptionsBuilder = new ArangoSearchOptionsBuilder(
        this.searchOptionResolver,
        edgeDocumentName,
        this.isOutgoing ? ArangoQueryType.OUTGOING_EDGE : ArangoQueryType.INGOING_EDGE,
        this.graphElementType,
        this.subQueryPostfix
    );
  }

  public ArangoEdgeKeptAttributesBuilder setKeptAttributes() {
    return this.arangoEdgeKeptAttributesBuilder;
  }

  public ArangoMappedObjectBuilder setMappedScalars() {
    return this.arangoMappedObjectBuilder;
  }

  public ArangoSearchOptionsBuilder setSearchOptions() {
    return this.arangoSearchOptionsBuilder;
  }

  public ArangoNodeGetSubQueryBuilder joinNodeGetSubQuery(String nodeType) {
    return this.arangoQueryByNodeTypeBuilder.addGetNodeOption(nodeType);
  }

  public ArangoNodeGetSubQueryBuilder mapNodeGetSubQuery(String nodeType) {
    if (!this.isNodeFieldMapped) {
      this.arangoMappedObjectBuilder.mapCustomField(
          "node",
          this.createSubQueryVariable(this.createChildSubQueryPostfix(this.subQueryPostfix, 0))
              .getItem(0)
              .getField("data")
      );
      this.isNodeFieldMapped = true;
    }
    return this.arangoQueryByNodeTypeBuilder.addGetNodeOption(nodeType);
  }

  @Override
  public ArangoQuery build(GraphLoaderReturnType... returnTypes) {
    var subQueryVariable =
        this.createSubQueryVariable(this.createChildSubQueryPostfix(this.subQueryPostfix, 0));
    var keptAttributes = this.arangoEdgeKeptAttributesBuilder.build();
    var edges = this.createEdges(subQueryVariable, keptAttributes);
    var nodes = this.createNodes(subQueryVariable);
    var graph = this.createGraphObject(nodes, edges);

    return this.createAqlBody(keptAttributes, graph, returnTypes);
  }

  @Override
  public ArangoQuery buildAsMain(GraphLoaderReturnType... returnTypes) {
    var subQueryVariable =
        this.createSubQueryVariable(this.createChildSubQueryPostfix(this.subQueryPostfix, 0));
    var keptAttributes = this.arangoEdgeKeptAttributesBuilder.build();
    var main = keptAttributes.getAqlNode();
    var nodes = this.createNodes(subQueryVariable);
    var edges = this.getChildSubQueryEdges(subQueryVariable);
    var graph = this.createMainGraphObject(main, nodes, edges);

    return this.createAqlBody(keptAttributes, graph, returnTypes);
  }

  @NotNull
  protected abstract ArangoQuery createAqlBody(
      ArangoQuery keptAttributes,
      AqlObject graph,
      GraphLoaderReturnType... returnTypes
  );

  @NotNull
  protected ArangoQuery createBaseAqlBody(
      ArangoQuery keptAttributes,
      AqlObject graph,
      AqlNode getDocumentLine,
      GraphLoaderReturnType... returnTypes
  ) {
    var mappedObject = this.arangoMappedObjectBuilder.build();
    var dataObject = new AqlObject(Map.of(
        new AqlString("edges"), mappedObject.getAqlNode()
    ));
    var childQuery = this.arangoQueryByNodeTypeBuilder.build(returnTypes);
    var searchOptions = this.arangoSearchOptionsBuilder.build();

    var letJoinedNode = new AqlLet(
        this.createSubQueryVariable(this.createChildSubQueryPostfix(this.subQueryPostfix, 0)),
        childQuery.getAqlNode()
    );

    var forQuery = new AqlRootNode(
        getDocumentLine,
        searchOptions.getAqlNode(),
        letJoinedNode,
        this.createOtherNodeLine(),
        this.createReturnStatement(
            graph,
            dataObject,
            returnTypes
        )
    );

    var bindParameters = this.mergeBindParameters(
        keptAttributes.getBindParameters(),
        mappedObject.getBindParameters(),
        childQuery.getBindParameters(),
        searchOptions.getBindParameters()
    );

    return new ArangoQuery(
        forQuery,
        bindParameters
    );
  }

  private AqlNode createEdges(AqlVariable subQueryVariable, ArangoQuery keptAttributes) {
    return new AqlPush(
        this.getChildSubQueryEdges(subQueryVariable),
        keptAttributes.getAqlNode()
    );
  }

  private AqlNode createNodes(AqlVariable subQueryVariable) {
    return new AqlPush(
        this.getChildSubQueryNodes(subQueryVariable),
        new ArangoNodeKeptAttributesBuilder(
            this.structureSchemaFinder,
            this.createOtherNodeVariableName(),
            "",
            ""
        ).build().getAqlNode()
    );
  }

  protected AqlVariable buildJoinedNodeGetter() {
    return this.createEdgeDocumentName(this.subQueryPostfix)
        .getField(this.isOutgoing ? "_to" : "_from");
  }

  private AqlLet createOtherNodeLine() {
    return new AqlLet(
        this.createOtherNodeVariableName(),
        new AqlDocument(
            this.createEdgeDocumentName(this.subQueryPostfix)
                .getField(this.isOutgoing ? "_from" : "_to")
        )
    );
  }

  @NotNull
  private AqlVariable createOtherNodeVariableName() {
    var childSubQueryPrefix = this.createChildSubQueryPostfix(this.subQueryPostfix, 0);
    return new AqlVariable("otherNode_" + childSubQueryPrefix);
  }
}
