package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

public class AqlInteger implements AqlNode {

  private final Integer integer;

  public AqlInteger(Integer integer) {
    this.integer = integer;
  }

  public Integer getInteger() {
    return integer;
  }

  @Override
  public String toQueryString() {
    return String.format("%s", this.integer);
  }
}
