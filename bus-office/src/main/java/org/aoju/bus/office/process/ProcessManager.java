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
package org.aoju.bus.office.process;

import java.io.IOException;

/**
 * 提供管理正在运行的流程所需的服务.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ProcessManager {

    /**
     * 查找具有指定命令行的运行进程的PID.
     *
     * @param query 用于查找带有要pid的进程的查询.
     * @return 如果没有找到，则使用pid;
     * 如果没有找到，则使用{@link org.aoju.bus.office.Builder#PID_NOT_FOUND};
     * 如果没有找到，则使用{@link org.aoju.bus.office.Builder#PID_UNKNOWN}
     * @throws IOException 如果IO错误发生.
     */
    long find(ProcessQuery query) throws IOException;

    /**
     * 终止指定的进程
     *
     * @param process 进程信息.
     * @param pid     进程对应pid.
     * @throws IOException 如果IO错误发生.
     */
    void kill(Process process, long pid) throws IOException;

}
