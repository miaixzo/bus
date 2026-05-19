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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The approval state class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ApprovalState implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852236560169L;

    private Boolean approvalRulesOverwritten;
    private List<ApprovalRule> rules;

    /**
     * Returns the approval rules overwritten.
     *
     * @return the result
     */

    public Boolean getApprovalRulesOverwritten() {
        return approvalRulesOverwritten;
    }

    /**
     * Sets the approval rules overwritten.
     *
     * @param approvalRulesOverwritten the approval rules overwritten value
     */

    public void setApprovalRulesOverwritten(Boolean approvalRulesOverwritten) {
        this.approvalRulesOverwritten = approvalRulesOverwritten;
    }

    /**
     * Returns the rules.
     *
     * @return the result
     */

    public List<ApprovalRule> getRules() {
        return rules;
    }

    /**
     * Sets the rules.
     *
     * @param rules the rules value
     */

    public void setRules(List<ApprovalRule> rules) {
        this.rules = rules;
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
