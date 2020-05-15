/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

/**
 * 提供进程相关支持
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {

    Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * 检索指定进程的进程关联掩码和系统的系统关联掩码
     *
     * @param hProcess              进程的句柄，需要它的关联掩码
     *                              该句柄必须具有{@link WinNT#PROCESS_QUERY_INFORMATION}
     *                              或{@link WinNT#PROCESS_QUERY_LIMITED_INFORMATION}访问权限
     * @param lpProcessAffinityMask 指向接收指定进程的关联掩码的变量的指针
     * @param lpSystemAffinityMask  指向接收系统关联掩码的变量的指针
     * @return 如果函数成功，则返回{@code true}，并将{@code lpProcessAffinityMask}
     * 和{@code lpSystemAffinityMask}所指向的变量设置为适当的关联掩码
     */
    boolean GetProcessAffinityMask(HANDLE hProcess,
                                   ULONG_PTRByReference lpProcessAffinityMask,
                                   ULONG_PTRByReference lpSystemAffinityMask);

}
