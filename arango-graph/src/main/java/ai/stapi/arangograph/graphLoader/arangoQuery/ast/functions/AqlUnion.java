package ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import java.util.List;
import java.util.stream.Collectors;

public class AqlUnion implements AqlNode {

  private final List<AqlNode> aqlListExpressionList;

  public AqlUnion(List<AqlNode> aqlListExpressionList) {
    this.aqlListExpressionList = aqlListExpressionList;
  }

  public List<AqlNode> getAqlListExpressionList() {
    return aqlListExpressionList;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "UNION(%s)",
        this.aqlListExpressionList.stream().map(AqlNode::toQueryString)
            .collect(Collectors.joining(", "))
    );
  }
}
