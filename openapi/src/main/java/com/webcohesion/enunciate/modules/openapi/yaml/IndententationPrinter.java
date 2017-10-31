package com.webcohesion.enunciate.modules.openapi.yaml;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Printer helping keep track of YAML indentation levels.
 *
 * @author jb1811
 */
public class IndententationPrinter {
  private static final String SEQUENCE_PREFIX = "- ";
  private final StringBuilder sb = new StringBuilder();
  private Deque<IndentLevel> indentLevelStack = new ArrayDeque<>();
  private Deque<IndentLevel> memoryStack = new ArrayDeque<>();
  private boolean itemFollows;

  public IndententationPrinter(String initialIndentation) {
    indentLevelStack.push(new IndentLevel(initialIndentation));
  }

  private IndentLevel getActive() {
    return indentLevelStack.peek();
  }

  public void itemFollows() {
    this.itemFollows = true;
  }
  
  public IndententationPrinter item(String... lines) {
    itemFollows = true;
    return add(lines);
  }

  public IndententationPrinter add(String... lines) {
    addIndentationForAllButFirstLine();
    for (String line : lines) {
      sb.append(line);
    }
    return this;
  }

  private void addIndentationForAllButFirstLine() {
    if (sb.length() != 0) {
      String indent = getActive().indentation;
      if (itemFollows) {
        indent = indent.replaceAll("  $", SEQUENCE_PREFIX);
        itemFollows = false;
      }
      sb.append(System.lineSeparator()).append(indent);
    }
  }
  
  public IndententationPrinter nextLevel() {
    indentLevelStack.push(getActive().next());
    return this;
  }

  public IndententationPrinter prevLevel() {
    if (indentLevelStack.size() == 1) {
      throw new IllegalStateException("Cannot go beyond initial indentation");
    }
    indentLevelStack.pop();
    return this;
  }

  public IndententationPrinter pushNextLevel() {
    memoryStack.push(getActive());
    return nextLevel();
  }
  
  public IndententationPrinter popLevel() {
    IndentLevel revertTo = memoryStack.pop();
    while (indentLevelStack.peek() != revertTo) {
      indentLevelStack.pop();
    }
    return this;
  }
  
  public String toString() {
    return sb.toString();
  }

  public static class IndentLevel {
    private static final String INDENT_PREFIX = "  ";

    public final String indentation;
    
    public IndentLevel(String indentation) {
      this.indentation = indentation;
    }
    
    public IndentLevel next() {
      return new IndentLevel(indentation.replace('-', ' ') + INDENT_PREFIX);
    }
  }
}
