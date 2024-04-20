/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.shade.screw.dialect;

import lombok.Data;
import org.miaixz.bus.core.exception.InternalException;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/**
 * 数据库查询工厂
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class DatabaseQueryFactory implements Serializable {

    /**
     * DataSource
     */
    private DataSource dataSource;

    /**
     * 构造函数私有化
     * 禁止通过new方式实例化对象
     */
    private DatabaseQueryFactory() {

    }

    /**
     * 构造函数
     *
     * @param source {@link DataSource}
     */
    public DatabaseQueryFactory(DataSource source) {
        dataSource = source;
    }

    /**
     * 获取配置的数据库类型实例
     *
     * @return {@link DatabaseQuery} 数据库查询对象
     */
    public DatabaseQuery newInstance() {
        try {
            // 获取数据库URL 用于判断数据库类型
            String url = this.getDataSource().getConnection().getMetaData().getURL();
            // 获取实现类
            Class<? extends DatabaseQuery> query = DatabaseType.getDbType(url).getImplClass();
            // 获取有参构造
            Constructor<? extends DatabaseQuery> constructor = query
                    .getConstructor(DataSource.class);
            // 实例化
            return constructor.newInstance(dataSource);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException | SQLException e) {
            throw new InternalException(e);
        }
    }

}
