package ai.stapi.arangograph;

import ai.stapi.graphoperations.serializableGraph.SerializableAttributeValue;
import java.util.List;
import java.util.Map;

public class ArangoAttributeVersion {

  private final List<SerializableAttributeValue> values;
  private final Map<String, Object> metaData;

  public ArangoAttributeVersion(
      List<SerializableAttributeValue> values,
      Map<String, Object> metaData
  ) {
    this.values = values;
    this.metaData = metaData;
  }

  public List<SerializableAttributeValue> getValues() {
    return values;
  }

  public Map<String, Object> getMetaData() {
    return metaData;
  }

  @Override
  public String toString() {
    return "ArangoAttributeVersion{"
        + "values=" + values
        + ", metaData=" + metaData
        + '}';
  }
}
