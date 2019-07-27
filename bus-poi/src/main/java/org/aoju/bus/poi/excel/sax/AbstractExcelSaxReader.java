package org.aoju.bus.poi.excel.sax;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;

import java.io.File;
import java.io.InputStream;

/**
 * 抽象的Sax方式Excel读取器，提供一些共用方法
 *
 * @param <T> 子对象类型，用于标记返回值this
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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
