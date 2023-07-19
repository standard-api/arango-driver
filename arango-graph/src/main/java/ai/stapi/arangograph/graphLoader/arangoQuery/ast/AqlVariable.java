package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

public class AqlVariable implements AqlNode {

  private final String variableName;

  public AqlVariable(String variableName) {
    this.variableName = variableName;
  }

  public String getVariableName() {
    return variableName;
  }

  @Override
  public String toQueryString() {
    return this.variableName;
  }
}
