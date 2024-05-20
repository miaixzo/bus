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
package org.miaixz.bus.office.excel;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Excel文件工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelFileKit {

    /**
     * 是否为XLS格式的Excel文件（HSSF）
     * XLS文件主要用于Excel 97~2003创建
     * 此方法会自动调用{@link InputStream#reset()}方法
     *
     * @param in excel输入流
     * @return 是否为XLS格式的Excel文件（HSSF）
     */
    public static boolean isXls(final InputStream in) {
        return FileMagic.OLE2 == getFileMagic(in);
    }

    /**
     * 是否为XLSX格式的Excel文件（XSSF）
     * XLSX文件主要用于Excel 2007+创建
     * 此方法会自动调用{@link InputStream#reset()}方法
     *
     * @param in excel输入流
     * @return 是否为XLSX格式的Excel文件（XSSF）
     */
    public static boolean isXlsx(final InputStream in) {
        return FileMagic.OOXML == getFileMagic(in);
    }

    /**
     * 是否为XLSX格式的Excel文件（XSSF）
     * XLSX文件主要用于Excel 2007+创建
     *
     * @param file excel文件
     * @return 是否为XLSX格式的Excel文件（XSSF）
     */
    public static boolean isXlsx(final File file) {
        try {
            return FileMagic.valueOf(file) == FileMagic.OOXML;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * {@link java.io.PushbackInputStream}
     * PushbackInputStream的markSupported()为false，并不支持mark和reset
     * 如果强转成PushbackInputStream在调用FileMagic.valueOf(inputStream)时会报错
     * {@link FileMagic}
     * 报错内容：getFileMagic() only operates on streams which support mark(int)
     * 此处修改成 final InputStream in = FileMagic.prepareToCheckMagic(in)
     *
     * @param in {@link InputStream}
     *           kefan.qu
     */
    private static FileMagic getFileMagic(InputStream in) {
        final FileMagic magic;
        in = FileMagic.prepareToCheckMagic(in);
        try {
            magic = FileMagic.valueOf(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return magic;
    }

}
