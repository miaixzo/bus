/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.health.common.unix;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.unix.LibCAPI;
import com.sun.jna.ptr.PointerByReference;

/**
 * C动态库，包含所有基于*nix的操作系统的公共代码。这个类应该被认为是非api的，
 * 因为如果/当它的代码被合并到JNA项目中时，它可能会被删除。
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public interface CLibrary extends LibCAPI, Library {

    /**
     * 常量 <code>AI_CANONNAME=2</code>
     */
    int AI_CANONNAME = 2;

    /**
     * 返回调用进程的进程ID。ID保证是惟一的，对于构造临时文件名很有用
     *
     * @return 调用进程的进程ID
     */
    int getpid();

    /**
     * getaddrinfo()返回一个或多个addrinfo结构，其中每个结构
     * 包含一个Internet地址，可以在绑定(2)或连接(2)的调用中指定
     *
     * @param node    一个数字网络地址或网络主机名，其网络地址被查找并解析
     * @param service 设置每个返回地址结构中的端口
     * @param hints   指定选择由res指向的列表中返回的套接字地址结构的条件
     * @param res     返回地址结构
     * @return 0成功;设置errno失败
     */
    int getaddrinfo(String node, String service, Addrinfo hints, PointerByReference res);

    /**
     * 释放为动态分配的链表res分配的内存
     *
     * @param res 指向getaddrinfo返回的链表的指针
     */
    void freeaddrinfo(Pointer res);

    /**
     * 将getaddrinfo错误代码转换为人类可读的字符串，适合于错误报告
     *
     * @param e 来自getaddrinfo的错误代码
     * @return 人类可读的错误代码版本
     */
    String gai_strerror(int e);

    /**
     * 将符号链接路径的内容放在大小为bufsiz的缓冲区buf中
     *
     * @param path    一个符号链接
     * @param buf     包含符号链接指向的位置的实际路径
     * @param bufsize 缓冲区中的数据大小
     * @return readlink()将符号链接路径的内容放在大小为bufsiz的缓冲区buf中
     * readlink()不会向buf追加空字节。它将截断内容(长度为bufsiz字符)，以防缓冲区太小而容纳不了所有内容
     */
    int readlink(String path, Pointer buf, int bufsize);

    /**
     * 返回类型为BSD sysctl kernel .boottime
     */
    @FieldOrder({"tv_sec", "tv_usec"})
    class Timeval extends Structure {
        /**
         * 秒
         */
        public long tv_sec;
        /**
         * 微秒
         */
        public long tv_usec;
    }

    @FieldOrder({"sa_family", "sa_data"})
    class Sockaddr extends Structure {

        public short sa_family;
        public byte[] sa_data = new byte[14];

        public static class ByReference extends Sockaddr implements Structure.ByReference {
        }

    }

    @FieldOrder({"ai_flags", "ai_family", "ai_socktype", "ai_protocol", "ai_addrlen", "ai_addr", "ai_canonname", "ai_next"})
    class Addrinfo extends Structure {

        public int ai_flags;
        public int ai_family;
        public int ai_socktype;
        public int ai_protocol;
        public int ai_addrlen;
        public Sockaddr.ByReference ai_addr;
        public String ai_canonname;
        public ByReference ai_next;

        public Addrinfo() {
        }

        public Addrinfo(Pointer p) {
            super(p);
            read();
        }

        public static class ByReference extends Addrinfo implements Structure.ByReference {

        }
    }

}
