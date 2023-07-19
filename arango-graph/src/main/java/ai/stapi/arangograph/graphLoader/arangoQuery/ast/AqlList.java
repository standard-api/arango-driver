package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AqlList implements AqlNode {

  private final List<AqlNode> aqlNodeList;

  public AqlList(List<AqlNode> aqlNodeList) {
    this.aqlNodeList = aqlNodeList;
  }

  public AqlList(AqlNode... aqlNodeList) {
    this.aqlNodeList = Arrays.stream(aqlNodeList).toList();
  }

  public List<AqlNode> getAqlNodeList() {
    return aqlNodeList;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "[ %s ]",
        this.getAqlNodeList().stream()
            .map(AqlNode::toQueryString)
            .collect(Collectors.joining(", "))
    );
  }
}
