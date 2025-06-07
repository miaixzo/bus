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
package org.miaixz.bus.office.excel;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * 分组行 用于标识和写出复杂表头。 分组概念灵感来自于EasyPOI的设计理念，见：https://blog.csdn.net/qq_45752401/article/details/121250993
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RowGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852283968653L;

    private String name;
    private CellStyle style;
    private List<RowGroup> children;

    /**
     * 构造
     *
     * @param name 分组名称
     */
    public RowGroup(final String name) {
        this.name = name;
    }

    /**
     * 创建分组
     *
     * @param name 分组名称
     * @return RowGroup
     */
    public static RowGroup of(final String name) {
        return new RowGroup(name);
    }

    /**
     * 获取分组名称
     *
     * @return 分组名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置分组名称
     *
     * @param name 分组名称
     * @return this
     */
    public RowGroup setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * 获取样式
     *
     * @return 样式
     */
    public CellStyle getStyle() {
        return style;
    }

    /**
     * 设置样式
     *
     * @param style 样式
     * @return this
     */
    public RowGroup setStyle(final CellStyle style) {
        this.style = style;
        return this;
    }

    /**
     * 获取子分组
     *
     * @return 子分组
     */
    public List<RowGroup> getChildren() {
        return children;
    }

    /**
     * 设置子分组
     *
     * @param children 子分组
     * @return this
     */
    public RowGroup setChildren(final List<RowGroup> children) {
        this.children = children;
        return this;
    }

    /**
     * 添加指定名臣的子分组，最终分组
     *
     * @param name 子分组的名称
     * @return this
     */
    public RowGroup addChild(final String name) {
        return addChild(of(name));
    }

    /**
     * 添加子分组
     *
     * @param child 子分组
     * @return this
     */
    public RowGroup addChild(final RowGroup child) {
        if (null == this.children) {
            // 无随机获取节点，节省空间
            this.children = new LinkedList<>();
        }
        this.children.add(child);
        return this;
    }

    /**
     * 分组占用的最大列数，取决于子分组占用列数
     *
     * @return 列数
     */
    public int maxColumnCount() {
        if (CollKit.isEmpty(this.children)) {
            // 无子分组，1列
            return 1;
        }
        return children.stream().mapToInt(RowGroup::maxColumnCount).sum();
    }

    /**
     * 获取最大行数，取决于子分组行数 结果为：标题行占用行数 + 子分组占用行数
     *
     * @return 最大行数
     */
    public int maxRowCount() {
        int maxRowCount = childrenMaxRowCount();
        if (null != this.name) {
            maxRowCount++;
        }

        if (0 == maxRowCount) {
            throw new IllegalArgumentException("Empty RowGroup!, please set the name or add children.");
        }

        return maxRowCount;
    }

    /**
     * 获取子分组最大占用行数
     * 
     * @return 子分组最大占用行数
     */
    public int childrenMaxRowCount() {
        int maxRowCount = 0;
        if (null != this.children) {
            maxRowCount = this.children.stream().mapToInt(RowGroup::maxRowCount).max().orElse(0);
        }
        return maxRowCount;
    }

}
