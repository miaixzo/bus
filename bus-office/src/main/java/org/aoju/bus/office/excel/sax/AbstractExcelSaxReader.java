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
package org.aoju.bus.office.excel.sax;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;

import java.io.File;
import java.io.InputStream;

/**
 * 抽象的Sax方式Excel读取器，提供一些共用方法
 *
 * @param <T> 子对象类型，用于标记返回值this
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public abstract class AbstractExcelSaxReader<T> implements ExcelSaxReader<T> {

    @Override
    public T read(String path) throws InstrumentException {
        return read(FileUtils.file(path));
    }

    @Override
    public T read(File file) throws InstrumentException {
        return read(file, -1);
    }

    @Override
    public T read(InputStream in) throws InstrumentException {
        return read(in, -1);
    }

    @Override
    public T read(String path, int sheetIndex) throws InstrumentException {
        return read(FileUtils.file(path), sheetIndex);
    }

}
