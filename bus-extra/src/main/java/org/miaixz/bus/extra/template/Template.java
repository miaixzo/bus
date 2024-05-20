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
package org.miaixz.bus.extra.template;

import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.*;
import java.util.Map;

/**
 * 抽象模板接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Template {

    /**
     * 将模板与绑定参数融合后输出到Writer
     *
     * @param bindingMap 绑定的参数，此Map中的参数会替换模板中的变量
     * @param writer     输出
     */
    void render(Map<?, ?> bindingMap, Writer writer);

    /**
     * 将模板与绑定参数融合后输出到流
     *
     * @param bindingMap 绑定的参数，此Map中的参数会替换模板中的变量
     * @param out        输出
     */
    void render(Map<?, ?> bindingMap, OutputStream out);

    /**
     * 写出到文件
     *
     * @param bindingMap 绑定的参数，此Map中的参数会替换模板中的变量
     * @param file       输出到的文件
     */
    default void render(final Map<?, ?> bindingMap, final File file) {
        BufferedOutputStream out = null;
        try {
            out = FileKit.getOutputStream(file);
            this.render(bindingMap, out);
        } finally {
            IoKit.closeQuietly(out);
        }
    }

    /**
     * 将模板与绑定参数融合后返回为字符串
     *
     * @param bindingMap 绑定的参数，此Map中的参数会替换模板中的变量
     * @return 融合后的内容
     */
    default String render(final Map<?, ?> bindingMap) {
        final StringWriter writer = new StringWriter();
        render(bindingMap, writer);
        return writer.toString();
    }
}
