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
package org.miaixz.bus.mapper.support.keygen;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.miaixz.bus.mapper.parsing.SqlSourceEnhancer;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * The default primary key enhancer, which handles primary key generation logic before insertion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrimaryKeyEnhancer implements SqlSourceEnhancer {

    /**
     * Customizes the SQL source based on the primary key generator type.
     *
     * @param sqlSource The original SQL source.
     * @param entity    The entity table information.
     * @param ms        The {@link MappedStatement} object.
     * @param context   The provider context, containing method and interface information.
     * @return The customized SQL source.
     */
    @Override
    public SqlSource customize(SqlSource sqlSource, TableMeta entity, MappedStatement ms, ProviderContext context) {
        if (ms.getKeyGenerator() != null && ms.getKeyGenerator() instanceof GenIdKeyGenerator) {
            return new GenIdSqlSource(sqlSource, (GenIdKeyGenerator) ms.getKeyGenerator());
        }
        return sqlSource;
    }

}
