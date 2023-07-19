package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.schema.adHocLoaders.AbstractJavaModelDefinitionsLoader;
import ai.stapi.schema.scopeProvider.ScopeOptions;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArangoQueryBuilderTestStructureLoader
    extends AbstractJavaModelDefinitionsLoader<StructureDefinitionData> {

  public static final String SCOPE = "ArangoQueryBuilderTest";
  public static final String TAG = ScopeOptions.TEST_TAG;

  protected ArangoQueryBuilderTestStructureLoader() {
    super(SCOPE, TAG, StructureDefinitionData.SERIALIZATION_TYPE);
  }

  @Override
  public List<StructureDefinitionData> load() {
    return List.of(
        new StructureDefinitionData(
            "example_node_type",
            "http://example.com/StructureDefinition/example_node_type",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "ExampleNodeType",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_node_type.example_sorting_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "Example sorting attribute",
                        "A sorting attribute for the example node type",
                        null
                    ),
                    new ElementDefinition(
                        "example_node_type.example_attribute_name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    ),
                    new ElementDefinition(
                        "example_node_type.exampleName",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example name",
                        "A name for the example node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_start_node_type",
            "http://example.com/fhir/StructureDefinition/example_start_node_type",
            "active",
            "An example FHIR structure definition for a starting node type",
            "complex-type",
            false,
            "ExampleStartNodeType",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "ExampleStartNodeType.some_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "A string attribute for the starting node type",
                        "A string attribute for the starting node type",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleStartNodeType.example_edge_type",
                        List.of(new ElementDefinitionType("example_other_node_type")),
                        0,
                        "*",
                        "A list of edges to other nodes",
                        "A list of edges to other nodes",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleStartNodeType.isExample",
                        List.of(new ElementDefinitionType("boolean")),
                        0,
                        "*",
                        "boolean attribute",
                        "boolean attribute",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleStartNodeType.quantity",
                        List.of(new ElementDefinitionType("integer")),
                        0,
                        "*",
                        "quantity attribute",
                        "quantity attribute",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleStartNodeType.example_edge_type_1",
                        List.of(new ElementDefinitionType("example_node_from_1")),
                        0,
                        "*",
                        "A list of edges to other nodes",
                        "A list of edges to other nodes",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleStartNodeType.example_edge_type_2",
                        List.of(new ElementDefinitionType("example_node_from_2")),
                        0,
                        "*",
                        "A list of edges to other nodes",
                        "A list of edges to other nodes",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleStartNodeType.example_edge_type_3",
                        List.of(new ElementDefinitionType("example_node_from_3")),
                        0,
                        "*",
                        "A list of edges to other nodes",
                        "A list of edges to other nodes",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_other_node_type",
            "http://example.com/StructureDefinition/example_other_node_type",
            "active",
            "An example structure definition for a custom node type",
            "complex-type",
            false,
            "ExampleOtherNodeType",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "ExampleOtherNodeType.example_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "An example attribute for the custom node type",
                        "An example attribute for the custom node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_node_from_1",
            "http://example.com/StructureDefinition/example_node_from_1",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_node_from_1",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_node_from_1.some_node_from_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_node_from_2",
            "http://example.com/StructureDefinition/example_node_from_2",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_node_from_2",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_node_from_2.some_node_from_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_node_from_3",
            "http://example.com/StructureDefinition/example_node_from_3",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_node_from_3",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_node_from_3.some_node_from_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_main_edge_type",
            "http://example.com/StructureDefinition/example_main_edge_type",
            "active",
            "An example structure definition for a edge type",
            "complex-type",
            false,
            "example_main_edge_type",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_main_edge_type.example_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example edge type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_edge_type_2_2",
            "http://example.com/StructureDefinition/example_edge_type_2_2",
            "active",
            "An example structure definition for a edge type",
            "complex-type",
            false,
            "example_edge_type_2_2",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_edge_type_2_2.some_edge_deeper_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example edge type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_edge_type_3",
            "http://example.com/StructureDefinition/example_edge_type_3",
            "active",
            "An example structure definition for a edge type",
            "complex-type",
            false,
            "example_edge_type_3",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_edge_type_3.example_edge_attribute_name_3",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example edge type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_node_to",
            "http://example.com/StructureDefinition/example_node_to",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_node_to",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_node_to.some_node_to_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    ),
                    new ElementDefinition(
                        "example_node_to.example_second_edge",
                        List.of(new ElementDefinitionType("example_second_node_to")),
                        1,
                        "*",
                        "Example field which leads to another node",
                        "Example field which leads to another node",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_node_to_1",
            "http://example.com/StructureDefinition/example_node_to_1",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_node_to_1",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_node_to_1.some_node_to_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    ),
                    new ElementDefinition(
                        "example_node_to_1.example_second_edge",
                        List.of(new ElementDefinitionType("example_second_node_to")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_node_to_2",
            "http://example.com/StructureDefinition/example_node_to_2",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_node_to_2",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_node_to_2.example_edge_type_2_2",
                        List.of(new ElementDefinitionType("example_deeper_node")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    ),
                    new ElementDefinition(
                        "example_node_to_2.some_other_node_to_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_deeper_node",
            "http://example.com/StructureDefinition/example_deeper_node",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_deeper_node",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_deeper_node.some_node_deeper_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "example_second_node_to",
            "http://example.com/StructureDefinition/example_second_node_to",
            "active",
            "An example structure definition for a node type",
            "complex-type",
            false,
            "example_second_node_to",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "example_second_node_to.some_node_to_2_attribute",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Example attribute name",
                        "An attribute name for the example node type",
                        null
                    )
                )
            )
        )
    );
  }
}
