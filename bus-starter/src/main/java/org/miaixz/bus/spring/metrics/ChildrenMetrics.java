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
 * @since Java 17+
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
