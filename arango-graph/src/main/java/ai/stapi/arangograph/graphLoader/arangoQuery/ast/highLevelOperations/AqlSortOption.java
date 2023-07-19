package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;

public class AqlSortOption implements AqlNode {

  private final AqlNode expression;
  private final String direction;

  private AqlSortOption(AqlNode expression, String direction) {
    this.expression = expression;
    this.direction = direction;
  }

  public static AqlSortOption asc(AqlNode expression) {
    return new AqlSortOption(expression, "ASC");
  }

  public static AqlSortOption desc(AqlNode expression) {
    return new AqlSortOption(expression, "DESC");
  }

  @Override
  public String toQueryString() {
    return String.format("%s %s", this.expression.toQueryString(), this.direction);
  }
}
