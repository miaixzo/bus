/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.writer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.office.excel.cell.VirtualCell;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.SheetKit;

/**
 * Template context, which records the cells where variables are located in the template.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TemplateContext {

    /**
     * Regular expression for variables.
     * <ol>
     * <li>Variable names can only contain letters, numbers, underscores, dollar signs, and periods, and cannot start
     * with a number.</li>
     * <li>Variables start with '{' and end with '}'.</li>
     * <li>'\{' indicates an escaped character, not a variable symbol.</li>
     * <li>Variables starting with '.' indicate a list. If '.' appears in the middle, it indicates a sub-object of an
     * expression.</li>
     * </ol>
     */
    private static final Pattern VAR_PATTERN = Pattern.compile("(?<!\\\\)\\{([.$_a-zA-Z]+\\d*[.$_a-zA-Z]*)}");
    private static final Pattern ESCAPE_VAR_PATTERN = Pattern.compile("\\\\\\{([.$_a-zA-Z]+\\d*[.$_a-zA-Z]*)\\\\}");

    /**
     * Map storing the mapping between variable names and their corresponding cells.
     */
    private final Map<String, Cell> varMap = new LinkedHashMap<>();

    /**
     * Constructs a new {@code TemplateContext}.
     *
     * @param templateSheet The template sheet.
     */
    public TemplateContext(final Sheet templateSheet) {
        init(templateSheet);
    }

    /**
     * Gets the cell corresponding to the variable name. List variables start with a dot.
     *
     * @param varName The variable name.
     * @return The {@link Cell} object.
     */
    public Cell getCell(final String varName) {
        return varMap.get(varName);
    }

    /**
     * Gets the bottom index of the row corresponding to the current filled data variable. This method is used to obtain
     * the filled row to shift rows after the filled row.
     * <ul>
     * <li>If it is an actual cell, it is filled directly without shifting, returning 0.</li>
     * <li>If it is a {@link VirtualCell}, it returns the row number of the bottom-most virtual cell.</li>
     * </ul>
     *
     * @param rowDataBean The data bean to fill.
     * @return The maximum row index. -1 indicates no data filled, 0 indicates no shifting needed.
     */
    public int getBottomRowIndex(final Object rowDataBean) {
        final AtomicInteger bottomRowIndex = new AtomicInteger(-1);
        this.varMap.forEach((name, cell) -> {
            if (null != BeanKit.getProperty(rowDataBean, name)) {
                if (cell instanceof VirtualCell) {
                    bottomRowIndex.set(Math.max(bottomRowIndex.get(), cell.getRowIndex()));
                } else if (bottomRowIndex.get() < 0) {
                    // Actual cell, fill directly, no need to shift.
                    bottomRowIndex.set(0);
                }
            }
        });
        return bottomRowIndex.get();
    }

    /**
     * Fills the cell pointed to by the variable name.
     *
     * @param rowDataBean The key-value pair of a row of data.
     * @param isListVar   {@code true} if it is a list fill (automatically points to the next column), {@code false}
     *                    otherwise (variable is deleted after filling).
     */
    public void fill(final Object rowDataBean, final boolean isListVar) {
        final Map<String, Cell> varMap = this.varMap;
        varMap.forEach((name, cell) -> {
            if (null == cell) {
                return;
            }

            final String templateStr = cell.getStringCellValue();
            // Fill cell.
            if (fill(cell, name, rowDataBean)) {
                // Point to the next cell.
                putNext(name, cell, templateStr, isListVar);
            }
        });

        if (!isListVar) {
            // Clear variables that have been matched.
            MapKit.removeNullValue(varMap);
        }
    }

    /**
     * Points the variable to the cell in the next row. If it is a list, it points to a virtual cell in the next row
     * (without creating a physical cell). If it is not a list, this variable is cleared.
     *
     * @param name        The variable name.
     * @param currentCell The current cell.
     * @param templateStr The template string.
     * @param isListVar   {@code true} if it is a list fill, {@code false} otherwise.
     */
    private void putNext(final String name, final Cell currentCell, final String templateStr, final boolean isListVar) {
        if (isListVar) {
            // Point to the cell in the next column.
            final Cell next = new VirtualCell(currentCell, currentCell.getColumnIndex(), currentCell.getRowIndex() + 1,
                    templateStr);
            varMap.put(name, next);
        } else {
            // Not a list, one-time fill, meaning the variable is disassociated from this cell after filling.
            varMap.put(name, null);
        }
    }

    /**
     * Fills data into a cell.
     *
     * @param cell        The cell. If it is not a variable cell in the template, it is a {@link VirtualCell}.
     * @param name        The variable name.
     * @param rowDataBean The data to fill, can be a {@link Map} or a Bean.
     * @return {@code true} if filling is successful, {@code false} if no data is available for filling.
     */
    private boolean fill(Cell cell, final String name, final Object rowDataBean) {
        final String templateStr = cell.getStringCellValue();
        if (cell instanceof VirtualCell) {
            // Virtual cell, convert to actual cell.
            final Cell newCell;

            newCell = CellKit.getCell(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex(), true);
            Assert.notNull(newCell, "Can not get or create cell at {},{}", cell.getColumnIndex(), cell.getRowIndex());

            newCell.setCellStyle(cell.getCellStyle());
            cell = newCell;
        }

        final Object cellValue;
        // Template replacement.
        if (StringKit.equals(name, StringKit.unWrap(templateStr, Symbol.BRACE_LEFT, Symbol.BRACE_RIGHT))) {
            // A cell has only one variable, supporting multi-level expressions.
            cellValue = BeanKit.getProperty(rowDataBean, name);
            if (null == cellValue) {
                // No value provided for the corresponding expression, skip filling.
                return false;
            }
        } else {
            // Multiple variables or template filling in the template, assign directly as String.
            // Variables for which no value is found remain unchanged.
            cellValue = StringKit.formatByBean(templateStr, rowDataBean, true);
            if (ObjectKit.equals(cellValue, templateStr)) {
                // No changes in the template, meaning no variable replacement, skip filling.
                return false;
            }
        }
        CellKit.setCellValue(cell, cellValue);
        return true;
    }

    /**
     * Initializes the template context by extracting variables and their positions, and replacing escaped variables.
     *
     * @param templateSheet The template sheet.
     */
    private void init(final Sheet templateSheet) {
        SheetKit.walk(templateSheet, (cell, ctx) -> {
            if (CellType.STRING == cell.getCellType()) {
                // Only read string type cells.
                final String cellValue = cell.getStringCellValue();

                // A string may contain multiple variables.
                final List<String> vars = PatternKit.findAllGroup1(VAR_PATTERN, cellValue);
                if (CollKit.isNotEmpty(vars)) {
                    // Template variables.
                    for (final String var : vars) {
                        varMap.put(var, cell);
                    }
                }

                // Replace escaped variables.
                final String text = PatternKit.replaceAll(
                        cellValue,
                        ESCAPE_VAR_PATTERN,
                        (matcher) -> Symbol.BRACE_LEFT + matcher.group(1) + Symbol.BRACE_RIGHT);
                if (!StringKit.equals(cellValue, text)) {
                    cell.setCellValue(text);
                }
            }
        });
    }

    @Override
    public String toString() {
        return "TemplateContext{" + "varMap=" + varMap + '}';
    }

}
