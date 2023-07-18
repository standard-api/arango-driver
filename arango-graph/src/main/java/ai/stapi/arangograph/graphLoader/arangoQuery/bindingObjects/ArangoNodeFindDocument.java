package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

import java.util.List;

public class ArangoNodeFindDocument {

  private List<Object> data;
  private NodeArangoFindGraphDocument graphResponse;

  private ArangoNodeFindDocument() {
  }

  public ArangoNodeFindDocument(List<Object> data, NodeArangoFindGraphDocument graphResponse) {
    this.data = data;
    this.graphResponse = graphResponse;
  }

  public List<Object> getData() {
    return data;
  }

  public NodeArangoFindGraphDocument getGraphResponse() {
    return graphResponse;
  }
}
