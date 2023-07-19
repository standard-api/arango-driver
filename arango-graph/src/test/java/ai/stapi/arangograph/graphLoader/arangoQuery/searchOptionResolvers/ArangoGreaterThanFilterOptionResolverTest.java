package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.exampleImplementations.ArangoFilterOptionResolverTestStructureLoader;
import ai.stapi.graphoperations.graphLoader.search.filterOption.GreaterThanFilterOption;
import ai.stapi.objectRenderer.infrastructure.objectToJsonStringRenderer.ObjectToJSonStringOptions;
import ai.stapi.test.schemaintegration.SchemaIntegrationTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(ArangoFilterOptionResolverTestStructureLoader.SCOPE)
public class ArangoGreaterThanFilterOptionResolverTest extends SchemaIntegrationTestCase {

  @Autowired
  private ArangoGreaterThanFilterOptionResolver resolver;

  @Test
  public void itShouldSupportGreaterThanFilterOption() {
    var greaterThanOptions = new GreaterThanFilterOption<>("irrelevant", 2);
    var resolver = this.resolver;
    Assertions.assertTrue(
        resolver.supports(greaterThanOptions),
        "GreaterThanFilterOptionResolver should support GreaterThanFilterOption."
    );
  }

  @Test
  public void itShouldResolveOptionWithIntegerValue() {
    var greaterThanOptions = new GreaterThanFilterOption<>("example_attribute_type", 2);
    var actual = this.resolver.resolve(
        greaterThanOptions,
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
  public void itShouldResolveOptionWithFloatValue() {
    var greaterThanOptions = new GreaterThanFilterOption<>("example_attribute_type", 2.3f);
    var actual = this.resolver.resolve(
        greaterThanOptions,
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

  private ObjectToJSonStringOptions getOptions() {
    return new ObjectToJSonStringOptions(
        ObjectToJSonStringOptions.RenderFeature.SORT_FIELDS,
        ObjectToJSonStringOptions.RenderFeature.RENDER_GETTERS
    );
  }

}
