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
package org.miaixz.bus.mapper.mapping;

import java.util.List;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.ORDER;

/**
 * SPI 接口：对最终的 SQL 进行处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SqlScriptWrapper extends ORDER {

    /**
     * 包装 SQL 脚本，依次应用所有 SqlScriptWrapper 实现
     *
     * @param context   当前接口和方法信息
     * @param entity    实体类信息
     * @param sqlScript SQL 脚本
     * @return 包装后的 SQL 脚本
     */
    static SqlScript wrapSqlScript(ProviderContext context, MapperTable entity, SqlScript sqlScript) {
        for (SqlScriptWrapper wrapper : Holder.sqlScriptWrappers) {
            sqlScript = wrapper.wrap(context, entity, sqlScript);
        }
        return sqlScript;
    }

    /**
     * 对 SQL 脚本进行加工处理
     *
     * @param context   当前接口和方法信息
     * @param entity    实体类信息
     * @param sqlScript SQL 脚本
     * @return 加工后的 SQL 脚本
     */
    SqlScript wrap(ProviderContext context, MapperTable entity, SqlScript sqlScript);

    /**
     * 实例持有类，管理 SqlScriptWrapper 的 SPI 实现
     */
    class Holder {
        /**
         * 通过 SPI 加载的 SqlScriptWrapper 实现列表
         */
        static final List<SqlScriptWrapper> sqlScriptWrappers = NormalSpiLoader.loadList(false, SqlScriptWrapper.class);
    }

}