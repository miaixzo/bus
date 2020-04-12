/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.office.support.excel.sax;

import org.aoju.bus.core.lang.Normal;

/**
 * 单元格数据类型枚举
 *
 * @author Kimi Liu
 * @version 5.8.3
 * @since JDK 1.8+
 */
public enum CellDataType {
    /**
     * Boolean类型
     */
    BOOL("b"),
    /**
     * 类型错误
     */
    ERROR("e"),
    /**
     * 计算结果类型
     */
    FORMULA("str"),
    /**
     * 富文本类型
     */
    INLINESTR("inlineStr"),
    /**
     * 字符串类型
     */
    SSTINDEX("s"),
    /**
     * 数字类型
     */
    NUMBER(Normal.EMPTY),
    /**
     * 日期类型
     */
    DATE("m/d/yy"),
    /**
     * 空类型
     */
    NULL(Normal.EMPTY);

    /**
     * 属性值
     */
    private String name;

    /**
     * 构造
     *
     * @param name 类型属性值
     */
    CellDataType(String name) {
        this.name = name;
    }

    /**
     * 类型字符串转为枚举
     *
     * @param name 类型字符串
     * @return 类型枚举
     */
    public static CellDataType of(String name) {
        if (null == name) {
            //默认数字
            return NUMBER;
        }

        if (BOOL.name.equals(name)) {
            return BOOL;
        } else if (ERROR.name.equals(name)) {
            return ERROR;
        } else if (INLINESTR.name.equals(name)) {
            return INLINESTR;
        } else if (SSTINDEX.name.equals(name)) {
            return SSTINDEX;
        } else if (FORMULA.name.equals(name)) {
            return FORMULA;
        } else {
            return NULL;
        }
    }

    /**
     * 获取对应类型的属性值
     *
     * @return 属性值
     */
    public String getName() {
        return name;
    }

}
