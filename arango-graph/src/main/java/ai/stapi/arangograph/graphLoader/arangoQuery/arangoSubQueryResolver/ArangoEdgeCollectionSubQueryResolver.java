package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoEdgeCollectionSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractNodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.GraphElementQueryDescription;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectObjectGraphMapping;

public class ArangoEdgeCollectionSubQueryResolver extends AbstractArangoSubQueryResolver {

  public ArangoEdgeCollectionSubQueryResolver(GenericSubQueryResolver genericSubQueryResolver) {
    super(genericSubQueryResolver);
  }

  @Override
  public boolean supports(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    return builder instanceof ArangoEdgeCollectionSubQueryBuilder
        && graphDescription instanceof AbstractEdgeDescription;
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    var edgeDescription = (AbstractEdgeDescription) graphDescription;
    var collectionSubQueryBuilder = (ArangoEdgeCollectionSubQueryBuilder) builder;
    this.resolveAttributes(edgeDescription, collectionSubQueryBuilder.setKeptAttributes());
    if (edgeDescription instanceof GraphElementQueryDescription graphElementQueryDescription) {
      this.resolveSearchOptions(
          graphElementQueryDescription.getSearchQueryParameters(),
          collectionSubQueryBuilder.setSearchOptions()
      );
    }
    var nodeDescriptions = this.getChildNodeDescriptions(edgeDescription);
    nodeDescriptions.forEach(description -> {
      var param = (NodeDescriptionParameters) description.getParameters();

      this.genericSubQueryResolver.resolve(
          collectionSubQueryBuilder.joinNodeGetSubQuery(param.getNodeType()),
          description
      );
    });
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, ObjectGraphMapping objectGraphMapping) {
    var objectOGM = (ObjectObjectGraphMapping) objectGraphMapping;
    var subQueryBuilder = (ArangoEdgeCollectionSubQueryBuilder) builder;
    if (objectOGM.getGraphDescription() instanceof GraphElementQueryDescription graphElementQueryDescription) {
      this.resolveSearchOptions(
          graphElementQueryDescription.getSearchQueryParameters(),
          subQueryBuilder.setSearchOptions()
      );
    }
    this.resolveMappedAttributes(objectOGM.getFields(), subQueryBuilder.setMappedScalars());
    var nodeFields = objectOGM.getFields().entrySet()
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
      var joinedNodeBuilder = subQueryBuilder.mapNodeGetSubQuery(
          nodeParams.getNodeType()
      );
      this.genericSubQueryResolver.resolve(joinedNodeBuilder, fieldObjectGraphMapping);
    });
  }
}
