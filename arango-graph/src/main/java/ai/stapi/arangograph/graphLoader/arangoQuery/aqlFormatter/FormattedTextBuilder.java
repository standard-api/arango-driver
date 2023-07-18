package ai.stapi.arangograph.graphLoader.arangoQuery.aqlFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class FormattedTextBuilder {

  public static final String DEFAULT_SYMBOL_SPACE = " ";
  private static final String SYMBOL_NEWLINE = "\n";
  private static final String SYMBOL_INDENT = "    ";

  private Integer currentLineNumber = 0;

  private Integer currentIndent = 0;

  private Integer currentPartOnLine = 0;

  private final String symbolSpace = DEFAULT_SYMBOL_SPACE;

  private final String symbolNewline = SYMBOL_NEWLINE;

  private final String symbolIndent = SYMBOL_INDENT;

  private final ArrayList<String> textParts = new ArrayList<>();

  public FormattedTextBuilder addTextPart(String textPart) {
    this.textParts.add(textPart);
    this.currentPartOnLine = this.currentPartOnLine + 1;
    return this;
  }

  public FormattedTextBuilder addNewline() {
    this.textParts.add(this.symbolNewline);
    List<Integer> range = IntStream.range(0, this.currentIndent)
        .boxed()
        .toList();
    range.forEach(index -> this.textParts.add(this.symbolIndent));
    this.currentLineNumber = this.currentLineNumber + 1;
    this.currentPartOnLine = 0;
    return this;
  }

  public FormattedTextBuilder addSpace() {
    this.textParts.add(this.symbolSpace);
    return this;
  }

  public Integer getCurrentLineNumber() {
    return currentLineNumber;
  }

  public Integer getCurrentIndent() {
    return currentIndent;
  }

  public Integer countParts() {
    return this.textParts.size();
  }

  public Integer getCurrentPartOnLine() {
    return currentPartOnLine;
  }

  public String build() {
    return String.join("", this.textParts);
  }

  public FormattedTextBuilder increaseIndent() {
    this.currentIndent = this.currentIndent + 1;
    return this;
  }

  public FormattedTextBuilder decreaseIndent() {
    this.currentIndent = this.currentIndent - 1;
    return this;
  }
}
