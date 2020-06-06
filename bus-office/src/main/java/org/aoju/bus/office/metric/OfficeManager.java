/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.builtin.MadeInOffice;

/**
 * office管理器知道如何执行{@link MadeInOffice}。在执行转换任务之前必须启动office管理器，
 * 并且在不再需要它时必须停止它。停止后就无法重新启动office管理器
 *
 * @author Kimi Liu
 * @version 5.9.8
 * @since JDK 1.8+
 */
public interface OfficeManager {

    /**
     * 执行指定的任务并阻塞，直到任务终止
     *
     * @param task 要执行的任务
     * @throws InstrumentException 如果发生错误
     */
    void execute(MadeInOffice task) throws InstrumentException;

    /**
     * 获取管理器是否正在运行
     *
     * @return 如果管理器正在运行，则为{@code true}，否则为{@code false}
     */
    boolean isRunning();

    /**
     * 启动管理器
     *
     * @throws InstrumentException 如果管理器不能启动
     */
    void start() throws InstrumentException;

    /**
     * 停止管理器
     *
     * @throws InstrumentException 如果管理器不能停止
     */
    void stop() throws InstrumentException;

}
