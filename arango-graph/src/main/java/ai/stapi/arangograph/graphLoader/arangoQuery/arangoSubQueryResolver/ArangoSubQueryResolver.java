package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;

public interface ArangoSubQueryResolver {

  boolean supports(ArangoSubQueryBuilder builder, GraphDescription graphDescription);

  void resolve(ArangoSubQueryBuilder builder, GraphDescription graphDescription);

  void resolve(ArangoSubQueryBuilder builder, ObjectGraphMapping objectGraphMapping);
}
