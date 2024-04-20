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
package org.miaixz.bus.logger;

import org.miaixz.bus.logger.level.*;

/**
 * 日志统一接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Log extends TraceLog, DebugLog, InfoLog, WarnLog, ErrorLog {

    /**
     * @return 日志对象的Name
     */
    String getName();

    /**
     * 是否开启指定日志
     *
     * @param level 日志级别
     * @return 是否开启指定级别
     */
    boolean isEnabled(Level level);

    /**
     * 打印指定级别的日志
     *
     * @param level     级别
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(Level level, String format, Object... arguments);

    /**
     * 打印 指定级别的日志
     *
     * @param level     级别
     * @param t         错误对象
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(Level level, Throwable t, String format, Object... arguments);

    /**
     * 打印 ERROR 等级的日志
     *
     * @param fqcn      完全限定类名(Fully Qualified Class Name),用于定位日志位置
     * @param level     级别
     * @param t         错误对象
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(String fqcn, Level level, Throwable t, String format, Object... arguments);

}
