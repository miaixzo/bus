/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.office.support.excel.sax;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.office.support.excel.ExcelSaxKit;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 在Sax方式读取Excel时，读取sheet标签中sheetId和rid的对应关系
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class SheetSaxReader extends DefaultHandler {

    private final static String TAG_NAME = "sheet";
    private final static String RID_ATTR = "r:id";
    private final static String SHEET_ID_ATTR = "sheetId";
    private final static String NAME_ATTR = "name";

    private final Map<String, String> ID_RID_MAP = new HashMap<>();
    private final Map<String, String> NAME_RID_MAP = new HashMap<>();

    /**
     * 读取Wordkbook的XML中sheet标签中sheetId和rid的对应关系
     *
     * @param xssfReader XSSF读取器
     * @return {@link SheetSaxReader}
     */
    public SheetSaxReader read(XSSFReader xssfReader) {
        InputStream workbookData = null;
        try {
            workbookData = xssfReader.getWorkbookData();
            ExcelSaxKit.readFrom(workbookData, this);
        } catch (InvalidFormatException | IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoKit.close(workbookData);
        }
        return this;
    }

    /**
     * 根据sheetId获取rid
     *
     * @param sheetId Sheet的ID
     * @return rid
     */
    public String getRidBySheetId(String sheetId) {
        return ID_RID_MAP.get(sheetId);
    }

    /**
     * 根据sheet name获取rid
     *
     * @param sheetName Sheet的name
     * @return rid
     */
    public String getRidByName(String sheetName) {
        return NAME_RID_MAP.get(sheetName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (TAG_NAME.equalsIgnoreCase(localName)) {
            final int length = attributes.getLength();
            String sheetId = null;
            String rid = null;
            String name = null;
            for (int i = 0; i < length; i++) {
                switch (attributes.getLocalName(i)) {
                    case SHEET_ID_ATTR:
                        sheetId = attributes.getValue(i);
                        break;
                    case RID_ATTR:
                        rid = attributes.getValue(i);
                        break;
                    case NAME_ATTR:
                        name = attributes.getValue(i);
                        break;
                }
                if (StringKit.isNotEmpty(sheetId)) {
                    ID_RID_MAP.put(sheetId, rid);
                }
                if (StringKit.isNotEmpty(name)) {
                    NAME_RID_MAP.put(name, rid);
                }
            }
        }
    }

}
