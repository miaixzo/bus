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
package org.miaixz.bus.office.excel.reader;

import java.util.List;

/**
 * Shared state models used by streaming read callbacks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ExcelReadState {

    private ExcelReadState() {

    }

    /**
     * Workbook metadata used by streaming callbacks.
     *
     * @param sourceName source file name or logical name
     * @param xlsx       whether the workbook is xlsx
     * @param sheets     resolved sheet list, may be empty when unavailable
     */
    public record WorkbookContext(String sourceName, boolean xlsx, List<SheetContext> sheets) {

    }

    /**
     * Sheet metadata used by streaming callbacks.
     *
     * @param sheetIndex zero-based sheet index
     * @param sheetName  sheet name, may be blank when unavailable
     */
    public record SheetContext(int sheetIndex, String sheetName) {

    }

    /**
     * Progress snapshot for streaming reads.
     *
     * @param workbook         workbook context
     * @param currentSheet     current sheet context, may be {@code null}
     * @param processedRows    processed row count after filtering
     * @param currentSheetRows processed rows in current sheet after filtering
     * @param currentRowIndex  last observed sheet row index
     * @param elapsedMillis    elapsed milliseconds since read start
     */
    public record Progress(WorkbookContext workbook, SheetContext currentSheet, long processedRows,
            long currentSheetRows, long currentRowIndex, long elapsedMillis) {
    }

}
