package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoEdgeGetSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractNodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectObjectGraphMapping;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ArangoEdgeGetSubQueryResolver extends AbstractArangoSubQueryResolver {

  public ArangoEdgeGetSubQueryResolver(@Lazy GenericSubQueryResolver genericSubQueryResolver) {
    super(genericSubQueryResolver);
  }

  @Override
  public boolean supports(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    return builder instanceof ArangoEdgeGetSubQueryBuilder
        && graphDescription instanceof AbstractEdgeDescription;
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    var edgeDescription = (AbstractEdgeDescription) graphDescription;
    var subQueryBuilder = (ArangoEdgeGetSubQueryBuilder) builder;
    this.resolveAttributes(edgeDescription, subQueryBuilder.setKeptAttributes());
    var nodeDescriptions = this.getChildNodeDescriptions(edgeDescription);
    nodeDescriptions.forEach(description -> {
      var nodeDescription = (AbstractNodeDescription) description;
      var param = (NodeDescriptionParameters) nodeDescription.getParameters();

      this.genericSubQueryResolver.resolve(
          subQueryBuilder.joinNodeGetSubQuery(param.getNodeType()),
          nodeDescription
      );
    });
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, ObjectGraphMapping objectGraphMapping) {
    var objectOgm = (ObjectObjectGraphMapping) objectGraphMapping;
    var subQueryBuilder = (ArangoEdgeGetSubQueryBuilder) builder;
    this.resolveMappedAttributes(objectOgm.getFields(), subQueryBuilder.setMappedScalars());
    var nodeFields = objectOgm.getFields().entrySet()
        .stream()
        .filter(
            entry -> {
              var fieldOgm = entry.getValue().getFieldObjectGraphMapping();
              if (fieldOgm instanceof ObjectObjectGraphMapping) {
                return fieldOgm.getGraphDescription() instanceof AbstractNodeDescription;
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
