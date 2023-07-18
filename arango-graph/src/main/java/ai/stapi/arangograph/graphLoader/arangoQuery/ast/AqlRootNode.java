package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AqlRootNode implements AqlNode {

  private final List<AqlNode> aqlNodeList;

  public AqlRootNode(List<AqlNode> aqlNodeList) {
    this.aqlNodeList = aqlNodeList;
  }

  public AqlRootNode(AqlNode... aqlNodeList) {
    this.aqlNodeList = Arrays.stream(aqlNodeList).toList();
  }

  public List<AqlNode> getAqlNodeList() {
    return aqlNodeList;
  }

  @Override
  public String toQueryString() {
    return this.aqlNodeList.stream().map(AqlNode::toQueryString).collect(Collectors.joining(" "));
  }
}
