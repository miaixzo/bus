package org.aoju.bus.poi.excel.sax;

import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.File;
import java.io.InputStream;

/**
 * Sax方式读取Excel接口，提供一些共用方法
 *
 * @param <T> 子对象类型，用于标记返回值this
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ExcelSaxReader<T> {
    /**
     * 开始读取Excel，读取所有sheet
     *
     * @param path Excel文件路径
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(String path) throws InstrumentException;

    /**
     * 开始读取Excel，读取所有sheet
     *
     * @param file Excel文件
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(File file) throws InstrumentException;

    /**
     * 开始读取Excel，读取所有sheet，读取结束后并不关闭流
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
     * @param rid  Excel中的sheet rid编号，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(String path, int rid) throws InstrumentException;

    /**
     * 开始读取Excel
     *
     * @param file Excel文件
     * @param rid  Excel中的sheet rid编号，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(File file, int rid) throws InstrumentException;

    /**
     * 开始读取Excel，读取结束后并不关闭流
     *
     * @param in  Excel流
     * @param rid Excel中的sheet rid编号，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    T read(InputStream in, int rid) throws InstrumentException;

}
