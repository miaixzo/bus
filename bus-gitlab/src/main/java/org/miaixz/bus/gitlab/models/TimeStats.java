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
 * The time stats class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TimeStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852281923917L;

    private Integer timeEstimate;
    private Integer totalTimeSpent;
    private Duration humanTimeEstimate;
    private Duration humanTotalTimeSpent;

    /**
     * Returns the time estimate.
     *
     * @return the result
     */

    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    /**
     * Sets the time estimate.
     *
     * @param timeEstimate the time estimate value
     */

    public void setTimeEstimate(Integer timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    /**
     * Returns the total time spent.
     *
     * @return the result
     */

    public Integer getTotalTimeSpent() {
        return totalTimeSpent;
    }

    /**
     * Sets the total time spent.
     *
     * @param totalTimeSpent the total time spent value
     */

    public void setTotalTimeSpent(Integer totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    /**
     * Returns the human time estimate.
     *
     * @return the result
     */

    public Duration getHumanTimeEstimate() {
        return humanTimeEstimate;
    }

    /**
     * Sets the human time estimate.
     *
     * @param humanTimeEstimate the human time estimate value
     */

    public void setHumanTimeEstimate(Duration humanTimeEstimate) {
        this.humanTimeEstimate = humanTimeEstimate;
    }

    /**
     * Returns the human total time spent.
     *
     * @return the result
     */

    public Duration getHumanTotalTimeSpent() {
        return humanTotalTimeSpent;
    }

    /**
     * Sets the human total time spent.
     *
     * @param humanTotalTimeSpent the human total time spent value
     */

    public void setHumanTotalTimeSpent(Duration humanTotalTimeSpent) {
        this.humanTotalTimeSpent = humanTotalTimeSpent;
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
