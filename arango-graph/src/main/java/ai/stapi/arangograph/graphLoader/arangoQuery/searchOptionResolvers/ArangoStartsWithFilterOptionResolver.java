package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.StartsWithFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

public class ArangoStartsWithFilterOptionResolver
    extends AbstractArangoOneValueFilterOprionResolver<StartsWithFilterOption> {

  public ArangoStartsWithFilterOptionResolver(
      ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    super(
        arangoGenericSearchOptionResolver,
        structureSchemaFinder,
        genericSubQueryResolver,
        graphLoaderOGMFactory
    );
  }

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof StartsWithFilterOption;
  }

  @Override
  public ArangoQuery resolveTyped(
      StartsWithFilterOption option,
      ArangoSearchResolvingContext context
  ) {
    var decoratedValue = option.getParameters().getAttributeValue() + "%";
    var aqlOperator = AqlOperator.like();
    return this.getArangoQuery(option, context, decoratedValue, aqlOperator);
  }
}
