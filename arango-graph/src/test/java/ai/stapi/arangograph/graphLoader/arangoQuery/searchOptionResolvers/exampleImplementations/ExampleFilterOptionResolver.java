package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.exampleImplementations;

import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.SearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.SearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EqualsFilterOption;
import org.springframework.stereotype.Service;

@Service
public class ExampleFilterOptionResolver implements SearchOptionResolver<ExampleResolvedQueryPart> {

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof EqualsFilterOption<?>;
  }

  @Override
  public ExampleResolvedQueryPart resolve(SearchOption<?> option, SearchResolvingContext context) {
    return new ExampleResolvedQueryPart(true);
  }
}
