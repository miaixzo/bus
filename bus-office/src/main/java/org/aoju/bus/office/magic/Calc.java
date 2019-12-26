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
import com.sun.star.sheet.XSpreadsheetDocument;
import org.aoju.bus.office.Builder;

/**
 * 使Office Calc文档(电子表格)更容易使用的实用函数集合.
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public final class Calc {

    /**
     * 获取给定文档是否为电子表格文档.
     *
     * @param document 要测试的文档.
     * @return 如果文档是电子表格文档，则为{@code true}，否则为{@code false}.
     */
    public static boolean isCalc(final XComponent document) {
        return Info.isDocumentType(document, Builder.CALC_SERVICE);
    }

    /**
     * 将给定的文档转换为{@link XSpreadsheetDocument}.
     *
     * @param document 要转换的文档.
     * @return 如果文档不是电子表格文档，则为null.
     */
    public static XSpreadsheetDocument getCalcDoc(final XComponent document) {
        if (document == null) {
            return null;
        }
        return Lo.qi(XSpreadsheetDocument.class, document);
    }

}
