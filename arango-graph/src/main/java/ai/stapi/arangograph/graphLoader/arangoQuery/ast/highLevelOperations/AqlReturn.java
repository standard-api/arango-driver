package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlReturn implements AqlNode {

  private final AqlNode expression;

  public AqlReturn(AqlNode expression) {
    this.expression = expression;
  }

  public AqlNode getExpression() {
    return expression;
  }

  @Override
  public String toQueryString() {
    return String.format("RETURN %s", this.expression.toQueryString());
  }
}
