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
package org.miaixz.bus.office.excel.writer;

import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Template Excel writer. Parses existing templates and fills template variables with data.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetTemplateWriter {

    /**
     * The sheet being written to.
     */
    private final Sheet sheet;
    /**
     * Excel output configuration.
     */
    private final ExcelWriteConfig config;
    /**
     * Template context, storing variables and their position information in the template.
     */
    private final TemplateContext templateContext;

    /**
     * Constructs a new {@code SheetTemplateWriter}.
     *
     * @param sheet  The {@link Sheet} to write to.
     * @param config The Excel write configuration.
     */
    public SheetTemplateWriter(final Sheet sheet, final ExcelWriteConfig config) {
        this.sheet = sheet;
        this.config = config;
        this.templateContext = new TemplateContext(sheet);
    }

    /**
     * Fills non-list template variables (one-time variables).
     *
     * @param rowMap The row data map.
     * @return This {@code SheetTemplateWriter} instance, for chaining.
     */
    public SheetTemplateWriter fillOnce(final Map<?, ?> rowMap) {
        this.templateContext.fill(rowMap, false);
        return this;
    }

    /**
     * Fills a template row, used for list filling.
     *
     * @param rowBean The row data as a Bean or Map.
     * @return This {@code SheetTemplateWriter} instance, for chaining.
     */
    public SheetTemplateWriter fillRow(final Object rowBean) {
        if (this.config.insertRow) {
            // All existing rows below the current filled template row are shifted down.
            final int bottomRowIndex = this.templateContext.getBottomRowIndex(rowBean);
            if (bottomRowIndex < 0) {
                // No fillable rows.
                return this;
            }
            if (bottomRowIndex != 0) {
                final int lastRowNum = this.sheet.getLastRowNum();
                if (bottomRowIndex <= lastRowNum) {
                    // If the bottom of the filled row has data, otherwise skip.
                    // The row number of the virtual row is the row to be filled, and the existing data in this row is
                    // shifted down.
                    this.sheet.shiftRows(bottomRowIndex, this.sheet.getLastRowNum(), 1);
                }
            }
        }

        this.templateContext.fill(rowBean, true);

        return this;
    }

}
