/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.source;

import java.io.Closeable;
import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

/**
 * 提供一个字节流 使用此接口从任何地方读取数据 它的位置:来自网络、存储或内存中的缓冲区 来源可能 分层以转换提供的数据,例如解压、解密或移除协议框架
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Source extends Closeable {

    /**
     * 从中删除至少1个字节，最多为{@code byteCount}字节， 并将它们 附加到{@code sink}。返回读取的字节数，如果该源已耗尽，则返回-1
     *
     * @param sink      缓冲
     * @param byteCount 长度大小
     * @return the long
     * @throws IOException 异常
     */
    long read(Buffer sink, long byteCount) throws IOException;

    /**
     * 返回此源的超时时间.
     *
     * @return 超时时间
     */
    Timeout timeout();

    /**
     * 关闭此源并释放此源持有的资源. 读取闭源是一个错误。多次关闭源是安全的.
     *
     * @throws IOException 异常
     */
    @Override
    void close() throws IOException;

}
