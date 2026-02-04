/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.reader;

import org.miaixz.bus.office.excel.ExcelConfig;

/**
 * Excel read configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelReadConfig extends ExcelConfig {

    /**
     * Whether to ignore empty rows.
     */
    protected boolean ignoreEmptyRow = true;

    /**
     * Checks whether empty rows should be ignored.
     *
     * @return {@code true} if empty rows are ignored, {@code false} otherwise.
     */
    public boolean isIgnoreEmptyRow() {
        return this.ignoreEmptyRow;
    }

    /**
     * Sets whether empty rows should be ignored.
     *
     * @param ignoreEmptyRow {@code true} to ignore empty rows, {@code false} otherwise.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setIgnoreEmptyRow(final boolean ignoreEmptyRow) {
        this.ignoreEmptyRow = ignoreEmptyRow;
        return this;
    }

}
