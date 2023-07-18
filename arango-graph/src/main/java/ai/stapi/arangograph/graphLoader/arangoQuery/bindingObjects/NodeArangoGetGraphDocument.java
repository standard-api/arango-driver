package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

import ai.stapi.graph.graphelements.Edge;
import ai.stapi.graph.graphelements.Node;
import java.util.List;

public class NodeArangoGetGraphDocument {

  Node mainGraphElement;
  List<Edge> edges;
  List<Node> nodes;

  private NodeArangoGetGraphDocument() {
  }

  public NodeArangoGetGraphDocument(
      Node mainGraphElement,
      List<Edge> edges,
      List<Node> nodes
  ) {
    this.mainGraphElement = mainGraphElement;
    this.edges = edges;
    this.nodes = nodes;
  }

  public Node getMainGraphElement() {
    return mainGraphElement;
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public List<Node> getNodes() {
    return nodes;
  }
}
