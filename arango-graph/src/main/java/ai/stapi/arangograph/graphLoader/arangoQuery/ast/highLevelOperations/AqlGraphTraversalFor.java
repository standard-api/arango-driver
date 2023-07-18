package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlInteger;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public class AqlGraphTraversalFor implements AqlNode {

  private final AqlVariable nodeVariable;

  private final TraversalDirection traversalDirection;
  private final AqlNode startNode;

  private final List<AqlNode> edgeCollections;
  @Nullable
  private final AqlVariable edgeVariable;
  @Nullable
  private final AqlVariable pathVariable;
  @Nullable
  private final AqlInteger min;
  @Nullable
  private final AqlInteger max;

  public AqlGraphTraversalFor(
      AqlVariable nodeVariable,
      TraversalDirection traversalDirection,
      AqlNode startNode,
      List<AqlNode> edgeCollections,
      @Nullable AqlVariable edgeVariable,
      @Nullable AqlVariable pathVariable,
      @Nullable AqlInteger min,
      @Nullable AqlInteger max
  ) {
    this.nodeVariable = nodeVariable;
    this.traversalDirection = traversalDirection;
    this.startNode = startNode;
    this.edgeCollections = edgeCollections;
    this.edgeVariable = edgeVariable;
    this.pathVariable = pathVariable;
    this.min = min;
    this.max = max;
  }

  public AqlVariable getNodeVariable() {
    return nodeVariable;
  }

  public TraversalDirection getTraversalDirection() {
    return traversalDirection;
  }

  public AqlNode getStartNode() {
    return startNode;
  }

  public List<AqlNode> getEdgeCollections() {
    return edgeCollections;
  }

  @Nullable
  public AqlVariable getEdgeVariable() {
    return edgeVariable;
  }

  @Nullable
  public AqlVariable getPathVariable() {
    return pathVariable;
  }

  @Nullable
  public AqlInteger getMin() {
    return min;
  }

  @Nullable
  public AqlInteger getMax() {
    return max;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "FOR %s IN %s %s %s %s",
        this.createForVariables(),
        this.createMinMax(),
        this.traversalDirection,
        this.startNode.toQueryString(),
        this.edgeCollections.stream().map(AqlNode::toQueryString).collect(Collectors.joining(", "))
    );
  }

  private String createForVariables() {
    var variables = new ArrayList<String>();
    variables.add(this.nodeVariable.toQueryString());
    if (this.edgeVariable != null) {
      variables.add(this.edgeVariable.toQueryString());
    }
    if (this.pathVariable != null) {
      variables.add(this.pathVariable.toQueryString());
    }
    return String.join(", ", variables);
  }

  private String createMinMax() {
    if (this.min == null) {
      return "";
    }
    if (this.max == null) {
      return this.min.toQueryString() + "";
    }
    return String.format("%s..%s", this.min.toQueryString(), this.max.toQueryString());
  }
}
