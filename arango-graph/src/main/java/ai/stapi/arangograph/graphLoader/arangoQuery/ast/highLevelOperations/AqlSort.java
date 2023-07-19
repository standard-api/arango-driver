package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import java.util.List;
import java.util.stream.Collectors;

public class AqlSort implements AqlNode {

  private final List<AqlSortOption> sortOptionList;

  public AqlSort(List<AqlSortOption> sortOptionList) {
    this.sortOptionList = sortOptionList;
  }

  public List<AqlSortOption> getSortOptionList() {
    return sortOptionList;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "SORT %s",
        this.sortOptionList.stream().map(AqlSortOption::toQueryString)
            .collect(Collectors.joining(", "))
    );
  }
}
