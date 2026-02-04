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

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

public class ApprovalRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852236195081L;

    private Long id;
    private String name;
    private String ruleType;
    private String reportType;
    private List<User> eligibleApprovers;
    private Integer approvalsRequired;
    private ApprovalRule sourceRule;
    private List<User> users;
    private List<Group> groups;
    private Boolean containsHiddenGroups;
    private List<User> approvedBy;
    private Boolean approved;
    private Boolean appliesToAllProtectedBranches;
    private List<ProtectedBranch> protectedBranches;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public List<User> getEligibleApprovers() {
        return eligibleApprovers;
    }

    public void setEligibleApprovers(List<User> eligibleApprovers) {
        this.eligibleApprovers = eligibleApprovers;
    }

    public Integer getApprovalsRequired() {
        return approvalsRequired;
    }

    public void setApprovalsRequired(Integer approvalsRequired) {
        this.approvalsRequired = approvalsRequired;
    }

    public ApprovalRule getSourceRule() {
        return sourceRule;
    }

    public void setSourceRule(ApprovalRule sourceRule) {
        this.sourceRule = sourceRule;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Boolean getContainsHiddenGroups() {
        return containsHiddenGroups;
    }

    public void setContainsHiddenGroups(Boolean containsHiddenGroups) {
        this.containsHiddenGroups = containsHiddenGroups;
    }

    public List<User> getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(List<User> approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Boolean getAppliesToAllProtectedBranches() {
        return appliesToAllProtectedBranches;
    }

    public void setAppliesToAllProtectedBranches(Boolean appliesToAllProtectedBranches) {
        this.appliesToAllProtectedBranches = appliesToAllProtectedBranches;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public List<ProtectedBranch> getProtectedBranches() {
        return protectedBranches;
    }

    public void setProtectedBranches(List<ProtectedBranch> protectedBranches) {
        this.protectedBranches = protectedBranches;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
