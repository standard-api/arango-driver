package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.GreaterThanOrEqualFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ArangoGreaterThanOrEqualFilterOptionResolver
    extends AbstractArangoOneValueFilterOprionResolver<GreaterThanOrEqualFilterOption<?>> {

  public ArangoGreaterThanOrEqualFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder, 
      GenericSubQueryResolver genericSubQueryResolver,
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
    return option instanceof GreaterThanOrEqualFilterOption<?>;
  }

  @Override
  public ArangoQuery resolveTyped(
      GreaterThanOrEqualFilterOption<?> option,
      ArangoSearchResolvingContext context
  ) {
    var aqlOperator = AqlOperator.greaterOrEqual();
    var decoratedValue = option.getParameters().getAttributeValue();
    return this.getArangoQuery(option, context, decoratedValue, aqlOperator);
  }
}
