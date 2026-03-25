/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * Grouped row, used to identify and write complex headers. The concept of grouping is inspired by the design philosophy
 * of EasyPOI, see: https://blog.csdn.net/qq_45752401/article/details/121250993
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RowGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852283968653L;

    /**
     * Group name.
     */
    private String name;
    /**
     * Cell style for this group.
     */
    private CellStyle style;
    /**
     * Child groups.
     */
    private List<RowGroup> children;

    /**
     * Constructor.
     *
     * @param name The group name.
     */
    public RowGroup(final String name) {
        this.name = name;
    }

    /**
     * Creates a group.
     *
     * @param name The group name.
     * @return RowGroup
     */
    public static RowGroup of(final String name) {
        return new RowGroup(name);
    }

    /**
     * Gets the group name.
     *
     * @return The group name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the group name.
     *
     * @param name The group name.
     * @return this
     */
    public RowGroup setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the style.
     *
     * @return The style.
     */
    public CellStyle getStyle() {
        return style;
    }

    /**
     * Sets the style.
     *
     * @param style The style.
     * @return this
     */
    public RowGroup setStyle(final CellStyle style) {
        this.style = style;
        return this;
    }

    /**
     * Gets the child groups.
     *
     * @return The child groups.
     */
    public List<RowGroup> getChildren() {
        return children;
    }

    /**
     * Sets the child groups.
     *
     * @param children The child groups.
     * @return this
     */
    public RowGroup setChildren(final List<RowGroup> children) {
        this.children = children;
        return this;
    }

    /**
     * Adds a child group with the specified name, which is a leaf group.
     *
     * @param name The name of the child group.
     * @return this
     */
    public RowGroup addChild(final String name) {
        return addChild(of(name));
    }

    /**
     * Adds a child group.
     *
     * @param child The child group.
     * @return this
     */
    public RowGroup addChild(final RowGroup child) {
        if (null == this.children) {
            // Using LinkedList to save space as random access is not needed.
            this.children = new LinkedList<>();
        }
        this.children.add(child);
        return this;
    }

    /**
     * The maximum number of columns occupied by the group, which depends on the columns occupied by its children.
     *
     * @return The number of columns.
     */
    public int maxColumnCount() {
        if (CollKit.isEmpty(this.children)) {
            // No child groups, 1 column.
            return 1;
        }
        return children.stream().mapToInt(RowGroup::maxColumnCount).sum();
    }

    /**
     * Gets the maximum number of rows, which depends on the rows occupied by its children. The result is: number of
     * header rows + number of rows occupied by child groups.
     *
     * @return The maximum number of rows.
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
     * Gets the maximum number of rows occupied by child groups.
     *
     * @return The maximum number of rows occupied by child groups.
     */
    public int childrenMaxRowCount() {
        int maxRowCount = 0;
        if (null != this.children) {
            maxRowCount = this.children.stream().mapToInt(RowGroup::maxRowCount).max().orElse(0);
        }
        return maxRowCount;
    }

}
