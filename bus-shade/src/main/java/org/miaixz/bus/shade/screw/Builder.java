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
package org.miaixz.bus.shade.screw;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.shade.screw.execute.ProduceExecute;

/**
 * 默认常量
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * 百分号
     */
    public static final String PERCENT_SIGN = Symbol.PERCENT;
    /**
     * 暂未支持
     */
    public static final String NOT_SUPPORTED = "Not supported yet!";

    /**
     * 默认国际化
     */
    public static final String DEFAULT_LOCALE = "zh_CN";
    /**
     * Mac
     */
    public static final String MAC = "Mac";
    /**
     * Windows
     */
    public static final String WINDOWS = "Windows";
    /**
     * 小数点0
     */
    public static final String ZERO_DECIMAL_DIGITS = "0";
    /**
     * 默认描述
     */
    public static final String DESCRIPTION = "数据库设计文档";
    /**
     * mysql useInformationSchema
     */
    public static final String USE_INFORMATION_SCHEMA = "useInformationSchema";
    /**
     * oracle 连接参数备注
     */
    public static final String ORACLE_REMARKS = "remarks";
    /**
     * 零
     */
    public static final String ZERO = "0";
    /**
     * N
     */
    public static final String N = "N";
    /**
     * Y
     */
    public static final String Y = "Y";

    /**
     * 创建数据结构文档
     *
     * @param config 配置
     */
    public static void createFile(Config config) {
        new ProduceExecute(config).execute();
    }

}
