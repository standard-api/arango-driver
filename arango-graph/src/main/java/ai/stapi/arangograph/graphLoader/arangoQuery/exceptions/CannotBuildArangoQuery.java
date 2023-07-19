package ai.stapi.arangograph.graphLoader.arangoQuery.exceptions;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;

public class CannotBuildArangoQuery extends RuntimeException {

  private CannotBuildArangoQuery(String message) {
    super("Cannot build arango query, because " + message);
  }

  public static CannotBuildArangoQuery becauseNoMainQuerySet() {
    return new CannotBuildArangoQuery(
        "there was no main query set."
    );
  }

  public static CannotBuildArangoQuery becauseGraphTraversalCannotBeJoinedFromEdge() {
    return new CannotBuildArangoQuery(
        "graph traversal cannot be joined from edge."
    );
  }

  public static CannotBuildArangoQuery becauseThereWereMoreSubQueryResolversForBuilder(
      ArangoSubQueryBuilder builder,
      GraphDescription graphDescription
  ) {
    return new CannotBuildArangoQuery(
        "there were more supporting resolvers for one builder."
            + "\nBuilder class: " + builder.getClass().getSimpleName()
            + "\nProvided Graph Description: " + graphDescription.getClass().getSimpleName()
    );
  }

  public static CannotBuildArangoQuery becauseThereWasNoSubQueryResolversForBuilder(
      ArangoSubQueryBuilder builder,
      GraphDescription graphDescription
  ) {
    return new CannotBuildArangoQuery(
        "there was no supporting resolvers for builder."
            + "\nBuilder class: " + builder.getClass().getSimpleName()
            + "\nProvided Graph Description: " + graphDescription.getClass().getSimpleName()
    );
  }

  public static CannotBuildArangoQuery becauseEncounteredNonExisitingOriginQueryType(
      ArangoQueryType originQueryType) {
    return new CannotBuildArangoQuery(
        "unknown origin query type encountered when resolving deep search option."
            + "\nArango Query Type: " + originQueryType.name()
    );
  }
}
