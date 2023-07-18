package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlString;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlKeep;
import ai.stapi.graphoperations.serializableGraph.GraphElementKeys;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.arangodb.internal.DocumentFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ArangoKeptAttributesBuilder {

  protected final AqlVariable documentName;
  protected final String subQueryPostfix;
  protected final Map<String, Object> bindParameters;
  protected final List<AqlNode> attributesToKeep;
  private final StructureSchemaFinder structureSchemaFinder;
  private final String graphElementType;
  private boolean keepAllAttributes = false;
  
  private boolean keepId = false;

  protected ArangoKeptAttributesBuilder(
      StructureSchemaFinder structureSchemaFinder,
      AqlVariable documentName,
      String subQueryPostfix,
      String graphElementType
  ) {
    this.structureSchemaFinder = structureSchemaFinder;
    this.documentName = documentName;
    this.subQueryPostfix = subQueryPostfix;
    this.graphElementType = graphElementType;
    this.bindParameters = new HashMap<>();
    this.attributesToKeep = new ArrayList<>();
  }

  protected abstract String[] provideDefaultMetaAttributes();
  
  public ArangoKeptAttributesBuilder addId() {
    this.keepId = true;
    return this;
  }

  public ArangoKeptAttributesBuilder addAttribute(String attributeName) {
    var postfix = this.subQueryPostfix.isBlank() ? "" : "__" + this.subQueryPostfix;
    var placeHolder = "keptAttributeName_" + this.attributesToKeep.size() + postfix;
    this.attributesToKeep.add(new AqlVariable("@" + placeHolder));
    this.bindParameters.put(placeHolder, attributeName);
    return this;
  }

  public ArangoKeptAttributesBuilder addAttribute(String attributeName, String placeholder) {
    this.attributesToKeep.add(new AqlVariable("@" + placeholder));
    this.bindParameters.put(placeholder, attributeName);
    return this;
  }

  public ArangoKeptAttributesBuilder keepAllAttributes() {
    this.keepAllAttributes = true;
    return this;
  }

  protected ArangoQuery build() {
    return new ArangoQuery(
        this.buildAqlObject(),
        this.bindParameters
    );
  }

  public AqlNode buildOneAttributeFirstValue() {
    if (this.keepId) {
      return this.documentName.getField(DocumentFields.KEY);
    }
    return this.buildOneAttribute().getItem(0).getField("value");
  }

  public AqlNode buildOneListOrLeafAttribute() {
    if (this.keepId) {
      return this.documentName.getField(DocumentFields.KEY);
    }
    var attribute = this.buildOneAttribute();
    var attributeName = (String) this.bindParameters.values().iterator().next();
    var fieldDefinition = this.structureSchemaFinder.getFieldDefinitionOrFallback(
        this.graphElementType,
        attributeName
    );
    if (!fieldDefinition.isList()) {
      return attribute.getItem(0).getField("value");
    } else {
      return attribute.getAllItems().getField("value");
    }
  }

  private AqlNode buildOneAttribute() {
    return this.documentName
        .getField(GraphElementKeys.ATTRIBUTES)
        .getField(this.attributesToKeep.get(0).toQueryString())
        .getItem(0)
        .getField("values");
  }


  private AqlNode buildAqlObject() {
    if (this.keepAllAttributes) {
      return this.documentName;
    }
    var fieldMap = new HashMap<AqlNode, AqlNode>();
    Arrays.stream(this.provideDefaultMetaAttributes())
        .forEach(
            metaAttribute -> fieldMap.put(
                new AqlString(metaAttribute),
                this.documentName.getField(metaAttribute)
            )
        );

    var keep = new AqlKeep(
        this.documentName.getField(GraphElementKeys.ATTRIBUTES),
        this.attributesToKeep
    );

    fieldMap.put(
        new AqlString(GraphElementKeys.ATTRIBUTES),
        this.attributesToKeep.isEmpty() ? new AqlObject(Map.of()) : keep
    );

    return new AqlObject(fieldMap);
  }

  public boolean isExactlyOneAttributeSet() {
    return this.attributesToKeep.size() == 1 || this.keepId;
  }
}
