package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

import ai.stapi.graph.graphelements.Edge;
import ai.stapi.graph.graphelements.Node;
import java.util.List;

public class EdgeArangoFindGraphDocument {

  List<Edge> mainGraphElements;
  List<Edge> edges;
  List<Node> nodes;

  private EdgeArangoFindGraphDocument() {
  }

  public EdgeArangoFindGraphDocument(
      List<Edge> mainGraphElements,
      List<Edge> edges,
      List<Node> nodes
  ) {
    this.mainGraphElements = mainGraphElements;
    this.edges = edges;
    this.nodes = nodes;
  }

  public List<Edge> getMainGraphElements() {
    return mainGraphElements;
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public List<Node> getNodes() {
    return nodes;
  }
}
