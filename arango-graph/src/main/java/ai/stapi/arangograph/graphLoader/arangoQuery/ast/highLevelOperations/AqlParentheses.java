package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlParentheses implements AqlNode {

  private final AqlNode aqlNode;

  public AqlParentheses(AqlNode aqlNode) {
    this.aqlNode = aqlNode;
  }

  public AqlNode getAqlNode() {
    return aqlNode;
  }

  @Override
  public String toQueryString() {
    return String.format("( %s )", this.aqlNode.toQueryString());
  }
}
