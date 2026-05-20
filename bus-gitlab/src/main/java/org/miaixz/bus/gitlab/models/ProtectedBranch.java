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
 * The protected branch class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProtectedBranch implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852275321353L;

    private Long id;
    private String name;
    private List<BranchAccessLevel> pushAccessLevels;
    private List<BranchAccessLevel> mergeAccessLevels;
    private List<BranchAccessLevel> unprotectAccessLevels;
    private Boolean codeOwnerApprovalRequired;
    private Boolean allowForcePush;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the push access levels.
     *
     * @return the result
     */

    public List<BranchAccessLevel> getPushAccessLevels() {
        return this.pushAccessLevels;
    }

    /**
     * Sets the push access levels.
     *
     * @param pushAccessLevels the push access levels value
     */

    public void setPushAccessLevels(List<BranchAccessLevel> pushAccessLevels) {
        this.pushAccessLevels = pushAccessLevels;
    }

    /**
     * Returns the merge access levels.
     *
     * @return the result
     */

    public List<BranchAccessLevel> getMergeAccessLevels() {
        return this.mergeAccessLevels;
    }

    /**
     * Sets the merge access levels.
     *
     * @param mergeAccessLevels the merge access levels value
     */

    public void setMergeAccessLevels(List<BranchAccessLevel> mergeAccessLevels) {
        this.mergeAccessLevels = mergeAccessLevels;
    }

    /**
     * Returns the unprotect access levels.
     *
     * @return the result
     */

    public List<BranchAccessLevel> getUnprotectAccessLevels() {
        return unprotectAccessLevels;
    }

    /**
     * Sets the unprotect access levels.
     *
     * @param unprotectAccessLevels the unprotect access levels value
     */

    public void setUnprotectAccessLevels(List<BranchAccessLevel> unprotectAccessLevels) {
        this.unprotectAccessLevels = unprotectAccessLevels;
    }

    /**
     * Returns whether the valid is enabled.
     *
     * @param branch the branch value
     * @return the result
     */

    public static final boolean isValid(ProtectedBranch branch) {
        return (branch != null && branch.getName() != null);
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public ProtectedBranch withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the push access levels and returns this instance.
     *
     * @param pushAccessLevels the push access levels value
     * @return the result
     */

    public ProtectedBranch withPushAccessLevels(List<BranchAccessLevel> pushAccessLevels) {
        this.pushAccessLevels = pushAccessLevels;
        return this;
    }

    /**
     * Sets the merge access levels and returns this instance.
     *
     * @param mergeAccessLevels the merge access levels value
     * @return the result
     */

    public ProtectedBranch withMergeAccessLevels(List<BranchAccessLevel> mergeAccessLevels) {
        this.mergeAccessLevels = mergeAccessLevels;
        return this;
    }

    /**
     * Returns the code owner approval required.
     *
     * @return the result
     */

    public Boolean getCodeOwnerApprovalRequired() {
        return codeOwnerApprovalRequired;
    }

    /**
     * Sets the code owner approval required.
     *
     * @param codeOwnerApprovalRequired the code owner approval required value
     */

    public void setCodeOwnerApprovalRequired(Boolean codeOwnerApprovalRequired) {
        this.codeOwnerApprovalRequired = codeOwnerApprovalRequired;
    }

    /**
     * Sets the code owner approval required and returns this instance.
     *
     * @param codeOwnerApprovalRequired the code owner approval required value
     * @return the result
     */

    public ProtectedBranch withCodeOwnerApprovalRequired(Boolean codeOwnerApprovalRequired) {
        this.codeOwnerApprovalRequired = codeOwnerApprovalRequired;
        return this;
    }

    /**
     * Returns the allow force push.
     *
     * @return the result
     */

    public Boolean getAllowForcePush() {
        return allowForcePush;
    }

    /**
     * Sets the allow force push.
     *
     * @param allowForcePush the allow force push value
     */

    public void setAllowForcePush(Boolean allowForcePush) {
        this.allowForcePush = allowForcePush;
    }

    /**
     * Sets the allow force push and returns this instance.
     *
     * @param allowForcePush the allow force push value
     * @return the result
     */

    public ProtectedBranch withAllowForcePush(Boolean allowForcePush) {
        this.allowForcePush = allowForcePush;
        return this;
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
