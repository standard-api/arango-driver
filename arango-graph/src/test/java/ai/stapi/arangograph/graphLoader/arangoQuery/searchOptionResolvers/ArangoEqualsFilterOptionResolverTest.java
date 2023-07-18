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
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(ArangoFilterOptionResolverTestStructureLoader.SCOPE)
public class ArangoEqualsFilterOptionResolverTest extends SchemaIntegrationTestCase {

  @Autowired
  private ArangoEqualsFilterOptionResolver resolver;


  @Test
  public void itShouldSupportEqualsFilterOption() {
    var equalsOptions = new EqualsFilterOption<>("irrelevant", "irrelevant");
    var resolver = this.resolver;
    Assertions.assertTrue(
        resolver.supports(equalsOptions),
        "EqualsFilterOptionResolver should support EqualsFilterOption."
    );
  }

  @Test
  public void itShouldResolveOptionWithStringValue() {
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
    this.thenObjectApproved(actual, getOptions());
  }

  @Test
  public void itShouldResolveOptionWithBooleanValue() {
    var equalsOptions = new EqualsFilterOption<>("example_attribute_type", true);
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
    this.thenObjectApproved(actual, getOptions());
  }

  private ObjectToJSonStringOptions getOptions() {
    return new ObjectToJSonStringOptions(
        ObjectToJSonStringOptions.RenderFeature.SORT_FIELDS,
        ObjectToJSonStringOptions.RenderFeature.RENDER_GETTERS
    );
  }
}
