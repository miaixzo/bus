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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.cell.MergedCell;
import org.miaixz.bus.office.excel.cell.NullCell;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.cell.editors.TrimEditor;
import org.miaixz.bus.office.excel.cell.setters.CellSetter;
import org.miaixz.bus.office.excel.cell.setters.CellSetterFactory;
import org.miaixz.bus.office.excel.cell.values.CompositeCellValue;
import org.miaixz.bus.office.excel.style.StyleSet;

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
    public static Object getCellValue(final Cell cell) {
        return getCellValue(cell, false);
    }

    /**
     * 获取单元格值
     *
     * @param cell            {@link Cell}单元格
     * @param isTrimCellValue 如果单元格类型为字符串，是否去掉两边空白符
     * @return 值，类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(final Cell cell, final boolean isTrimCellValue) {
        if (null == cell) {
            return null;
        }
        return getCellValue(cell, cell.getCellType(), isTrimCellValue);
    }

    /**
     * 获取单元格值
     *
     * @param cell       {@link Cell}单元格
     * @param cellEditor 单元格值编辑器。可以通过此编辑器对单元格值做自定义操作
     * @return 值，类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(final Cell cell, final CellEditor cellEditor) {
        return getCellValue(cell, null, cellEditor);
    }

    /**
     * 获取单元格值
     *
     * @param cell            {@link Cell}单元格
     * @param cellType        单元格值类型{@link CellType}枚举
     * @param isTrimCellValue 如果单元格类型为字符串，是否去掉两边空白符
     * @return 值，类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(final Cell cell, final CellType cellType, final boolean isTrimCellValue) {
        return getCellValue(cell, cellType, isTrimCellValue ? new TrimEditor() : null);
    }

    /**
     * 获取单元格值 如果单元格值为数字格式，则判断其格式中是否有小数部分，无则返回Long类型，否则返回Double类型
     *
     * @param cell       {@link Cell}单元格
     * @param cellType   单元格值类型{@link CellType}枚举，如果为{@code null}默认使用cell的类型
     * @param cellEditor 单元格值编辑器。可以通过此编辑器对单元格值做自定义操作
     * @return 值，类型可能为：Date、Double、Boolean、String
     */
    public static Object getCellValue(final Cell cell, final CellType cellType, final CellEditor cellEditor) {
        return CompositeCellValue.of(cell, cellType, cellEditor).getValue();
    }

    /**
     * 设置单元格值 根据传入的styleSet自动匹配样式 当为头部样式时默认赋值头部样式，但是头部中如果有数字、日期等类型，将按照数字、日期样式设置
     *
     * @param cell       单元格
     * @param value      值
     * @param styleSet   单元格样式集，包括日期等样式，null表示无样式
     * @param isHeader   是否为标题单元格
     * @param cellEditor 单元格值编辑器，可修改单元格值或修改单元格，{@code null}表示不编辑
     */
    public static void setCellValue(final Cell cell, final Object value, final StyleSet styleSet,
            final boolean isHeader, final CellEditor cellEditor) {
        if (null == cell) {
            return;
        }

        CellStyle cellStyle = null;
        if (null != styleSet) {
            cellStyle = styleSet.getStyleFor(new CellReference(cell), value, isHeader);
        }

        setCellValue(cell, value, cellStyle, cellEditor);
    }

    /**
     * 设置单元格值 根据传入的styleSet自动匹配样式 当为头部样式时默认赋值头部样式，但是头部中如果有数字、日期等类型，将按照数字、日期样式设置
     *
     * @param cell       单元格
     * @param value      值
     * @param style      自定义样式，null表示无样式
     * @param cellEditor 单元格值编辑器，可修改单元格值或修改单元格，{@code null}表示不编辑
     */
    public static void setCellValue(final Cell cell, final Object value, final CellStyle style,
            final CellEditor cellEditor) {
        cell.setCellStyle(style);
        setCellValue(cell, value, cellEditor);
    }

    /**
     * 设置单元格值 根据传入的styleSet自动匹配样式 当为头部样式时默认赋值头部样式，但是头部中如果有数字、日期等类型，将按照数字、日期样式设置
     *
     * @param cell       单元格
     * @param value      值或{@link CellSetter}
     * @param cellEditor 单元格值编辑器，可修改单元格值或修改单元格，{@code null}表示不编辑
     */
    public static void setCellValue(final Cell cell, Object value, final CellEditor cellEditor) {
        if (null == cell) {
            return;
        }

        if (null != cellEditor) {
            value = cellEditor.edit(cell, value);
        }

        setCellValue(cell, value);
    }

    /**
     * 设置单元格值 根据传入的styleSet自动匹配样式 当为头部样式时默认赋值头部样式，但是头部中如果有数字、日期等类型，将按照数字、日期样式设置
     *
     * @param cell  单元格
     * @param value 值或{@link CellSetter}
     */
    public static void setCellValue(final Cell cell, final Object value) {
        if (null == cell) {
            return;
        }

        // 在使用BigWriter(SXSSF)模式写出数据时，单元格值为直接值，非引用值（is标签）
        // 而再使用ExcelWriter(XSSF)编辑时，会写出引用值，导致失效。
        // 此处做法是先清空单元格值，再写入
        if (CellType.BLANK != cell.getCellType()) {
            cell.setBlank();
        }

        CellSetterFactory.createCellSetter(value).setValue(cell);
    }

    /**
     * 获取或创建指定坐标单元格
     *
     * @param sheet {@link Sheet}
     * @param x     X坐标，从0计数，即列号
     * @param y     Y坐标，从0计数，即行号
     * @return {@link Cell}
     */
    public static Cell getOrCreateCell(final Sheet sheet, final int x, final int y) {
        return getCell(sheet, x, y, true);
    }

    /**
     * 获取指定坐标单元格，如果isCreateIfNotExist为false，则在单元格不存在时返回{@code null}
     *
     * @param sheet              {@link Sheet}
     * @param x                  X坐标，从0计数，即列号
     * @param y                  Y坐标，从0计数，即行号
     * @param isCreateIfNotExist 单元格不存在时是否创建
     * @return {@link Cell}
     */
    public static Cell getCell(final Sheet sheet, final int x, final int y, final boolean isCreateIfNotExist) {
        final Row row = isCreateIfNotExist ? RowKit.getOrCreateRow(sheet, y) : sheet.getRow(y);
        if (null != row) {
            return isCreateIfNotExist ? getOrCreateCell(row, x) : row.getCell(x);
        }
        return null;
    }

    /**
     * 获取单元格，如果单元格不存在，返回{@link NullCell}
     *
     * @param row       Excel表的行
     * @param cellIndex 列号
     * @return {@link Row}
     */
    public static Cell getCell(final Row row, final int cellIndex) {
        if (null == row) {
            return null;
        }
        final Cell cell = row.getCell(cellIndex);
        if (null == cell) {
            return new NullCell(row, cellIndex);
        }
        return cell;
    }

    /**
     * 获取已有单元格或创建新单元格
     *
     * @param row       Excel表的行
     * @param cellIndex 列号
     * @return {@link Row}
     */
    public static Cell getOrCreateCell(final Row row, final int cellIndex) {
        if (null == row) {
            return null;
        }
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
    public static boolean isMergedRegion(final Sheet sheet, final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return isMergedRegion(sheet, cellReference.getCol(), cellReference.getRow());
    }

    /**
     * 判断指定的单元格是否是合并单元格
     *
     * @param cell {@link Cell}
     * @return 是否是合并单元格
     */
    public static boolean isMergedRegion(final Cell cell) {
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
    public static boolean isMergedRegion(final Sheet sheet, final int x, final int y) {
        final int sheetMergeCount = sheet.getNumMergedRegions();
        CellRangeAddress ca;
        for (int i = 0; i < sheetMergeCount; i++) {
            ca = sheet.getMergedRegion(i);
            if (y >= ca.getFirstRow() && y <= ca.getLastRow() && x >= ca.getFirstColumn() && x <= ca.getLastColumn()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 合并单元格，可以根据设置的值来合并行和列
     *
     * @param sheet            表对象
     * @param cellRangeAddress 合并单元格范围，定义了起始行列和结束行列
     * @return 合并后的单元格号
     */
    public static int mergingCells(final Sheet sheet, final CellRangeAddress cellRangeAddress) {
        return mergingCells(sheet, cellRangeAddress, null);
    }

    /**
     * 合并单元格，可以根据设置的值来合并行和列
     *
     * @param sheet            表对象
     * @param cellRangeAddress 合并单元格范围，定义了起始行列和结束行列
     * @param cellStyle        单元格样式，只提取边框样式，null表示无样式
     * @return 合并后的单元格号
     */
    public static int mergingCells(final Sheet sheet, final CellRangeAddress cellRangeAddress,
            final CellStyle cellStyle) {
        if (cellRangeAddress.getNumberOfCells() <= 1) {
            // 非合并单元格，无需合并
            return -1;
        }
        StyleKit.setBorderStyle(sheet, cellRangeAddress, cellStyle);
        return sheet.addMergedRegion(cellRangeAddress);
    }

    /**
     * 获取合并单元格中的第一个单元格 传入的cell可以是合并单元格范围内的任意一个单元格
     *
     * @param cell {@link Cell}
     * @return 合并单元格
     */
    public static Cell getFirstCellOfMerged(final Cell cell) {
        if (null == cell) {
            return null;
        }

        final MergedCell mergedCell = getMergedCell(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex());
        if (null != mergedCell) {
            return mergedCell.getFirst();
        }

        return cell;
    }

    /**
     * 获取合并单元格 传入的x,y坐标（列行数）可以是合并单元格范围内的任意一个单元格
     *
     * @param sheet {@link Sheet}
     * @param x     列号，从0开始，可以是合并单元格范围中的任意一列
     * @param y     行号，从0开始，可以是合并单元格范围中的任意一行
     * @return 合并单元格，如果非合并单元格，返回坐标对应的单元格
     */
    public static MergedCell getMergedCell(final Sheet sheet, final int x, final int y) {
        if (null == sheet) {
            return null;
        }

        final CellRangeAddress mergedRegion = SheetKit.getMergedRegion(sheet, x, y);
        if (null != mergedRegion) {
            return MergedCell.of(getCell(sheet, mergedRegion.getFirstColumn(), mergedRegion.getFirstRow(), false),
                    mergedRegion);
        }
        return null;
    }

    /**
     * 为特定单元格添加批注
     *
     * @param cell          单元格
     * @param commentText   批注内容
     * @param commentAuthor 作者
     */
    public static void setComment(final Cell cell, final String commentText, final String commentAuthor) {
        setComment(cell, commentText, commentAuthor, null);
    }

    /**
     * 为特定单元格添加批注
     *
     * @param cell          单元格
     * @param commentText   批注内容
     * @param commentAuthor 作者，{@code null}表示无作者
     * @param anchor        批注的位置、大小等信息，null表示使用默认
     */
    public static void setComment(final Cell cell, final String commentText, final String commentAuthor,
            ClientAnchor anchor) {
        final Sheet sheet = cell.getSheet();
        final CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        if (anchor == null) {
            anchor = factory.createClientAnchor();
            // 默认位置，在注释的单元格的右方
            anchor.setCol1(cell.getColumnIndex() + 1);
            anchor.setCol2(cell.getColumnIndex() + 3);
            anchor.setRow1(cell.getRowIndex());
            anchor.setRow2(cell.getRowIndex() + 2);
            // 自适应
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        }

        final Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
        // 修正在XSSFCell中未设置地址导致错位问题
        comment.setAddress(cell.getAddress());
        comment.setString(factory.createRichTextString(commentText));
        if (null != commentAuthor) {
            comment.setAuthor(commentAuthor);
        }
        cell.setCellComment(comment);
    }

    /**
     * 移除指定单元格
     *
     * @param cell 单元格
     */
    public static void remove(final Cell cell) {
        if (null != cell) {
            cell.getRow().removeCell(cell);
        }
    }

    /**
     * 根据字符串表示法创建一个CellRangeAddress对象 该方法提供了一种通过直接使用字符串引用来创建CellRangeAddress对象的便捷方式 字符串引用应符合Excel中单元格范围的常规表示法，如"B1" 或
     * "A1:B2"。
     *
     * @param ref 代表单元格范围的字符串，格式为"A1:B2"。
     * @return 根据给定字符串创建的CellRangeAddress对象。
     * @see CellRangeAddress#valueOf(String)
     */
    public static CellRangeAddress of(final String ref) {
        return CellRangeAddress.valueOf(ref);
    }

    /**
     * 根据给定的行和列索引创建一个CellRangeAddress对象 这个方法提供了一种简洁的方式，来表示一个Excel表格中特定的区域，由起始行和列以及结束行和列定义。
     *
     * @param firstRow 起始行的索引，从0开始计数。
     * @param lastRow  结束行的索引，从0开始计数，必须大于firstRow。
     * @param firstCol 起始列的索引，从0开始计数。
     * @param lastCol  结束列的索引，从0开始计数，必须大于firstCol。
     * @return 返回一个新的CellRangeAddress对象，表示由参数定义的区域。
     */
    public static CellRangeAddress of(final int firstRow, final int lastRow, final int firstCol, final int lastCol) {
        return new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
    }

    /**
     * 根据给定的行和列索引创建一个单行的CellRangeAddress对象，从首列开始。 这个方法提供了一种简洁的方式，来表示一个Excel表格中特定的区域，由起始行和列以及结束行和列定义。
     *
     * @param row     行的索引，从0开始计数。
     * @param lastCol 结束列的索引，从0开始计数，必须大于firstCol。
     * @return 返回一个新的CellRangeAddress对象，表示由参数定义的区域。
     */
    public static CellRangeAddress ofSingleRow(final int row, final int lastCol) {
        return ofSingleRow(row, 0, lastCol);
    }

    /**
     * 根据给定的行和列索引创建一个单行的CellRangeAddress对象 这个方法提供了一种简洁的方式，来表示一个Excel表格中特定的区域，由起始行和列以及结束行和列定义。
     *
     * @param row      行的索引，从0开始计数。
     * @param firstCol 起始列的索引，从0开始计数。
     * @param lastCol  结束列的索引，从0开始计数，必须大于firstCol。
     * @return 返回一个新的CellRangeAddress对象，表示由参数定义的区域。
     */
    public static CellRangeAddress ofSingleRow(final int row, final int firstCol, final int lastCol) {
        return of(row, row, firstCol, lastCol);
    }

    /**
     * 根据给定的行和列索引创建一个单列CellRangeAddress对象，从首行开始。 这个方法提供了一种简洁的方式，来表示一个Excel表格中特定的区域，由起始行和列以及结束行和列定义。
     *
     * @param lastRow 结束行的索引，从0开始计数，必须大于firstRow。
     * @param col     列的索引，从0开始计数。
     * @return 返回一个新的CellRangeAddress对象，表示由参数定义的区域。
     */
    public static CellRangeAddress ofSingleColumn(final int lastRow, final int col) {
        return ofSingleColumn(0, lastRow, col);
    }

    /**
     * 根据给定的行和列索引创建一个单列CellRangeAddress对象。 这个方法提供了一种简洁的方式，来表示一个Excel表格中特定的区域，由起始行和列以及结束行和列定义。
     *
     * @param firstRow 起始行的索引，从0开始计数。
     * @param lastRow  结束行的索引，从0开始计数，必须大于firstRow。
     * @param col      列的索引，从0开始计数。
     * @return 返回一个新的CellRangeAddress对象，表示由参数定义的区域。
     */
    public static CellRangeAddress ofSingleColumn(final int firstRow, final int lastRow, final int col) {
        return of(firstRow, lastRow, col, col);
    }

    /**
     * 获取合并单元格{@link CellRangeAddress}，如果不是返回null
     *
     * @param sheet       {@link Sheet}
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return {@link CellRangeAddress}
     */
    public static CellRangeAddress getCellRangeAddress(final Sheet sheet, final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getCellRangeAddress(sheet, cellReference.getCol(), cellReference.getRow());
    }

    /**
     * 获取合并单元格{@link CellRangeAddress}，如果不是返回null
     *
     * @param cell {@link Cell}
     * @return {@link CellRangeAddress}
     */
    public static CellRangeAddress getCellRangeAddress(final Cell cell) {
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
    public static CellRangeAddress getCellRangeAddress(final Sheet sheet, final int x, final int y) {
        if (sheet != null) {
            final int sheetMergeCount = sheet.getNumMergedRegions();
            CellRangeAddress ca;
            for (int i = 0; i < sheetMergeCount; i++) {
                ca = sheet.getMergedRegion(i);
                if (y >= ca.getFirstRow() && y <= ca.getLastRow() && x >= ca.getFirstColumn()
                        && x <= ca.getLastColumn()) {
                    return ca;
                }
            }
        }
        return null;
    }

    /**
     * 将Sheet列号变为列名
     *
     * @param index 列号, 从0开始
     * @return 0-A; 1-B...26-AA
     */
    public static String indexToColName(int index) {
        if (index < 0) {
            return null;
        }
        final StringBuilder colName = StringKit.builder();
        do {
            if (colName.length() > 0) {
                index--;
            }
            final int remainder = index % 26;
            colName.append((char) (remainder + 'A'));
            index = (index - remainder) / 26;
        } while (index > 0);
        return colName.reverse().toString();
    }

    /**
     * 根据表元的列名转换为列号
     *
     * @param colName 列名, 从A开始
     * @return A1-0; B1-1...AA1-26
     */
    public static int colNameToIndex(final String colName) {
        final int length = colName.length();
        char c;
        int index = -1;
        for (int i = 0; i < length; i++) {
            c = Character.toUpperCase(colName.charAt(i));
            if (Character.isDigit(c)) {
                break;// 确定指定的char值是否为数字
            }
            index = (index + 1) * 26 + (int) c - 'A';
        }
        return index;
    }

    /**
     * 将Excel中地址标识符（例如A11，B5）等转换为行列表示 例如：A11 - col:0,row:10，B5-col:1,row:4
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return 坐标点
     */
    public static CellReference toCellReference(final String locationRef) {
        return new CellReference(locationRef);
    }

}
