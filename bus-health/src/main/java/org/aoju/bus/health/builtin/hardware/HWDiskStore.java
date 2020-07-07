/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;

import java.util.List;

/**
 * 一种存储机构，其中数据通过各种电子、磁、光或机械变化记录到一个或多个旋转磁盘
 * 或闪存(如可移动或固态驱动器)的表面层。与文件系统相比，定义操作系统使用存储的
 * 方式，磁盘存储代表文件系统用于文件存储的硬件
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
@ThreadSafe
public interface HWDiskStore {

    /**
     * 磁盘的名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 磁盘模式
     *
     * @return 模式
     */
    String getModel();

    /**
     * 磁盘序列号(如果可用)
     *
     * @return 序列号
     */
    String getSerial();

    /**
     * 磁盘的大小
     *
     * @return 磁盘大小，以字节为单位
     */
    long getSize();

    /**
     * 从磁盘读取的次数
     *
     * @return 操作次数
     */
    long getReads();

    /**
     * 从磁盘读取的字节数
     *
     * @return 读取的字节数
     */
    long getReadBytes();

    /**
     * 写入磁盘的次数
     *
     * @return 操作次数
     */
    long getWrites();

    /**
     * 写入磁盘的字节数
     *
     * @return 写的字节数
     */
    long getWriteBytes();

    /**
     * 磁盘队列的长度(#I/O's in progress)
     * 包括已经发送到设备驱动程序但尚未完成的I/O请求
     * macOS上不支持
     *
     * @return 当前磁盘队列长度
     */
    long getCurrentQueueLength();

    /**
     * 读或写所用的时间，以毫秒为单位
     *
     * @return 传输时间
     */
    long getTransferTime();

    /**
     * 这个磁盘上的分区
     *
     * @return 这个驱动器上分区的 {@code UnmodifiableList}
     */
    List<HWPartition> getPartitions();

    /**
     * 此磁盘的统计信息更新的时间
     *
     * @return 时间戳，以毫秒为单位，从epoch开始
     */
    long getTimeStamp();

    /**
     * 尽最大努力更新有关驱动器的所有统计信息，而不需要重新创建驱动器列表
     * 此方法提供了更频繁的单个驱动器统计信息的定期更新，但如果更新所有驱动器
     * 使用此方法的效率可能会更低。它不会检测可移动驱动器是否已被删除
     * 并在方法调用之间被另一个驱动器替换
     *
     * @return 如果更新(可能)成功，则为True;如果没有找到磁盘，则为false
     */
    boolean updateAttributes();

}
