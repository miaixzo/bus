/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.socket.origin.nio;

import java.nio.channels.SelectionKey;

/**
 * SelectionKey Operation的枚举封装
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public enum Operation {

    /**
     * 读操作
     */
    READ(SelectionKey.OP_READ),
    /**
     * 写操作
     */
    WRITE(SelectionKey.OP_WRITE),
    /**
     * 连接操作
     */
    CONNECT(SelectionKey.OP_CONNECT),
    /**
     * 接受连接操作
     */
    ACCEPT(SelectionKey.OP_ACCEPT);

    private int value;

    /**
     * 构造
     *
     * @param value 值
     * @see SelectionKey#OP_READ
     * @see SelectionKey#OP_WRITE
     * @see SelectionKey#OP_CONNECT
     * @see SelectionKey#OP_ACCEPT
     */
    Operation(int value) {
        this.value = value;
    }

    /**
     * 获取值
     *
     * @return 值
     * @see SelectionKey#OP_READ
     * @see SelectionKey#OP_WRITE
     * @see SelectionKey#OP_CONNECT
     * @see SelectionKey#OP_ACCEPT
     */
    public int getValue() {
        return this.value;
    }

}
