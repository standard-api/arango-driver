package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlSortOption;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.AscendingSortOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.HashMap;

public class ArangoAscendingSortOptionResolver
    extends AbstractArangoSearchOptionResolver<AscendingSortOption> {

  public ArangoAscendingSortOptionResolver(
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
    return option instanceof AscendingSortOption;
  }

  @Override
  protected ArangoQuery resolveTyped(AscendingSortOption option,
      ArangoSearchResolvingContext context) {
    var attributeTypePlaceholder =
        this.createAttributeNamePlaceholder(context, option.getOptionType());
    var searchOptionSubQueryPostfix =
        this.createSearchOptionSubQueryPostfix(context, option.getOptionType());
    var bindParameters = new HashMap<String, Object>();
    if (option.isLeaf()) {
      bindParameters.put(attributeTypePlaceholder, option.getAttributeName());
      var sort = AqlSortOption.asc(
          this.getFirstAttributeValue(context, attributeTypePlaceholder)
      );
      return new ArangoQuery(sort, bindParameters);
    }
    var searchOptionSubQuery = this.createSearchOptionSubQuery(
        option.getParameters(),
        context,
        searchOptionSubQueryPostfix,
        GraphLoaderReturnType.SORT_OPTION
    );
    return new ArangoQuery(
        AqlSortOption.asc(searchOptionSubQuery.getAqlNode()),
        searchOptionSubQuery.getBindParameters()
    );
  }
}
