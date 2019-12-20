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
package org.aoju.bus.office.support.excel.sax;

import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.File;
import java.io.InputStream;

/**
 * Sax方式读取Excel接口,提供一些共用方法
 *
 * @param <T> 子对象类型,用于标记返回值this
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public interface ExcelSaxReader<T> {
    /**
     * 开始读取Excel,读取所有sheet
     *
     * @param path Excel文件路径
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(String path) throws InstrumentException;

    /**
     * 开始读取Excel,读取所有sheet
     *
     * @param file Excel文件
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(File file) throws InstrumentException;

    /**
     * 开始读取Excel,读取所有sheet,读取结束后并不关闭流
     *
     * @param in Excel包流
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(InputStream in) throws InstrumentException;

    /**
     * 开始读取Excel
     *
     * @param path 文件路径
     * @param rid  Excel中的sheet rid编号,如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(String path, int rid) throws InstrumentException;

    /**
     * 开始读取Excel
     *
     * @param file Excel文件
     * @param rid  Excel中的sheet rid编号,如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(File file, int rid) throws InstrumentException;

    /**
     * 开始读取Excel,读取结束后并不关闭流
     *
     * @param in  Excel流
     * @param rid Excel中的sheet rid编号,如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(InputStream in, int rid) throws InstrumentException;

}
