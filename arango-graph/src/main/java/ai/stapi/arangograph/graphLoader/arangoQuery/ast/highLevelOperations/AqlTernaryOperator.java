package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlTernaryOperator implements AqlNode {

  private final AqlNode booleanExpression;
  private final AqlNode leftExpression;
  private final AqlNode rightExpression;

  public AqlTernaryOperator(AqlNode booleanExpression, AqlNode leftExpression,
      AqlNode rightExpression) {
    this.booleanExpression = booleanExpression;
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public AqlNode getBooleanExpression() {
    return booleanExpression;
  }

  public AqlNode getLeftExpression() {
    return leftExpression;
  }

  public AqlNode getRightExpression() {
    return rightExpression;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "%s ? ( %s ) : ( %s )",
        this.booleanExpression.toQueryString(),
        this.leftExpression.toQueryString(),
        this.rightExpression.toQueryString()
    );
  }
}
