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
package org.miaixz.bus.shade.screw.process;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for data processing, specifically for filtering tables.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
public class ProcessConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * A list of table names to ignore during processing.
     */
    private List<String> ignoreTableName;
    /**
     * A list of table name prefixes to ignore. Tables with these prefixes will be excluded.
     */
    private List<String> ignoreTablePrefix;
    /**
     * A list of table name suffixes to ignore. Tables with these suffixes will be excluded.
     */
    private List<String> ignoreTableSuffix;
    /**
     * A list of specific table names to include. If specified, only these tables will be processed.
     */
    private List<String> designatedTableName;
    /**
     * A list of table name prefixes to include. If specified, only tables with these prefixes will be processed.
     */
    private List<String> designatedTablePrefix;
    /**
     * A list of table name suffixes to include. If specified, only tables with these suffixes will be processed.
     */
    private List<String> designatedTableSuffix;

}
