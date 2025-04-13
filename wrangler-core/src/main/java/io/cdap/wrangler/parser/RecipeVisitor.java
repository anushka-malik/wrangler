/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.LazyNumber;
import io.cdap.wrangler.api.RecipeSymbol;
import io.cdap.wrangler.api.SourceInfo;
import io.cdap.wrangler.api.Triplet;
import io.cdap.wrangler.api.parser.Bool;
import io.cdap.wrangler.api.parser.BoolList;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ByteSizeList;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.ColumnNameList;
import io.cdap.wrangler.api.parser.DirectiveName;
import io.cdap.wrangler.api.parser.Expression;
import io.cdap.wrangler.api.parser.Numeric;
import io.cdap.wrangler.api.parser.NumericList;
import io.cdap.wrangler.api.parser.Properties;
import io.cdap.wrangler.api.parser.Ranges;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TextList;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TimeDurationList;
import io.cdap.wrangler.api.parser.Token;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class <code>RecipeVisitor</code> implements the visitor pattern
 * used during traversal of the AST tree. The <code>ParserTree#Walker</code>
 * invokes appropriate methods as call backs with information about the node.
 *
 * <p>In order to understand what's being invoked, please look at the grammar file
 * <tt>Directive.g4</tt></p>.
 *
 * <p>This class exposes a <code>getTokenGroups</code> method for retrieving the
 * <code>RecipeSymbol</code> after visiting. The <code>RecipeSymbol</code> represents
 * all the <code>TokenGroup</code> for all directives in a recipe. Each directive
 * will create a <code>TokenGroup</code></p>
 *
 * <p> As the <code>ParseTree</code> is walking through the call graph, it generates
 * one <code>TokenGroup</code> for each directive in the recipe. Each <code>TokenGroup</code>
 * contains parsed <code>Tokens</code> for that directive along with more information like
 * <code>SourceInfo</code>. A collection of <code>TokenGroup</code> consistutes a <code>RecipeSymbol</code>
 * that is returned by this function.</p>
 */

 public final class RecipeVisitor extends DirectivesBaseVisitor<RecipeSymbol.Builder> {
  private final RecipeSymbol.Builder builder = new RecipeSymbol.Builder();

  /**
   * Returns the compiled RecipeSymbol after visiting.
   *
   * @return RecipeSymbol
   */
  public RecipeSymbol getCompiledUnit() {
    return builder.build();
  }

  @Override
  public RecipeSymbol.Builder visitDirective(
    final DirectivesParser.DirectiveContext ctx) {
    builder.createTokenGroup(getOriginalSource(ctx));
    return super.visitDirective(ctx);
  }

  @Override
  public RecipeSymbol.Builder visitPropertyList(
    final DirectivesParser.PropertyListContext ctx) {
    Map<String, Token> props = new HashMap<>();
    for (DirectivesParser.PropertyContext property : ctx.property()) {
      String identifier = property.Identifier().getText();
      Token token;
      if (property.Number() != null) {
        token = new Numeric(new LazyNumber(property.Number().getText()));
      } else if (property.Bool() != null) {
        token = new Bool(Boolean.parseBoolean(property.Bool().getText()));
      } else {
        String text = property.text().getText();
        token = new Text(text.substring(1, text.length() - 1));
      }
      props.put(identifier, token);
    }
    builder.addToken(new Properties(props));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitPragmaLoadDirective(
    final DirectivesParser.PragmaLoadDirectiveContext ctx) {
    for (TerminalNode identifier : ctx.identifierList().Identifier()) {
      builder.addLoadableDirective(identifier.getText());
    }
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitPragmaVersion(
    final DirectivesParser.PragmaVersionContext ctx) {
    builder.addVersion(ctx.Number().getText());
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitNumberRanges(
    final DirectivesParser.NumberRangesContext ctx) {
    List<Triplet<Numeric, Numeric, String>> output = new ArrayList<>();
    for (DirectivesParser.NumberRangeContext range : ctx.numberRange()) {
      List<TerminalNode> numbers = range.Number();
      String text = range.value().getText();
      if (text.startsWith("'") && text.endsWith("'")) {
        text = text.substring(1, text.length() - 1);
      }
      output.add(new Triplet<>(
        new Numeric(new LazyNumber(numbers.get(0).getText())),
        new Numeric(new LazyNumber(numbers.get(1).getText())), text));
    }
    builder.addToken(new Ranges(output));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitEcommand(
    final DirectivesParser.EcommandContext ctx) {
    builder.addToken(new DirectiveName(ctx.Identifier().getText()));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitColumn(
    final DirectivesParser.ColumnContext ctx) {
    builder.addToken(new ColumnName(ctx.Column().getText().substring(1)));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitText(
    final DirectivesParser.TextContext ctx) {
    String value = ctx.String().getText();
    builder.addToken(new Text(value.substring(1, value.length() - 1)));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitNumber(
    final DirectivesParser.NumberContext ctx) {
    builder.addToken(new Numeric(new LazyNumber(ctx.Number().getText())));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitBool(
    final DirectivesParser.BoolContext ctx) {
    builder.addToken(new Bool(Boolean.parseBoolean(ctx.Bool().getText())));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitCondition(
    final DirectivesParser.ConditionContext ctx) {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < ctx.getChildCount() - 1; ++i) {
      sb.append(ctx.getChild(i).getText()).append(" ");
    }
    builder.addToken(new Expression(sb.toString()));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitCommand(
    final DirectivesParser.CommandContext ctx) {
    builder.addToken(new DirectiveName(ctx.Identifier().getText()));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitColList(
    final DirectivesParser.ColListContext ctx) {
    List<String> names = new ArrayList<>();
    for (TerminalNode column : ctx.Column()) {
      names.add(column.getText().substring(1));
    }
    builder.addToken(new ColumnNameList(names));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitNumberList(
    final DirectivesParser.NumberListContext ctx) {
    List<LazyNumber> numerics = new ArrayList<>();
    for (TerminalNode number : ctx.Number()) {
      numerics.add(new LazyNumber(number.getText()));
    }
    builder.addToken(new NumericList(numerics));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitBoolList(
    final DirectivesParser.BoolListContext ctx) {
    List<Boolean> booleans = new ArrayList<>();
    for (TerminalNode bool : ctx.Bool()) {
      booleans.add(Boolean.parseBoolean(bool.getText()));
    }
    builder.addToken(new BoolList(booleans));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitStringList(
    final DirectivesParser.StringListContext ctx) {
    List<String> strs = new ArrayList<>();
    for (TerminalNode string : ctx.String()) {
      String text = string.getText();
      strs.add(text.substring(1, text.length() - 1));
    }
    builder.addToken(new TextList(strs));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitByteSize(
    final DirectivesParser.ByteSizeContext ctx) {
    builder.addToken(new ByteSize(ctx.getText()));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitByteSizeList(
    final DirectivesParser.ByteSizeListContext ctx) {
    List<ByteSize> byteSizes = new ArrayList<>();
    for (ParseTree child : ctx.children) {
      if (child instanceof DirectivesParser.ByteSizeContext) {
        byteSizes.add(new ByteSize(child.getText()));
      }
    }
    builder.addToken(new ByteSizeList(byteSizes));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitTimeDuration(
    final DirectivesParser.TimeDurationContext ctx) {
    builder.addToken(new TimeDuration(ctx.getText()));
    return builder;
  }

  @Override
  public RecipeSymbol.Builder visitTimeDurationList(
    final DirectivesParser.TimeDurationListContext ctx) {
    List<TimeDuration> durations = new ArrayList<>();
    for (ParseTree child : ctx.children) {
      if (child instanceof DirectivesParser.TimeDurationContext) {
        durations.add(new TimeDuration(child.getText()));
      }
    }
    builder.addToken(new TimeDurationList(durations));
    return builder;
  }

  /**
   * Helper method to extract original source text and position.
   *
   * @param ctx the parser rule context
   * @return SourceInfo containing position and raw text
   */
  private SourceInfo getOriginalSource(final ParserRuleContext ctx) {
    int a = ctx.getStart().getStartIndex();
    int b = ctx.getStop().getStopIndex();
    Interval interval = new Interval(a, b);
    String text = ctx.start.getInputStream().getText(interval);
    int lineno = ctx.getStart().getLine();
    int column = ctx.getStart().getCharPositionInLine();
    return new SourceInfo(lineno, column, text);
  }
}
