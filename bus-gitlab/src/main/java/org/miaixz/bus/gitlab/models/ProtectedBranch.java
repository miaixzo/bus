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
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;
import java.io.Serial;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BranchAccessLevel> getPushAccessLevels() {
        return this.pushAccessLevels;
    }

    public void setPushAccessLevels(List<BranchAccessLevel> pushAccessLevels) {
        this.pushAccessLevels = pushAccessLevels;
    }

    public List<BranchAccessLevel> getMergeAccessLevels() {
        return this.mergeAccessLevels;
    }

    public void setMergeAccessLevels(List<BranchAccessLevel> mergeAccessLevels) {
        this.mergeAccessLevels = mergeAccessLevels;
    }

    public List<BranchAccessLevel> getUnprotectAccessLevels() {
        return unprotectAccessLevels;
    }

    public void setUnprotectAccessLevels(List<BranchAccessLevel> unprotectAccessLevels) {
        this.unprotectAccessLevels = unprotectAccessLevels;
    }

    public static final boolean isValid(ProtectedBranch branch) {
        return (branch != null && branch.getName() != null);
    }

    public ProtectedBranch withName(String name) {
        this.name = name;
        return this;
    }

    public ProtectedBranch withPushAccessLevels(List<BranchAccessLevel> pushAccessLevels) {
        this.pushAccessLevels = pushAccessLevels;
        return this;
    }

    public ProtectedBranch withMergeAccessLevels(List<BranchAccessLevel> mergeAccessLevels) {
        this.mergeAccessLevels = mergeAccessLevels;
        return this;
    }

    public Boolean getCodeOwnerApprovalRequired() {
        return codeOwnerApprovalRequired;
    }

    public void setCodeOwnerApprovalRequired(Boolean codeOwnerApprovalRequired) {
        this.codeOwnerApprovalRequired = codeOwnerApprovalRequired;
    }

    public ProtectedBranch withCodeOwnerApprovalRequired(Boolean codeOwnerApprovalRequired) {
        this.codeOwnerApprovalRequired = codeOwnerApprovalRequired;
        return this;
    }

    public Boolean getAllowForcePush() {
        return allowForcePush;
    }

    public void setAllowForcePush(Boolean allowForcePush) {
        this.allowForcePush = allowForcePush;
    }

    public ProtectedBranch withAllowForcePush(Boolean allowForcePush) {
        this.allowForcePush = allowForcePush;
        return this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
