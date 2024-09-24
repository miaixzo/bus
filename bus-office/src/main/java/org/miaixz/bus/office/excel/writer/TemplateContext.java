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
package org.miaixz.bus.office.excel.writer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.office.excel.cell.VirtualCell;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.SheetKit;

/**
 * 模板上下文，记录了模板中变量所在的Cell
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TemplateContext {

    /**
     * 变量正则
     * <ol>
     * <li>变量名只能包含字母、数字、下划线、$符号、.符号，不能以数字开头</li>
     * <li>变量以 { 开始，以 } 结束</li>
     * <li>\{表示转义，非变量符号</li>
     * <li>.开头的变量表示列表，.出现在中间，表示表达式子对象</li>
     * </ol>
     */
    private static final Pattern VAR_PATTERN = Pattern.compile("(?<!\\\\)\\{([.$_a-zA-Z]+\\d*[.$_a-zA-Z]*)}");
    private static final Pattern ESCAPE_VAR_PATTERN = Pattern.compile("\\\\\\{([.$_a-zA-Z]+\\d*[.$_a-zA-Z]*)\\\\}");

    /**
     * 存储变量对应单元格的映射
     */
    private final Map<String, Cell> varMap = new LinkedHashMap<>();

    /**
     * 构造
     *
     * @param templateSheet 模板sheet
     */
    public TemplateContext(final Sheet templateSheet) {
        init(templateSheet);
    }

    /**
     * 获取变量对应的单元格，列表变量以.开头
     *
     * @param varName 变量名
     * @return 单元格
     */
    public Cell getCell(final String varName) {
        return varMap.get(varName);
    }

    /**
     * 获取当前替换的数据行对应变量的底部索引 此方法用户获取填充行，以便下移填充行后的行
     * <ul>
     * <li>如果为实体单元格，直接填充，无需下移，返回0</li>
     * <li>如果为{@link VirtualCell}，返回最底部虚拟单元格各行号</li>
     * </ul>
     *
     * @param rowDataBean 填充数据
     * @return 最大行索引，-1表示无数据填充，0表示无需下移
     */
    public int getBottomRowIndex(final Object rowDataBean) {
        final AtomicInteger bottomRowIndex = new AtomicInteger(-1);
        this.varMap.forEach((name, cell) -> {
            if (null != BeanKit.getProperty(rowDataBean, name)) {
                if (cell instanceof VirtualCell) {
                    bottomRowIndex.set(Math.max(bottomRowIndex.get(), cell.getRowIndex()));
                } else if (bottomRowIndex.get() < 0) {
                    // 实体单元格，直接填充，无需下移
                    bottomRowIndex.set(0);
                }
            }
        });
        return bottomRowIndex.get();
    }

    /**
     * 填充变量名name指向的单元格
     *
     * @param rowDataBean 一行数据的键值对
     * @param isListVar   是否为列表填充，列表填充会自动指向下一列，否则填充结束后删除变量
     */
    public void fill(final Object rowDataBean, final boolean isListVar) {
        final Map<String, Cell> varMap = this.varMap;
        varMap.forEach((name, cell) -> {
            if (null == cell) {
                return;
            }

            final String templateStr = cell.getStringCellValue();
            // 填充单元格
            if (fill(cell, name, rowDataBean)) {
                // 指向下一个单元格
                putNext(name, cell, templateStr, isListVar);
            }
        });

        if (!isListVar) {
            // 清理已经匹配完毕的变量
            MapKit.removeNullValue(varMap);
        }
    }

    /**
     * 将变量指向下一行的单元格 如果为列表，则指向下一行的虚拟单元格（不创建单元格） 如果非列表，则清空此变量
     *
     * @param name        变量名
     * @param currentCell 当前单元格
     * @param templateStr 模板字符串
     * @param isListVar   是否为列表填充
     */
    private void putNext(final String name, final Cell currentCell, final String templateStr, final boolean isListVar) {
        if (isListVar) {
            // 指向下一列的单元格
            final Cell next = new VirtualCell(currentCell, currentCell.getColumnIndex(), currentCell.getRowIndex() + 1,
                    templateStr);
            varMap.put(name, next);
        } else {
            // 非列表，一次性填充，即变量填充后，和此单元格去掉关联
            varMap.put(name, null);
        }
    }

    /**
     * 填充数据
     *
     * @param cell        单元格，非模板中变量所在单元格则为{@link VirtualCell}
     * @param name        变量名
     * @param rowDataBean 填充的数据，可以为Map或Bean
     * @return 是否填充成功，{@code false}表示无数据
     */
    private boolean fill(Cell cell, final String name, final Object rowDataBean) {
        final String templateStr = cell.getStringCellValue();
        if (cell instanceof VirtualCell) {
            // 虚拟单元格，转换为实际单元格
            final Cell newCell;

            newCell = CellKit.getCell(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex(), true);
            Assert.notNull(newCell, "Can not get or create cell at {},{}", cell.getColumnIndex(), cell.getRowIndex());

            newCell.setCellStyle(cell.getCellStyle());
            cell = newCell;
        }

        final Object cellValue;
        // 模板替换
        if (StringKit.equals(name, StringKit.unWrap(templateStr, Symbol.BRACE_LEFT, Symbol.BRACE_RIGHT))) {
            // 一个单元格只有一个变量，支持多级表达式
            cellValue = BeanKit.getProperty(rowDataBean, name);
            if (null == cellValue) {
                // 对应表达式无提供的值，跳过填充
                return false;
            }
        } else {
            // 模板中存在多个变量或模板填充，直接赋值为String
            // 没有找到值的变量保留原样
            cellValue = StringKit.formatByBean(templateStr, rowDataBean, true);
            if (ObjectKit.equals(cellValue, templateStr)) {
                // 模板无修改，说明没有变量替换，跳过填充
                return false;
            }
        }
        CellKit.setCellValue(cell, cellValue);
        return true;
    }

    /**
     * 初始化，提取变量及位置，并将转义的变量回填
     *
     * @param templateSheet 模板sheet
     */
    private void init(final Sheet templateSheet) {
        SheetKit.walk(templateSheet, (cell, ctx) -> {
            if (CellType.STRING == cell.getCellType()) {
                // 只读取字符串类型的单元格
                final String cellValue = cell.getStringCellValue();

                // 字符串中可能有多个变量
                final List<String> vars = PatternKit.findAllGroup1(VAR_PATTERN, cellValue);
                if (CollKit.isNotEmpty(vars)) {
                    // 模板变量
                    for (final String var : vars) {
                        varMap.put(var, cell);
                    }
                }

                // 替换转义的变量
                final String text = PatternKit.replaceAll(cellValue, ESCAPE_VAR_PATTERN,
                        (matcher) -> Symbol.BRACE_LEFT + matcher.group(1) + Symbol.BRACE_RIGHT);
                if (!StringKit.equals(cellValue, text)) {
                    cell.setCellValue(text);
                }
            }
        });
    }

    @Override
    public String toString() {
        return "TemplateContext{" + "varMap=" + varMap + '}';
    }

}
