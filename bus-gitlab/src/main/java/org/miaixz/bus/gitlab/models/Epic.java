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
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The epic class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Epic extends AbstractEpic<Epic> {

    @Serial
    private static final long serialVersionUID = 2852252677578L;

    private Boolean startDateIsFixed;
    private Boolean dueDateIsFixed;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date dueDateFromInheritedSource;

    private Boolean subscribed;

    /**
     * Returns the start date is fixed.
     *
     * @return the result
     */

    public Boolean getStartDateIsFixed() {
        return startDateIsFixed;
    }

    /**
     * Sets the start date is fixed.
     *
     * @param startDateIsFixed the start date is fixed value
     */

    public void setStartDateIsFixed(Boolean startDateIsFixed) {
        this.startDateIsFixed = startDateIsFixed;
    }

    /**
     * Returns the due date is fixed.
     *
     * @return the result
     */

    public Boolean getDueDateIsFixed() {
        return dueDateIsFixed;
    }

    /**
     * Sets the due date is fixed.
     *
     * @param dueDateIsFixed the due date is fixed value
     */

    public void setDueDateIsFixed(Boolean dueDateIsFixed) {
        this.dueDateIsFixed = dueDateIsFixed;
    }

    /**
     * Returns the due date from inherited source.
     *
     * @return the result
     */

    public Date getDueDateFromInheritedSource() {
        return dueDateFromInheritedSource;
    }

    /**
     * Sets the due date from inherited source.
     *
     * @param dueDateFromInheritedSource the due date from inherited source value
     */

    public void setDueDateFromInheritedSource(Date dueDateFromInheritedSource) {
        this.dueDateFromInheritedSource = dueDateFromInheritedSource;
    }

    /**
     * Returns the subscribed.
     *
     * @return the result
     */

    public Boolean getSubscribed() {
        return subscribed;
    }

    /**
     * Sets the subscribed.
     *
     * @param subscribed the subscribed value
     */

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
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
