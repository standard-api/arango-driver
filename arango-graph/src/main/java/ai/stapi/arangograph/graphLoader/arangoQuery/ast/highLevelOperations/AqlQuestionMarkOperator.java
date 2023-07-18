package ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations;

import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class AqlQuestionMarkOperator implements AqlNode {

  private final AqlOperator arrayComparisonOperator;
  @Nullable
  private AqlNode rightHandExpression;

  public AqlQuestionMarkOperator(
      @Nullable AqlNode rightHandExpression,
      AqlOperator arrayComparisonOperator
  ) {
    this.rightHandExpression = rightHandExpression;
    this.arrayComparisonOperator = arrayComparisonOperator;
  }

  @Override
  public String toQueryString() {
    return String.format(
        "[? %s FILTER CURRENT %s]",
        this.arrayComparisonOperator.toQueryString(),
        Objects.requireNonNull(this.rightHandExpression).toQueryString()
    );
  }

  @Nullable
  public AqlNode getRightHandExpression() {
    return rightHandExpression;
  }

  public void setRightHandExpression(@Nullable AqlNode rightHandExpression) {
    this.rightHandExpression = rightHandExpression;
  }

  @Nullable
  public AqlNode getDeepestRightHandExpression() {
    if (this.isDeepestRightHandExpressionSet()
        && (this.rightHandExpression instanceof AqlQuestionMarkOperator questionMarkOperator)) {
      return questionMarkOperator.getDeepestRightHandExpression();

    }
    return rightHandExpression;
  }

  public void setDeepestRightHandExpression(AqlNode rightHandExpression) {
    if (this.isRightHandExpressionSet()) {
      if (this.rightHandExpression instanceof AqlQuestionMarkOperator questionMarkOperator) {
        questionMarkOperator.setDeepestRightHandExpression(rightHandExpression);
      } else {
        this.rightHandExpression = rightHandExpression;
      }
    } else {
      this.rightHandExpression = rightHandExpression;
    }
  }

  public AqlOperator getArrayComparisonOperator() {
    return arrayComparisonOperator;
  }

  public boolean isRightHandExpressionSet() {
    return rightHandExpression != null;
  }

  public boolean isDeepestRightHandExpressionSet() {
    if (!this.isRightHandExpressionSet()) {
      return false;
    }
    if (this.rightHandExpression instanceof AqlQuestionMarkOperator questionMarkOperator) {
      return questionMarkOperator.isDeepestRightHandExpressionSet();
    }
    return true;
  }
}
