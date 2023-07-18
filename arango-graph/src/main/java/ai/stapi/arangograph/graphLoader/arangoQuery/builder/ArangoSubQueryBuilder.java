package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlString;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlReturn;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface ArangoSubQueryBuilder {

  String NODE_DOCUMENT_BASE_NAME = "nodeElement";
  String EDGE_DOCUMENT_BASE_NAME = "edgeElement";
  String BASE_SUB_QUERY_VARIABLE_NAME = "subQuery";
  String NODES = "nodes";
  String EDGES = "edges";
  
  String GRAPH_RESPONSE = "graphResponse";
  String DATA = "data";

  ArangoQuery build(GraphLoaderReturnType... returnTypes);

  default String createChildSubQueryPostfix(String subQueryPostfix, Integer index) {
    if (subQueryPostfix.isBlank()) {
      return "" + index;
    }
    return subQueryPostfix + "_" + index;
  }

  default AqlVariable createEdgeDocumentName(String subQueryPostfix) {
    if (subQueryPostfix.isBlank()) {
      return new AqlVariable(ArangoSubQueryBuilder.EDGE_DOCUMENT_BASE_NAME);
    }
    return new AqlVariable(ArangoSubQueryBuilder.EDGE_DOCUMENT_BASE_NAME + "_" + subQueryPostfix);
  }

  default AqlVariable createNodeDocumentName(String subQueryPostfix) {
    if (subQueryPostfix.isBlank()) {
      return new AqlVariable(ArangoSubQueryBuilder.NODE_DOCUMENT_BASE_NAME);
    }
    return new AqlVariable(ArangoSubQueryBuilder.NODE_DOCUMENT_BASE_NAME + "_" + subQueryPostfix);
  }

  default AqlVariable createSubQueryVariable(String subQueryPostfix) {
    if (subQueryPostfix.isBlank()) {
      return new AqlVariable(ArangoSubQueryBuilder.BASE_SUB_QUERY_VARIABLE_NAME);
    }
    return new AqlVariable(
        String.format("%s_%s", ArangoSubQueryBuilder.BASE_SUB_QUERY_VARIABLE_NAME, subQueryPostfix)
    );
  }

  default AqlObject createGraphObject(AqlNode nodes, AqlNode edges) {
    return new AqlObject(Map.of(
        new AqlString(NODES), nodes,
        new AqlString(EDGES), edges
    ));
  }

  default AqlReturn createReturnStatement(AqlNode graph, AqlNode data,
                                          GraphLoaderReturnType... returnTypes) {
    var set = Arrays.stream(returnTypes).collect(Collectors.toSet());
    return new AqlReturn(
        new AqlObject(Map.of(
            new AqlString(GRAPH_RESPONSE),
            set.contains(GraphLoaderReturnType.GRAPH) ? graph : new AqlObject(Map.of()),
            new AqlString(DATA),
            set.contains(GraphLoaderReturnType.OBJECT) ? data : new AqlObject(Map.of())
        ))
    );
  }

  default AqlVariable getChildSubQueryEdges(AqlVariable subQueryVariable) {
    return subQueryVariable.getAllItems()
        .getField(GRAPH_RESPONSE)
        .getField(EDGES)
        .getFlattenItems();
  }

  default AqlVariable getChildSubQueryNodes(AqlVariable subQueryVariable) {
    return subQueryVariable.getAllItems()
        .getField(GRAPH_RESPONSE)
        .getField(NODES)
        .getFlattenItems();
  }

  default Map<String, Object> mergeBindParameters(Map<String, Object>... bindParameters) {
    var result = new HashMap<String, Object>();
    Arrays.stream(bindParameters).forEach(result::putAll);
    return result;
  }

  default AqlVariable createCollectionVariable(String subQueryPostfix) {
    if (subQueryPostfix.isBlank()) {
      return new AqlVariable("collection");
    }
    return new AqlVariable("collection_" + subQueryPostfix);
  }

  default AqlVariable createIdVariable(String subQueryPostfix) {
    if (subQueryPostfix.isBlank()) {
      return new AqlVariable("graphElementIdPlaceholder");
    }
    return new AqlVariable("graphElementIdPlaceholder" + subQueryPostfix);
  }

  default AqlObject createConnectionObject(AqlNode edge) {
    return new AqlObject(Map.of(
        new AqlString("edges"), edge
    ));
  }
}
