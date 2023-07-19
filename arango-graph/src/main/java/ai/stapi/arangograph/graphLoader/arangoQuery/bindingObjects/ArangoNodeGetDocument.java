package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

public class ArangoNodeGetDocument {

  private Object data;
  private NodeArangoGetGraphDocument graphResponse;

  private ArangoNodeGetDocument() {
  }

  public ArangoNodeGetDocument(Object data, NodeArangoGetGraphDocument graphResponse) {
    this.data = data;
    this.graphResponse = graphResponse;
  }

  public Object getData() {
    return data;
  }

  public NodeArangoGetGraphDocument getGraphResponse() {
    return graphResponse;
  }
}
