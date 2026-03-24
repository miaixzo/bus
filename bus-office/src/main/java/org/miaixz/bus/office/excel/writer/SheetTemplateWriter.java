/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.writer;

import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Template Excel writer. Parses existing templates and fills template variables with data.
 *
 * @author Kimi Liu
 * @since Java 21+
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
