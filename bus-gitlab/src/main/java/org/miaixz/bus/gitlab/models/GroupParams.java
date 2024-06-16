/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.gitlab.models;

import org.miaixz.bus.gitlab.Constants;
import org.miaixz.bus.gitlab.GitLabApiForm;
import org.miaixz.bus.gitlab.GroupApi;

import java.io.Serializable;

/**
 * This class is utilized by the {@link GroupApi#createGroup(GroupParams)}
 * and {@link GroupApi#updateGroup(Object, GroupParams)} methods to set
 * the parameters for the call to the GitLab API.
 */
public class GroupParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String path;
    private String description;
    private String visibility;
    private Boolean shareWithGroupLock;
    private Boolean requireTwoFactorAuthentication;
    private Integer twoFactorGracePeriod;
    private Constants.ProjectCreationLevel projectCreationLevel;
    private Boolean autoDevopsEnabled;
    private Constants.SubgroupCreationLevel subgroupCreationLevel;
    private Boolean emailsDisabled;
    private Boolean lfsEnabled;
    private Boolean requestAccessEnabled;
    private Long parentId;
    private Integer sharedRunnersMinutesLimit;
    private Integer extraSharedRunnersMinutesLimit;
    private Constants.DefaultBranchProtectionLevel defaultBranchProtection;
    private Boolean preventSharingGroupsOutsideHierarchy;
    private Boolean preventForkingOutsideGroup;
    private Boolean membershipLock;
    private Long fileTemplateProjectId;

    /**
     * The parent group ID for creating nested group. For create only.
     *
     * @param parentId the parent group ID for creating nested group
     * @return this GroupParms instance
     */
    public GroupParams withParentId(Long parentId) {
        this.parentId = parentId;
        return (this);
    }

    /**
     * Prevent adding new members to project membership within this group.  For update only.
     *
     * @param membershipLock if true, prevent adding new members to project membership within this group
     * @return this GroupParms instance
     */
    public GroupParams withMembershipLock(Boolean membershipLock) {
        this.membershipLock = membershipLock;
        return (this);
    }

    /**
     * The ID of a project to load custom file templates from.  For update only.
     *
     * @param fileTemplateProjectId the ID of a project to load custom file templates from
     * @return this GroupParms instance
     */
    public GroupParams withFileTemplateProjectId(Long fileTemplateProjectId) {
        this.fileTemplateProjectId = fileTemplateProjectId;
        return (this);
    }

    public GroupParams withName(String name) {
        this.name = name;
        return (this);
    }

    public GroupParams withPath(String path) {
        this.path = path;
        return (this);
    }

    public GroupParams withDescription(String description) {
        this.description = description;
        return (this);
    }

    public GroupParams withVisibility(String visibility) {
        this.visibility = visibility;
        return (this);
    }

    public GroupParams withShareWithGroupLock(Boolean shareWithGroupLock) {
        this.shareWithGroupLock = shareWithGroupLock;
        return (this);
    }

    public GroupParams withRequireTwoFactorAuthentication(Boolean requireTwoFactorAuthentication) {
        this.requireTwoFactorAuthentication = requireTwoFactorAuthentication;
        return (this);
    }

    public GroupParams withTwoFactorGracePeriod(Integer twoFactorGracePeriod) {
        this.twoFactorGracePeriod = twoFactorGracePeriod;
        return (this);
    }

    public GroupParams withProjectCreationLevel(Constants.ProjectCreationLevel projectCreationLevel) {
        this.projectCreationLevel = projectCreationLevel;
        return (this);
    }

    public GroupParams withAutoDevopsEnabled(Boolean autoDevopsEnabled) {
        this.autoDevopsEnabled = autoDevopsEnabled;
        return (this);
    }

    public GroupParams withSubgroupCreationLevel(Constants.SubgroupCreationLevel subgroupCreationLevel) {
        this.subgroupCreationLevel = subgroupCreationLevel;
        return (this);
    }

    public GroupParams withEmailsDisabled(Boolean emailsDisabled) {
        this.emailsDisabled = emailsDisabled;
        return (this);
    }

    public GroupParams withLfsEnabled(Boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
        return (this);
    }

    public GroupParams withRequestAccessEnabled(Boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
        return (this);
    }

    public GroupParams withSharedRunnersMinutesLimit(Integer sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit = sharedRunnersMinutesLimit;
        return (this);
    }

    public GroupParams withExtraSharedRunnersMinutesLimit(Integer extraSharedRunnersMinutesLimit) {
        this.extraSharedRunnersMinutesLimit = extraSharedRunnersMinutesLimit;
        return (this);
    }

    public GroupParams withDefaultBranchProtection(Constants.DefaultBranchProtectionLevel defaultBranchProtection) {
        this.defaultBranchProtection = defaultBranchProtection;
        return (this);
    }

    public GroupParams withPreventSharingGroupsOutsideHierarchy(Boolean preventSharingGroupsOutsideHierarchy) {
        this.preventSharingGroupsOutsideHierarchy = preventSharingGroupsOutsideHierarchy;
        return (this);
    }

    public GroupParams withPreventForkingOutsideGroup(Boolean preventForkingOutsideGroup) {
        this.preventForkingOutsideGroup = preventForkingOutsideGroup;
        return (this);
    }

    /**
     * Get the form params for a group create oir update call.
     *
     * @param isCreate set to true for a create group call, false for update
     * @return a GitLabApiForm instance holding the parameters for the group create or update operation
     * @throws RuntimeException if required parameters are missing
     */
    public GitLabApiForm getForm(boolean isCreate) {

        GitLabApiForm form = new GitLabApiForm()
                .withParam("name", name, isCreate)
                .withParam("path", path, isCreate)
                .withParam("description", description)
                .withParam("visibility", visibility)
                .withParam("share_with_group_lock", shareWithGroupLock)
                .withParam("require_two_factor_authentication", requireTwoFactorAuthentication)
                .withParam("two_factor_grace_period", twoFactorGracePeriod)
                .withParam("project_creation_level", projectCreationLevel)
                .withParam("auto_devops_enabled", autoDevopsEnabled)
                .withParam("subgroup_creation_level", subgroupCreationLevel)
                .withParam("emails_disabled", emailsDisabled)
                .withParam("lfs_enabled", lfsEnabled)
                .withParam("request_access_enabled", requestAccessEnabled)
                .withParam("shared_runners_minutes_limit", sharedRunnersMinutesLimit)
                .withParam("extra_shared_runners_minutes_limit", extraSharedRunnersMinutesLimit)
                .withParam("default_branch_protection", defaultBranchProtection)
                .withParam("prevent_sharing_groups_outside_hierarchy", preventSharingGroupsOutsideHierarchy)
                .withParam("prevent_forking_outside_group", preventForkingOutsideGroup);

        if (isCreate) {
            form.withParam("parent_id", parentId);
        } else {
            form.withParam("membership_lock", membershipLock)
                    .withParam("file_template_project_id", fileTemplateProjectId);
        }

        return (form);
    }
}
