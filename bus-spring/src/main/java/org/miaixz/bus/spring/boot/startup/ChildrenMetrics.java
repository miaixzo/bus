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
package org.miaixz.bus.spring.boot.startup;

import java.util.List;
import java.util.Map;

/**
 * Immutable startup stage containing an immutable snapshot of child stages.
 *
 * @param <T> child stage type
 * @author Kimi Liu
 * @since Java 21+
 */
public class ChildrenMetrics<T extends BaseMetrics> extends BaseMetrics {

    /**
     * Immutable child-stage snapshot.
     */
    private final List<T> children;

    /**
     * Creates a stage with children and no attributes.
     *
     * @param name      stage name
     * @param startTime stage start time in milliseconds
     * @param endTime   stage end time in milliseconds
     * @param children  child stages
     */
    public ChildrenMetrics(String name, long startTime, long endTime, List<? extends T> children) {
        this(name, startTime, endTime, Map.of(), children);
    }

    /**
     * Creates a stage with attributes and children.
     *
     * @param name       stage name
     * @param startTime  stage start time in milliseconds
     * @param endTime    stage end time in milliseconds
     * @param attributes stage attributes
     * @param children   child stages
     */
    protected ChildrenMetrics(String name, long startTime, long endTime, Map<String, String> attributes,
            List<? extends T> children) {
        super(name, startTime, endTime, attributes);
        this.children = List.copyOf(children == null ? List.of() : children);
    }

    /**
     * Returns the immutable child-stage snapshot.
     *
     * @return child stages
     */
    public List<T> getChildren() {
        return children;
    }

}
