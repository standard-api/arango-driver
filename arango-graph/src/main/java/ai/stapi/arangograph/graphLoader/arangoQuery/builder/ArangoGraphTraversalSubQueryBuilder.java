package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlPush;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlGraphTraversalFor;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLet;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlReturn;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.TraversalDirection;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.EdgeDescriptionParameters;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ArangoGraphTraversalSubQueryBuilder implements ArangoSubQueryBuilder {

  private final ArangoGenericSearchOptionResolver searchOptionResolver;
  private final StructureSchemaFinder structureSchemaFinder;
  private final AqlVariable traversingStart;
  private final AbstractEdgeDescription edgeDescription;
  private final ArangoSearchOptionsBuilder edgeSearchOptionBuilder;
  private final ArangoEdgeKeptAttributesBuilder edgeKeptAttributesBuilder;
  private final ArangoMappedObjectBuilder edgeMappedObjectBuilder;
  private final ArangoSearchOptionsBuilder nodeSearchOptionBuilder;

  private final ArangoQueryByNodeTypeBuilder arangoQueryByNodeTypeBuilder;

  private final String subQueryPostfix;
  private boolean isNodeFieldMapped = false;

  private boolean asConnections = false;

  public ArangoGraphTraversalSubQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      AbstractEdgeDescription edgeDescription,
      String subQueryPostfix,
      AqlVariable variableTraversingStart
  ) {
    this.searchOptionResolver = searchOptionResolver;
    this.structureSchemaFinder = structureSchemaFinder;
    this.subQueryPostfix = subQueryPostfix;
    var edgeType = ArangoGraphTraversalSubQueryBuilder.getEdgeType(edgeDescription);
    this.traversingStart = variableTraversingStart;
    this.edgeDescription = edgeDescription;
    this.edgeSearchOptionBuilder = new ArangoSearchOptionsBuilder(
        this.searchOptionResolver,
        this.createEdgeDocumentName(this.subQueryPostfix),
        ArangoQueryType.GRAPH_TRAVERSAL,
        edgeType,
        this.subQueryPostfix
    );
    this.nodeSearchOptionBuilder = new ArangoSearchOptionsBuilder(
        this.searchOptionResolver,
        this.createNodeDocumentName(this.subQueryPostfix),
        ArangoQueryType.GRAPH_TRAVERSAL,
        null,
        this.subQueryPostfix
    );
    this.edgeKeptAttributesBuilder = new ArangoEdgeKeptAttributesBuilder(
        this.structureSchemaFinder,
        this.createEdgeDocumentName(this.subQueryPostfix),
        this.subQueryPostfix,
        edgeType
    );
    this.edgeMappedObjectBuilder = new ArangoMappedObjectBuilder(
        this.edgeKeptAttributesBuilder,
        this.structureSchemaFinder,
        this.createEdgeDocumentName(this.subQueryPostfix),
        "edge_" + this.subQueryPostfix,
        edgeType
    );
    this.arangoQueryByNodeTypeBuilder = ArangoQueryByNodeTypeBuilder.asGraphTraversal(
        searchOptionResolver,
        this.structureSchemaFinder,
        this.subQueryPostfix,
        this.createNodeDocumentName(this.subQueryPostfix)
    );
  }

  public static ArangoGraphTraversalSubQueryBuilder asConnections(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaProvider,
      AbstractEdgeDescription edgeDescription,
      String subQueryPostfix,
      AqlVariable variableTraversingStart
  ) {
    var arangoGraphTraversalSubQueryBuilder = new ArangoGraphTraversalSubQueryBuilder(
        searchOptionResolver,
        structureSchemaProvider,
        edgeDescription,
        subQueryPostfix,
        variableTraversingStart
    );
    arangoGraphTraversalSubQueryBuilder.asConnections = true;
    return arangoGraphTraversalSubQueryBuilder;
  }

  private static String getEdgeType(AbstractEdgeDescription edgeDescription) {
    var parameters = (EdgeDescriptionParameters) edgeDescription.getParameters();
    return parameters.getEdgeType();
  }

  public ArangoGraphTraversalNodeOptionBuilder setOtherNode(String nodeType) {
    this.nodeSearchOptionBuilder.setGraphElementType(nodeType);
    return this.arangoQueryByNodeTypeBuilder.addGraphTraversalNodeOption(nodeType);
  }

  public ArangoGraphTraversalNodeOptionBuilder mapOtherNode(String nodeType) {
    if (!this.isNodeFieldMapped) {
      this.nodeSearchOptionBuilder.setGraphElementType(nodeType);
      this.edgeMappedObjectBuilder.mapCustomField(
          "node",
          this.createSubQueryVariable(this.createChildSubQueryPostfix(this.subQueryPostfix, 0))
              .getItem(0)
              .getField("data")
      );
      this.isNodeFieldMapped = true;
    }
    return this.arangoQueryByNodeTypeBuilder.addGraphTraversalNodeOption(nodeType);
  }

  public ArangoEdgeKeptAttributesBuilder setEdgeKeptAttributes() {
    return this.edgeKeptAttributesBuilder;
  }

  public ArangoSearchOptionsBuilder setEdgeSearchOptions() {
    return this.edgeSearchOptionBuilder;
  }

  public ArangoMappedObjectBuilder setEdgeMappedScalars() {
    return this.edgeMappedObjectBuilder;
  }

  public ArangoSearchOptionsBuilder setNodeSearchOptions() {
    return this.nodeSearchOptionBuilder;
  }

  @Override
  public ArangoQuery build(GraphLoaderReturnType... returnTypes) {
    var edgeSearchOptions = this.edgeSearchOptionBuilder.build();
    var edgeKeptAttributes = this.edgeKeptAttributesBuilder.build();
    var nodeSearchOptions = this.nodeSearchOptionBuilder.build();
    var edgeMappedObject = this.edgeMappedObjectBuilder.build();
    var queryByNodeType = this.arangoQueryByNodeTypeBuilder.build(returnTypes);
    var finalBindParameters = this.buildBindParameters(
        edgeSearchOptions,
        edgeKeptAttributes,
        nodeSearchOptions,
        edgeMappedObject,
        queryByNodeType
    );
    var aqlRootNode = this.buildAqlRootNode(
        edgeSearchOptions,
        edgeKeptAttributes,
        nodeSearchOptions,
        edgeMappedObject,
        queryByNodeType,
        returnTypes
    );

    return new ArangoQuery(aqlRootNode, finalBindParameters);
  }

  public String getEdgeType() {
    return ArangoGraphTraversalSubQueryBuilder.getEdgeType(this.edgeDescription);
  }

  @NotNull
  private HashMap<String, Object> buildBindParameters(
      ArangoQuery edgeSearchOptions,
      ArangoQuery edgeKeptAttributes,
      ArangoQuery nodeSearchOptions,
      ArangoQuery edgeMappedObject,
      ArangoQuery queryByNodeType
  ) {
    var finalBindParameters = new HashMap<String, Object>();
    finalBindParameters.put(
        this.createCollectionVariable(this.subQueryPostfix).markAsBindParameter().getVariableName(),
        ArangoGraphTraversalSubQueryBuilder.getEdgeType(edgeDescription)
    );
    finalBindParameters.putAll(edgeSearchOptions.getBindParameters());
    finalBindParameters.putAll(edgeKeptAttributes.getBindParameters());
    finalBindParameters.putAll(nodeSearchOptions.getBindParameters());
    finalBindParameters.putAll(queryByNodeType.getBindParameters());
    finalBindParameters.putAll(edgeMappedObject.getBindParameters());
    return finalBindParameters;
  }

  @NotNull
  private AqlRootNode buildAqlRootNode(
      ArangoQuery edgeSearchOptions,
      ArangoQuery edgeKeptAttributes,
      ArangoQuery nodeSearchOptions,
      ArangoQuery edgeMappedObject,
      ArangoQuery queryByNodeType,
      GraphLoaderReturnType... returnTypes
  ) {
    return new AqlRootNode(
        this.buildBeginningStatement(),
        nodeSearchOptions.getAqlNode(),
        edgeSearchOptions.getAqlNode(),
        new AqlLet(
            this.createSubQueryVariable(this.createChildSubQueryPostfix(this.subQueryPostfix, 0)),
            queryByNodeType.getAqlNode()
        ),
        this.buildEndStatement(
            edgeKeptAttributes,
            edgeMappedObject,
            returnTypes
        )
    );
  }

  private AqlNode buildBeginningStatement() {
    return new AqlGraphTraversalFor(
        this.createNodeDocumentName(this.subQueryPostfix),
        this.edgeDescription.isOutgoing() ? TraversalDirection.OUTBOUND
            : TraversalDirection.INBOUND,
        this.traversingStart,
        List.of(this.createCollectionVariable(this.subQueryPostfix).markAsBindParameter()
            .markAsBindParameter()),
        this.createEdgeDocumentName(this.subQueryPostfix),
        null,
        null,
        null
    );
  }

  private AqlNode buildEndStatement(
      ArangoQuery edgeKeptAttributes,
      ArangoQuery edgeMappedObject,
      GraphLoaderReturnType... returnTypes
  ) {
    var childSubQueryPostfix = this.createChildSubQueryPostfix(this.subQueryPostfix, 0);
    var subQueryVariable = this.createSubQueryVariable(childSubQueryPostfix);
    var set = Arrays.stream(returnTypes).collect(Collectors.toSet());
    if (set.contains(GraphLoaderReturnType.SORT_OPTION)) {
      if (this.edgeKeptAttributesBuilder.isExactlyOneAttributeSet()) {
        return new AqlReturn(this.edgeKeptAttributesBuilder.buildOneAttributeFirstValue());
      }
      return new AqlReturn(subQueryVariable.getItem(0));
    }
    if (set.contains(GraphLoaderReturnType.FILTER_OPTION)) {
      if (this.edgeKeptAttributesBuilder.isExactlyOneAttributeSet()) {
        return new AqlReturn(this.edgeKeptAttributesBuilder.buildOneListOrLeafAttribute());
      }
      return new AqlReturn(subQueryVariable.getItem(0));
    }
    var edges = this.createEdges(subQueryVariable, edgeKeptAttributes);
    var nodes = this.createNodes(subQueryVariable);
    var graph = this.createGraphObject(nodes, edges);
    if (this.asConnections) {
      return this.createReturnStatement(
          graph,
          this.createConnectionObject(edgeMappedObject.getAqlNode()),
          returnTypes
      );
    }
    var mappedEdgeObjectVariable = new AqlVariable("mappedEdgeObject" + this.subQueryPostfix);
    return new AqlRootNode(
        new AqlLet(mappedEdgeObjectVariable, edgeMappedObject.getAqlNode()),
        this.createReturnStatement(
            graph,
            mappedEdgeObjectVariable.getField("node"),
            returnTypes
        )
    );
  }

  private AqlNode createEdges(AqlVariable subQueryVariable, ArangoQuery keptAttributes) {
    return new AqlPush(
        this.getChildSubQueryEdges(subQueryVariable),
        keptAttributes.getAqlNode()
    );
  }

  private AqlNode createNodes(AqlVariable subQueryVariable) {
    return this.getChildSubQueryNodes(subQueryVariable);
  }
}
