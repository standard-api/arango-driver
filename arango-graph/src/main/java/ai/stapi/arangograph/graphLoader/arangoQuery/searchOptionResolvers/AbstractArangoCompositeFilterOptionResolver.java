package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.arrayComparisonOperators.AqlAll;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.arrayComparisonOperators.AqlAny;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.arrayComparisonOperators.AqlNone;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlParentheses;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlQuestionMarkOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.CollectionComparisonOperator;
import ai.stapi.graphoperations.graphLoader.search.GenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AbstractCompositeFilterOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.filterOption.CompositeFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractArangoCompositeFilterOptionResolver<S extends CompositeFilterOption>
    extends AbstractCompositeFilterOptionResolver<S, ArangoSearchResolvingContext, ArangoQuery> {
  
  protected AbstractArangoCompositeFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      GenericSearchOptionResolver<ArangoQuery> genericSearchOptionResolver
  ) {
    super(structureSchemaFinder, genericSearchOptionResolver);
  }

  @Override
  protected ArangoQuery postProcessResolvedFilter(
      ArangoQuery resolvedFilter,
      ArangoSearchResolvingContext context
  ) {
    return new ArangoQuery(
        new AqlParentheses(resolvedFilter.getAqlNode()),
        resolvedFilter.getBindParameters()
    );
  }

  @NotNull
  protected AqlNode getOperatorAndRightHand(
      AqlOperator operator,
      AqlNode rightExpression,
      CollectionComparisonOperator collectionComparisonOperator
  ) {
    if (operator.getOperatorName().equals(AqlOperator.like().getOperatorName())) {
      return new AqlQuestionMarkOperator(
          new AqlRootNode(
              operator,
              rightExpression
          ),
          new AqlOperator(collectionComparisonOperator.name())
      );
    } else {
      return new AqlRootNode(
          collectionComparisonOperator.equals(CollectionComparisonOperator.ALL) ?
              new AqlAll(operator) :
              collectionComparisonOperator.equals(CollectionComparisonOperator.NONE) ?
                  new AqlNone(operator) :
                  new AqlAny(operator),
          rightExpression
      );
    }
  }
}
