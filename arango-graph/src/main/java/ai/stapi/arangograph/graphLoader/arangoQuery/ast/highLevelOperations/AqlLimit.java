package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import org.jetbrains.annotations.Nullable;

public class AqlLimit implements AqlNode {

  private final AqlNode count;
  @Nullable
  private AqlNode offset;

  public AqlLimit(AqlNode count) {
    this.count = count;
  }

  public AqlLimit(AqlNode count, @Nullable AqlNode offset) {
    this.count = count;
    this.offset = offset;
  }

  public AqlNode getCount() {
    return count;
  }

  @Nullable
  public AqlNode getOffset() {
    return offset;
  }

  @Override
  public String toQueryString() {
    if (this.offset == null) {
      return String.format("LIMIT %s", this.count.toQueryString());
    }
    return String.format("LIMIT %s, %s", this.offset.toQueryString(), this.count.toQueryString());
  }
}
