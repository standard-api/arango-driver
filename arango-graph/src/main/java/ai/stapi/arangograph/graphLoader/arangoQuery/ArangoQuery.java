package ai.stapi.arangograph.graphLoader.arangoQuery;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.graphoperations.graphLoader.search.ResolvedQueryPart;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

public class ArangoQuery implements ResolvedQueryPart {

  private final AqlNode aqlNode;
  private final Map<String, Object> bindParameters;

  public ArangoQuery(AqlNode aqlNode, Map<String, Object> bindParameters) {
    this.aqlNode = aqlNode;
    this.bindParameters = bindParameters;
  }

  @JsonIgnore
  public AqlNode getAqlNode() {
    return aqlNode;
  }

  public String getQueryString() {
    return this.aqlNode.toQueryString();
  }

  public Map<String, Object> getBindParameters() {
    return bindParameters;
  }
}
