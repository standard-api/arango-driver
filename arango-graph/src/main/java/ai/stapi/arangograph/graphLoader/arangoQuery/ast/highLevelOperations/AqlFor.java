package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;

public class AqlFor implements AqlNode {

  private final AqlVariable aqlVariable;
  private final AqlNode aqlList;

  public AqlFor(AqlVariable aqlVariable, AqlNode aqlList) {
    this.aqlVariable = aqlVariable;
    this.aqlList = aqlList;
  }

  public AqlVariable getAqlVariable() {
    return aqlVariable;
  }

  public AqlNode getAqlList() {
    return aqlList;
  }

  @Override
  public String toQueryString() {
    return String.format("FOR %s IN %s", this.aqlVariable.toQueryString(),
        this.aqlList.toQueryString());
  }
}
