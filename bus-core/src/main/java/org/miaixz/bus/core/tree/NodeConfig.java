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
package org.miaixz.bus.core.tree;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration for tree node properties. This class allows customization of the property names used for ID, parent ID,
 * weight, name, and children in a tree structure.
 *
 * @author Kimi Liu
 * @since Java 21+
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
