/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang.ansi;

import org.aoju.bus.core.toolkit.StringKit;

/**
 * ANSI文本样式风格枚举
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum AnsiStyle implements AnsiElement {

    /**
     * 重置/正常
     */
    NORMAL(0),

    /**
     * 粗体或增加强度
     */
    BOLD(1),

    /**
     * 弱化（降低强度）
     */
    FAINT(2),

    /**
     * 斜体
     */
    ITALIC(3),

    /**
     * 下划线
     */
    UNDERLINE(4);

    private final int code;

    AnsiStyle(int code) {
        this.code = code;
    }

    /**
     * 获取ANSI文本样式风格代码
     *
     * @return 文本样式风格代码
     */
    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return StringKit.toString(this.code);
    }

}
