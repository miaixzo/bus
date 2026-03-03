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

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class ApprovalRuleParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852236337509L;

    private Integer approvalsRequired;
    private String name;
    private Boolean appliesToAllProtectedBranches;
    private List<Long> groupIds;
    private List<Long> protectedBranchIds;
    private String reportType;
    private String ruleType;
    private List<Long> userIds;
    private List<String> usernames;

    /**
     * @param approvalsRequired The number of required approvals for this rule.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withApprovalsRequired(Integer approvalsRequired) {
        this.approvalsRequired = approvalsRequired;
        return (this);
    }

    /**
     * @param name The name of the approval rule.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withName(String name) {
        this.name = name;
        return (this);
    }

    /**
     * @param appliesToAllProtectedBranches Whether the rule is applied to all protected branches. If set to true, the
     *                                      value of protected_branch_ids is ignored. Default is false. Introduced in
     *                                      GitLab 15.3.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withAppliesToAllProtectedBranches(Boolean appliesToAllProtectedBranches) {
        this.appliesToAllProtectedBranches = appliesToAllProtectedBranches;
        return (this);
    }

    /**
     * @param groupIds The IDs of groups as approvers.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
        return (this);
    }

    /**
     * @param protectedBranchIds The IDs of protected branches to scope the rule by. To identify the ID, use the API.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withProtectedBranchIds(List<Long> protectedBranchIds) {
        this.protectedBranchIds = protectedBranchIds;
        return (this);
    }

    /**
     * @param reportType The report type required when the rule type is report_approver. The supported report types are
     *                   license_scanning (Deprecated in GitLab 15.9) and code_coverage.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withReportType(String reportType) {
        this.reportType = reportType;
        return (this);
    }

    /**
     * @param ruleType The type of rule. any_approver is a pre-configured default rule with approvals_required at 0.
     *                 Other rules are regular and report_approver.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withRuleType(String ruleType) {
        this.ruleType = ruleType;
        return (this);
    }

    /**
     * @param userIds The IDs of users as approvers. If you provide both user_ids and usernames, both lists of users are
     *                added.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withUserIds(List<Long> userIds) {
        this.userIds = userIds;
        return (this);
    }

    /**
     * @param usernames The usernames of approvers for this rule (same as user_ids but requires a list of usernames). If
     *                  you provide both user_ids and usernames, both lists of users are added.
     * @return this ApprovalRuleParams instance
     */
    public ApprovalRuleParams withUsernames(List<String> usernames) {
        this.usernames = usernames;
        return (this);
    }

    /**
     * Get the form params specified by this instance.
     *
     * @return a GitLabApiForm instance holding the form parameters for this ApprovalRuleParams instance
     */
    public GitLabForm getForm() {
        return new GitLabForm().withParam("approvals_required", approvalsRequired, true).withParam("name", name, true)
                .withParam("applies_to_all_protected_branches", appliesToAllProtectedBranches)
                .withParam("group_ids", groupIds).withParam("protected_branch_ids", protectedBranchIds)
                .withParam("report_type", reportType).withParam("rule_type", ruleType).withParam("user_ids", userIds)
                .withParam("usernames", usernames);
    }

}
