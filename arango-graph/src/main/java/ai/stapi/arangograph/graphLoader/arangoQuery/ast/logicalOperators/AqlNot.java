package ai.stapi.arangograph.graphLoader.arangoQuery.ast.logicalOperators;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlNot implements AqlNode {

  private final AqlNode aqlNode;

  public AqlNot(AqlNode aqlNode) {
    this.aqlNode = aqlNode;
  }

  public AqlNode getAqlNode() {
    return aqlNode;
  }

  @Override
  public String toQueryString() {
    return String.format("NOT %s", this.aqlNode.toQueryString());
  }
}
