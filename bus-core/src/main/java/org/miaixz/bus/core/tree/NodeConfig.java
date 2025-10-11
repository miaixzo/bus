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
package org.miaixz.bus.core.tree;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration for tree node properties. This class allows customization of the property names used for ID, parent ID,
 * weight, name, and children in a tree structure.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NodeConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852250253009L;

    /**
     * The default node configuration instance.
     */
    public static final NodeConfig DEFAULT_CONFIG = new NodeConfig();

    /**
     * The name of the ID property. Default is "id".
     */
    private String idKey = "id";
    /**
     * The name of the parent ID property. Default is "parentId".
     */
    private String parentIdKey = "parentId";
    /**
     * The name of the weight property. Default is "selector".
     */
    private String weightKey = "selector";
    /**
     * The name of the name property. Default is "name".
     */
    private String nameKey = "name";
    /**
     * The name of the children property. Default is "children".
     */
    private String childrenKey = "children";
    /**
     * The maximum depth of the tree to build, starting from 0. A null value means unlimited depth.
     */
    private Integer deep;

    /**
     * Gets the property name for the node's ID.
     *
     * @return The ID property name.
     */
    public String getIdKey() {
        return this.idKey;
    }

    /**
     * Sets the property name for the node's ID.
     *
     * @param idKey The new ID property name.
     * @return this
     */
    public NodeConfig setIdKey(final String idKey) {
        this.idKey = idKey;
        return this;
    }

    /**
     * Gets the property name for the node's weight.
     *
     * @return The weight property name.
     */
    public String getWeightKey() {
        return this.weightKey;
    }

    /**
     * Sets the property name for the node's weight.
     *
     * @param weightKey The new weight property name.
     * @return this
     */
    public NodeConfig setWeightKey(final String weightKey) {
        this.weightKey = weightKey;
        return this;
    }

    /**
     * Gets the property name for the node's name.
     *
     * @return The name property name.
     */
    public String getNameKey() {
        return this.nameKey;
    }

    /**
     * Sets the property name for the node's name.
     *
     * @param nameKey The new name property name.
     * @return this
     */
    public NodeConfig setNameKey(final String nameKey) {
        this.nameKey = nameKey;
        return this;
    }

    /**
     * Gets the property name for the node's children list.
     *
     * @return The children property name.
     */
    public String getChildrenKey() {
        return this.childrenKey;
    }

    /**
     * Sets the property name for the node's children list.
     *
     * @param childrenKey The new children property name.
     * @return this
     */
    public NodeConfig setChildrenKey(final String childrenKey) {
        this.childrenKey = childrenKey;
        return this;
    }

    /**
     * Gets the property name for the node's parent ID.
     *
     * @return The parent ID property name.
     */
    public String getParentIdKey() {
        return this.parentIdKey;
    }

    /**
     * Sets the property name for the node's parent ID.
     *
     * @param parentIdKey The new parent ID property name.
     * @return this
     */
    public NodeConfig setParentIdKey(final String parentIdKey) {
        this.parentIdKey = parentIdKey;
        return this;
    }

    /**
     * Gets the maximum recursion depth for building the tree.
     *
     * @return The maximum depth, or {@code null} for unlimited.
     */
    public Integer getDeep() {
        return this.deep;
    }

    /**
     * Sets the maximum recursion depth for building the tree.
     *
     * @param deep The maximum depth (starting from 0).
     * @return this
     */
    public NodeConfig setDeep(final Integer deep) {
        this.deep = deep;
        return this;
    }

}
