package ai.stapi.arangograph.graphLoader.arangoQuery.ast.arrayComparisonOperators;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;

public class AqlAll extends AqlOperator {

  private final AqlOperator aqlNode;

  public AqlAll(AqlOperator aqlNode) {
    super("ALL");
    this.aqlNode = aqlNode;
  }

  public AqlNode getAqlNode() {
    return aqlNode;
  }

  @Override
  public String toQueryString() {
    return String.format("%s %s", this.operatorName, this.aqlNode.toQueryString());
  }
}
