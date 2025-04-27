/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * 模板Excel写入器 解析已有模板，并填充模板中的变量为数据
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetTemplateWriter {

    private final Sheet sheet;
    /**
     * Excel输出配置
     */
    private final ExcelWriteConfig config;
    /**
     * 模板上下文，存储模板中变量及其位置信息
     */
    private final TemplateContext templateContext;

    /**
     * 构造
     *
     * @param sheet  {@link Sheet}
     * @param config Excel写配置
     */
    public SheetTemplateWriter(final Sheet sheet, final ExcelWriteConfig config) {
        this.sheet = sheet;
        this.config = config;
        this.templateContext = new TemplateContext(sheet);
    }

    /**
     * 填充非列表模板变量（一次性变量）
     *
     * @param rowMap 行数据
     * @return this
     */
    public SheetTemplateWriter fillOnce(final Map<?, ?> rowMap) {
        this.templateContext.fill(rowMap, false);
        return this;
    }

    /**
     * 填充模板行，用于列表填充
     *
     * @param rowBean 行的Bean或Map数据
     * @return this
     */
    public SheetTemplateWriter fillRow(final Object rowBean) {
        if (this.config.insertRow) {
            // 当前填充行的模板行以下全部下移
            final int bottomRowIndex = this.templateContext.getBottomRowIndex(rowBean);
            if (bottomRowIndex < 0) {
                // 无可填充行
                return this;
            }
            if (bottomRowIndex != 0) {
                final int lastRowNum = this.sheet.getLastRowNum();
                if (bottomRowIndex <= lastRowNum) {
                    // 填充行底部需有数据，无数据跳过
                    // 虚拟行的行号就是需要填充的行，这行的已有数据整体下移
                    this.sheet.shiftRows(bottomRowIndex, this.sheet.getLastRowNum(), 1);
                }
            }
        }

        this.templateContext.fill(rowBean, true);

        return this;
    }

}
