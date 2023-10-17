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
package org.aoju.bus.office.excel.sax;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.toolkit.FileKit;

import java.io.File;
import java.io.InputStream;

/**
 * Sax方式读取Excel接口,提供一些共用方法
 *
 * @param <T> 子对象类型,用于标记返回值this
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ExcelSaxReader<T> {

    /**
     * sheet r:Id 前缀
     */
    String RID_PREFIX = "rId";
    /**
     * sheet name 前缀
     */
    String SHEET_NAME_PREFIX = "sheetName:";

    /**
     * 开始读取Excel
     *
     * @param file    Excel文件
     * @param idOrRid Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
     * @return this
     * @throws InternalException POI异常
     */
    T read(File file, String idOrRid) throws InternalException;

    /**
     * 开始读取Excel，读取结束后并不关闭流
     *
     * @param in      Excel流
     * @param idOrRid Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
     * @return this
     * @throws InternalException POI异常
     */
    T read(InputStream in, String idOrRid) throws InternalException;

    /**
     * 开始读取Excel，读取所有sheet
     *
     * @param path Excel文件路径
     * @return this
     * @throws InternalException POI异常
     */
    default T read(String path) throws InternalException {
        return read(FileKit.file(path));
    }

    /**
     * 开始读取Excel,读取所有sheet
     *
     * @param file Excel文件
     * @return this
     * @throws InternalException POI异常
     */
    default T read(File file) throws InternalException {
        return read(file, -1);
    }

    /**
     * 开始读取Excel,读取所有sheet,读取结束后并不关闭流
     *
     * @param in Excel包流
     * @return this
     * @throws InternalException POI异常
     */
    default T read(InputStream in) throws InternalException {
        return read(in, -1);
    }

    /**
     * 开始读取Excel
     *
     * @param path 文件路径
     * @param rid  Excel中的sheet rid编号，如果为-1处理所有编号的sheet
     * @return this
     * @throws InternalException POI异常
     */
    default T read(String path, int rid) throws InternalException {
        return read(FileKit.file(path), rid);
    }

    /**
     * 开始读取Excel
     *
     * @param path 文件路径
     * @param rid  Excel中的sheet rid编号,如果为-1处理所有编号的sheet
     * @return this
     * @throws InternalException POI异常
     */
    default T read(String path, String rid) throws InternalException {
        return read(FileKit.file(path), rid);
    }

    /**
     * 开始读取Excel
     *
     * @param file Excel文件
     * @param rid  Excel中的sheet rid编号,如果为-1处理所有编号的sheet
     * @return this
     * @throws InternalException POI异常
     */
    default T read(File file, int rid) throws InternalException {
        return read(file, String.valueOf(rid));
    }

    /**
     * 开始读取Excel,读取结束后并不关闭流
     *
     * @param in  Excel流
     * @param rid Excel中的sheet rid编号,如果为-1处理所有编号的sheet
     * @return this
     * @throws InternalException POI异常
     */
    default T read(InputStream in, int rid) throws InternalException {
        return read(in, String.valueOf(rid));
    }

}
