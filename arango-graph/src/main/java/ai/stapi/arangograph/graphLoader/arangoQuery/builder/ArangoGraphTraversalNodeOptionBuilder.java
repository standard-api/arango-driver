package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlReturn;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.EdgeDescriptionParameters;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ArangoGraphTraversalNodeOptionBuilder
    implements ArangoGraphTraversalJoinable, ArangoSubQueryBuilder {

  private final StructureSchemaFinder structureSchemaFinder;
  private final ArangoGenericSearchOptionResolver searchOptionResolver;
  private final ArangoNodeKeptAttributesBuilder keptAttributesBuilder;
  private final ArangoMappedObjectBuilder mappedObjectBuilder;

  private final String subQueryPostfix;

  private final Map<String, ArangoGraphTraversalSubQueryBuilder> subQueryBuilders;

  private final String nodeType;

  private final AqlVariable nodeDocumentVariable;

  public ArangoGraphTraversalNodeOptionBuilder(
      StructureSchemaFinder structureSchemaFinder,
      ArangoGenericSearchOptionResolver searchOptionResolver,
      String graphElementType,
      String subQueryPostfix,
      AqlVariable nodeDocumentVariable
  ) {
    this.structureSchemaFinder = structureSchemaFinder;
    this.searchOptionResolver = searchOptionResolver;
    this.subQueryPostfix = subQueryPostfix;
    this.nodeType = graphElementType;
    this.nodeDocumentVariable = nodeDocumentVariable;
    this.keptAttributesBuilder = new ArangoNodeKeptAttributesBuilder(
        this.structureSchemaFinder,
        nodeDocumentVariable,
        this.subQueryPostfix,
        this.nodeType
    );
    this.mappedObjectBuilder = new ArangoMappedObjectBuilder(
        this.keptAttributesBuilder,
        this.structureSchemaFinder,
        nodeDocumentVariable,
        "node_" + this.subQueryPostfix,
        graphElementType
    );
    this.subQueryBuilders = new HashMap<>();
  }

  @NotNull
  private static String getSubQueryVariable(String childSubQueryPrefix) {
    return BASE_SUB_QUERY_VARIABLE_NAME + "_graphTraverOption_" + childSubQueryPrefix;
  }

  public ArangoMappedObjectBuilder setMappedScalars() {
    return this.mappedObjectBuilder;
  }

  public ArangoNodeKeptAttributesBuilder setKeptAttributes() {
    return this.keptAttributesBuilder;
  }

  public ArangoGraphTraversalSubQueryBuilder joinGraphTraversal(
      AbstractEdgeDescription edgeDescription) {
    var childSubQueryPrefix = this.getChildSubQueryPostfix();
    var subQueryBuilder = new ArangoGraphTraversalSubQueryBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        edgeDescription,
        childSubQueryPrefix,
        this.nodeDocumentVariable
    );
    this.subQueryBuilders.put(
        getSubQueryVariable(childSubQueryPrefix),
        subQueryBuilder
    );
    return subQueryBuilder;
  }

  @Override
  public ArangoGraphTraversalSubQueryBuilder mapGraphTraversal(
      String fieldName,
      AbstractEdgeDescription edgeDescription
  ) {
    var childSubQueryPrefix = this.getChildSubQueryPostfix();
    var subQueryVariable = getSubQueryVariable(childSubQueryPrefix);
    var edgeType = ((EdgeDescriptionParameters) edgeDescription.getParameters()).getEdgeType();
    var fieldDefinition = this.structureSchemaFinder.getFieldDefinitionOrFallback(
        this.nodeType,
        edgeType
    );
    if (fieldDefinition.isList()) {
      this.mappedObjectBuilder.mapCustomField(fieldName,
          new AqlVariable(subQueryVariable + "[*].data"));
    } else {
      this.mappedObjectBuilder.mapCustomField(fieldName,
          new AqlVariable(subQueryVariable + "[0].data"));
    }
    var subQueryBuilder = new ArangoGraphTraversalSubQueryBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        edgeDescription,
        childSubQueryPrefix,
        this.nodeDocumentVariable
    );
    this.subQueryBuilders.put(
        subQueryVariable,
        subQueryBuilder
    );
    return subQueryBuilder;
  }

  public ArangoGraphTraversalSubQueryBuilder mapGraphTraversalAsConnections(
      String fieldName,
      AbstractEdgeDescription edgeDescription
  ) {
    var childSubQueryPrefix = this.getChildSubQueryPostfix();
    var subQueryVariable = getSubQueryVariable(childSubQueryPrefix);
    var edgeType = ((EdgeDescriptionParameters) edgeDescription.getParameters()).getEdgeType();
    var fieldDefinition = this.structureSchemaFinder.getFieldDefinitionOrFallback(
        this.nodeType,
        edgeType
    );
    if (fieldDefinition.isList()) {
      this.mappedObjectBuilder.mapCustomField(fieldName + "Connections",
          new AqlVariable(subQueryVariable + "[*].data"));
    } else {
      this.mappedObjectBuilder.mapCustomField(fieldName + "Connections",
          new AqlVariable(subQueryVariable + "[0].data"));
    }

    var subQueryBuilder = ArangoGraphTraversalSubQueryBuilder.asConnections(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        edgeDescription,
        childSubQueryPrefix,
        this.nodeDocumentVariable
    );
    this.subQueryBuilders.put(
        subQueryVariable,
        subQueryBuilder
    );
    return subQueryBuilder;
  }

  @Override
  public ArangoQuery build(GraphLoaderReturnType... returnTypes) {
    var buildSubQueries = this.buildSubQueries(returnTypes);
    var keptAttributes = this.keptAttributesBuilder.build();
    var mappedObject = this.mappedObjectBuilder.build();
    Map<String, Object> finalBindParameters = new HashMap<>();
    finalBindParameters.putAll(keptAttributes.getBindParameters());
    finalBindParameters.putAll(mappedObject.getBindParameters());
    buildSubQueries.values()
        .forEach(query -> finalBindParameters.putAll(query.getBindParameters()));
    return new ArangoQuery(
        new AqlVariable(
            this.buildQueryString(
                buildSubQueries,
                keptAttributes,
                mappedObject,
                returnTypes)
        ),
        finalBindParameters
    );
  }

  @NotNull
  private String buildQueryString(
      Map<String, ArangoQuery> subQueries,
      ArangoQuery keptAttributes,
      ArangoQuery mappedObject,
      GraphLoaderReturnType... returnTypes
  ) {
    var subQueryString = this.buildSubQueriesString(subQueries);
    var set = Arrays.stream(returnTypes).collect(Collectors.toSet());
    if (set.contains(GraphLoaderReturnType.SORT_OPTION)) {
      if (this.keptAttributesBuilder.isExactlyOneAttributeSet()) {
        return new AqlReturn(
            this.keptAttributesBuilder.buildOneAttributeFirstValue()).toQueryString();
      }
      return String.format(
          "%s RETURN %s[0]",
          subQueryString,
          this.subQueryBuilders.keySet().iterator().next()
      );
    }
    if (set.contains(GraphLoaderReturnType.FILTER_OPTION)) {
      if (this.keptAttributesBuilder.isExactlyOneAttributeSet()) {
        return new AqlReturn(
            this.keptAttributesBuilder.buildOneListOrLeafAttribute()
        ).toQueryString();
      }
      var graphTraversalBuilderEntry = this.subQueryBuilders.entrySet().iterator().next();
      var fieldDefinition = this.structureSchemaFinder.getFieldDefinitionOrFallback(
          this.nodeType,
          graphTraversalBuilderEntry.getValue().getEdgeType()
      );
      return String.format(
          "%s RETURN %s%s",
          subQueryString,
          graphTraversalBuilderEntry.getKey(),
          fieldDefinition.isList() ? "" : "[0]"
      );
    }
    var graph = String.format(
        "{ edges: %s, nodes: PUSH(%s, %s) }",
        this.buildJoinedEdges(),
        this.buildJoinedNodes(),
        keptAttributes.getQueryString()
    );
    return String.format(
        "%s RETURN { data: %s, graphResponse: %s }",
        subQueryString,
        set.contains(GraphLoaderReturnType.OBJECT) ? mappedObject.getQueryString() : "{}",
        set.contains(GraphLoaderReturnType.GRAPH) ? graph : "{}"
    );
  }

  private Map<String, ArangoQuery> buildSubQueries(GraphLoaderReturnType... returnTypes) {
    return this.subQueryBuilders.entrySet()
        .stream()
        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(),
            entry.getValue().build(returnTypes)))
        .collect(
            Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
  }

  private String buildSubQueriesString(Map<String, ArangoQuery> subQueries) {
    var strings = subQueries.entrySet().stream()
        .map(entry -> this.buildSubQueryString(entry.getKey(), entry.getValue().getQueryString()))
        .toList();

    return String.join(" ", strings);
  }

  private String buildSubQueryString(String variableKey, String subQueryString) {
    var variableAssignmentStatement = "LET " + variableKey + " = (";
    var endStatement = ")";
    return String.join(
        " ",
        variableAssignmentStatement,
        subQueryString,
        endStatement
    );
  }

  protected String buildJoinedEdges() {
    if (this.subQueryBuilders.size() == 0) {
      return "[]";
    }
    if (this.subQueryBuilders.size() == 1) {
      var key = String.join("", this.subQueryBuilders.keySet());
      return key + "[*].graphResponse.edges[**]";
    }
    return "UNION("
        + this.subQueryBuilders.keySet().stream()
        .map(key -> key + "[*].graphResponse.edges[**]")
        .collect(Collectors.joining(", "))
        + ")";
  }

  protected String buildJoinedNodes() {
    if (this.subQueryBuilders.size() == 0) {
      return "[]";
    }
    if (this.subQueryBuilders.size() == 1) {
      var key = String.join("", this.subQueryBuilders.keySet());
      return key + "[*].graphResponse.nodes[**]";
    }
    return "UNION("
        + this.subQueryBuilders.keySet().stream()
        .map(key -> key + "[*].graphResponse.nodes[**]")
        .collect(Collectors.joining(", "))
        + ")";
  }

  private AqlVariable buildDocumentName() {
    if (this.subQueryPostfix.isBlank()) {
      return new AqlVariable(NODE_DOCUMENT_BASE_NAME);
    }
    return new AqlVariable(NODE_DOCUMENT_BASE_NAME + "_" + this.subQueryPostfix);
  }

  protected String getChildSubQueryPostfix() {
    if (this.subQueryPostfix.isBlank()) {
      return String.valueOf(this.subQueryBuilders.size());
    }
    return this.subQueryPostfix + "_" + this.subQueryBuilders.size();
  }
}
