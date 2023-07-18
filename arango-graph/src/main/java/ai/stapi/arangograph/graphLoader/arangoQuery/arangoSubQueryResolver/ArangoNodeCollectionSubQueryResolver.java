package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoNodeCollectionSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractNodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.GraphElementQueryDescription;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectObjectGraphMapping;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ArangoNodeCollectionSubQueryResolver extends AbstractArangoSubQueryResolver {

  public ArangoNodeCollectionSubQueryResolver(
      @Lazy GenericSubQueryResolver genericSubQueryResolver) {
    super(genericSubQueryResolver);
  }

  @Override
  public boolean supports(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    return builder instanceof ArangoNodeCollectionSubQueryBuilder
        && graphDescription instanceof AbstractNodeDescription;
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    var collectionSubQueryBuilder = (ArangoNodeCollectionSubQueryBuilder) builder;
    this.resolveAttributes(graphDescription, collectionSubQueryBuilder.setKeptAttributes());
    this.resolveGraphTraversalJoins(graphDescription, collectionSubQueryBuilder);
    if (graphDescription instanceof GraphElementQueryDescription graphElementQueryDescription) {
      this.resolveSearchOptions(
          graphElementQueryDescription.getSearchQueryParameters(),
          collectionSubQueryBuilder.setSearchOptions()
      );
    }
  }

  @Override
  public void resolve(ArangoSubQueryBuilder builder, ObjectGraphMapping objectGraphMapping) {
    var objectOgm = (ObjectObjectGraphMapping) objectGraphMapping;
    var subQueryBuilder = (ArangoNodeCollectionSubQueryBuilder) builder;
    var graphDescription = objectGraphMapping.getGraphDescription();
    this.resolveMappedAttributes(objectOgm.getFields(), subQueryBuilder.setMappedScalars());
    if (graphDescription instanceof GraphElementQueryDescription graphElementQueryDescription) {
      this.resolveSearchOptions(
          graphElementQueryDescription.getSearchQueryParameters(),
          subQueryBuilder.setSearchOptions()
      );
    }
    this.resolveGraphTraversalMapping(objectOgm.getFields(), subQueryBuilder);
  }
}
