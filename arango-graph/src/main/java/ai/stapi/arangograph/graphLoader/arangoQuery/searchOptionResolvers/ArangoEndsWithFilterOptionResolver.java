package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EndsWithFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ArangoEndsWithFilterOptionResolver
    extends AbstractArangoOneValueFilterOprionResolver<EndsWithFilterOption> {

  public ArangoEndsWithFilterOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    super(arangoGenericSearchOptionResolver, structureSchemaFinder, genericSubQueryResolver,
        graphLoaderOGMFactory);
  }

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof EndsWithFilterOption;
  }

  @Override
  public ArangoQuery resolveTyped(
      EndsWithFilterOption option,
      ArangoSearchResolvingContext context
  ) {
    var decoratedValue = "%" + option.getParameters().getAttributeValue();
    var aqlOperator = AqlOperator.like();
    return this.getArangoQuery(option, context, decoratedValue, aqlOperator);
  }

}
