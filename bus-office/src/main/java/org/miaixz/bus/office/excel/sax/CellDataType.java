/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.office.excel.sax;

/**
 * 单元格数据类型枚举
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * 计算结果类型，此类型使用f标签辅助判断，而非属性
     */
    FORMULA("formula"),
    /**
     * 富文本类型
     */
    INLINESTR("inlineStr"),
    /**
     * 共享字符串索引类型
     */
    SSTINDEX("s"),
    /**
     * 数字类型
     */
    NUMBER(""),
    /**
     * 日期类型，此类型使用值判断，而非属性
     */
    DATE("m/d/yy"),
    /**
     * 空类型
     */
    NULL("");

    /**
     * 属性值
     */
    private final String name;

    /**
     * 构造
     *
     * @param name 类型属性值
     */
    CellDataType(final String name) {
        this.name = name;
    }

    /**
     * 类型字符串转为枚举
     *
     * @param name 类型字符串
     * @return 类型枚举
     */
    public static CellDataType of(final String name) {
        if (null == name) {
            //默认空
            return NULL;
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
