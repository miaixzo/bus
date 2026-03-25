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
package org.miaixz.bus.limiter.magic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration representing different grades or types of flow control. These grades are typically used to specify the
 * dimension of limiting, such as by thread or by QPS.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum FlowGrade {

    /**
     * Flow control based on the number of concurrent threads.
     */
    FLOW_GRADE_THREAD(0),
    /**
     * Flow control based on Queries Per Second (QPS).
     */
    FLOW_GRADE_QPS(1);

    /**
     * The integer value representing the grade of flow control.
     */
    private int grade;

}
