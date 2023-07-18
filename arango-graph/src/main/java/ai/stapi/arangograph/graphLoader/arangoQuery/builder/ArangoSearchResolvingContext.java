package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.graphoperations.graphLoader.search.SearchResolvingContext;

public class ArangoSearchResolvingContext implements SearchResolvingContext {

  private final AqlVariable documentName;
  private final ArangoQueryType originQueryType;

  private final String graphElementType;
  private final String placeholderPostfix;
  private final String subQueryPostfix;

  public ArangoSearchResolvingContext(
      AqlVariable documentName,
      ArangoQueryType originQueryType,
      String graphElementType, String placeholderPostfix,
      String subQueryPostfix
  ) {
    this.documentName = documentName;
    this.originQueryType = originQueryType;
    this.graphElementType = graphElementType;
    this.placeholderPostfix = placeholderPostfix;
    this.subQueryPostfix = subQueryPostfix;
  }

  public ArangoSearchResolvingContext(
      String documentName,
      ArangoQueryType originQueryType,
      String graphElementType,
      String placeholderPostfix,
      String subQueryPostfix
  ) {
    this(new AqlVariable(documentName), originQueryType, graphElementType, placeholderPostfix,
        subQueryPostfix);
  }

  public ArangoSearchResolvingContext(AqlVariable documentName, ArangoQueryType originQueryType,
      String graphElementType) {
    this(documentName, originQueryType, graphElementType, "", "");
  }

  public ArangoSearchResolvingContext(String documentName, ArangoQueryType originQueryType,
      String graphElementType) {
    this(documentName, originQueryType, graphElementType, "", "");
  }

  public String getPlaceholderPostfix() {
    return placeholderPostfix;
  }

  public AqlVariable getDocumentName() {
    return documentName;
  }

  public String getSubQueryPostfix() {
    return subQueryPostfix;
  }

  public ArangoQueryType getOriginQueryType() {
    return originQueryType;
  }

  public String getGraphElementType() {
    return graphElementType;
  }
}
