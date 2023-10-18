/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org mybatis.io and other contributors.           *
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
package org.aoju.bus.mapper.additional.dialect.oracle;

import org.aoju.bus.mapper.annotation.KeySql;
import org.aoju.bus.mapper.annotation.RegisterMapper;
import org.apache.ibatis.annotations.InsertProvider;

import java.util.List;

/**
 * Oracle批量插入
 * 支持@{@link KeySql#genId()}，不支持@{@link KeySql#sql()}
 * 因INSERT ALL语法不支持序列，可手工获取序列并设置至Entity或绑定触发器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@RegisterMapper
public interface InsertListMapper<T> {

    /**
     * 批量操作
     *
     * @param recordList 记录值
     * @return the int
     */
    @InsertProvider(type = OracleProvider.class, method = "dynamicSQL")
    int insertList(List<? extends T> recordList);

}
