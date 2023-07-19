package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoNodeGetSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractNodeDescription;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectObjectGraphMapping;

public class ArangoNodeGetSubQueryResolver extends AbstractArangoSubQueryResolver {


  public ArangoNodeGetSubQueryResolver(GenericSubQueryResolver genericSubQueryResolver) {
    super(genericSubQueryResolver);
  }

  @Override
  public boolean supports(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    return builder instanceof ArangoNodeGetSubQueryBuilder
        && graphDescription instanceof AbstractNodeDescription;
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    var subQueryBuilder = (ArangoNodeGetSubQueryBuilder) builder;
    this.resolveAttributes(graphDescription, subQueryBuilder.setKeptAttributes());
    this.resolveGraphTraversalJoins(graphDescription, subQueryBuilder);
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, ObjectGraphMapping objectGraphMapping) {
    var objectOGM = (ObjectObjectGraphMapping) objectGraphMapping;
    var subQueryBuilder = (ArangoNodeGetSubQueryBuilder) builder;
    this.resolveMappedAttributes(objectOGM.getFields(), subQueryBuilder.setMappedScalars());
    this.resolveGraphTraversalMapping(objectOGM.getFields(), subQueryBuilder);
  }
}
