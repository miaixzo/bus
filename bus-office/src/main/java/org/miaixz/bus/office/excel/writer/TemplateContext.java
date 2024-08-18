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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.SheetKit;

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
    private final Map<String, Cell> varMap = new HashMap<>();

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
                        (matcher) -> "{" + matcher.group(1) + "}");
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
