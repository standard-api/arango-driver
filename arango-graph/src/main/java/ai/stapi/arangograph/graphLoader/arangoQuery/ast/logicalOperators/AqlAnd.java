package ai.stapi.arangograph.graphLoader.arangoQuery.ast.logicalOperators;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlAnd implements AqlNode {

  private final AqlNode leftNode;
  private final AqlNode rightNode;

  public AqlAnd(AqlNode leftNode, AqlNode rightNode) {
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
    return String.format("%s AND %s", this.leftNode.toQueryString(),
        this.rightNode.toQueryString());
  }
}
