package ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions;


import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;

public class AqlDocument implements AqlNode {

  private final AqlNode documentId;

  public AqlDocument(AqlNode documentId) {
    this.documentId = documentId;
  }

  public AqlDocument(String collection, String key) {
    this.documentId = new AqlVariable(String.format("%s/%s", collection, key));
  }

  public AqlNode getDocumentId() {
    return documentId;
  }


  @Override
  public String toQueryString() {
    return String.format("DOCUMENT(%s)", this.documentId.toQueryString());
  }
}
