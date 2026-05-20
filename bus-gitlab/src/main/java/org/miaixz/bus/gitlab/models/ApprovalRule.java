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
 * The approval rule class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
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
        return name;
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
     * Returns the rule type.
     *
     * @return the result
     */

    public String getRuleType() {
        return ruleType;
    }

    /**
     * Sets the rule type.
     *
     * @param ruleType the rule type value
     */

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    /**
     * Returns the report type.
     *
     * @return the result
     */

    public String getReportType() {
        return reportType;
    }

    /**
     * Sets the report type.
     *
     * @param reportType the report type value
     */

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    /**
     * Returns the eligible approvers.
     *
     * @return the result
     */

    public List<User> getEligibleApprovers() {
        return eligibleApprovers;
    }

    /**
     * Sets the eligible approvers.
     *
     * @param eligibleApprovers the eligible approvers value
     */

    public void setEligibleApprovers(List<User> eligibleApprovers) {
        this.eligibleApprovers = eligibleApprovers;
    }

    /**
     * Returns the approvals required.
     *
     * @return the result
     */

    public Integer getApprovalsRequired() {
        return approvalsRequired;
    }

    /**
     * Sets the approvals required.
     *
     * @param approvalsRequired the approvals required value
     */

    public void setApprovalsRequired(Integer approvalsRequired) {
        this.approvalsRequired = approvalsRequired;
    }

    /**
     * Returns the source rule.
     *
     * @return the result
     */

    public ApprovalRule getSourceRule() {
        return sourceRule;
    }

    /**
     * Sets the source rule.
     *
     * @param sourceRule the source rule value
     */

    public void setSourceRule(ApprovalRule sourceRule) {
        this.sourceRule = sourceRule;
    }

    /**
     * Returns the users.
     *
     * @return the result
     */

    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets the users.
     *
     * @param users the users value
     */

    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Returns the groups.
     *
     * @return the result
     */

    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Sets the groups.
     *
     * @param groups the groups value
     */

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    /**
     * Returns the contains hidden groups.
     *
     * @return the result
     */

    public Boolean getContainsHiddenGroups() {
        return containsHiddenGroups;
    }

    /**
     * Sets the contains hidden groups.
     *
     * @param containsHiddenGroups the contains hidden groups value
     */

    public void setContainsHiddenGroups(Boolean containsHiddenGroups) {
        this.containsHiddenGroups = containsHiddenGroups;
    }

    /**
     * Returns the approved by.
     *
     * @return the result
     */

    public List<User> getApprovedBy() {
        return approvedBy;
    }

    /**
     * Sets the approved by.
     *
     * @param approvedBy the approved by value
     */

    public void setApprovedBy(List<User> approvedBy) {
        this.approvedBy = approvedBy;
    }

    /**
     * Returns the applies to all protected branches.
     *
     * @return the result
     */

    public Boolean getAppliesToAllProtectedBranches() {
        return appliesToAllProtectedBranches;
    }

    /**
     * Sets the applies to all protected branches.
     *
     * @param appliesToAllProtectedBranches the applies to all protected branches value
     */

    public void setAppliesToAllProtectedBranches(Boolean appliesToAllProtectedBranches) {
        this.appliesToAllProtectedBranches = appliesToAllProtectedBranches;
    }

    /**
     * Returns the approved.
     *
     * @return the result
     */

    public Boolean getApproved() {
        return approved;
    }

    /**
     * Sets the approved.
     *
     * @param approved the approved value
     */

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    /**
     * Returns the protected branches.
     *
     * @return the result
     */

    public List<ProtectedBranch> getProtectedBranches() {
        return protectedBranches;
    }

    /**
     * Sets the protected branches.
     *
     * @param protectedBranches the protected branches value
     */

    public void setProtectedBranches(List<ProtectedBranch> protectedBranches) {
        this.protectedBranches = protectedBranches;
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
