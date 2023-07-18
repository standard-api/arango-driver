package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

import ai.stapi.graph.graphelements.Edge;
import ai.stapi.graph.graphelements.Node;
import java.util.List;

public class EdgeArangoGetGraphDocument {

  Edge mainGraphElement;
  List<Edge> edges;
  List<Node> nodes;

  private EdgeArangoGetGraphDocument() {
  }

  public EdgeArangoGetGraphDocument(
      Edge mainGraphElement,
      List<Edge> edges,
      List<Node> nodes
  ) {
    this.mainGraphElement = mainGraphElement;
    this.edges = edges;
    this.nodes = nodes;
  }

  public Edge getMainGraphElement() {
    return mainGraphElement;
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public List<Node> getNodes() {
    return nodes;
  }
}
