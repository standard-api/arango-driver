package ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;

public class AqlParseIdentifier implements AqlNode {

  private final AqlNode documentExpression;

  public AqlParseIdentifier(AqlNode documentExpression) {
    this.documentExpression = documentExpression;
  }

  public AqlNode getDocumentExpression() {
    return documentExpression;
  }

  public AqlVariable getCollection() {
    return new AqlVariable(this.toQueryString()).getField("collection");
  }

  @Override
  public String toQueryString() {
    return String.format("PARSE_IDENTIFIER(%s)", this.documentExpression.toQueryString());
  }
}
