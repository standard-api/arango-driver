package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.exampleImplementations.ArangoFilterOptionResolverTestStructureLoader;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AndFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EqualsFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.GreaterThanFilterOption;
import ai.stapi.objectRenderer.infrastructure.objectToJsonStringRenderer.ObjectToJSonStringOptions;
import ai.stapi.test.schemaintegration.SchemaIntegrationTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(ArangoFilterOptionResolverTestStructureLoader.SCOPE)
public class ArangoAndFilterOptionResolverTest extends SchemaIntegrationTestCase {

  @Autowired
  private ArangoAndFilterOptionResolver resolver;

  @Test
  public void itShouldSupportAndFilterOption() {
    var fitler1 = new EqualsFilterOption<>("example_attribute_1", "Example Equal Value");
    var fitler2 = new GreaterThanFilterOption<>("example_attribute_2", 5);
    var andFilter = new AndFilterOption(List.of(fitler1, fitler2));
    var resolver = this.resolver;
    Assertions.assertTrue(
        resolver.supports(andFilter),
        "AndFilterOptionResolver should support AndFilterOption."
    );
  }

  @Test
  public void itShouldResolveOptionOfOne() {
    var fitler1 = new EqualsFilterOption<>("example_attribute_1", "Example Equal Value");
    var andFilter = new AndFilterOption(List.of(fitler1));
    var resolved = this.resolver.resolve(
        andFilter,
        new ArangoSearchResolvingContext("exampleDocumentName", ArangoQueryType.NODE,
            "graphElementType")
    );
    this.thenObjectApproved(resolved, this.getOptions());
  }

  @Test
  public void itShouldResolveOptionOfTwo() {
    var fitler1 = new EqualsFilterOption<>("example_attribute_1", "Example Equal Value");
    var fitler2 = new GreaterThanFilterOption<>("example_attribute_2", 5);
    var andFilter = new AndFilterOption(List.of(fitler1, fitler2));
    var resolved = this.resolver.resolve(
        andFilter,
        new ArangoSearchResolvingContext("exampleDocumentName", ArangoQueryType.NODE,
            "graphElementType")
    );
    this.thenObjectApproved(resolved, this.getOptions());
  }

  private ObjectToJSonStringOptions getOptions() {
    return new ObjectToJSonStringOptions(
        ObjectToJSonStringOptions.RenderFeature.SORT_FIELDS,
        ObjectToJSonStringOptions.RenderFeature.RENDER_GETTERS
    );
  }
}
