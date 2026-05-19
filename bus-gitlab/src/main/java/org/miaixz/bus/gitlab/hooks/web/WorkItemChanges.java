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
package org.miaixz.bus.gitlab.hooks.web;

import java.util.Date;

/**
 * The work item changes class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WorkItemChanges extends EventChanges {

    private ChangeContainer<String> heathStatus;
    private ChangeContainer<Date> lastEditedAt;

    /**
     * Returns the heath status.
     *
     * @return the result
     */

    public ChangeContainer<String> getHeathStatus() {
        return heathStatus;
    }

    /**
     * Sets the heath status.
     *
     * @param heathStatus the heath status value
     */

    public void setHeathStatus(ChangeContainer<String> heathStatus) {
        this.heathStatus = heathStatus;
    }

    /**
     * Returns the last edited at.
     *
     * @return the result
     */

    public ChangeContainer<Date> getLastEditedAt() {
        return lastEditedAt;
    }

    /**
     * Sets the last edited at.
     *
     * @param lastEditedAt the last edited at value
     */

    public void setLastEditedAt(ChangeContainer<Date> lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

}
