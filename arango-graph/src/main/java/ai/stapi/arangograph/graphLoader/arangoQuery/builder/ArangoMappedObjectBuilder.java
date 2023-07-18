package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.functions.AqlParseIdentifier;
import ai.stapi.graphoperations.serializableGraph.GraphElementKeys;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.HashMap;
import java.util.Map;

public class ArangoMappedObjectBuilder {

  private final ArangoKeptAttributesBuilder keptAttributesBuilder;
  private final StructureSchemaFinder structureSchemaFinder;
  private final AqlVariable documentName;
  private final String subQueryPostfix;
  private final String graphElementType;
  private final Map<String, Object> bindParameters;
  private final Map<AqlNode, AqlNode> fields;


  public ArangoMappedObjectBuilder(
      ArangoKeptAttributesBuilder keptAttributesBuilder,
      StructureSchemaFinder structureSchemaFinder,
      AqlVariable documentName,
      String subQueryPostfix,
      String graphElementType
  ) {
    this.keptAttributesBuilder = keptAttributesBuilder;
    this.structureSchemaFinder = structureSchemaFinder;
    this.documentName = documentName;
    this.subQueryPostfix = subQueryPostfix;
    this.graphElementType = graphElementType;
    this.bindParameters = new HashMap<>();
    this.fields = new HashMap<>();
  }

  public ArangoMappedObjectBuilder mapAttribute(String fieldName, String attributeName) {
    var postfix = this.subQueryPostfix.isBlank() ? "" : "__" + this.subQueryPostfix;
    var attributeNamePlaceHolder = "mappedAttributeName_" + this.fields.size() + postfix;
    var fieldNamePlaceHolder = "mappedFieldName_" + this.fields.size() + postfix;
    this.keptAttributesBuilder.addAttribute(attributeName, attributeNamePlaceHolder);
    this.bindParameters.put(fieldNamePlaceHolder, fieldName);

    var fieldDefinition = this.structureSchemaFinder.getFieldDefinitionOrFallback(
        this.graphElementType,
        attributeName
    );

    var getAttributeVariable = this.documentName
        .getField(GraphElementKeys.ATTRIBUTES)
        .getField("@" + attributeNamePlaceHolder)
        .getItem(0)
        .getField("values");

    this.fields.put(
        new AqlVariable("@" + fieldNamePlaceHolder),
        fieldDefinition.isList()
            ? getAttributeVariable.getAllItems().getField("value")
            : getAttributeVariable.getItem(0).getField("value")
    );
    return this;
  }

  public ArangoMappedObjectBuilder mapId(String fieldName) {
    var postfix = this.subQueryPostfix.isBlank() ? "" : "__" + this.subQueryPostfix;
    var fieldNamePlaceHolder = "mappedFieldName_" + this.fields.size() + postfix;
    this.bindParameters.put(fieldNamePlaceHolder, fieldName);
    this.fields.put(
        new AqlVariable("@" + fieldNamePlaceHolder),
        this.documentName.getField("_key")
    );
    return this;
  }

  public ArangoMappedObjectBuilder mapType(String fieldName) {
    var postfix = this.subQueryPostfix.isBlank() ? "" : "__" + this.subQueryPostfix;
    var fieldNamePlaceHolder = "mappedFieldName_" + this.fields.size() + postfix;
    this.bindParameters.put(fieldNamePlaceHolder, fieldName);
    this.fields.put(
        new AqlVariable("@" + fieldNamePlaceHolder),
        new AqlParseIdentifier(this.documentName).getCollection()
    );
    return this;
  }

  public ArangoMappedObjectBuilder mapCustomField(String fieldName, AqlNode fieldValue) {
    var postfix = this.subQueryPostfix.isBlank() ? "" : "__" + this.subQueryPostfix;
    var fieldNamePlaceHolder = "mappedFieldName_" + this.fields.size() + postfix;
    this.bindParameters.put(fieldNamePlaceHolder, fieldName);
    this.fields.put(
        new AqlVariable("@" + fieldNamePlaceHolder),
        fieldValue
    );
    return this;
  }

  protected ArangoQuery build() {
    return new ArangoQuery(
        new AqlObject(this.fields),
        this.bindParameters
    );
  }
}
