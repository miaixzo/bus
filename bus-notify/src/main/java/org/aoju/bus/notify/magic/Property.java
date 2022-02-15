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
package org.aoju.bus.notify.magic;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 消息模版
 *
 * @author Justubborn
 * @version 6.3.5
 * @since JDK1.8+
 */
@Getter
@Setter
@SuperBuilder
public class Property {

    /**
     * 发送者
     */
    protected String sender;

    /**
     * 接收者
     */
    protected String receive;

    /**
     * 内容类型
     */
    private Type type;

    /**
     * 发送模型
     */
    private Mode mode;

    public enum Type {
        /**
         * html
         */
        HTML,
        /**
         * 文本
         */
        TEXT,
        /**
         * 语音
         */
        VOICE,
        /**
         * 文件
         */
        FILE,
        /**
         * 文件
         */
        OTHER
    }

    public enum Mode {
        /**
         * 单发
         */
        SINGLE,
        /**
         * 批量
         */
        BATCH
    }

}
