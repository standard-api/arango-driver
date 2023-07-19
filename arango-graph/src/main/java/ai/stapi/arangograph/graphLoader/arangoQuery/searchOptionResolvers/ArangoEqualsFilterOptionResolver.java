package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EqualsFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

public class ArangoEqualsFilterOptionResolver
    extends AbstractArangoOneValueFilterOprionResolver<EqualsFilterOption<?>> {

  public ArangoEqualsFilterOptionResolver(
      ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder, GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOgmFactory
  ) {
    super(
        arangoGenericSearchOptionResolver, 
        structureSchemaFinder, 
        genericSubQueryResolver,
        graphLoaderOgmFactory
    );
  }

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof EqualsFilterOption<?>;
  }

  @Override
  protected ArangoQuery resolveTyped(
      EqualsFilterOption<?> option,
      ArangoSearchResolvingContext context
  ) {
    var decoratedValue = option.getParameters().getAttributeValue();
    var aqlOperator = AqlOperator.equality();
    return this.getArangoQuery(option, context, decoratedValue, aqlOperator);
  }
}
