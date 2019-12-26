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
package org.aoju.bus.office.magic;

import com.sun.star.lang.XComponent;
import org.aoju.bus.office.Builder;

/**
 * 使office draw文档(Drawing)更容易使用的实用函数集合
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public final class Draw {

    /**
     * 获取给定文档是否为draw文档.
     *
     * @param document 要测试的文档.
     * @return 如果文档是draw文档，则{@code true}，否则{@code false}.
     */
    public static boolean isDraw(final XComponent document) {
        return Info.isDocumentType(document, Builder.DRAW_SERVICE);
    }

    /**
     * 获取给定文档是否为演示文档.
     *
     * @param document 要测试的文档.
     * @return 如果文档是演示文档，则{@code true}，否则{@code false}.
     */
    public static boolean isImpress(final XComponent document) {
        return Info.isDocumentType(document, Builder.IMPRESS_SERVICE);
    }

}
