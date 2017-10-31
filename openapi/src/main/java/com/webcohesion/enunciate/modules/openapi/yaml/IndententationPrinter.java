package com.webcohesion.enunciate.modules.openapi.yaml;

/**
 * Printer helping keep track of YAML indentation levels.
 *
 * @author jb1811
 */
public class IndententationPrinter {
  private String indentation;
  private StringBuilder sb = new StringBuilder();
  int level = 0;

  public IndententationPrinter(String initialIndentation) {
    this.indentation = initialIndentation;
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
      sb.append(System.lineSeparator()).append(indentation);
    }
  }
  
  public IndententationPrinter nextLevel() {
    indentation = indentation + "  ";
    level += 1;
    return this;
  }
  
  public IndententationPrinter prevLevel() {
    if (level == 0) {
      throw new IllegalStateException("Cannot go beyond initial indentation");
    }
    indentation = indentation.substring(2);
    level -= 1;
    return this;
  }
  
  public String toString() {
    return sb.toString();
  }
}
