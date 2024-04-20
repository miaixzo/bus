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
package org.miaixz.bus.office.excel.sax;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.office.excel.ExcelSaxKit;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 在Sax方式读取Excel时，读取sheet标签中sheetId和rid的对应关系
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetRidReader extends DefaultHandler {

    private final static String TAG_NAME = "sheet";
    private final static String RID_ATTR = "r:id";
    private final static String SHEET_ID_ATTR = "sheetId";
    private final static String NAME_ATTR = "name";

    private final Map<Integer, Integer> ID_RID_MAP = new LinkedHashMap<>();
    private final Map<String, Integer> NAME_RID_MAP = new LinkedHashMap<>();

    /**
     * 从{@link XSSFReader}中解析sheet名、sheet id等相关信息
     *
     * @param reader {@link XSSFReader}
     * @return SheetRidReader
     */
    public static SheetRidReader parse(XSSFReader reader) {
        return new SheetRidReader().read(reader);
    }

    /**
     * 读取Wordkbook的XML中sheet标签中sheetId和rid的对应关系
     *
     * @param xssfReader XSSF读取器
     * @return this
     */
    public SheetRidReader read(XSSFReader xssfReader) {
        InputStream workbookData = null;
        try {
            workbookData = xssfReader.getWorkbookData();
            ExcelSaxKit.readFrom(workbookData, this);
        } catch (InvalidFormatException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(workbookData);
        }
        return this;
    }

    /**
     * 根据sheetId获取rid，从1开始
     *
     * @param sheetId Sheet的ID，从1开始
     * @return rid，从1开始
     */
    public Integer getRidBySheetId(int sheetId) {
        return ID_RID_MAP.get(sheetId);
    }

    /**
     * 根据sheetId获取rid，从0开始
     *
     * @param sheetId Sheet的ID，从0开始
     * @return rid，从0开始
     */
    public Integer getRidBySheetIdBase0(int sheetId) {
        final Integer rid = getRidBySheetId(sheetId + 1);
        if (null != rid) {
            return rid - 1;
        }
        return null;
    }

    /**
     * 根据sheet name获取rid，从1开始
     *
     * @param sheetName Sheet的name
     * @return rid，从1开始
     */
    public Integer getRidByName(String sheetName) {
        return NAME_RID_MAP.get(sheetName);
    }

    /**
     * 根据sheet name获取rid，从0开始
     *
     * @param sheetName Sheet的name
     * @return rid，从0开始
     */
    public Integer getRidByNameBase0(String sheetName) {
        final Integer rid = getRidByName(sheetName);
        if (null != rid) {
            return rid - 1;
        }
        return null;
    }

    /**
     * 通过sheet的序号获取rid
     *
     * @param index 序号，从0开始
     * @return rid
     */
    public Integer getRidByIndex(int index) {
        return CollKit.get(this.NAME_RID_MAP.values(), index);
    }

    /**
     * 通过sheet的序号获取rid
     *
     * @param index 序号，从0开始
     * @return rid，从0开始
     */
    public Integer getRidByIndexBase0(int index) {
        final Integer rid = CollKit.get(this.NAME_RID_MAP.values(), index);
        if (null != rid) {
            return rid - 1;
        }
        return null;
    }

    /**
     * 获取所有sheet名称
     *
     * @return sheet名称
     */
    public List<String> getSheetNames() {
        return CollKit.toList(this.NAME_RID_MAP.keySet());
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_NAME.equalsIgnoreCase(localName)) {
            final String ridStr = attributes.getValue(RID_ATTR);
            if (StringKit.isEmpty(ridStr)) {
                return;
            }
            final int rid = Integer.parseInt(StringKit.removePrefixIgnoreCase(ridStr, Excel07SaxReader.RID_PREFIX));

            // sheet名和rid映射
            final String name = attributes.getValue(NAME_ATTR);
            if (StringKit.isNotEmpty(name)) {
                NAME_RID_MAP.put(name, rid);
            }

            // sheetId和rid映射
            final String sheetIdStr = attributes.getValue(SHEET_ID_ATTR);
            if (StringKit.isNotEmpty(sheetIdStr)) {
                ID_RID_MAP.put(Integer.parseInt(sheetIdStr), rid);
            }
        }
    }

}
