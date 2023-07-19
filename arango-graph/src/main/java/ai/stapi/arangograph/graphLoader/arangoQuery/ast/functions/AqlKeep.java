package ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import java.util.List;
import java.util.stream.Collectors;

public class AqlKeep implements AqlNode {

  private final AqlNode aqlObjectExpression;
  private final List<AqlNode> attributeNames;

  public AqlKeep(
      AqlNode aqlObjectExpression,
      List<AqlNode> attributeNames
  ) {
    this.aqlObjectExpression = aqlObjectExpression;
    this.attributeNames = attributeNames;
  }

  public AqlNode getAqlObjectExpression() {
    return aqlObjectExpression;
  }

  public List<AqlNode> getAttributeNames() {
    return attributeNames;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "KEEP(%s, %s)",
        this.aqlObjectExpression.toQueryString(),
        this.attributeNames.stream().map(AqlNode::toQueryString).collect(Collectors.joining(", "))
    );
  }
}
