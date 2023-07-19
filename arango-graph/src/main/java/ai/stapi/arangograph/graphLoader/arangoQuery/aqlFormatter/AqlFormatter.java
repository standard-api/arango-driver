package ai.stapi.arangograph.graphLoader.arangoQuery.aqlFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AqlFormatter {

  private static final List<String> SYMBOLS_WITH_NEWLINE_BEFORE = new ArrayList<>(List.of(
      "LET",
      "}",
      ")",
      "FOR",
      "RETURN",
      "FILTER",
      "SORT",
      "LIMIT"
  ));

  private static final List<String> SYMBOLS_WITH_NEWLINE_AFTER = new ArrayList<>(List.of(
      "(",
      "{"
  ));
  private static final List<String> SYMBOLS_CONTAINING_STRING_WITH_NEWLINE_AFTER =
      new ArrayList<>(List.of(
          ","
      ));

  private static final List<String> SYMBOLS_INCREASING_INDENT = new ArrayList<>(List.of(
      "(",
      "{"
  ));

  private static final List<String> SYMBOLS_DECREASING_INDENT = new ArrayList<>(List.of(
      ")",
      "}"
  ));


  public String format(String simpleAQL) {
    simpleAQL = simpleAQL
        .replace("(", " ( ")
        .replace(")", " ) ")
        .replace("{", " { ")
        .replace("}", " } ")
        .replace(",", " , ");

    var aqlTokens = Arrays.stream(simpleAQL.split("\\s+")).toList();

    var textBuilder = new FormattedTextBuilder();
    aqlTokens.forEach(
        token -> {
          if (this.shouldIncreaseIndent(token)) {
            textBuilder.increaseIndent();
          }
          if (this.shouldDecreaseIndent(token)) {
            textBuilder.decreaseIndent();
          }
          if (this.shouldAddSpace(textBuilder)) {
            textBuilder.addSpace();
          }
          if (this.shouldAddNewlineBefore(
              token,
              textBuilder
          )) {
            textBuilder.addNewline();
          }
          textBuilder.addTextPart(token);
          if (this.shouldAddNewlineAfter(token)) {
            textBuilder.addNewline();
          }
        }
    );

    return textBuilder.addNewline().build()
        .replace(" ,", ",")
        .replace(" .", ".")
        .replaceAll("[^\\S\\r\\n]+\n", "\n");
  }

  private boolean shouldDecreaseIndent(String token) {
    return SYMBOLS_DECREASING_INDENT.contains(token);
  }

  private boolean shouldIncreaseIndent(String token) {
    return SYMBOLS_INCREASING_INDENT.contains(token);
  }

  private boolean shouldAddNewlineBefore(String token, FormattedTextBuilder textBuilder) {
    if (textBuilder.countParts() == 0) {
      return false;
    }
      return SYMBOLS_WITH_NEWLINE_BEFORE.contains(token);
  }

  private boolean shouldAddNewlineAfter(String token) {
    if (SYMBOLS_WITH_NEWLINE_AFTER.contains(token)) {
      return true;
    }
    return SYMBOLS_CONTAINING_STRING_WITH_NEWLINE_AFTER.stream()
        .anyMatch(token::contains);
  }

  private boolean shouldAddSpace(FormattedTextBuilder textBuilder) {
    return textBuilder.countParts() != 0 && textBuilder.getCurrentPartOnLine() != 0;
  }
}
