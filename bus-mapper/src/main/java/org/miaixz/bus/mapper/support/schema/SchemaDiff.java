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
package org.miaixz.bus.mapper.support.schema;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Risk;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Schema difference between entity metadata and a database snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class SchemaDiff {

    /**
     * Schema behavior type represented by the difference.
     */
    private Behavior type;

    /**
     * Risk level associated with the difference.
     */
    private Risk riskLevel;

    /**
     * Entity table metadata associated with the difference.
     */
    private TableMeta tableMeta;

    /**
     * Entity column metadata associated with the difference.
     */
    private ColumnMeta columnMeta;

    /**
     * Database column snapshot associated with the difference.
     */
    private ColumnSnapshot actualColumn;

    /**
     * Index metadata associated with the difference.
     */
    private IndexMeta index;

    /**
     * Human-readable difference message.
     */
    private String message;

    /**
     * Creates a schema difference.
     *
     * @param type      the behavior type
     * @param riskLevel the risk level
     * @param table     the table metadata
     * @param message   the difference message
     * @return the schema difference
     */
    public static SchemaDiff of(Behavior type, Risk riskLevel, TableMeta table, String message) {
        return new SchemaDiff().type(type).riskLevel(riskLevel).tableMeta(table).message(message);
    }

}
