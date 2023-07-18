package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlFilter implements AqlNode {

  private final AqlNode aqlBooleanExpression;

  public AqlFilter(AqlNode aqlBooleanExpression) {
    this.aqlBooleanExpression = aqlBooleanExpression;
  }

  public AqlNode getAqlBooleanExpression() {
    return aqlBooleanExpression;
  }

  @Override
  public String toQueryString() {
    return String.format("FILTER %s", this.aqlBooleanExpression.toQueryString());
  }
}
