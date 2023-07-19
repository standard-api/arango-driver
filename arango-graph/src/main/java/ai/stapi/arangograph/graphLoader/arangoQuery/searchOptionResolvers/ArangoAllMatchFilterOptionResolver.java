package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperatorStatement;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlParentheses;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlQuestionMarkOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.CollectionComparisonOperator;
import ai.stapi.graphoperations.graphLoader.search.GenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.SearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AllMatchFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.HashMap;
import java.util.Objects;

public class ArangoAllMatchFilterOptionResolver
    extends AbstractArangoCompositeFilterOptionResolver<AllMatchFilterOption> {

  public ArangoAllMatchFilterOptionResolver(
      StructureSchemaFinder structureSchemaFinder,
      GenericSearchOptionResolver<ArangoQuery> genericFilterOptionResolver
  ) {
    super(structureSchemaFinder, genericFilterOptionResolver);
  }

  @Override
  public boolean supports(SearchOption<?> option) {
    return option instanceof AllMatchFilterOption;
  }

  @Override
  protected ArangoQuery postProcessResolvedFilter(
      ArangoQuery resolvedFilter,
      ArangoSearchResolvingContext context
  ) {
    var resolvedFilterWithParentheses = super.postProcessResolvedFilter(resolvedFilter, context);
    var parentheses = (AqlParentheses) resolvedFilterWithParentheses.getAqlNode();
    if (parentheses.getAqlNode() instanceof AqlOperatorStatement operatorStatement) {
      var operator = operatorStatement.getOperator();
      var rightExpression = operatorStatement.getRightExpression();
      var operatorAndRightHand = this.getOperatorAndRightHand(
          operator,
          rightExpression,
          CollectionComparisonOperator.ALL
      );

      return new ArangoQuery(
          new AqlParentheses(
              new AqlRootNode(
                  operatorStatement.getLeftExpression(),
                  operatorAndRightHand
              )
          ),
          resolvedFilterWithParentheses.getBindParameters()
      );
    } else {
      var rootNode = (AqlRootNode) parentheses.getAqlNode();
      var questionMarkOperator = rootNode.getAqlNodeList().stream()
          .filter(AqlQuestionMarkOperator.class::isInstance)
          .map(AqlQuestionMarkOperator.class::cast)
          .findFirst()
          .orElseThrow();

      var oldRightHand = (AqlRootNode) questionMarkOperator.getDeepestRightHandExpression();
      var operatorAndRightHand = this.getOperatorAndRightHand(
          (AqlOperator) Objects.requireNonNull(oldRightHand).getAqlNodeList().get(0),
          oldRightHand.getAqlNodeList().get(1),
          CollectionComparisonOperator.ALL
      );
      questionMarkOperator.setRightHandExpression(operatorAndRightHand);
      return resolvedFilterWithParentheses;
    }
  }

  @Override
  protected SearchResolvingContext createLeafChildContext(
      ArangoSearchResolvingContext parentContext, Integer leafIndex) {
    return new ArangoSearchResolvingContext(
        parentContext.getDocumentName(),
        parentContext.getOriginQueryType(),
        parentContext.getGraphElementType(),
        "allMatch_" + parentContext.getPlaceholderPostfix() + leafIndex,
        parentContext.getSubQueryPostfix()
    );
  }

  @Override
  protected SearchResolvingContext createCompositeChildContext(
      ArangoSearchResolvingContext parentContext
  ) {
    return new ArangoSearchResolvingContext(
        parentContext.getDocumentName(),
        parentContext.getOriginQueryType(),
        parentContext.getGraphElementType(),
        "allMatch_" + parentContext.getPlaceholderPostfix(),
        parentContext.getSubQueryPostfix()
    );
  }

  @Override
  protected ArangoQuery reduceChildResolvedFilters(
      ArangoQuery reduced,
      ArangoQuery childResolvedFilter
  ) {
    var mergedBindParam = new HashMap<String, Object>();
    mergedBindParam.putAll(reduced.getBindParameters());
    mergedBindParam.putAll(childResolvedFilter.getBindParameters());
    return reduced;
  }

}
