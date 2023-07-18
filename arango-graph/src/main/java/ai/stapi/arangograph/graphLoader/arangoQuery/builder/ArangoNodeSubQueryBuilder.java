package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlList;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlUnion;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLet;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlReturn;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.EdgeDescriptionParameters;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class ArangoNodeSubQueryBuilder
    implements ArangoMainQueryBuilder, ArangoGraphTraversalJoinable {

  protected final ArangoGenericSearchOptionResolver searchOptionResolver;
  protected final StructureSchemaFinder structureSchemaFinder;
  protected final ArangoNodeKeptAttributesBuilder arangoNodeKeptAttributesBuilder;
  protected final ArangoMappedObjectBuilder arangoMappedObjectBuilder;
  protected final ArangoSearchOptionsBuilder arangoSearchOptionsBuilder;
  protected final String graphElementType;
  protected final String subQueryPostfix;
  protected final Map<String, ArangoGraphTraversalSubQueryBuilder> subQueryBuilders;

  protected final Map<String, Object> bindParameters;

  public ArangoNodeSubQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String graphElementType,
      String subQueryPostfix
  ) {
    this.searchOptionResolver = searchOptionResolver;
    this.structureSchemaFinder = structureSchemaFinder;
    this.graphElementType = graphElementType;
    this.subQueryPostfix = subQueryPostfix;
    var nodeDocumentName = this.createNodeDocumentName(this.subQueryPostfix);
    this.arangoNodeKeptAttributesBuilder = new ArangoNodeKeptAttributesBuilder(
        this.structureSchemaFinder,
        nodeDocumentName,
        this.subQueryPostfix,
        this.graphElementType
    );
    this.arangoMappedObjectBuilder = new ArangoMappedObjectBuilder(
        this.arangoNodeKeptAttributesBuilder,
        this.structureSchemaFinder,
        nodeDocumentName,
        this.subQueryPostfix,
        this.graphElementType
    );
    this.arangoSearchOptionsBuilder = new ArangoSearchOptionsBuilder(
        this.searchOptionResolver,
        nodeDocumentName,
        ArangoQueryType.NODE,
        this.graphElementType,
        this.subQueryPostfix
    );
    this.subQueryBuilders = new LinkedHashMap<>();
    this.bindParameters = new HashMap<>();
  }

  public ArangoNodeKeptAttributesBuilder setKeptAttributes() {
    return this.arangoNodeKeptAttributesBuilder;
  }

  public ArangoMappedObjectBuilder setMappedScalars() {
    return this.arangoMappedObjectBuilder;
  }

  public ArangoSearchOptionsBuilder setSearchOptions() {
    return this.arangoSearchOptionsBuilder;
  }

  public ArangoGraphTraversalSubQueryBuilder joinGraphTraversal(
      AbstractEdgeDescription edgeDescription) {
    var childSubQueryPrefix = this.getChildSubQueryPostfix();
    var subQueryBuilder = new ArangoGraphTraversalSubQueryBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        edgeDescription,
        childSubQueryPrefix,
        this.createNodeDocumentName(this.subQueryPostfix)
    );
    this.subQueryBuilders.put(
        this.createSubQueryVariable(childSubQueryPrefix).getVariableName(),
        subQueryBuilder
    );
    return subQueryBuilder;
  }

  public ArangoGraphTraversalSubQueryBuilder mapGraphTraversal(
      String fieldName,
      AbstractEdgeDescription edgeDescription
  ) {
    var childSubQueryPrefix = this.getChildSubQueryPostfix();
    var subQueryVariable = this.createSubQueryVariable(childSubQueryPrefix);
    this.mapGraphTraversalJoin(edgeDescription, fieldName, subQueryVariable);
    var subQueryBuilder = new ArangoGraphTraversalSubQueryBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        edgeDescription,
        childSubQueryPrefix,
        this.createNodeDocumentName(this.subQueryPostfix)
    );
    this.subQueryBuilders.put(
        subQueryVariable.getVariableName(),
        subQueryBuilder
    );
    return subQueryBuilder;
  }

  public ArangoGraphTraversalSubQueryBuilder mapGraphTraversalAsConnections(
      String fieldName,
      AbstractEdgeDescription edgeDescription
  ) {
    var connectionFieldName = fieldName + "Connections";
    var childSubQueryPrefix = this.getChildSubQueryPostfix();
    var subQueryVariable = this.createSubQueryVariable(childSubQueryPrefix);
    this.mapGraphTraversalJoin(edgeDescription, connectionFieldName, subQueryVariable);
    var subQueryBuilder = ArangoGraphTraversalSubQueryBuilder.asConnections(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        edgeDescription,
        childSubQueryPrefix,
        this.createNodeDocumentName(this.subQueryPostfix)
    );
    this.subQueryBuilders.put(
        subQueryVariable.getVariableName(),
        subQueryBuilder
    );
    return subQueryBuilder;
  }

  @Override
  public ArangoQuery build(GraphLoaderReturnType... returnTypes) {
    var keptAttributes = this.arangoNodeKeptAttributesBuilder.build();
    var nodes = this.createNodes(keptAttributes);
    var edges = this.createEdges();
    var graph = this.createGraphObject(nodes, edges);

    return this.createAqlBody(keptAttributes, graph, returnTypes);
  }

  @Override
  public ArangoQuery buildAsMain(GraphLoaderReturnType... returnTypes) {
    var keptAttributes = this.arangoNodeKeptAttributesBuilder.build();
    var main = keptAttributes.getAqlNode();
    var nodes = this.createNodes();
    var edges = this.createEdges();
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
    var searchOptions = this.arangoSearchOptionsBuilder.build();
    var subQueryStatements = new ArrayList<AqlLet>();
    var subQueryBindParams = new ArrayList<Map<String, Object>>();
    this.subQueryBuilders.forEach((key, value) -> {
      var arangoQuery = value.build(returnTypes);
      subQueryStatements.add(
          new AqlLet(
              new AqlVariable(key),
              arangoQuery.getAqlNode()
          )
      );
      subQueryBindParams.add(arangoQuery.getBindParameters());
    });

    var aqlNodes = new ArrayList<AqlNode>();
    aqlNodes.add(getDocumentLine);
    aqlNodes.add(searchOptions.getAqlNode());
    aqlNodes.addAll(subQueryStatements);
    var set = Arrays.stream(returnTypes).collect(Collectors.toSet());
    if (set.contains(GraphLoaderReturnType.SORT_OPTION)) {
      if (this.arangoNodeKeptAttributesBuilder.isExactlyOneAttributeSet()) {
        aqlNodes.add(
            new AqlReturn(this.arangoNodeKeptAttributesBuilder.buildOneAttributeFirstValue()));
      } else {
        aqlNodes.add(
            new AqlReturn(new AqlVariable(this.subQueryBuilders.keySet().iterator().next())));
      }
    } else if (set.contains(GraphLoaderReturnType.FILTER_OPTION)) {
      if (this.arangoNodeKeptAttributesBuilder.isExactlyOneAttributeSet()) {
        aqlNodes.add(
            new AqlReturn(this.arangoNodeKeptAttributesBuilder.buildOneListOrLeafAttribute()));
      } else {
        var graphTraversalBuilderEntry = this.subQueryBuilders.entrySet().iterator().next();
        var isList = this.structureSchemaFinder.isList(
            this.graphElementType,
            graphTraversalBuilderEntry.getValue().getEdgeType()
        );
        var subQueryVariable = new AqlVariable(graphTraversalBuilderEntry.getKey());
        if (!isList) {
          aqlNodes.add(new AqlReturn(subQueryVariable).getItem(0));
        } else {
          aqlNodes.add(new AqlReturn(subQueryVariable));
        }

      }
    } else {
      aqlNodes.add(
          this.createReturnStatement(
              graph,
              mappedObject.getAqlNode(),
              returnTypes
          )
      );
    }
    var forQuery = new AqlRootNode(aqlNodes);

    var bindParameters = this.mergeBindParameters(
        keptAttributes.getBindParameters(),
        mappedObject.getBindParameters(),
        searchOptions.getBindParameters()
    );
    subQueryBindParams.forEach(bindParameters::putAll);

    return new ArangoQuery(
        forQuery,
        bindParameters
    );
  }

  private void mapGraphTraversalJoin(
      AbstractEdgeDescription edgeDescription,
      String connectionFieldName,
      AqlVariable subQueryVariable
  ) {
    var edgeType = ((EdgeDescriptionParameters) edgeDescription.getParameters()).getEdgeType();
    var fieldDefinition = this.structureSchemaFinder.getFieldDefinitionOrFallback(
        this.graphElementType,
        edgeType
    );
    if (!fieldDefinition.isList()) {
      this.arangoMappedObjectBuilder.mapCustomField(
          connectionFieldName,
          subQueryVariable.getItem(0).getField("data")
      );
    } else {
      this.arangoMappedObjectBuilder.mapCustomField(
          connectionFieldName,
          subQueryVariable.getAllItems().getField("data")
      );
    }
  }

  private AqlNode createEdges() {
    var unionList = new ArrayList<AqlNode>();
    this.subQueryBuilders.keySet().forEach(key ->
        unionList.add(this.getChildSubQueryEdges(new AqlVariable(key)))
    );
    if (this.subQueryBuilders.size() > 1) {
      return new AqlUnion(unionList);
    }
    if (this.subQueryBuilders.size() == 1) {
      return unionList.get(0);
    }
    return new AqlList();
  }

  private AqlNode createNodes(ArangoQuery keptAttributes) {
    var mainElementList = new AqlList(keptAttributes.getAqlNode());
    if (this.subQueryBuilders.size() > 0) {
      var unionList = new ArrayList<AqlNode>();
      this.subQueryBuilders.keySet().forEach(key ->
          unionList.add(this.getChildSubQueryNodes(new AqlVariable(key)))
      );
      var finalList = new ArrayList<AqlNode>(List.of(mainElementList));
      finalList.addAll(unionList);
      return new AqlUnion(finalList);
    }
    return mainElementList;
  }

  private AqlNode createNodes() {
    var unionList = new ArrayList<AqlNode>();
    this.subQueryBuilders.keySet().forEach(key ->
        unionList.add(this.getChildSubQueryNodes(new AqlVariable(key)))
    );
    if (this.subQueryBuilders.size() > 1) {
      return new AqlUnion(unionList);
    }
    if (this.subQueryBuilders.size() == 1) {
      return unionList.get(0);
    }
    return new AqlList();
  }

  private String getChildSubQueryPostfix() {
    return this.createChildSubQueryPostfix(this.subQueryPostfix, this.subQueryBuilders.size());
  }
}
