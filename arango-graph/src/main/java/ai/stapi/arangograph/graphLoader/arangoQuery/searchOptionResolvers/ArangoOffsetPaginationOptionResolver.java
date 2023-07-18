package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLimit;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.paginationOption.OffsetPaginationOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.HashMap;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ArangoOffsetPaginationOptionResolver
    extends AbstractArangoSearchOptionResolver<OffsetPaginationOption> {

  public ArangoOffsetPaginationOptionResolver(
      @Lazy ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder, GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOgmFactory) {
    super(arangoGenericSearchOptionResolver, structureSchemaFinder, genericSubQueryResolver,
        graphLoaderOgmFactory);
  }

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof OffsetPaginationOption;
  }

  @Override
  protected ArangoQuery resolveTyped(
      OffsetPaginationOption option,
      ArangoSearchResolvingContext context
  ) {
    var postfix = context.getSubQueryPostfix();
    var offset = postfix.isBlank() ? "offset" : String.format("offset__%s", postfix);
    var limit = postfix.isBlank() ? "limit" : String.format("limit__%s", postfix);
    var bindParameters = new HashMap<String, Object>();
    bindParameters.put(offset, option.getParameters().getOffset());
    bindParameters.put(limit, option.getParameters().getLimit());
    return new ArangoQuery(
        new AqlLimit(
            new AqlVariable(limit).markAsBindParameter(),
            new AqlVariable(offset).markAsBindParameter()
        ),
        bindParameters
    );
  }
}
