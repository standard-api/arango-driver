package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

public class ArangoEdgeGetDocument {

  private Object data;
  private EdgeArangoGetGraphDocument graphResponse;

  private ArangoEdgeGetDocument() {
  }

  public ArangoEdgeGetDocument(Object data, EdgeArangoGetGraphDocument graphResponse) {
    this.data = data;
    this.graphResponse = graphResponse;
  }

  public Object getData() {
    return data;
  }

  public EdgeArangoGetGraphDocument getGraphResponse() {
    return graphResponse;
  }
}
