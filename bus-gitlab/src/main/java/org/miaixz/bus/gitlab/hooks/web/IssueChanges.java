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
 * The issue changes class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IssueChanges extends EventChanges {

    private ChangeContainer<Date> dueDate;
    private ChangeContainer<Boolean> confidential;
    private ChangeContainer<String> heathStatus;

    /**
     * Returns the due date.
     *
     * @return the result
     */

    public ChangeContainer<Date> getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date.
     *
     * @param dueDate the due date value
     */

    public void setDueDate(ChangeContainer<Date> dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Returns the confidential.
     *
     * @return the result
     */

    public ChangeContainer<Boolean> getConfidential() {
        return confidential;
    }

    /**
     * Sets the confidential.
     *
     * @param confidential the confidential value
     */

    public void setConfidential(ChangeContainer<Boolean> confidential) {
        this.confidential = confidential;
    }

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

}
