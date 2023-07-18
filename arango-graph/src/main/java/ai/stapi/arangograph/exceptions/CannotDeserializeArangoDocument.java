package ai.stapi.arangograph.exceptions;

import ai.stapi.arangograph.ArangoAttributeVersion;
import ai.stapi.graphoperations.serializableGraph.GraphElementKeys;
import java.util.List;
import java.util.Map;

public class CannotDeserializeArangoDocument extends RuntimeException {

  protected CannotDeserializeArangoDocument(String message) {
    super("Cannot deserialize Arango Document, because " + message);
  }

  public static CannotDeserializeArangoDocument becauseDocumentDoesNotContainAttributesProperty(
      String id,
      String graphElementType,
      Map<String, Object> properties
  ) {
    return new CannotDeserializeArangoDocument(
        "document does not contain property with key: "
            + GraphElementKeys.ATTRIBUTES
            + "\nProvided Id: " + id
            + "\nProvided graph element type: " + graphElementType
            + "\nActual properties: " + properties
    );
  }

  public static CannotDeserializeArangoDocument becauseAttributesPropertyIsNotCorrectType(
      String id,
      String graphElementType,
      Object uncasedAttributes
  ) {
    return new CannotDeserializeArangoDocument(
        "attributes property is not Map: "
            + "\nProvided Id: " + id
            + "\nProvided graph element type: " + graphElementType
            + "\nActual attributes: " + uncasedAttributes
    );
  }

  public static CannotDeserializeArangoDocument becauseAttributeWasNotList(
      String id,
      String graphElementType,
      String attributeName,
      Object attributeValues
  ) {
    return new CannotDeserializeArangoDocument(
        "attribute was not list: "
            + "\nProvided Id: " + id
            + "\nProvided graph element type: " + graphElementType
            + "\nAttribute name: " + attributeName
            + "\nActual attribute values: " + attributeValues
    );
  }

  public static CannotDeserializeArangoDocument becauseAttributeNameWasNotString(
      String id,
      String graphElementType,
      Object attributeName
  ) {
    return new CannotDeserializeArangoDocument(
        "attribute was not list: "
            + "\nProvided Id: " + id
            + "\nProvided graph element type: " + graphElementType
            + "\nActual attribute name: " + attributeName
    );
  }

  public static CannotDeserializeArangoDocument becauseAttributeVersionWasNotOfCorrectType(
      String id,
      String graphElementType,
      String stringAttributeName,
      Object arangoAttributeVersion
  ) {
    return new CannotDeserializeArangoDocument(
        "attribute version was not of type: " + ArangoAttributeVersion.class.getSimpleName()
            + "\nProvided Id: " + id
            + "\nProvided graph element type: " + graphElementType
            + "\nAttribute name: " + stringAttributeName
            + "\nActual attribute version: " + arangoAttributeVersion
    );
  }

  public static CannotDeserializeArangoDocument becauseAttributeVersionValuesWasNotOfCorrectType(
      String id,
      String graphElementType,
      String stringAttributeName,
      Object arangoAttributeVersion
  ) {
    return new CannotDeserializeArangoDocument(
        "attribute version values was not of type: " + List.class.getSimpleName()
            + "\nProvided Id: " + id
            + "\nProvided graph element type: " + graphElementType
            + "\nAttribute name: " + stringAttributeName
            + "\nActual attribute version: " + arangoAttributeVersion
    );
  }

  public static CannotDeserializeArangoDocument becauseAttributeVersionValueWasNotOfCorrectType(
      String id,
      String graphElementType,
      String stringAttributeName,
      Object arangoAttributeVersionValue
  ) {
    return new CannotDeserializeArangoDocument(
        "attribute version value was not of type: " + Map.class.getSimpleName()
            + "\nProvided Id: " + id
            + "\nProvided graph element type: " + graphElementType
            + "\nAttribute name: " + stringAttributeName
            + "\nActual attribute version value: " + arangoAttributeVersionValue
    );
  }
}
