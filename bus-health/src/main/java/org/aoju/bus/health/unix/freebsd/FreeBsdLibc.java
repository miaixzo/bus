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
package org.aoju.bus.health.unix.freebsd;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.health.unix.CLibrary;

/**
 * C动态库,这个类应该被认为是非api的，因为如果/当
 * 它的代码被合并到JNA项目中时，它可能会被删除
 *
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
public interface FreeBsdLibc extends CLibrary {

    /**
     * 常量 <code>INSTANCE</code>
     */
    FreeBsdLibc INSTANCE = Native.load("libc", FreeBsdLibc.class);
    /**
     * 常量 <code>UINT64_SIZE=Native.getNativeSize(long.class)</code>
     */
    int UINT64_SIZE = Native.getNativeSize(long.class);
    /**
     * 常量 <code>INT_SIZE=Native.getNativeSize(int.class)</code>
     */
    int INT_SIZE = Native.getNativeSize(int.class);
    /**
     * 常量 <code>CPUSTATES=5</code>
     */
    int CPUSTATES = 5;
    /**
     * 常量 <code>CP_USER=0</code>
     */
    int CP_USER = 0;
    /**
     * 常量 <code>CP_NICE=1</code>
     */
    int CP_NICE = 1;
    /**
     * 常量 <code>CP_SYS=2</code>
     */
    int CP_SYS = 2;
    /**
     * 常量 <code>CP_INTR=3</code>
     */
    int CP_INTR = 3;
    /**
     * 常量 <code>CP_IDLE=4</code>
     */
    int CP_IDLE = 4;

    /**
     * 函数的作用是:检索系统信息，并允许具有适当权限的进程设置系统信息sysctl()提供的信息
     * 包括整数、字符串和表。
     * 状态是使用“管理信息库”(MIB)样式名来描述的，它列在name中，是一个整数的namelen长度数组.
     * 信息被复制到oldp指定的缓冲区中。缓冲区的大小由oldlenp在调用之前指定的位置给出，该位置
     * 给出在成功调用之后以及在返回错误代码ENOMEM的调用之后复制的数据量。如果可用的数据量大于
     * 提供的缓冲区的大小，则调用提供与提供的缓冲区相匹配的所有数据，并返回错误代码ENOMEM
     * 如果不需要旧值，oldp和oldlenp应该设置为NULL
     *
     * @param name    整数的MIB数组
     * @param namelen MIB数组的长度
     * @param oldp    信息检索
     * @param oldlenp 检索到的信息的大小
     * @param newp    待写信息
     * @param newlen  要写入的信息的大小
     * @return 0成功;设置errno失败
     */
    int sysctl(int[] name, int namelen, Pointer oldp, IntByReference oldlenp, Pointer newp, int newlen);

    /**
     * sysctlbyname()函数接受名称的ASCII表示形式，并在内部查找整数名称向量
     * 除此之外，它的行为与标准的sysctl()函数相同
     *
     * @param name    MIB名称的ASCII表示
     * @param oldp    信息检索
     * @param oldlenp 检索到的信息的大小
     * @param newp    待写信息
     * @param newlen  要写入的信息的大小
     * @return 0成功;设置errno失败
     */
    int sysctlbyname(String name, Pointer oldp, IntByReference oldlenp, Pointer newp, int newlen);

    /**
     * sysctlnametomib()函数接受名称的ASCII表示形式，查找整数名称向量，并返回mibp指向的mib数组
     * 中的数字表示形式。mib数组中的元素数量由调用前sizep指定的位置给出，而该位置给出了调用成功后
     * 复制的条目数量。在随后的sysctl()调用中，可以使用得到的mib和size来获得与所请求的ASCII名称
     * 相关联的数据。此接口用于希望重复请求相同变量的应用程序(sysctl()函数的运行时间约为通过
     * sysctlbyname()函数发出的相同请求的三分之一)
     *
     * @param name 名称的ASCII表示
     * @param mibp 包含对应名称向量的整数数组
     * @param size 输入时，返回数组中的元素数;在输出时，复制的项数
     * @return 0成功;设置errno失败
     */
    int sysctlnametomib(String name, Pointer mibp, IntByReference size);

    @FieldOrder({"cpu_ticks"})
    class CpTime extends Structure {
        public long[] cpu_ticks = new long[CPUSTATES];
    }

}
