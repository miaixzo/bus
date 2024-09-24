/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.xyz;

import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.cellwalk.CellHandler;
import org.apache.poi.ss.util.cellwalk.CellWalk;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.miaixz.bus.core.xyz.FieldKit;

/**
 * {@link Sheet}相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetKit {

    /**
     * 获取或者创建sheet表 如果sheet表在Workbook中已经存在，则获取之，否则创建之
     *
     * @param book      工作簿{@link Workbook}
     * @param sheetName 工作表名，{@code null}表示默认
     * @return 工作表 {@link Sheet}
     */
    public static Sheet getOrCreateSheet(final Workbook book, final String sheetName) {
        if (null == book) {
            return null;
        }

        Sheet sheet;
        if (null == sheetName) {
            sheet = getOrCreateSheet(book, 0);
        } else {
            sheet = book.getSheet(sheetName);
            if (null == sheet) {
                sheet = book.createSheet(sheetName);
            }
        }
        return sheet;
    }

    /**
     * 获取或者创建sheet表 自定义需要读取或写出的Sheet，如果给定的sheet不存在，创建之（命名为默认） 在读取中，此方法用于切换读取的sheet，在写出时，此方法用于新建或者切换sheet
     *
     * @param book       工作簿{@link Workbook}
     * @param sheetIndex 工作表序号
     * @return 工作表 {@link Sheet}
     */
    public static Sheet getOrCreateSheet(final Workbook book, final int sheetIndex) {
        Sheet sheet = null;
        try {
            sheet = book.getSheetAt(sheetIndex);
        } catch (final IllegalArgumentException ignore) {
            // ignore
        }
        if (null == sheet) {
            sheet = book.createSheet();
        }
        return sheet;
    }

    /**
     * sheet是否为空
     *
     * @param sheet {@link Sheet}
     * @return sheet是否为空
     */
    public static boolean isEmpty(final Sheet sheet) {
        return null == sheet || (sheet.getLastRowNum() == 0 && sheet.getPhysicalNumberOfRows() == 0);
    }

    /**
     * 遍历Sheet中的所有单元格
     *
     * @param sheet       {@link Sheet}
     * @param cellHandler 单元格处理器
     */
    public static void walk(final Sheet sheet, final CellHandler cellHandler) {
        walk(sheet, new CellRangeAddress(0, sheet.getLastRowNum(), 0, sheet.getLastRowNum()), cellHandler);
    }

    /**
     * 遍历Sheet中的指定区域单元格
     *
     * @param sheet       {@link Sheet}
     * @param range       区域
     * @param cellHandler 单元格处理器
     */
    public static void walk(final Sheet sheet, final CellRangeAddress range, final CellHandler cellHandler) {
        final CellWalk cellWalk = new CellWalk(sheet, range);
        cellWalk.traverse(cellHandler);
    }

    /**
     * 设置忽略错误，即Excel中的绿色警告小标，只支持XSSFSheet和SXSSFSheet
     * 见：https://stackoverflow.com/questions/23488221/how-to-remove-warning-in-excel-using-apache-poi-in-java
     *
     * @param sheet             {@link Sheet}
     * @param cellRangeAddress  指定单元格范围
     * @param ignoredErrorTypes 忽略的错误类型列表
     * @throws UnsupportedOperationException 如果sheet不是XSSFSheet
     */
    public static void addIgnoredErrors(final Sheet sheet, final CellRangeAddress cellRangeAddress,
            final IgnoredErrorType... ignoredErrorTypes) throws UnsupportedOperationException {
        if (sheet instanceof XSSFSheet) {
            ((XSSFSheet) sheet).addIgnoredErrors(cellRangeAddress, ignoredErrorTypes);
        } else if (sheet instanceof SXSSFSheet) {
            // SXSSFSheet并未提供忽略错误方法，获得其内部_sh字段设置
            final XSSFSheet xssfSheet = (XSSFSheet) FieldKit.getFieldValue(sheet, "_sh");
            if (null != xssfSheet) {
                xssfSheet.addIgnoredErrors(cellRangeAddress, ignoredErrorTypes);
            }
        } else {
            throw new UnsupportedOperationException("Only XSSFSheet supports addIgnoredErrors");
        }
    }

    /**
     * 获取指定坐标点对应的合并单元格范围
     *
     * @param sheet {@link Sheet}
     * @param x     x坐标，即列号
     * @param y     行号
     * @return CellRangeAddress or null
     */
    public static CellRangeAddress getMergedRegion(final Sheet sheet, final int x, final int y) {
        for (final CellRangeAddress ca : sheet.getMergedRegions()) {
            if (ca.isInRange(y, x)) {
                return ca;
            }
        }
        return null;
    }

}
