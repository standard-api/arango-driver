package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

public class AqlString implements AqlNode {

  private final String string;

  public AqlString(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }

  @Override
  public String toQueryString() {
    return String.format("\"%s\"", this.string);
  }
}
