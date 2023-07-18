package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.exampleImplementations.ArangoFilterOptionResolverTestStructureLoader;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EqualsFilterOption;
import ai.stapi.objectRenderer.infrastructure.objectToJsonStringRenderer.ObjectToJSonStringOptions;
import ai.stapi.test.schemaintegration.SchemaIntegrationTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(ArangoFilterOptionResolverTestStructureLoader.SCOPE)
public class ArangoGenericSearchOptionResolverTest extends SchemaIntegrationTestCase {

  @Autowired
  private ArangoGenericSearchOptionResolver resolver;

  @Test
  void itShouldResolveByRightResolver() {
    var equalsOptions = new EqualsFilterOption<>("example_attribute_type", "string value");
    var actual = this.resolver.resolve(
        equalsOptions,
        new ArangoSearchResolvingContext(
            "exampleDocumentName",
            ArangoQueryType.NODE,
            "graphElementType",
            "examplePostfix",
            "exampleQueryPostfix"
        )
    );
    this.thenObjectApproved(actual, this.getOptions());
  }

  @Test
  void itShouldAutowireOnlyFilterOptionResolversReturningArangoResolvedFilter() {
    var equalsOptions = new EqualsFilterOption<>("example_attribute_type", "string value");
    Executable executable = () -> this.resolver.resolve(
        equalsOptions,
        new ArangoSearchResolvingContext("", ArangoQueryType.NODE, "graphElementType")
    );
    Assertions.assertDoesNotThrow(executable);
  }

  private ObjectToJSonStringOptions getOptions() {
    return new ObjectToJSonStringOptions(
        ObjectToJSonStringOptions.RenderFeature.SORT_FIELDS,
        ObjectToJSonStringOptions.RenderFeature.RENDER_GETTERS
    );
  }
}
