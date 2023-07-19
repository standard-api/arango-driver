package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.aqlFormatter.AqlFormatter;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.exceptions.CannotBuildArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.arangograph.graphLoader.fixtures.AttributeTypes;
import ai.stapi.arangograph.graphLoader.fixtures.testsystem.TestSystemModelDefinitionsLoader;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.EdgeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.IngoingEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.OutgoingEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.UuidIdentityDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.AttributeQueryDescription;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.graphoperations.graphLoader.search.exceptions.SearchOptionNotSupportedByExactlyOneResolver;
import ai.stapi.graphoperations.graphLoader.search.filterOption.ContainsFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EqualsFilterOption;
import ai.stapi.graphoperations.graphLoader.search.paginationOption.OffsetPaginationOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.AscendingSortOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.DescendingSortOption;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.objectRenderer.infrastructure.objectToJsonStringRenderer.ObjectToJSonStringOptions;
import ai.stapi.schema.structureSchema.AbstractStructureType;
import ai.stapi.schema.structureSchema.FieldDefinition;
import ai.stapi.schema.structureSchema.ResourceStructureType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import ai.stapi.test.schemaintegration.SchemaIntegrationTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope({
    ArangoQueryBuilderTestStructureLoader.SCOPE,
    TestSystemModelDefinitionsLoader.SCOPE
})
class ArangoQueryBuilderTest extends SchemaIntegrationTestCase {

  @Autowired
  private ArangoQueryBuilderProvider builderProvider;

  @Test
  void itShouldThrowExceptionWhenNoMainQuerySet() {
    var builder = this.builderProvider.provide();
    Executable throwable = () -> builder.build(GraphLoaderReturnType.GRAPH);
    this.thenExceptionMessageApproved(CannotBuildArangoQuery.class, throwable);
  }

  @Test
  void itShouldReturnDefaultFindQueryWhenNothingSet() {
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type");

    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryWithOneSortOption() {
    var sortOption = new AscendingSortOption(AttributeTypes.EXAMPLE_NAME);
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type").setSearchOptions().addSortOption(sortOption);
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryWithTwoSortOption() {
    var sortOption1 = new AscendingSortOption(AttributeTypes.EXAMPLE_NAME);
    var sortOption2 = new DescendingSortOption(AttributeTypes.EXAMPLE_QUANTITY);
    var builder = this.builderProvider.provide();
    builder
        .setFindNodesMainQuery("example_node_type")
        .setSearchOptions()
        .addSortOption(sortOption1)
        .addSortOption(sortOption2);
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldThrowErrorWhenAddingFilterOptionsNotSupportedByAnySetResolver() {
    var givenBuilder = this.getEmptyBuilder();
    Executable executable =
        () -> givenBuilder.setFindNodesMainQuery("example_node_type").setSearchOptions()
            .addFilterOption(new EqualsFilterOption<>(
                "irrelevant",
                "irrelevant"
            ));
    Assertions.assertThrows(
        SearchOptionNotSupportedByExactlyOneResolver.class,
        executable
    );
  }

  @Test
  void itShouldReturnQueryWithOneFilterOption() {
    var filterOption = new EqualsFilterOption<>(
        AttributeTypes.EXAMPLE_NAME,
        "Example Equal Filter Value"
    );
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type").setSearchOptions()
        .addFilterOption(filterOption);
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryWithMoreFilterOption() {
    var filterOption1 = new EqualsFilterOption<>(
        AttributeTypes.EXAMPLE_NAME,
        "Example Equal Filter Value"
    );
    var filterOption2 = new EqualsFilterOption<>(
        AttributeTypes.EXAMPLE_NAME,
        "Example Equal Name"
    );
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type")
        .setSearchOptions()
        .addFilterOption(filterOption1)
        .addFilterOption(filterOption2);

    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldThrowErrorWhenSetPaginationNotSupportedByAnyResolver() {
    var givenBuilder = this.getEmptyBuilder();
    Executable executable = () -> givenBuilder.setFindNodesMainQuery("example_node_type")
        .setSearchOptions()
        .setPaginationOption(new OffsetPaginationOption(
            5,
            10
        ));
    Assertions.assertThrows(
        SearchOptionNotSupportedByExactlyOneResolver.class,
        executable
    );
  }

  @Test
  void itShouldReturnQueryWithPaginationOption() {
    var paginationOption = new OffsetPaginationOption(
        3,
        10
    );
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type").setSearchOptions()
        .setPaginationOption(paginationOption);
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryWithFilterSortAndPagingOption() {
    var paginationOption = new OffsetPaginationOption(
        3,
        10
    );
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type")
        .setSearchOptions()
        .addSortOption(new AscendingSortOption("example_sorting_attribute"))
        .setPaginationOption(paginationOption)
        .addFilterOption(new EqualsFilterOption<>(
            "example_attribute_name",
            "example value"
        ));

    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryWithIdFilterOption() {
    var filterOption = new EqualsFilterOption<>(
        new UuidIdentityDescription(),
        "ExampleId"
    );
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type").setSearchOptions()
        .addFilterOption(filterOption);
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryWithGraphTraversalWithOnlyEdgeSpecified() {
    var builder = this.builderProvider.provide();
    builder.setFindNodesMainQuery("example_node_type").joinGraphTraversal(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type")
        )
    );

    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryContainingGraphTraversalWithOtherNodeSpecified() {
    var builder = this.builderProvider.provide();
    var graphTraversalBuilder =
        builder.setFindNodesMainQuery("example_start_node_type").joinGraphTraversal(
            new OutgoingEdgeDescription(
                new EdgeDescriptionParameters("example_edge_type")
            )
        );
    graphTraversalBuilder.setOtherNode("example_other_node_type");
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnComplexFindQuery() {
    var builder = this.builderProvider.provide();
    var mainQuery = builder.setFindNodesMainQuery("example_start_node_type");

    mainQuery.setSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_attribute", "some_value"))
        .addSortOption(new AscendingSortOption("some_attribute"))
        .setPaginationOption(new OffsetPaginationOption(5, 100));

    mainQuery.setKeptAttributes()
        .addAttribute("some_attribute")
        .addAttribute("isExample")
        .addAttribute("quantity");

    var firstJoin = mainQuery.joinGraphTraversal(
        new IngoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type_1")
        )
    );

    firstJoin.setOtherNode("example_node_from_1").setKeptAttributes().keepAllAttributes();
    firstJoin.setNodeSearchOptions().addFilterOption(
        new EqualsFilterOption<>("some_node_from_attribute", "someValue")
    );

    var secondJoin = mainQuery.joinGraphTraversal(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type_2")
        )
    );

    var deeperJoin = secondJoin.setOtherNode("example_node_to_2")
        .joinGraphTraversal(
            new OutgoingEdgeDescription(new EdgeDescriptionParameters("example_edge_type_2_2"))
        );

    deeperJoin.setEdgeKeptAttributes()
        .addAttribute("some_edge_deeper_attribute");

    deeperJoin.setEdgeSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_edge_deeper_attribute", "someValue"));

    deeperJoin.setOtherNode("example_deeper_node").setKeptAttributes()
        .addAttribute("some_node_deeper_attribute");

    deeperJoin.setNodeSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_node_deeper_attribute", "someValue"));

    var thirdJoin = mainQuery.joinGraphTraversal(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type_3")
        )
    );

    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnDefaultGetQueryWhenNothingSet() {
    var builder = this.builderProvider.provide();
    builder.setGetNodeMainQuery(
        "example_node_type",
        UniversallyUniqueIdentifier.fromString("576a533b-5764-401b-876c-9df37461bf90")
    );
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnGetEdgeQueryWithJoins() {
    var builder = this.builderProvider.provide();
    var mainQuery = builder.setGetOutgoingEdgeMainQuery(
        "example_main_edge_type",
        UniversallyUniqueIdentifier.fromString("576a533b-5764-401b-876c-9df37461bf90")
    );

    mainQuery.setKeptAttributes().addAttribute("example_attribute");

    var example_node_to = mainQuery.joinNodeGetSubQuery("example_node_to");
    example_node_to.setKeptAttributes().keepAllAttributes();
    example_node_to.joinGraphTraversal(
        new OutgoingEdgeDescription(new EdgeDescriptionParameters("example_second_edge"))
    ).setOtherNode("example_second_node_to");
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnComplexMappedObjectFindQuery() {
    var builder = this.builderProvider.provide();
    var mainQuery = builder.setFindNodesMainQuery("example_start_node_type");

    mainQuery.setSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_attribute", "some_value"))
        .addSortOption(new AscendingSortOption("some_attribute"))
        .setPaginationOption(new OffsetPaginationOption(5, 100));

    mainQuery.setMappedScalars()
        .mapAttribute("someAttribute", "some_attribute")
        .mapAttribute("isExample", "isExample")
        .mapAttribute("quantity", "quantity");

    var firstJoin = mainQuery.mapGraphTraversalAsConnections(
        "hasFirstNodes",
        new IngoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type_1")
        )
    );

    firstJoin.mapOtherNode("example_node_from_1")
        .setMappedScalars().mapAttribute("firstNodeAttributeName", "some_node_from_attribute");
    firstJoin.setNodeSearchOptions().addFilterOption(
        new EqualsFilterOption<>("some_node_from_attribute", "someValue")
    );

    var secondJoin = mainQuery.mapGraphTraversalAsConnections(
        "hasSecondNodes",
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type_2")
        )
    );

    var deeperJoin = secondJoin.mapOtherNode(
            "example_node_to_2"
        )
        .mapGraphTraversalAsConnections(
            "hasDeeperNodes",
            new OutgoingEdgeDescription(new EdgeDescriptionParameters("example_edge_type_2_2"))
        );

    deeperJoin.setEdgeMappedScalars()
        .mapAttribute("someEdgeDeeperAttribute", "some_edge_deeper_attribute");

    deeperJoin.setEdgeSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_edge_deeper_attribute", "someValue"));

    deeperJoin.mapOtherNode("example_deeper_node").setMappedScalars()
        .mapAttribute("someNodeDeeperAttribute", "some_node_deeper_attribute");

    deeperJoin.setNodeSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_node_deeper_attribute", "someValue"));

    var thirdJoin = mainQuery.mapGraphTraversalAsConnections(
            "hasThirdNodes",
            new OutgoingEdgeDescription(
                new EdgeDescriptionParameters("example_edge_type_3")
            )
        ).setEdgeMappedScalars()
        .mapAttribute("someThirdEdgeAttribute", "example_edge_attribute_name_3");

    var actualQuery = builder.build(GraphLoaderReturnType.OBJECT);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnGetEdgeMappedObject() {
    var builder = this.builderProvider.provide();
    var mainQuery = builder.setGetOutgoingEdgeMainQuery(
        "example_main_edge_type",
        UniversallyUniqueIdentifier.fromString("576a533b-5764-401b-876c-9df37461bf90")
    );

    mainQuery.setMappedScalars().mapAttribute("exampleAttribute", "example_attribute");

    var example_node_to = mainQuery.mapNodeGetSubQuery(
        "example_node_to"
    );
    example_node_to.setMappedScalars()
        .mapAttribute("someNodeToAttribute", "some_node_to_attribute");
    example_node_to.mapGraphTraversalAsConnections(
            "hasSecondNodeTo",
            new OutgoingEdgeDescription(new EdgeDescriptionParameters("example_second_edge"))
        ).mapOtherNode("example_second_node_to")
        .setMappedScalars()
        .mapAttribute("someNodeTo2Attribute", "some_node_to_2_attribute");
    var actualQuery = builder.build(GraphLoaderReturnType.OBJECT);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnFindEdgeMappedObject() {
    var builder = this.builderProvider.provide();
    var mainQuery = builder.setFindOutgoingEdgeMainQuery("example_main_edge_type");

    mainQuery.setMappedScalars().mapAttribute("exampleAttribute", "example_attribute");

    var example_node_to = mainQuery.mapNodeGetSubQuery(
        "example_node_to"
    );
    example_node_to.setMappedScalars()
        .mapAttribute("someNodeToAttribute", "some_node_to_attribute");
    example_node_to.mapGraphTraversalAsConnections(
            "hasSecondNodeTo",
            new OutgoingEdgeDescription(new EdgeDescriptionParameters("example_second_edge"))
        ).mapOtherNode("example_second_node_to")
        .setMappedScalars()
        .mapAttribute("someNodeTo2Attribute", "some_node_to_2_attribute");
    var actualQuery = builder.build(GraphLoaderReturnType.OBJECT);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnFindEdgeWithTwoNodeOptionsAndReturnMappedObjectAndGraph() {
    var builder = this.builderProvider.provide();
    var mainQuery = builder.setFindOutgoingEdgeMainQuery("example_main_edge_type");

    mainQuery.setMappedScalars().mapAttribute("exampleAttribute", "example_attribute");

    var exampleNodeTo1 = mainQuery.mapNodeGetSubQuery(
        "example_node_to_1"
    );
    exampleNodeTo1.setMappedScalars().mapAttribute("someNodeToAttribute", "some_node_to_attribute");
    exampleNodeTo1.mapGraphTraversalAsConnections(
            "hasSecondNodeTo",
            new OutgoingEdgeDescription(new EdgeDescriptionParameters("example_second_edge"))
        ).mapOtherNode("example_second_node_to")
        .setMappedScalars()
        .mapAttribute("someNodeTo2Attribute", "some_node_to_2_attribute");
    var exampleNodeTo2 = mainQuery.mapNodeGetSubQuery(
        "example_node_to_2"
    );
    exampleNodeTo2.setMappedScalars()
        .mapAttribute("someOtherNodeToAttribute", "some_other_node_to_attribute");
    var actualQuery = builder.build(GraphLoaderReturnType.OBJECT, GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnComplexMappedObjectAndGraphFindQuery() {
    var builder = this.builderProvider.provide();
    var mainQuery = builder.setFindNodesMainQuery("example_start_node_type");

    mainQuery.setSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_attribute", "some_value"))
        .addSortOption(new AscendingSortOption("some_attribute"))
        .setPaginationOption(new OffsetPaginationOption(5, 100));

    mainQuery.setMappedScalars()
        .mapAttribute("someAttribute", "some_attribute")
        .mapAttribute("isExample", "isExample")
        .mapAttribute("quantity", "quantity");

    var firstJoin = mainQuery.mapGraphTraversalAsConnections(
        "hasFirstNodes",
        new IngoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type_1")
        )
    );

    firstJoin.mapOtherNode("example_node_from_1")
        .setMappedScalars().mapAttribute("firstNodeAttributeName", "some_node_from_attribute");
    firstJoin.setNodeSearchOptions().addFilterOption(
        new EqualsFilterOption<>("some_node_from_attribute", "someValue")
    );

    var secondJoin = mainQuery.mapGraphTraversalAsConnections(
        "hasSecondNodes",
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type_2")
        )
    );

    var deeperJoin = secondJoin.mapOtherNode(
            "example_node_to_2"
        )
        .mapGraphTraversalAsConnections(
            "hasDeeperNodes",
            new OutgoingEdgeDescription(new EdgeDescriptionParameters("example_edge_type_2_2"))
        );

    deeperJoin.setEdgeMappedScalars()
        .mapAttribute("someEdgeDeeperAttribute", "some_edge_deeper_attribute");

    deeperJoin.setEdgeSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_edge_deeper_attribute", "someValue"));

    deeperJoin.mapOtherNode("example_deeper_node").setMappedScalars()
        .mapAttribute("someNodeDeeperAttribute", "some_node_deeper_attribute");

    deeperJoin.setNodeSearchOptions()
        .addFilterOption(new ContainsFilterOption("some_node_deeper_attribute", "someValue"));

    var thirdJoin = mainQuery.mapGraphTraversalAsConnections(
            "hasThirdNodes",
            new OutgoingEdgeDescription(
                new EdgeDescriptionParameters("example_edge_type_3")
            )
        ).setEdgeMappedScalars()
        .mapAttribute("someThirdEdgeAttribute", "example_edge_attribute_name_3");

    var actualQuery = builder.build(
        GraphLoaderReturnType.OBJECT,
        GraphLoaderReturnType.GRAPH
    );
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryContainingDeepSort() {
    var deepSort = new AscendingSortOption(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type"),
            new NodeDescription(
                new NodeDescriptionParameters("example_other_node_type"),
                new AttributeQueryDescription("example_attribute")
            )
        )
    );
    var builder = this.builderProvider.provide();
    var startNodeBuilder = builder.setFindNodesMainQuery("example_start_node_type");
    startNodeBuilder.setSearchOptions().addSortOption(deepSort);
    var graphTraversalBuilder = startNodeBuilder.joinGraphTraversal(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type")
        )
    );
    var otherNodeBuilder = graphTraversalBuilder.setOtherNode("example_other_node_type");
    otherNodeBuilder.setKeptAttributes().addAttribute("example_attribute");
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryContainingDeepFilter() {
    var deepFilter = new EqualsFilterOption<>(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type"),
            new NodeDescription(
                new NodeDescriptionParameters("example_other_node_type"),
                new AttributeQueryDescription("example_attribute")
            )
        ),
        "Example Value"
    );
    var builder = this.builderProvider.provide();
    var startNodeBuilder = builder.setFindNodesMainQuery("example_start_node_type");
    startNodeBuilder.setSearchOptions().addFilterOption(deepFilter);
    var graphTraversalBuilder = startNodeBuilder.joinGraphTraversal(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type")
        )
    );
    var otherNodeBuilder = graphTraversalBuilder.setOtherNode("example_other_node_type");
    otherNodeBuilder.setKeptAttributes().addAttribute("example_attribute");
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  @Test
  void itShouldReturnQueryContainingDeepIdFilter() {
    var deepFilter = new EqualsFilterOption<>(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type"),
            new NodeDescription(
                new NodeDescriptionParameters("example_other_node_type"),
                new UuidIdentityDescription()
            )
        ),
        "ExampleId"
    );
    var builder = this.builderProvider.provide();
    var startNodeBuilder = builder.setFindNodesMainQuery("example_start_node_type");
    startNodeBuilder.setSearchOptions().addFilterOption(deepFilter);
    var graphTraversalBuilder = startNodeBuilder.joinGraphTraversal(
        new OutgoingEdgeDescription(
            new EdgeDescriptionParameters("example_edge_type")
        )
    );
    var otherNodeBuilder = graphTraversalBuilder.setOtherNode("example_other_node_type");
    otherNodeBuilder.setKeptAttributes().addAttribute("example_attribute");
    var actualQuery = builder.build(GraphLoaderReturnType.GRAPH);
    this.thenArangoQueryApproved(actualQuery);
  }

  private ArangoQueryBuilder getEmptyBuilder() {
    return new ArangoQueryBuilder(
        new ArangoGenericSearchOptionResolver(List.of()),
        new StructureSchemaFinder() {

          @Override
          public boolean isList(String serializationType, String fieldName) {
            return false;
          }

          @Override
          public boolean containsType(String rootType, String typeToBeFound) {
            return false;
          }

          @Override
          public AbstractStructureType getStructureType(String serializationType) {
            return null;
          }

          @Override
          public Map<String, FieldDefinition> getFieldDefinitionsFor(String serializationType,
              List<String> fieldNames) {
            return null;
          }

          @Override
          public FieldDefinition getFieldDefinitionFor(String serializationType, String fieldName) {
            return null;
          }

          @Override
          public Map<String, FieldDefinition> getAllFieldDefinitionsFor(String serializationType) {
            return null;
          }

          @NotNull
          @Override
          public HashMap<String, FieldDefinition> getFieldDefinitionHashMap(
              Map<?, ?> attributes,
              String serializationType
          ) {
            return null;
          }

          @Override
          public boolean isResource(String serializationType) {
            return false;
          }

          @Override
          public boolean structureExists(String serializationType) {
            return false;
          }

          @Override
          public AbstractStructureType getSchemaStructure(String serializationType) {
            return null;
          }

          @Override
          public FieldDefinition getFieldDefinitionOrFallback(String serializationType,
              String fieldName) {
            return null;
          }

          @Override
          public List<ResourceStructureType> getAllResources() {
            return null;
          }

          @Override
          public boolean inherits(String serializationType, String inheritedSerializationType) {
            return false;
          }

          @Override
          public boolean isEqualOrInherits(String serializationType,
              String inheritedSerializationType) {
            return false;
          }
        }
    );
  }

  private void thenArangoQueryApproved(ArangoQuery query) {
    var aqlFormatter = new AqlFormatter();
    var formattedQueryString = aqlFormatter.format(query.getQueryString());
    this.thenObjectApproved(
        new ArangoQuery(new AqlVariable(formattedQueryString), query.getBindParameters()),
        new ObjectToJSonStringOptions(
            ObjectToJSonStringOptions.RenderFeature.SORT_FIELDS,
            ObjectToJSonStringOptions.RenderFeature.RENDER_GETTERS
        )
    );
  }

}
