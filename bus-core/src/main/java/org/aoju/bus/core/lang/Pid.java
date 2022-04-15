/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.StringKit;

import java.lang.management.ManagementFactory;

/**
 * 进程ID单例封装
 * 第一次访问时调用{@link ManagementFactory#getRuntimeMXBean()}获取PID信息，之后直接使用缓存值
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public enum Pid {

    INSTANCE;

    private final int pid;

    Pid() {
        this.pid = getPid();
    }

    /**
     * 获取当前进程ID，首先获取进程名称，读取@前的ID值，如果不存在，则读取进程名的hash值
     *
     * @return 进程ID
     * @throws InstrumentException 进程名称为空
     */
    private static int getPid() throws InstrumentException {
        final String processName = ManagementFactory.getRuntimeMXBean().getName();
        if (StringKit.isBlank(processName)) {
            throw new InstrumentException("Process name is blank!");
        }
        final int atIndex = processName.indexOf('@');
        if (atIndex > 0) {
            return Integer.parseInt(processName.substring(0, atIndex));
        } else {
            return processName.hashCode();
        }
    }

    /**
     * 获取PID值
     *
     * @return pid
     */
    public int get() {
        return this.pid;
    }

}
