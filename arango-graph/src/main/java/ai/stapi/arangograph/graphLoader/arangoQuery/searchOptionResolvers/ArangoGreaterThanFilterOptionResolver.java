package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.GreaterThanFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

public class ArangoGreaterThanFilterOptionResolver
    extends AbstractArangoOneValueFilterOprionResolver<GreaterThanFilterOption<?>> {

  public ArangoGreaterThanFilterOptionResolver(
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
    return option instanceof GreaterThanFilterOption<?>;
  }

  @Override
  public ArangoQuery resolveTyped(
      GreaterThanFilterOption<?> option,
      ArangoSearchResolvingContext context
  ) {
    var greaterThan = AqlOperator.greaterThan();
    var decoratedValue = option.getParameters().getAttributeValue();
    return this.getArangoQuery(option, context, decoratedValue, greaterThan);
  }
}
