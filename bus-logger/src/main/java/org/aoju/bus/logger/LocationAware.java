/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.logger;

import org.aoju.bus.logger.level.Level;

/**
 * 位置感知日志接口，此接口用于通过传入当前全类名的方式来感知日志打印的行号<br>
 * 此方法主要用于在继续封装日志实现的时候行号错误的问题，继续封装时传入封装类的全类名即可解决这个问题
 *
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public interface LocationAware extends Log {

    /**
     * 打印 指定级别的日志
     *
     * @param fqcn      完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
     * @param level     级别
     * @param t         错误对象
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(String fqcn, Level level, Throwable t, String format, Object... arguments);
}
