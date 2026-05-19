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

import org.miaixz.bus.gitlab.models.Constants.DefaultBranchProtectionLevel;
import org.miaixz.bus.gitlab.models.Constants.ProjectCreationLevel;
import org.miaixz.bus.gitlab.models.Constants.SubgroupCreationLevel;

/**
 * This class is utilized by the <code>org.miaixz.bus.gitlab.GroupApi#createGroup(GroupParams)</code> and
 * <code>org.miaixz.bus.gitlab.GroupApi#updateGroup(Object, GroupParams)</code> methods to set the parameters for the
 * call to the GitLab API.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GroupParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852256333908L;

    private String name;
    private String path;
    private String description;
    private String visibility;
    private Boolean shareWithGroupLock;
    private Boolean requireTwoFactorAuthentication;
    private Integer twoFactorGracePeriod;
    private ProjectCreationLevel projectCreationLevel;
    private Boolean autoDevopsEnabled;
    private SubgroupCreationLevel subgroupCreationLevel;
    private Boolean emailsDisabled;
    private Boolean lfsEnabled;
    private Boolean requestAccessEnabled;
    private Long parentId;
    private Integer sharedRunnersMinutesLimit;
    private Integer extraSharedRunnersMinutesLimit;
    private DefaultBranchProtectionLevel defaultBranchProtection;
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
     * Prevent adding new members to project membership within this group. For update only.
     *
     * @param membershipLock if true, prevent adding new members to project membership within this group
     * @return this GroupParms instance
     */
    public GroupParams withMembershipLock(Boolean membershipLock) {
        this.membershipLock = membershipLock;
        return (this);
    }

    /**
     * The ID of a project to load custom file templates from. For update only.
     *
     * @param fileTemplateProjectId the ID of a project to load custom file templates from
     * @return this GroupParms instance
     */
    public GroupParams withFileTemplateProjectId(Long fileTemplateProjectId) {
        this.fileTemplateProjectId = fileTemplateProjectId;
        return (this);
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public GroupParams withName(String name) {
        this.name = name;
        return (this);
    }

    /**
     * Sets the path and returns this instance.
     *
     * @param path the path value
     * @return the result
     */

    public GroupParams withPath(String path) {
        this.path = path;
        return (this);
    }

    /**
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public GroupParams withDescription(String description) {
        this.description = description;
        return (this);
    }

    /**
     * Sets the visibility and returns this instance.
     *
     * @param visibility the visibility value
     * @return the result
     */

    public GroupParams withVisibility(String visibility) {
        this.visibility = visibility;
        return (this);
    }

    /**
     * Sets the share with group lock and returns this instance.
     *
     * @param shareWithGroupLock the share with group lock value
     * @return the result
     */

    public GroupParams withShareWithGroupLock(Boolean shareWithGroupLock) {
        this.shareWithGroupLock = shareWithGroupLock;
        return (this);
    }

    /**
     * Sets the require two factor authentication and returns this instance.
     *
     * @param requireTwoFactorAuthentication the require two factor authentication value
     * @return the result
     */

    public GroupParams withRequireTwoFactorAuthentication(Boolean requireTwoFactorAuthentication) {
        this.requireTwoFactorAuthentication = requireTwoFactorAuthentication;
        return (this);
    }

    /**
     * Sets the two factor grace period and returns this instance.
     *
     * @param twoFactorGracePeriod the two factor grace period value
     * @return the result
     */

    public GroupParams withTwoFactorGracePeriod(Integer twoFactorGracePeriod) {
        this.twoFactorGracePeriod = twoFactorGracePeriod;
        return (this);
    }

    /**
     * Sets the project creation level and returns this instance.
     *
     * @param projectCreationLevel the project creation level value
     * @return the result
     */

    public GroupParams withProjectCreationLevel(ProjectCreationLevel projectCreationLevel) {
        this.projectCreationLevel = projectCreationLevel;
        return (this);
    }

    /**
     * Sets the auto devops enabled and returns this instance.
     *
     * @param autoDevopsEnabled the auto devops enabled value
     * @return the result
     */

    public GroupParams withAutoDevopsEnabled(Boolean autoDevopsEnabled) {
        this.autoDevopsEnabled = autoDevopsEnabled;
        return (this);
    }

    /**
     * Sets the subgroup creation level and returns this instance.
     *
     * @param subgroupCreationLevel the subgroup creation level value
     * @return the result
     */

    public GroupParams withSubgroupCreationLevel(SubgroupCreationLevel subgroupCreationLevel) {
        this.subgroupCreationLevel = subgroupCreationLevel;
        return (this);
    }

    /**
     * Sets the emails disabled and returns this instance.
     *
     * @param emailsDisabled the emails disabled value
     * @return the result
     */

    public GroupParams withEmailsDisabled(Boolean emailsDisabled) {
        this.emailsDisabled = emailsDisabled;
        return (this);
    }

    /**
     * Sets the lfs enabled and returns this instance.
     *
     * @param lfsEnabled the lfs enabled value
     * @return the result
     */

    public GroupParams withLfsEnabled(Boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
        return (this);
    }

    /**
     * Sets the request access enabled and returns this instance.
     *
     * @param requestAccessEnabled the request access enabled value
     * @return the result
     */

    public GroupParams withRequestAccessEnabled(Boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
        return (this);
    }

    /**
     * Sets the shared runners minutes limit and returns this instance.
     *
     * @param sharedRunnersMinutesLimit the shared runners minutes limit value
     * @return the result
     */

    public GroupParams withSharedRunnersMinutesLimit(Integer sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit = sharedRunnersMinutesLimit;
        return (this);
    }

    /**
     * Sets the extra shared runners minutes limit and returns this instance.
     *
     * @param extraSharedRunnersMinutesLimit the extra shared runners minutes limit value
     * @return the result
     */

    public GroupParams withExtraSharedRunnersMinutesLimit(Integer extraSharedRunnersMinutesLimit) {
        this.extraSharedRunnersMinutesLimit = extraSharedRunnersMinutesLimit;
        return (this);
    }

    /**
     * Sets the default branch protection and returns this instance.
     *
     * @param defaultBranchProtection the default branch protection value
     * @return the result
     */

    public GroupParams withDefaultBranchProtection(DefaultBranchProtectionLevel defaultBranchProtection) {
        this.defaultBranchProtection = defaultBranchProtection;
        return (this);
    }

    /**
     * Sets the prevent sharing groups outside hierarchy and returns this instance.
     *
     * @param preventSharingGroupsOutsideHierarchy the prevent sharing groups outside hierarchy value
     * @return the result
     */

    public GroupParams withPreventSharingGroupsOutsideHierarchy(Boolean preventSharingGroupsOutsideHierarchy) {
        this.preventSharingGroupsOutsideHierarchy = preventSharingGroupsOutsideHierarchy;
        return (this);
    }

    /**
     * Sets the prevent forking outside group and returns this instance.
     *
     * @param preventForkingOutsideGroup the prevent forking outside group value
     * @return the result
     */

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
    public GitLabForm getForm(boolean isCreate) {

        GitLabForm form = new GitLabForm().withParam("name", name, isCreate).withParam("path", path, isCreate)
                .withParam("description", description).withParam("visibility", visibility)
                .withParam("share_with_group_lock", shareWithGroupLock)
                .withParam("require_two_factor_authentication", requireTwoFactorAuthentication)
                .withParam("two_factor_grace_period", twoFactorGracePeriod)
                .withParam("project_creation_level", projectCreationLevel)
                .withParam("auto_devops_enabled", autoDevopsEnabled)
                .withParam("subgroup_creation_level", subgroupCreationLevel)
                .withParam("emails_disabled", emailsDisabled).withParam("lfs_enabled", lfsEnabled)
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
