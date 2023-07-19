package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.exceptions.CannotBuildArangoQuery;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class GenericSubQueryResolver {

  private final List<ArangoSubQueryResolver> subQueryResolvers;

  public GenericSubQueryResolver(List<ArangoSubQueryResolver> subQueryResolvers) {
    this.subQueryResolvers = subQueryResolvers;
  }

  public void resolve(ArangoSubQueryBuilder builder, GraphDescription graphDescription) {
    var supported = getSupportedResolver(builder, graphDescription);

    supported.resolve(builder, graphDescription);
  }

  public void resolve(ArangoSubQueryBuilder builder, ObjectGraphMapping objectGraphMapping) {
    var graphDescription = objectGraphMapping.getGraphDescription();
    var supported = getSupportedResolver(builder, graphDescription);

    supported.resolve(builder, objectGraphMapping);
  }

  @NotNull
  private ArangoSubQueryResolver getSupportedResolver(
      ArangoSubQueryBuilder builder,
      GraphDescription graphDescription
  ) {
    var supported = this.subQueryResolvers.stream()
        .filter(resolver -> resolver.supports(builder, graphDescription))
        .toList();

    if (supported.size() == 0) {
      throw CannotBuildArangoQuery.becauseThereWasNoSubQueryResolversForBuilder(
          builder,
          graphDescription
      );
    }

    if (supported.size() > 1) {
      throw CannotBuildArangoQuery.becauseThereWereMoreSubQueryResolversForBuilder(
          builder,
          graphDescription
      );
    }

    return supported.get(0);
  }
}
