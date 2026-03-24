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
package org.miaixz.bus.spring.metrics;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A metrics model that holds child metrics, used to build a hierarchical metrics system.
 * <p>
 * This class extends {@link BaseMetrics} and adds the functionality to manage child metrics. By using a thread-safe
 * {@link CopyOnWriteArrayList} for storing child metrics, it ensures thread safety in a multi-threaded environment
 * while maintaining read performance. This design allows for building a tree-like structure of metrics, facilitating
 * the organization and display of complex monitoring data.
 * </p>
 *
 * @param <T> The type of the child metrics, which must extend {@link BaseMetrics}.
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class ChildrenMetrics<T extends BaseMetrics> extends BaseMetrics {

    /**
     * A list of child metrics, implemented using a thread-safe {@link CopyOnWriteArrayList}.
     * <p>
     * {@code CopyOnWriteArrayList} is suitable for concurrent scenarios where reads far outnumber writes, providing
     * thread safety while ensuring good read performance.
     * </p>
     */
    private List<T> children = new CopyOnWriteArrayList<>();

    /**
     * Adds a specified child metric to the {@link #children} list.
     *
     * @param child The child metric to add (must not be null).
     */
    public void addChild(T child) {
        this.children.add(child);
    }

}
