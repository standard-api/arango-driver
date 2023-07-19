package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoGraphTraversalNodeOptionBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoGraphTraversalSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractNodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.GraphElementQueryDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.NodeQueryGraphDescription;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectFieldDefinition;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectObjectGraphMapping;
import java.util.Map;

public class ArangoGraphTraversalSubQueryResolver extends AbstractArangoSubQueryResolver {

  public ArangoGraphTraversalSubQueryResolver(GenericSubQueryResolver genericSubQueryResolver) {
    super(genericSubQueryResolver);
  }

  @Override
  public boolean supports(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    return builder instanceof ArangoGraphTraversalSubQueryBuilder
        && graphDescription instanceof AbstractEdgeDescription;
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    var graphTraversalSubQueryBuilder = (ArangoGraphTraversalSubQueryBuilder) builder;
    var edgeDescription = (AbstractEdgeDescription) graphDescription;
    this.resolveAttributes(edgeDescription, graphTraversalSubQueryBuilder.setEdgeKeptAttributes());
    if (edgeDescription instanceof GraphElementQueryDescription graphElementQueryDescription) {
      this.resolveSearchOptions(
          graphElementQueryDescription.getSearchQueryParameters(),
          graphTraversalSubQueryBuilder.setEdgeSearchOptions()
      );
    }
    this.resolveJoinToOtherNodes(edgeDescription, graphTraversalSubQueryBuilder);
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, ObjectGraphMapping objectGraphMapping) {
    var objectOGM = (ObjectObjectGraphMapping) objectGraphMapping;
    var subQueryBuilder = (ArangoGraphTraversalSubQueryBuilder) builder;
    var graphDescription = objectOGM.getGraphDescription();
    if (graphDescription instanceof GraphElementQueryDescription graphElementQueryDescription) {
      this.resolveSearchOptions(
          graphElementQueryDescription.getSearchQueryParameters(),
          subQueryBuilder.setEdgeSearchOptions()
      );
    }
    this.resolveMappedAttributes(objectOGM.getFields(), subQueryBuilder.setEdgeMappedScalars());
    this.mapToOtherNode(
        objectOGM.getFields(),
        subQueryBuilder
    );
  }

  private void resolveJoinToOtherNodes(
      AbstractEdgeDescription edgeDescription,
      ArangoGraphTraversalSubQueryBuilder graphTraversalSubQueryBuilder
  ) {
    var nodeDescriptions = this.getChildNodeDescriptions(edgeDescription);
    nodeDescriptions.forEach(nodeDescription -> {
      var nodeParameters = (NodeDescriptionParameters) nodeDescription.getParameters();
      var nodeOptionBuilder =
          graphTraversalSubQueryBuilder.setOtherNode(nodeParameters.getNodeType());
      this.resolveAttributes(nodeDescription, nodeOptionBuilder.setKeptAttributes());
      if (nodeDescription instanceof NodeQueryGraphDescription nodeQueryGraphDescription) {
        this.resolveSearchOptionsWithoutPagination(
            nodeQueryGraphDescription.getSearchQueryParameters(),
            graphTraversalSubQueryBuilder.setNodeSearchOptions()
        );
      }
      this.resolveGraphTraversalJoins(nodeDescription, nodeOptionBuilder);
    });
  }

  private void mapToOtherNode(
      Map<String, ObjectFieldDefinition> fieldDefinitionMap,
      ArangoGraphTraversalSubQueryBuilder graphTraversalSubQueryBuilder
  ) {
    var nodeFields = fieldDefinitionMap.entrySet()
        .stream()
        .filter(
            entry -> {
              var fieldOGM = entry.getValue().getFieldObjectGraphMapping();
              if (fieldOGM instanceof ObjectObjectGraphMapping) {
                return fieldOGM.getGraphDescription() instanceof AbstractNodeDescription;
              }
              return false;
            }
        ).toList();

    nodeFields.forEach(nodeField -> {
      var fieldObjectGraphMapping = nodeField.getValue().getFieldObjectGraphMapping();
      var graphDescription = fieldObjectGraphMapping.getGraphDescription();
      var nodeParams = (NodeDescriptionParameters) graphDescription.getParameters();
      var nodeOptionBuilder = graphTraversalSubQueryBuilder.mapOtherNode(
          nodeParams.getNodeType()
      );
      this.mapGraphTraversalOtherNode(
          nodeOptionBuilder,
          graphTraversalSubQueryBuilder,
          (ObjectObjectGraphMapping) fieldObjectGraphMapping
      );
    });
  }

  private void mapGraphTraversalOtherNode(
      ArangoGraphTraversalNodeOptionBuilder builder,
      ArangoGraphTraversalSubQueryBuilder graphTraversalBuilder,
      ObjectObjectGraphMapping objectGraphMapping
  ) {
    var graphDescription = objectGraphMapping.getGraphDescription();
    this.resolveMappedAttributes(objectGraphMapping.getFields(), builder.setMappedScalars());
    if (graphDescription instanceof GraphElementQueryDescription graphElementQueryDescription) {
      this.resolveSearchOptionsWithoutPagination(
          graphElementQueryDescription.getSearchQueryParameters(),
          graphTraversalBuilder.setNodeSearchOptions()
      );
    }
    this.resolveGraphTraversalMapping(objectGraphMapping.getFields(), builder);
  }
}
