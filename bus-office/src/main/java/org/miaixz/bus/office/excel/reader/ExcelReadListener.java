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

/**
 * Listener for streaming read lifecycle callbacks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ExcelReadListener {

    /**
     * Called before workbook streaming starts.
     *
     * @param workbook workbook context.
     */
    default void onWorkbookStart(final ExcelReadState.WorkbookContext workbook) {
        // pass
    }

    /**
     * Called when a new sheet starts producing rows.
     *
     * @param sheet sheet context.
     */
    default void onSheetStart(final ExcelReadState.SheetContext sheet) {
        // pass
    }

    /**
     * Called when the current sheet ends.
     *
     * @param sheet sheet context.
     */
    default void onSheetEnd(final ExcelReadState.SheetContext sheet) {
        // pass
    }

    /**
     * Called periodically while streaming rows.
     *
     * @param progress progress snapshot.
     */
    default void onProgress(final ExcelReadState.Progress progress) {
        // pass
    }

    /**
     * Called after workbook streaming ends.
     *
     * @param progress final progress snapshot.
     */
    default void onWorkbookEnd(final ExcelReadState.Progress progress) {
        // pass
    }

}
