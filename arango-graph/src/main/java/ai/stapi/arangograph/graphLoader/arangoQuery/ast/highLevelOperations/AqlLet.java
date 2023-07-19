package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;

public class AqlLet implements AqlNode {

  private final AqlVariable variable;
  private final AqlNode aqlNode;

  public AqlLet(AqlVariable variable, AqlNode aqlNode) {
    this.variable = variable;
    this.aqlNode = aqlNode;
  }

  public AqlVariable getVariable() {
    return variable;
  }

  public AqlNode getAqlNode() {
    return aqlNode;
  }

  @Override
  public String toQueryString() {
    return String.format("LET %s = ( %s )", this.variable.toQueryString(),
        this.aqlNode.toQueryString());
  }
}
