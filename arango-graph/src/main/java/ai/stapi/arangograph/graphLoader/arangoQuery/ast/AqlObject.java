package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

import java.util.Map;
import java.util.stream.Collectors;

public class AqlObject implements AqlNode {

  private final Map<AqlNode, AqlNode> fields;

  public AqlObject(Map<AqlNode, AqlNode> fields) {
    this.fields = fields;
  }

  public Map<AqlNode, AqlNode> getFields() {
    return fields;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "{ %s }",
        fields.entrySet().stream()
            .map(entry -> String.format("[%s]: %s", entry.getKey().toQueryString(),
                entry.getValue().toQueryString()))
            .sorted()
            .collect(Collectors.joining(", "))
    );
  }
}
