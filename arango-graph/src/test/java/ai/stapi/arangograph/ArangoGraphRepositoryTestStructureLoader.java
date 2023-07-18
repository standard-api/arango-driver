package ai.stapi.arangograph;

import ai.stapi.schema.adHocLoaders.AbstractJavaModelDefinitionsLoader;
import ai.stapi.schema.scopeProvider.ScopeOptions;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArangoGraphRepositoryTestStructureLoader extends
    AbstractJavaModelDefinitionsLoader<StructureDefinitionData> {

  public static final String SCOPE = "InMemoryGraphRepositoryTest";
  public static final String TAG = ScopeOptions.TEST_TAG;

  protected ArangoGraphRepositoryTestStructureLoader() {
    super(SCOPE, TAG, StructureDefinitionData.SERIALIZATION_TYPE);
  }

  @Override
  public List<StructureDefinitionData> load() {
    return List.of(
        new StructureDefinitionData(
            "test_edge_type",
            "http://example.com/fhir/StructureDefinition/test_edge_type",
            "active",
            "Test Edge Type",
            "complex-type",
            false,
            "TestEdgeType",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "test_edge_type.test_timestamp",
                        List.of(new ElementDefinitionType("dateTime")),
                        1,
                        "1",
                        "Test Timestamp",
                        "Timestamp of the test",
                        null
                    ),
                    new ElementDefinition(
                        "test_edge_type.test_attribute_name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "Test Attribute Name",
                        "Name of the test attribute",
                        null
                    ),
                    new ElementDefinition(
                        "test_edge_type.test_double",
                        List.of(new ElementDefinitionType("decimal")),
                        1,
                        "1",
                        "Test Double",
                        "Double value of the test",
                        null
                    ),
                    new ElementDefinition(
                        "test_edge_type.test_boolean",
                        List.of(new ElementDefinitionType("boolean")),
                        1,
                        "1",
                        "Test Boolean",
                        "Boolean value of the test",
                        null
                    ),
                    new ElementDefinition(
                        "test_edge_type.test_integer",
                        List.of(new ElementDefinitionType("integer")),
                        1,
                        "1",
                        "Test Integer",
                        "Integer value of the test",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "same",
            "http://example.com/StructureDefinition/same",
            "active",
            "A structure with name and alias fields",
            "complex-type",
            false,
            "Same",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "same.name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "The name of the structure",
                        "The name of the structure",
                        null
                    ),
                    new ElementDefinition(
                        "same.alias",
                        List.of(new ElementDefinitionType("string")),
                        0,
                        "*",
                        "An alias for the structure",
                        "An alias for the structure",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "Same",
            "http://example.com/StructureDefinition/Same",
            "active",
            "A structure with name and alias fields",
            "complex-type",
            false,
            "Same",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "Same.name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "The name of the structure",
                        "The name of the structure",
                        null
                    ),
                    new ElementDefinition(
                        "Same.alias",
                        List.of(new ElementDefinitionType("string")),
                        0,
                        "*",
                        "An alias for the structure",
                        "An alias for the structure",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "test_edge",
            "http://example.com/StructureDefinition/test_edge",
            "active",
            "Test Structure Definition for Edge",
            "complex-type",
            false,
            "TestEdge",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "test_edge.name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "Name of the Test Edge",
                        "Name of the Test Edge",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "Type_A",
            "http://example.com/StructureDefinition/Type_A",
            "active",
            "Structure definition for type A",
            "complex-type",
            false,
            "Type_A",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "Type_A.name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "Name of type A",
                        "The name of the type A",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "Test_node",
            "http://example.com/StructureDefinition/Test_node",
            "active",
            "Structure definition for Test_node",
            "complex-type",
            false,
            "Test_node",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "Test_node.name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "Name of Test_node",
                        "The name of the Test_node",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "Test_node_type",
            "http://example.com/fhir/StructureDefinition/Test_node_type",
            "active",
            "Test Node Type",
            "complex-type",
            false,
            "TestNodeType",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "Test_node_type.test_timestamp",
                        List.of(new ElementDefinitionType("dateTime")),
                        1,
                        "1",
                        "Test Timestamp",
                        "Timestamp of the test",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_attribute_name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "Test Attribute Name",
                        "Name of the test attribute",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_double",
                        List.of(new ElementDefinitionType("decimal")),
                        1,
                        "1",
                        "Test Double",
                        "Double value of the test",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_boolean",
                        List.of(new ElementDefinitionType("boolean")),
                        1,
                        "1",
                        "Test Boolean",
                        "Boolean value of the test",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_integer",
                        List.of(new ElementDefinitionType("integer")),
                        1,
                        "1",
                        "Test Integer",
                        "Integer value of the test",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_list_attribute_name",
                        List.of(new ElementDefinitionType("string")),
                        0,
                        "*",
                        "Test List Attribute Name",
                        "Test List Attribute Description",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_set_attribute_name",
                        List.of(new ElementDefinitionType("string")),
                        0,
                        "*",
                        "Test Set Attribute Name",
                        "Test Set Attribute Description",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_union_type_attribute",
                        List.of(
                            new ElementDefinitionType("dateTime"),
                            new ElementDefinitionType("integer"),
                            new ElementDefinitionType("string")
                        ),
                        0,
                        "*",
                        "Test Union Type Attribute",
                        "Test Union Type Attribute Definition",
                        null
                    ),
                    new ElementDefinition(
                        "Test_node_type.test_name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "Test Attribute Name",
                        "Name of the test attribute",
                        null
                    )
                )
            )
        )
    );
  }
}
