/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.tree.parser;

import java.util.Map;

import org.miaixz.bus.core.tree.MapTree;
import org.miaixz.bus.core.tree.TreeNode;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * The default, simple converter for turning a {@link TreeNode} into a {@link MapTree}.
 *
 * @param <T> The type of the node's identifier.
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultNodeParser<T> implements NodeParser<TreeNode<T>, T> {

    /**
     * Constructs a new DefaultNodeParser. Utility class constructor for static access.
     */
    public DefaultNodeParser() {
    }

    /**
     * Converts a {@link TreeNode} object to a {@link MapTree} object. It maps the basic properties (ID, parent ID,
     * weight, name) and any extended properties.
     *
     * @param treeNode The source {@link TreeNode} object.
     * @param tree     The target {@link MapTree} object to populate.
     */
    @Override
    public void parse(final TreeNode<T> treeNode, final MapTree<T> tree) {
        tree.setId(treeNode.getId());
        tree.setParentId(treeNode.getParentId());
        tree.setWeight(treeNode.getWeight());
        tree.setName(treeNode.getName());

        // Copy extended fields
        final Map<String, Object> extra = treeNode.getExtra();
        if (MapKit.isNotEmpty(extra)) {
            extra.forEach(tree::putExtra);
        }
    }

}
