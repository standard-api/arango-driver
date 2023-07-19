package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

import ai.stapi.graph.graphelements.Edge;
import ai.stapi.graph.graphelements.Node;
import java.util.List;

public class NodeArangoFindGraphDocument {

  List<Node> mainGraphElements;
  List<Edge> edges;
  List<Node> nodes;

  private NodeArangoFindGraphDocument() {
  }

  public NodeArangoFindGraphDocument(
      List<Node> mainGraphElements,
      List<Edge> edges,
      List<Node> nodes
  ) {
    this.mainGraphElements = mainGraphElements;
    this.edges = edges;
    this.nodes = nodes;
  }

  public List<Node> getMainGraphElements() {
    return mainGraphElements;
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public List<Node> getNodes() {
    return nodes;
  }
}
