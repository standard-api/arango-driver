package ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AqlConcatSeparator implements AqlNode {

  private final AqlNode separator;
  private final List<AqlNode> aqlNodeList;

  public AqlConcatSeparator(AqlNode separator, List<AqlNode> aqlNodeList) {
    this.separator = separator;
    this.aqlNodeList = aqlNodeList;
  }

  public AqlConcatSeparator(AqlNode separator, AqlNode... aqlNodeList) {
    this.separator = separator;
    this.aqlNodeList = Arrays.stream(aqlNodeList).toList();
  }

  public AqlNode getSeparator() {
    return separator;
  }

  public List<AqlNode> getAqlNodeList() {
    return aqlNodeList;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "CONCAT_SEPARATOR(%s, %s)",
        this.separator.toQueryString(),
        this.aqlNodeList.stream().map(AqlNode::toQueryString).collect(Collectors.joining(", "))
    );
  }
}
