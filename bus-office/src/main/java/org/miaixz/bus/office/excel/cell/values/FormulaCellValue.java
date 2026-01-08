/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.office.excel.cell.values;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.miaixz.bus.office.excel.cell.setters.CellSetter;

/**
 * Represents a formula type cell value.
 *
 * <ul>
 * <li>In SAX read mode, this object is used to receive the cell's formula and formula result information.</li>
 * <li>In write mode, it is used to define the cell type to be written as a formula.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormulaCellValue implements CellValue<String>, CellSetter {

    /**
     * The formula string.
     */
    private final String formula;
    /**
     * The result of the formula. This can be omitted when using ExcelWriter.
     */
    private final Object result;
    /**
     * The type of the formula result.
     */
    private final CellType resultType;

    /**
     * Constructs a new {@code FormulaCellValue} with the specified formula.
     *
     * @param formula The formula string.
     */
    public FormulaCellValue(final String formula) {
        this(formula, null);
    }

    /**
     * Constructs a new {@code FormulaCellValue} with the specified formula and result.
     *
     * @param formula The formula string.
     * @param result  The result of the formula.
     */
    public FormulaCellValue(final String formula, final Object result) {
        this(formula, result, null);
    }

    /**
     * Constructs a new {@code FormulaCellValue} with the specified formula, result, and result type.
     *
     * @param formula    The formula string.
     * @param result     The result of the formula.
     * @param resultType The type of the formula result.
     */
    public FormulaCellValue(final String formula, final Object result, final CellType resultType) {
        this.formula = formula;
        this.result = result;
        this.resultType = resultType;
    }

    /**
     * Gets the type of the formula result.
     *
     * @return The result type. {@code null} indicates that the type is not explicitly defined.
     */
    public CellType getResultType() {
        return this.resultType;
    }

    /**
     * Gets the formula string from this cell value.
     *
     * @return The formula string.
     */
    @Override
    public String getValue() {
        return this.formula;
    }

    /**
     * Sets the formula to the specified cell.
     *
     * @param cell The {@link Cell} to set the formula to.
     */
    @Override
    public void setValue(final Cell cell) {
        cell.setCellFormula(this.formula);
    }

    /**
     * Gets the result of the formula.
     *
     * @return The result.
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Returns the string representation of the formula result.
     *
     * @return The string representation of the result.
     */
    @Override
    public String toString() {
        return getResult().toString();
    }

}
