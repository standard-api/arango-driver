package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

public class AqlOperatorStatement implements AqlNode {

  private final AqlNode leftExpression;
  private final AqlOperator operator;
  private final AqlNode rightExpression;

  public AqlOperatorStatement(
      AqlNode leftExpression,
      AqlOperator operator,
      AqlNode rightExpression
  ) {
    this.leftExpression = leftExpression;
    this.operator = operator;
    this.rightExpression = rightExpression;
  }

  public AqlNode getLeftExpression() {
    return leftExpression;
  }

  public AqlOperator getOperator() {
    return operator;
  }

  public AqlNode getRightExpression() {
    return rightExpression;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "%s %s %s",
        this.leftExpression.toQueryString(),
        this.operator.toQueryString(),
        this.rightExpression.toQueryString()
    );
  }
}
