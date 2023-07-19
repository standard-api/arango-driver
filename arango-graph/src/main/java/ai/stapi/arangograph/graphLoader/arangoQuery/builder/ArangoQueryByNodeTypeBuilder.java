package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ArangoQueryByNodeTypeBuilder implements ArangoSubQueryBuilder {

  private final ArangoGenericSearchOptionResolver searchOptionResolver;
  private final StructureSchemaFinder structureSchemaFinder;

  private final String subQueryPostfix;
  private final AqlVariable nodeDocumentVariableName;

  private final Map<String, Object> nodeOptionsPlaceholders;
  private final Map<String, ArangoSubQueryBuilder> subQueryBuilders;

  private final Map<String, Object> bindParameters;

  private boolean isUsedAsGraphTraversal = false;

  public ArangoQueryByNodeTypeBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String subQueryPostfix,
      AqlVariable nodeDocumentVariableName
  ) {
    this.searchOptionResolver = searchOptionResolver;
    this.structureSchemaFinder = structureSchemaFinder;
    this.subQueryPostfix = subQueryPostfix;
    this.nodeDocumentVariableName = nodeDocumentVariableName;
    this.nodeOptionsPlaceholders = new HashMap<>();
    this.subQueryBuilders = new HashMap<>();
    this.bindParameters = new HashMap<>();
  }

  public static ArangoQueryByNodeTypeBuilder asGraphTraversal(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      String subQueryPostfix,
      AqlVariable nodeDocumentVariableName
  ) {
    var builder = new ArangoQueryByNodeTypeBuilder(
        searchOptionResolver,
        structureSchemaFinder,
        subQueryPostfix,
        nodeDocumentVariableName
    );
    builder.isUsedAsGraphTraversal = true;
    return builder;
  }

  public ArangoNodeGetSubQueryBuilder addGetNodeOption(
      String nodeType
  ) {
    var nodeTypePlaceholder = this.buildJoinedNodeTypePlaceholder();
    var nodeGetSubQueryBuilder = new ArangoNodeGetSubQueryBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        nodeType,
        this.getChildSubQueryPostfix(),
        this.nodeDocumentVariableName
    );
    var subQueryVariableName = this.buildJoinedNodeSubQueryVariableName();
    this.subQueryBuilders.put(
        subQueryVariableName,
        nodeGetSubQueryBuilder
    );
    this.nodeOptionsPlaceholders.put(
        nodeTypePlaceholder,
        subQueryVariableName
    );
    this.bindParameters.put(
        nodeTypePlaceholder,
        nodeType
    );

    return nodeGetSubQueryBuilder;
  }

  public ArangoGraphTraversalNodeOptionBuilder addGraphTraversalNodeOption(
      String nodeType
  ) {
    var nodeTypePlaceholder = this.buildJoinedNodeTypePlaceholder();
    var nodeOptionBuilder = new ArangoGraphTraversalNodeOptionBuilder(
        this.structureSchemaFinder,
        this.searchOptionResolver,
        nodeType,
        this.subQueryPostfix,
        this.nodeDocumentVariableName
    );
    var subQueryVariableName = this.buildJoinedNodeSubQueryVariableName();
    this.subQueryBuilders.put(
        subQueryVariableName,
        nodeOptionBuilder
    );
    this.nodeOptionsPlaceholders.put(
        nodeTypePlaceholder,
        subQueryVariableName
    );
    this.bindParameters.put(
        nodeTypePlaceholder,
        nodeType
    );
    return nodeOptionBuilder;
  }

  public ArangoQuery build(GraphLoaderReturnType... returnTypes) {
    var buildSubQueries = this.buildSubQueries(returnTypes);
    var finalBindParameters = new HashMap<>(this.bindParameters);
    buildSubQueries.values()
        .forEach(query -> finalBindParameters.putAll(query.getBindParameters()));
    return new ArangoQuery(
        new AqlVariable(this.buildString(buildSubQueries, returnTypes)),
        finalBindParameters
    );
  }

  private String buildString(Map<String, ArangoQuery> subQueries,
      GraphLoaderReturnType... returnTypes) {
    var subQueriesStrings = this.nodeOptionsPlaceholders.entrySet().stream()
        .map(entry -> {
          String actualNodeTypeStatement;
          if (this.isUsedAsGraphTraversal) {
            actualNodeTypeStatement = String.format(
                "PARSE_IDENTIFIER(%s._id).collection",
                this.nodeDocumentVariableName.getVariableName()
            );
          } else {
            actualNodeTypeStatement = String.format(
                "PARSE_IDENTIFIER(%s).collection",
                this.nodeDocumentVariableName.getVariableName()
            );
          }
          var subQueryString = subQueries.get(entry.getValue()).getQueryString();
          return String.format("%s == @%s ? ( %s )", actualNodeTypeStatement, entry.getKey(),
              subQueryString);
        }).collect(Collectors.toList());

    subQueriesStrings.add(this.getDefaultValueIfNodeTypeNotPresent(returnTypes));

    return String.join(" : ", subQueriesStrings);
  }

  private Map<String, ArangoQuery> buildSubQueries(GraphLoaderReturnType... returnTypes) {
    return this.subQueryBuilders.entrySet()
        .stream()
        .map(entry -> new AbstractMap.SimpleEntry<>(
            entry.getKey(),
            entry.getValue().build(returnTypes)
        )).collect(
            Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)
        );
  }

  private String buildJoinedNodeTypePlaceholder() {
    var basePlaceholder = "joinedNodeCollection_" + this.nodeOptionsPlaceholders.size();
    return this.subQueryPostfix.isBlank() ? basePlaceholder
        : basePlaceholder + "__" + this.subQueryPostfix;
  }

  private String buildJoinedNodeSubQueryVariableName() {
    return BASE_SUB_QUERY_VARIABLE_NAME + "_" + this.getChildSubQueryPostfix();
  }

  private String getChildSubQueryPostfix() {
    if (this.subQueryPostfix.isBlank()) {
      return "" + this.subQueryBuilders.size();
    }
    return this.subQueryPostfix + "_" + this.subQueryBuilders.size();
  }

  private String getDefaultValueIfNodeTypeNotPresent(GraphLoaderReturnType... returnTypes) {
    var set = Arrays.stream(returnTypes).collect(Collectors.toSet());
    if (set.contains(GraphLoaderReturnType.SORT_OPTION) || set.contains(
        GraphLoaderReturnType.FILTER_OPTION)) {
      return "( RETURN null )";
    }
    if (this.isUsedAsGraphTraversal) {
      return String.format(
          "( RETURN { graphResponse: { nodes: [ %s ], edges: [] }, data: {} } )",
          new ArangoNodeKeptAttributesBuilder(
              this.structureSchemaFinder,
              this.nodeDocumentVariableName,
              this.getChildSubQueryPostfix(),
              ""
          ).build().getQueryString()
      );
    }
    return String.format(
        "( %s )",
        new ArangoNodeGetSubQueryBuilder(
            this.searchOptionResolver,
            this.structureSchemaFinder,
            "",
            this.getChildSubQueryPostfix(),
            this.nodeDocumentVariableName
        ).build(GraphLoaderReturnType.GRAPH).getQueryString()
    );
  }
}
