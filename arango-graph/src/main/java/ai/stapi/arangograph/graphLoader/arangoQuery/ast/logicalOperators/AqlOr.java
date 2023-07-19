package ai.stapi.arangograph.graphLoader.arangoQuery.ast.logicalOperators;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlOr implements AqlNode {

  private final AqlNode leftNode;
  private final AqlNode rightNode;

  public AqlOr(AqlNode leftNode, AqlNode rightNode) {
    this.leftNode = leftNode;
    this.rightNode = rightNode;
  }

  public AqlNode getLeftNode() {
    return leftNode;
  }

  public AqlNode getRightNode() {
    return rightNode;
  }

  @Override
  public String toQueryString() {
    return String.format("%s OR %s", this.leftNode.toQueryString(), this.rightNode.toQueryString());
  }
}
