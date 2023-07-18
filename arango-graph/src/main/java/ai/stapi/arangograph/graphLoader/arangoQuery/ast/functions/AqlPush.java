package ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlPush implements AqlNode {

  private final AqlNode aqlListExpression;

  private final AqlNode aqlNode;

  public AqlPush(AqlNode aqlListExpression, AqlNode aqlNode) {
    this.aqlListExpression = aqlListExpression;
    this.aqlNode = aqlNode;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "PUSH(%s, %s)",
        this.aqlListExpression.toQueryString(),
        this.aqlNode.toQueryString()
    );
  }
}
