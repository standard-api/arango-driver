package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.exampleImplementations;

import ai.stapi.graphoperations.graphLoader.search.ResolvedQueryPart;

public class ExampleResolvedQueryPart implements ResolvedQueryPart {

  private final boolean isResolved;

  public ExampleResolvedQueryPart(boolean isResolved) {
    this.isResolved = isResolved;
  }

  public boolean isResolved() {
    return isResolved;
  }
}
