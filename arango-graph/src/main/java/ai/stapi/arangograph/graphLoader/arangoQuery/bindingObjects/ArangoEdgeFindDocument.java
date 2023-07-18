package ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects;

import java.util.List;

public class ArangoEdgeFindDocument {

  private List<Object> data;
  private EdgeArangoFindGraphDocument graphResponse;

  private ArangoEdgeFindDocument() {
  }

  public ArangoEdgeFindDocument(List<Object> data, EdgeArangoFindGraphDocument graphResponse) {
    this.data = data;
    this.graphResponse = graphResponse;
  }

  public List<Object> getData() {
    return data;
  }

  public EdgeArangoFindGraphDocument getGraphResponse() {
    return graphResponse;
  }
}
