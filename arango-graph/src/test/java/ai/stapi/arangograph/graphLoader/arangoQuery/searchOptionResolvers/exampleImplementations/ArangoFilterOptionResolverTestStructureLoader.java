package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.exampleImplementations;

import ai.stapi.schema.adHocLoaders.AbstractJavaModelDefinitionsLoader;
import ai.stapi.schema.scopeProvider.ScopeOptions;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArangoFilterOptionResolverTestStructureLoader
    extends AbstractJavaModelDefinitionsLoader<StructureDefinitionData> {

  public static final String SCOPE = "ArangoFilterOptionResolverTest";
  public static final String TAG = ScopeOptions.TEST_TAG;

  public ArangoFilterOptionResolverTestStructureLoader() {
    super(SCOPE, TAG, StructureDefinitionData.SERIALIZATION_TYPE);
  }

  @Override
  public List<StructureDefinitionData> load() {
    return List.of(
        new StructureDefinitionData(
            "graphElementType",
            "http://example.com/fhir/StructureDefinition/graphElementType",
            "active",
            "Example graphElementType",
            "complex-type",
            false,
            "graphElementType",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "graphElementType.example_attribute_type",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "A string attribute of the test node",
                        null,
                        null
                    ),
                    new ElementDefinition(
                        "graphElementType.example_attribute_1",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "A string attribute of the test node",
                        null,
                        null
                    ),
                    new ElementDefinition(
                        "graphElementType.example_attribute_2",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "A string attribute of the test node",
                        null,
                        null
                    )
                )
            )
        )
    );
  }
}
