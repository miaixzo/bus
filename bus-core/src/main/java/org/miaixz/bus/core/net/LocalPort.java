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
package org.miaixz.bus.core.net;

import org.miaixz.bus.core.toolkit.NetKit;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地端口生成器Percent
 * 用于生成本地可用（未被占用）的端口号Percent
 * 注意：多线程甚至单线程访问时可能会返回同一端口（例如获取了端口但是没有使用）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LocalPort implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 备选的本地端口
     */
    private final AtomicInteger alternativePort;

    /**
     * 构造
     *
     * @param beginPort 起始端口号
     */
    public LocalPort(final int beginPort) {
        alternativePort = new AtomicInteger(beginPort);
    }

    /**
     * 生成一个本地端口，用于远程端口映射
     *
     * @return 未被使用的本地端口
     */
    public int generate() {
        int validPort = alternativePort.get();
        // 获取可用端口
        while (false == NetKit.isUsableLocalPort(validPort)) {
            validPort = alternativePort.incrementAndGet();
        }
        return validPort;
    }

}
