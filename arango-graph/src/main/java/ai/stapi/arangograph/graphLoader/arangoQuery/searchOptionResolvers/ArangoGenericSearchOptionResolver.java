package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.graphoperations.graphLoader.search.AbstractGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.SearchOptionResolver;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArangoGenericSearchOptionResolver
    extends AbstractGenericSearchOptionResolver<ArangoQuery> {

  public ArangoGenericSearchOptionResolver(
      List<SearchOptionResolver<ArangoQuery>> searchOptionResolvers) {
    super(searchOptionResolvers);
  }
}
