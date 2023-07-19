package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlFilter;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLimit;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlSort;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlSortOption;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.filterOption.FilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.LeafFilterOption;
import ai.stapi.graphoperations.graphLoader.search.paginationOption.PaginationOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.SortOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class ArangoSearchOptionsBuilder {

  protected final Map<String, Object> bindParameters;
  private final AqlVariable documentName;
  private final ArangoQueryType originQueryType;
  private final String subQueryPostfix;
  private final List<AqlSortOption> resolvedSortParts;
  private final List<AqlFilter> resolvedFilterParts;
  private final ArangoGenericSearchOptionResolver searchOptionResolver;
  private String graphElementType;
  @Nullable
  private AqlLimit resolvedPaginationPart;

  public ArangoSearchOptionsBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      AqlVariable documentName,
      ArangoQueryType originQueryType,
      String graphElementType,
      String subQueryPostfix
  ) {
    this.searchOptionResolver = searchOptionResolver;
    this.documentName = documentName;
    this.originQueryType = originQueryType;
    this.graphElementType = graphElementType;
    this.subQueryPostfix = subQueryPostfix;
    this.resolvedSortParts = new ArrayList<>();
    this.resolvedFilterParts = new ArrayList<>();
    this.resolvedPaginationPart = null;
    this.bindParameters = new HashMap<>();
  }

  public ArangoSearchOptionsBuilder addFilterOption(FilterOption<?> filterOption) {
    ArangoQuery resolved;
    if (filterOption instanceof LeafFilterOption<?>) {
      resolved = this.searchOptionResolver.resolve(
          filterOption,
          new ArangoSearchResolvingContext(
              this.documentName.getVariableName(),
              this.originQueryType,
              this.graphElementType,
              String.valueOf(this.resolvedFilterParts.size()),
              this.subQueryPostfix
          )
      );
    } else {
      resolved = this.searchOptionResolver.resolve(
          filterOption,
          new ArangoSearchResolvingContext(
              this.documentName.getVariableName(),
              this.originQueryType,
              this.graphElementType
          )
      );
    }
    this.resolvedFilterParts.add(new AqlFilter(resolved.getAqlNode()));
    this.bindParameters.putAll(resolved.getBindParameters());
    return this;
  }

  public ArangoSearchOptionsBuilder addSortOption(SortOption sortOption) {
    var resolved = this.searchOptionResolver.resolve(
        sortOption,
        new ArangoSearchResolvingContext(
            this.documentName.getVariableName(),
            this.originQueryType,
            this.graphElementType,
            String.valueOf(this.resolvedSortParts.size()),
            this.subQueryPostfix
        )
    );
    this.resolvedSortParts.add((AqlSortOption) resolved.getAqlNode());
    this.bindParameters.putAll(resolved.getBindParameters());
    return this;
  }

  public ArangoSearchOptionsBuilder setPaginationOption(PaginationOption<?> paginationOption) {
    if (paginationOption == null) {
      return this;
    }
    var resolved = this.searchOptionResolver.resolve(
        paginationOption,
        new ArangoSearchResolvingContext(
            this.documentName.getVariableName(),
            this.originQueryType,
            this.graphElementType,
            "",
            this.subQueryPostfix
        )
    );
    this.resolvedPaginationPart = (AqlLimit) resolved.getAqlNode();
    this.bindParameters.putAll(resolved.getBindParameters());
    return this;
  }

  public ArangoSearchOptionsBuilder addFilterOptions(List<FilterOption<?>> filterOptions) {
    filterOptions.forEach(this::addFilterOption);
    return this;
  }

  public ArangoSearchOptionsBuilder addSortOptions(
      List<SortOption> sortOptions) {
    sortOptions.forEach(this::addSortOption);
    return this;
  }

  protected ArangoQuery build() {
    return new ArangoQuery(
        this.buildQueryBody(),
        this.bindParameters
    );
  }

  private AqlNode buildQueryBody() {
    var queryLines = new ArrayList<AqlNode>(this.resolvedFilterParts);
    if (!this.resolvedSortParts.isEmpty()) {
      queryLines.add(new AqlSort(this.resolvedSortParts));
    }
    if (this.resolvedPaginationPart != null) {
      queryLines.add(this.resolvedPaginationPart);
    }
    return new AqlRootNode(queryLines);
  }

  public void setGraphElementType(String graphElementType) {
    this.graphElementType = graphElementType;
  }
}
