package ai.stapi.arangograph.graphLoader.arangoQuery.ast;

public interface AqlNode {

  String toQueryString();

  default AqlVariable getItem(Integer index) {
    return new AqlVariable(String.format("%s[%s]", this.toQueryString(), index));
  }

  default AqlVariable getAllItems() {
    return new AqlVariable(String.format("%s[*]", this.toQueryString()));
  }

  default AqlVariable getFlattenItems() {
    return new AqlVariable(String.format("%s[**]", this.toQueryString()));
  }

  default AqlVariable getField(String fieldName) {
    return new AqlVariable(String.format("%s.%s", this.toQueryString(), fieldName));
  }

  default AqlVariable markAsBindParameter() {
    return new AqlVariable("@" + this.toQueryString());
  }
}
