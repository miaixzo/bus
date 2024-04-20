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
package org.miaixz.bus.office.word;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.FileKit;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.IOException;

/**
 * Word工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WordKit {

    /**
     * 创建{@link XWPFDocument},如果文件已存在则读取之,否则创建新的
     *
     * @param file docx文件
     * @return {@link XWPFDocument}
     */
    public static XWPFDocument create(File file) {
        try {
            return FileKit.exists(file) ? new XWPFDocument(OPCPackage.open(file)) : new XWPFDocument();
        } catch (InvalidFormatException | IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建Word 07格式的生成器
     *
     * @return {@link Word07Writer}
     */
    public static Word07Writer getWriter() {
        return new Word07Writer();
    }

    /**
     * 创建Word 07格式的生成器
     *
     * @param destFile 目标文件
     * @return {@link Word07Writer}
     */
    public static Word07Writer getWriter(File destFile) {
        return new Word07Writer(destFile);
    }

}
