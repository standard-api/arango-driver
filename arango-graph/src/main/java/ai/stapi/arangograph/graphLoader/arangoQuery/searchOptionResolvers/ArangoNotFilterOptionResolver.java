package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.logicalOperators.AqlNot;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.search.GenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.SearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.search.filterOption.NotFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.HashMap;

public class ArangoNotFilterOptionResolver
    extends AbstractArangoCompositeFilterOptionResolver<NotFilterOption> {

  public ArangoNotFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    super(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof NotFilterOption;
  }

  @Override
  protected ArangoQuery postProcessResolvedFilter(ArangoQuery resolvedFilter,
      ArangoSearchResolvingContext context) {
    var resolvedFilterWithParentheses = super.postProcessResolvedFilter(resolvedFilter, context);
    return new ArangoQuery(
        new AqlNot(resolvedFilterWithParentheses.getAqlNode()),
        resolvedFilterWithParentheses.getBindParameters()
    );
  }

  @Override
  protected SearchResolvingContext createLeafChildContext(
      ArangoSearchResolvingContext parentContext, Integer leafIndex) {
    return new ArangoSearchResolvingContext(
        parentContext.getDocumentName(),
        parentContext.getOriginQueryType(),
        parentContext.getGraphElementType(),
        "not_" + parentContext.getPlaceholderPostfix() + leafIndex,
        parentContext.getSubQueryPostfix()
    );
  }

  @Override
  protected SearchResolvingContext createCompositeChildContext(
      ArangoSearchResolvingContext parentContext) {
    return new ArangoSearchResolvingContext(
        parentContext.getDocumentName(),
        parentContext.getOriginQueryType(),
        parentContext.getGraphElementType(),
        "not_" + parentContext.getPlaceholderPostfix(),
        parentContext.getSubQueryPostfix()
    );
  }

  @Override
  protected ArangoQuery reduceChildResolvedFilters(
      ArangoQuery reduced,
      ArangoQuery childResolvedFilter
  ) {
    var mergedBindParam = new HashMap<String, Object>();
    mergedBindParam.putAll(reduced.getBindParameters());
    mergedBindParam.putAll(childResolvedFilter.getBindParameters());
    return reduced;
  }

}
