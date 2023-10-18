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
package org.aoju.bus.office.excel;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.office.excel.cell.CellEditor;
import org.aoju.bus.office.excel.cell.CellLocation;
import org.aoju.bus.office.excel.cell.CellSetter;
import org.aoju.bus.office.excel.cell.NullCell;
import org.aoju.bus.office.excel.cell.editors.TrimEditor;
import org.aoju.bus.office.excel.cell.setters.CellSetterFactory;
import org.aoju.bus.office.excel.cell.values.ErrorCellValue;
import org.aoju.bus.office.excel.cell.values.NumericCellValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.ss.util.SheetUtil;

/**
 * Excel表格中单元格工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellKit {

    /**
     * 获取单元格值
     *
     * @param cell {@link Cell}单元格
     * @return 值，类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(Cell cell) {
        return getCellValue(cell, false);
    }

    /**
     * 获取单元格值
     *
     * @param cell            {@link Cell}单元格
     * @param isTrimCellValue 如果单元格类型为字符串,是否去掉两边空白符
     * @return 值, 类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(Cell cell, boolean isTrimCellValue) {
        if (null == cell) {
            return null;
        }
        return getCellValue(cell, cell.getCellType(), isTrimCellValue);
    }

    /**
     * 获取单元格值
     *
     * @param cell       {@link Cell}单元格
     * @param cellEditor 单元格值编辑器 可以通过此编辑器对单元格值做自定义操作
     * @return 值, 类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(Cell cell, CellEditor cellEditor) {
        return getCellValue(cell, null, cellEditor);
    }

    /**
     * 获取单元格值
     *
     * @param cell            {@link Cell}单元格
     * @param cellType        单元格值类型{@link CellType}枚举
     * @param isTrimCellValue 如果单元格类型为字符串,是否去掉两边空白符
     * @return 值, 类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(Cell cell, CellType cellType, final boolean isTrimCellValue) {
        return getCellValue(cell, cellType, isTrimCellValue ? new TrimEditor() : null);
    }

    /**
     * 获取单元格值
     * 如果单元格值为数字格式,则判断其格式中是否有小数部分,无则返回Long类型,否则返回Double类型
     *
     * @param cell       {@link Cell}单元格
     * @param cellType   单元格值类型{@link CellType}枚举,如果为{@code null}默认使用cell的类型
     * @param cellEditor 单元格值编辑器 可以通过此编辑器对单元格值做自定义操作
     * @return 值, 类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(Cell cell, CellType cellType, CellEditor cellEditor) {
        if (null == cell) {
            return null;
        }
        if (cell instanceof NullCell) {
            return null == cellEditor ? null : cellEditor.edit(cell, null);
        }
        if (null == cellType) {
            cellType = cell.getCellType();
        }

        // 尝试获取合并单元格，如果是合并单元格，则重新获取单元格类型
        final Cell mergedCell = getMergedRegionCell(cell);
        if (mergedCell != cell) {
            cell = mergedCell;
            cellType = cell.getCellType();
        }

        Object value;
        switch (cellType) {
            case NUMERIC:
                value = new NumericCellValue(cell).getValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case FORMULA:
                // 遇到公式时查找公式结果类型
                value = getCellValue(cell, cell.getCachedFormulaResultType(), cellEditor);
                break;
            case BLANK:
                value = Normal.EMPTY;
                break;
            case ERROR:
                value = new ErrorCellValue(cell).getValue();
                break;
            default:
                value = cell.getStringCellValue();
        }
        return null == cellEditor ? value : cellEditor.edit(cell, value);
    }

    /**
     * 设置单元格值
     * 根据传入的styleSet自动匹配样式
     * 当为头部样式时默认赋值头部样式,但是头部中如果有数字、日期等类型,将按照数字、日期样式设置
     *
     * @param cell       单元格
     * @param value      值
     * @param styleSet   单元格样式集，包括日期等样式，null表示无样式
     * @param isHeader   是否为标题单元格
     * @param cellEditor 单元格值编辑器，可修改单元格值或修改单元格，{@code null}表示不编辑
     */
    public static void setCellValue(final Cell cell, final Object value, final StyleSet styleSet, final boolean isHeader, final CellEditor cellEditor) {
        if (null == cell) {
            return;
        }

        if (null != styleSet) {
            cell.setCellStyle(styleSet.getStyleByValueType(value, isHeader));
        }

        setCellValue(cell, value, cellEditor);
    }

    /**
     * 设置单元格值
     * 根据传入的styleSet自动匹配样式
     * 当为头部样式时默认赋值头部样式，但是头部中如果有数字、日期等类型，将按照数字、日期样式设置
     *
     * @param cell       单元格
     * @param value      值
     * @param style      自定义样式，null表示无样式
     * @param cellEditor 单元格值编辑器，可修改单元格值或修改单元格，{@code null}表示不编辑
     */
    public static void setCellValue(final Cell cell, final Object value, final CellStyle style, final CellEditor cellEditor) {
        cell.setCellStyle(style);
        setCellValue(cell, value, cellEditor);
    }

    /**
     * 设置单元格值
     * 根据传入的styleSet自动匹配样式
     * 当为头部样式时默认赋值头部样式，但是头部中如果有数字、日期等类型，将按照数字、日期样式设置
     *
     * @param cell       单元格
     * @param value      值或{@link CellSetter}
     * @param cellEditor 单元格值编辑器，可修改单元格值或修改单元格，{@code null}表示不编辑
     */
    public static void setCellValue(final Cell cell, Object value, final CellEditor cellEditor) {
        if (null == cell) {
            return;
        }

        // 在使用BigWriter(SXSSF)模式写出数据时，单元格值为直接值，非引用值（is标签）
        // 而再使用ExcelWriter(XSSF)编辑时，会写出引用值，导致失效
        // 此处做法是先清空单元格值，再写入
        if (CellType.BLANK != cell.getCellType()) {
            cell.setBlank();
        }

        if (null != cellEditor) {
            value = cellEditor.edit(cell, value);
        }
        CellSetterFactory.createCellSetter(value).setValue(cell);
    }

    /**
     * 为特定单元格添加批注
     *
     * @param cell   单元格
     * @param text   批注内容
     * @param author 作者
     * @param anchor 批注的位置、大小等信息，null表示使用默认
     */
    public static void setComment(Cell cell, String text, String author, ClientAnchor anchor) {
        final Sheet sheet = cell.getSheet();
        final Workbook wb = sheet.getWorkbook();
        final Drawing<?> drawing = sheet.createDrawingPatriarch();
        final CreationHelper factory = wb.getCreationHelper();
        if (null == anchor) {
            anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex() + 1);
            anchor.setCol2(cell.getColumnIndex() + 3);
            anchor.setRow1(cell.getRowIndex());
            anchor.setRow2(cell.getRowIndex() + 2);
        }
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        comment.setAuthor(StringKit.nullToEmpty(author));
        cell.setCellComment(comment);
    }

    /**
     * 获取单元格，如果单元格不存在，返回{@link NullCell}
     *
     * @param row       Excel表的行
     * @param cellIndex 列号
     * @return {@link Cell}
     */
    public static Cell getCell(Row row, int cellIndex) {
        if (null == row) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (null == cell) {
            return new NullCell(row, cellIndex);
        }
        return cell;
    }

    /**
     * 获取已有行或创建新行
     *
     * @param row       Excel表的行
     * @param cellIndex 列号
     * @return {@link Cell}
     */
    public static Cell getOrCreateCell(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (null == cell) {
            cell = row.createCell(cellIndex);
        }
        return cell;
    }

    /**
     * 判断指定的单元格是否是合并单元格
     *
     * @param sheet       {@link Sheet}
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return 是否是合并单元格
     */
    public static boolean isMergedRegion(Sheet sheet, String locationRef) {
        final CellLocation cellLocation = ExcelKit.toLocation(locationRef);
        return isMergedRegion(sheet, cellLocation.getX(), cellLocation.getY());
    }

    /**
     * 判断指定的单元格是否是合并单元格
     *
     * @param cell {@link Cell}
     * @return 是否是合并单元格
     */
    public static boolean isMergedRegion(Cell cell) {
        return isMergedRegion(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex());
    }

    /**
     * 判断指定的单元格是否是合并单元格
     *
     * @param sheet {@link Sheet}
     * @param x     列号，从0开始
     * @param y     行号，从0开始
     * @return 是否是合并单元格
     */
    public static boolean isMergedRegion(Sheet sheet, int x, int y) {
        if (sheet != null) {
            final int sheetMergeCount = sheet.getNumMergedRegions();
            CellRangeAddress ca;
            for (int i = 0; i < sheetMergeCount; i++) {
                ca = sheet.getMergedRegion(i);
                if (y >= ca.getFirstRow() && y <= ca.getLastRow()
                        && x >= ca.getFirstColumn() && x <= ca.getLastColumn()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取合并单元格{@link CellRangeAddress}，如果不是返回null
     *
     * @param sheet       {@link Sheet}
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return {@link CellRangeAddress}
     */
    public static CellRangeAddress getCellRangeAddress(Sheet sheet, String locationRef) {
        final CellLocation cellLocation = ExcelKit.toLocation(locationRef);
        return getCellRangeAddress(sheet, cellLocation.getX(), cellLocation.getY());
    }

    /**
     * 获取合并单元格{@link CellRangeAddress}，如果不是返回null
     *
     * @param cell {@link Cell}
     * @return {@link CellRangeAddress}
     */
    public static CellRangeAddress getCellRangeAddress(Cell cell) {
        return getCellRangeAddress(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex());
    }

    /**
     * 获取合并单元格{@link CellRangeAddress}，如果不是返回null
     *
     * @param sheet {@link Sheet}
     * @param x     列号，从0开始
     * @param y     行号，从0开始
     * @return {@link CellRangeAddress}
     */
    public static CellRangeAddress getCellRangeAddress(Sheet sheet, int x, int y) {
        if (sheet != null) {
            final int sheetMergeCount = sheet.getNumMergedRegions();
            CellRangeAddress ca;
            for (int i = 0; i < sheetMergeCount; i++) {
                ca = sheet.getMergedRegion(i);
                if (y >= ca.getFirstRow() && y <= ca.getLastRow()
                        && x >= ca.getFirstColumn() && x <= ca.getLastColumn()) {
                    return ca;
                }
            }
        }
        return null;
    }

    /**
     * 设置合并单元格样式，如果不是则不设置
     *
     * @param cell      {@link Cell}
     * @param cellStyle {@link CellStyle}
     */
    public static void setMergedRegionStyle(Cell cell, CellStyle cellStyle) {
        CellRangeAddress cellRangeAddress = getCellRangeAddress(cell);
        if (cellRangeAddress != null) {
            setMergeCellStyle(cellStyle, cellRangeAddress, cell.getSheet());
        }
    }

    /**
     * 合并单元格,可以根据设置的值来合并行和列
     *
     * @param sheet       表对象
     * @param firstRow    起始行,0开始
     * @param lastRow     结束行,0开始
     * @param firstColumn 起始列,0开始
     * @param lastColumn  结束列,0开始
     * @return 合并后的单元格号
     */
    public static int mergingCells(Sheet sheet, int firstRow, int lastRow, int firstColumn, int lastColumn) {
        return mergingCells(sheet, firstRow, lastRow, firstColumn, lastColumn, null);
    }

    /**
     * 合并单元格，可以根据设置的值来合并行和列
     *
     * @param sheet       表对象
     * @param firstRow    起始行,0开始
     * @param lastRow     结束行,0开始
     * @param firstColumn 起始列,0开始
     * @param lastColumn  结束列,0开始
     * @param cellStyle   单元格样式,只提取边框样式,null表示无样式
     * @return 合并后的单元格号
     */
    public static int mergingCells(Sheet sheet, int firstRow, int lastRow, int firstColumn, int lastColumn, CellStyle cellStyle) {
        final CellRangeAddress cellRangeAddress = new CellRangeAddress(//
                firstRow, // first row (0-based)
                lastRow, // last row (0-based)
                firstColumn, // first column (0-based)
                lastColumn // last column (0-based)
        );

        setMergeCellStyle(cellStyle, cellRangeAddress, sheet);
        return sheet.addMergedRegion(cellRangeAddress);
    }

    /**
     * 获取合并单元格的值
     * 传入的x,y坐标(列行数)可以是合并单元格范围内的任意一个单元格
     *
     * @param sheet       {@link Sheet}
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return 合并单元格的值
     */
    public static Object getMergedRegionValue(Sheet sheet, String locationRef) {
        final CellLocation cellLocation = ExcelKit.toLocation(locationRef);
        return getMergedRegionValue(sheet, cellLocation.getX(), cellLocation.getY());
    }

    /**
     * 获取合并单元格的值
     * 传入的x,y坐标(列行数)可以是合并单元格范围内的任意一个单元格
     *
     * @param sheet {@link Sheet}
     * @param x     列号，从0开始，可以是合并单元格范围中的任意一列
     * @param y     行号，从0开始，可以是合并单元格范围中的任意一行
     * @return 合并单元格的值
     */
    public static Object getMergedRegionValue(Sheet sheet, int x, int y) {
        return getCellValue(SheetUtil.getCell(sheet, x, y));
    }

    /**
     * 获取合并单元格
     * 传入的x,y坐标(列行数)可以是合并单元格范围内的任意一个单元格
     *
     * @param cell {@link Cell}
     * @return 合并单元格
     */
    public static Cell getMergedRegionCell(Cell cell) {
        if (null == cell) {
            return null;
        }
        return ObjectKit.defaultIfNull(
                getMergedCell(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex()),
                cell);
    }

    /**
     * 获取合并单元格
     * 传入的x,y坐标(列行数)可以是合并单元格范围内的任意一个单元格
     *
     * @param sheet {@link Sheet}
     * @param x     列号，从0开始，可以是合并单元格范围中的任意一列
     * @param y     行号，从0开始，可以是合并单元格范围中的任意一行
     * @return 合并单元格，如果非合并单元格，返回坐标对应的单元格
     */
    public static Cell getMergedRegionCell(Sheet sheet, int x, int y) {
        return ObjectKit.defaultIfNull(
                getMergedCell(sheet, x, y),
                () -> SheetUtil.getCell(sheet, y, x));
    }

    /**
     * 获取合并单元格，非合并单元格返回<code>null</code>
     * 传入的x,y坐标（列行数）可以是合并单元格范围内的任意一个单元格
     *
     * @param sheet {@link Sheet}
     * @param x     列号，从0开始，可以是合并单元格范围中的任意一列
     * @param y     行号，从0开始，可以是合并单元格范围中的任意一行
     * @return 合并单元格，如果非合并单元格，返回<code>null</code>
     */
    private static Cell getMergedCell(Sheet sheet, int x, int y) {
        for (final CellRangeAddress ca : sheet.getMergedRegions()) {
            if (ca.isInRange(y, x)) {
                return SheetUtil.getCell(sheet, ca.getFirstRow(), ca.getFirstColumn());
            }
        }
        return null;
    }

    /**
     * 根据{@link CellStyle}设置合并单元格边框样式
     *
     * @param cellStyle        {@link CellStyle}
     * @param cellRangeAddress {@link CellRangeAddress}
     * @param sheet            {@link Sheet}
     */
    private static void setMergeCellStyle(CellStyle cellStyle, CellRangeAddress cellRangeAddress, Sheet sheet) {
        if (null != cellStyle) {
            RegionUtil.setBorderTop(cellStyle.getBorderTop(), cellRangeAddress, sheet);
            RegionUtil.setBorderRight(cellStyle.getBorderRight(), cellRangeAddress, sheet);
            RegionUtil.setBorderBottom(cellStyle.getBorderBottom(), cellRangeAddress, sheet);
            RegionUtil.setBorderLeft(cellStyle.getBorderLeft(), cellRangeAddress, sheet);
            RegionUtil.setTopBorderColor(cellStyle.getTopBorderColor(), cellRangeAddress, sheet);
            RegionUtil.setRightBorderColor(cellStyle.getRightBorderColor(), cellRangeAddress, sheet);
            RegionUtil.setLeftBorderColor(cellStyle.getLeftBorderColor(), cellRangeAddress, sheet);
            RegionUtil.setBottomBorderColor(cellStyle.getBottomBorderColor(), cellRangeAddress, sheet);
        }
    }

}
