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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The task completion status class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TaskCompletionStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852281866977L;

    private Integer count;
    private Integer completedCount;

    /**
     * Returns the count.
     *
     * @return the result
     */

    public Integer getCount() {
        return count;
    }

    /**
     * Sets the count.
     *
     * @param count the count value
     */

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Returns the completed count.
     *
     * @return the result
     */

    public Integer getCompletedCount() {
        return completedCount;
    }

    /**
     * Sets the completed count.
     *
     * @param completedCount the completed count value
     */

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
