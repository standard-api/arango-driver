package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.logicalOperators.AqlAnd;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.search.GenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.SearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AndFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.HashMap;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ArangoAndFilterOptionResolver
    extends AbstractArangoCompositeFilterOptionResolver<AndFilterOption> {


  protected ArangoAndFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      @Lazy GenericSearchOptionResolver<ArangoQuery> genericSearchOptionResolver
  ) {
    super(structureSchemaFinder, genericSearchOptionResolver);
  }

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof AndFilterOption;
  }

  @Override
  protected SearchResolvingContext createLeafChildContext(
      ArangoSearchResolvingContext parentContext, Integer leafIndex) {
    return new ArangoSearchResolvingContext(
        parentContext.getDocumentName(),
        parentContext.getOriginQueryType(),
        parentContext.getGraphElementType(),
        "and_" + parentContext.getPlaceholderPostfix() + leafIndex,
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
        "and_" + parentContext.getPlaceholderPostfix(),
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
    return new ArangoQuery(
        new AqlAnd(reduced.getAqlNode(), childResolvedFilter.getAqlNode()),
        mergedBindParam
    );
  }

}
