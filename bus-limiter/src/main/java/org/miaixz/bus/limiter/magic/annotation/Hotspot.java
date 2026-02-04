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
package org.miaixz.bus.limiter.magic.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.limiter.magic.FlowGrade;

/**
 * Annotation for marking methods that require hotspot protection. This annotation is used to apply flow control rules
 * to frequently accessed methods or resources, preventing them from being overloaded.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Hotspot {

    /**
     * Specifies the flow grade for the hotspot rule. This determines the type of metric used for limiting, e.g., QPS or
     * thread count.
     *
     * @return The {@link FlowGrade} for the hotspot rule.
     */
    FlowGrade grade();

    /**
     * Specifies the threshold count for the hotspot rule. When the resource usage exceeds this count, the hotspot
     * protection mechanism is triggered.
     *
     * @return The count threshold for the hotspot rule.
     */
    int count();

    /**
     * Specifies the duration in seconds for which the hotspot rule is active after being triggered.
     *
     * @return The duration in seconds for the hotspot rule.
     */
    int duration();

}
