package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

public class AqlOperator implements AqlNode {

  protected final String operatorName;

  public AqlOperator(String operatorName) {
    this.operatorName = operatorName;
  }

  public static AqlOperator equality() {
    return new AqlOperator("==");
  }

  public static AqlOperator inequality() {
    return new AqlOperator("!=");
  }

  public static AqlOperator lessThan() {
    return new AqlOperator("<");
  }

  public static AqlOperator lessOrEqual() {
    return new AqlOperator("<=");
  }

  public static AqlOperator greaterThan() {
    return new AqlOperator(">");
  }

  public static AqlOperator greaterOrEqual() {
    return new AqlOperator(">=");
  }

  public static AqlOperator in() {
    return new AqlOperator("IN");
  }

  public static AqlOperator notIn() {
    return new AqlOperator("NOT IN");
  }

  public static AqlOperator like() {
    return new AqlOperator("LIKE");
  }

  public static AqlOperator notLike() {
    return new AqlOperator("NOT LIKE");
  }

  public static AqlOperator matches() {
    return new AqlOperator("=~");
  }

  public static AqlOperator notMatches() {
    return new AqlOperator("!~");
  }

  @Override
  public String toQueryString() {
    return this.operatorName;
  }

  public String getOperatorName() {
    return operatorName;
  }
}
