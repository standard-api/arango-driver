package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlSortOption;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.DescendingSortOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.HashMap;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ArangoDescendingSortOptionResolver
    extends AbstractArangoSearchOptionResolver<DescendingSortOption> {

  public ArangoDescendingSortOptionResolver(
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
    return option instanceof DescendingSortOption;
  }

  @Override
  protected ArangoQuery resolveTyped(DescendingSortOption option,
      ArangoSearchResolvingContext context) {
    var attributeTypePlaceholder =
        this.createAttributeNamePlaceholder(context, option.getOptionType());
    var searchOptionSubQueryPostfix =
        this.createSearchOptionSubQueryPostfix(context, option.getOptionType());
    var bindParameters = new HashMap<String, Object>();
    if (option.isLeaf()) {
      bindParameters.put(attributeTypePlaceholder, option.getAttributeName());
      var sort = AqlSortOption.desc(
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
        AqlSortOption.desc(searchOptionSubQuery.getAqlNode()),
        searchOptionSubQuery.getBindParameters()
    );
  }

}
