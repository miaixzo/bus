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
import java.util.Date;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The user class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class User extends AbstractUser<User> {

    @Serial
    private static final long serialVersionUID = 2852282972009L;

    private String bio;
    private Boolean bot;
    private Boolean canCreateGroup;
    private Boolean canCreateProject;
    private Integer colorSchemeId;
    private Date confirmedAt;
    private Date currentSignInAt;
    private List<CustomAttribute> customAttributes;
    private Boolean external;
    private String externUid;
    private Integer extraSharedRunnersMinutesLimit;
    private List<Identity> identities;
    private Boolean isAdmin;
    private Date lastActivityOn;
    private Date lastSignInAt;
    private String linkedin;
    private String location;
    private Long namespaceId;
    private String organization;
    private Boolean privateProfile;
    private Integer projectsLimit;
    private String provider;
    private String publicEmail;
    private Integer sharedRunnersMinutesLimit;
    private String skype;
    private Integer themeId;
    private String twitter;
    private Boolean twoFactorEnabled;
    private String websiteUrl;
    private Boolean skipConfirmation;

    /**
     * Returns the bio.
     *
     * @return the result
     */

    public String getBio() {
        return bio;
    }

    /**
     * Sets the bio.
     *
     * @param bio the bio value
     */

    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Returns the bot.
     *
     * @return the result
     */

    public Boolean getBot() {
        return bot;
    }

    /**
     * Sets the bot.
     *
     * @param bot the bot value
     */

    public void setBot(Boolean bot) {
        this.bot = bot;
    }

    /**
     * Returns the can create group.
     *
     * @return the result
     */

    public Boolean getCanCreateGroup() {
        return canCreateGroup;
    }

    /**
     * Sets the can create group.
     *
     * @param canCreateGroup the can create group value
     */

    public void setCanCreateGroup(Boolean canCreateGroup) {
        this.canCreateGroup = canCreateGroup;
    }

    /**
     * Returns the can create project.
     *
     * @return the result
     */

    public Boolean getCanCreateProject() {
        return canCreateProject;
    }

    /**
     * Sets the can create project.
     *
     * @param canCreateProject the can create project value
     */

    public void setCanCreateProject(Boolean canCreateProject) {
        this.canCreateProject = canCreateProject;
    }

    /**
     * Returns the color scheme id.
     *
     * @return the result
     */

    public Integer getColorSchemeId() {
        return colorSchemeId;
    }

    /**
     * Sets the color scheme id.
     *
     * @param colorSchemeId the color scheme id value
     */

    public void setColorSchemeId(Integer colorSchemeId) {
        this.colorSchemeId = colorSchemeId;
    }

    /**
     * Returns the confirmed at.
     *
     * @return the result
     */

    public Date getConfirmedAt() {
        return confirmedAt;
    }

    /**
     * Sets the confirmed at.
     *
     * @param confirmedAt the confirmed at value
     */

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    /**
     * Returns the current sign in at.
     *
     * @return the result
     */

    public Date getCurrentSignInAt() {
        return currentSignInAt;
    }

    /**
     * Sets the current sign in at.
     *
     * @param currentSignInAt the current sign in at value
     */

    public void setCurrentSignInAt(Date currentSignInAt) {
        this.currentSignInAt = currentSignInAt;
    }

    /**
     * Returns the external.
     *
     * @return the result
     */

    public Boolean getExternal() {
        return external;
    }

    /**
     * Sets the external.
     *
     * @param external the external value
     */

    public void setExternal(Boolean external) {
        this.external = external;
    }

    /**
     * Sets the extern uid.
     *
     * @param externUid the extern uid value
     */

    public void setExternUid(String externUid) {
        this.externUid = externUid;
    }

    /**
     * Returns the extern uid.
     *
     * @return the result
     */

    public String getExternUid() {
        return this.externUid;
    }

    /**
     * Returns the extra shared runners minutes limit.
     *
     * @return the result
     */

    public Integer getExtraSharedRunnersMinutesLimit() {
        return extraSharedRunnersMinutesLimit;
    }

    /**
     * Sets the extra shared runners minutes limit.
     *
     * @param extraSharedRunnersMinutesLimit the extra shared runners minutes limit value
     */

    public void setExtraSharedRunnersMinutesLimit(Integer extraSharedRunnersMinutesLimit) {
        this.extraSharedRunnersMinutesLimit = extraSharedRunnersMinutesLimit;
    }

    /**
     * Returns the identities.
     *
     * @return the result
     */

    public List<Identity> getIdentities() {
        return identities;
    }

    /**
     * Sets the identities.
     *
     * @param identities the identities value
     */

    public void setIdentities(List<Identity> identities) {
        this.identities = identities;
    }

    /**
     * Returns the is admin.
     *
     * @return the result
     */

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * Sets the is admin.
     *
     * @param isAdmin the is admin value
     */

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Returns the last activity on.
     *
     * @return the result
     */

    public Date getLastActivityOn() {
        return lastActivityOn;
    }

    /**
     * Sets the last activity on.
     *
     * @param lastActivityOn the last activity on value
     */

    public void setLastActivityOn(Date lastActivityOn) {
        this.lastActivityOn = lastActivityOn;
    }

    /**
     * Returns the last sign in at.
     *
     * @return the result
     */

    public Date getLastSignInAt() {
        return lastSignInAt;
    }

    /**
     * Sets the last sign in at.
     *
     * @param lastSignInAt the last sign in at value
     */

    public void setLastSignInAt(Date lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }

    /**
     * Returns the linkedin.
     *
     * @return the result
     */

    public String getLinkedin() {
        return linkedin;
    }

    /**
     * Sets the linkedin.
     *
     * @param linkedin the linkedin value
     */

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    /**
     * Returns the location.
     *
     * @return the result
     */

    public String getLocation() {
        return location;
    }

    /**
     * Sets the location.
     *
     * @param location the location value
     */

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the namespace id.
     *
     * @return the result
     */

    public Long getNamespaceId() {
        return namespaceId;
    }

    /**
     * Sets the namespace id.
     *
     * @param namespaceId the namespace id value
     */

    public void setNamespaceId(Long namespaceId) {
        this.namespaceId = namespaceId;
    }

    /**
     * Returns the organization.
     *
     * @return the result
     */

    public String getOrganization() {
        return organization;
    }

    /**
     * Sets the organization.
     *
     * @param organization the organization value
     */

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * Returns the private profile.
     *
     * @return the result
     */

    public Boolean getPrivateProfile() {
        return privateProfile;
    }

    /**
     * Sets the private profile.
     *
     * @param privateProfile the private profile value
     */

    public void setPrivateProfile(Boolean privateProfile) {
        this.privateProfile = privateProfile;
    }

    /**
     * Returns the projects limit.
     *
     * @return the result
     */

    public Integer getProjectsLimit() {
        return projectsLimit;
    }

    /**
     * Sets the projects limit.
     *
     * @param projectsLimit the projects limit value
     */

    public void setProjectsLimit(Integer projectsLimit) {
        this.projectsLimit = projectsLimit;
    }

    /**
     * Returns the provider.
     *
     * @return the result
     */

    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider.
     *
     * @param provider the provider value
     */

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Returns the public email.
     *
     * @return the result
     */

    public String getPublicEmail() {
        return publicEmail;
    }

    /**
     * Sets the public email.
     *
     * @param publicEmail the public email value
     */

    public void setPublicEmail(String publicEmail) {
        this.publicEmail = publicEmail;
    }

    /**
     * Returns the shared runners minutes limit.
     *
     * @return the result
     */

    public Integer getSharedRunnersMinutesLimit() {
        return sharedRunnersMinutesLimit;
    }

    /**
     * Sets the shared runners minutes limit.
     *
     * @param sharedRunnersMinutesLimit the shared runners minutes limit value
     */

    public void setSharedRunnersMinutesLimit(Integer sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit = sharedRunnersMinutesLimit;
    }

    /**
     * Returns the skype.
     *
     * @return the result
     */

    public String getSkype() {
        return skype;
    }

    /**
     * Sets the skype.
     *
     * @param skype the skype value
     */

    public void setSkype(String skype) {
        this.skype = skype;
    }

    /**
     * Returns the theme id.
     *
     * @return the result
     */

    public Integer getThemeId() {
        return themeId;
    }

    /**
     * Sets the theme id.
     *
     * @param themeId the theme id value
     */

    public void setThemeId(Integer themeId) {
        this.themeId = themeId;
    }

    /**
     * Returns the twitter.
     *
     * @return the result
     */

    public String getTwitter() {
        return twitter;
    }

    /**
     * Sets the twitter.
     *
     * @param twitter the twitter value
     */

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    /**
     * Returns the two factor enabled.
     *
     * @return the result
     */

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    /**
     * Sets the two factor enabled.
     *
     * @param twoFactorEnabled the two factor enabled value
     */

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    /**
     * Returns the website url.
     *
     * @return the result
     */

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    /**
     * Sets the website url.
     *
     * @param websiteUrl the website url value
     */

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    /**
     * Returns the skip confirmation.
     *
     * @return the result
     */

    public Boolean getSkipConfirmation() {
        return skipConfirmation;
    }

    /**
     * Sets the skip confirmation.
     *
     * @param skipConfirmation the skip confirmation value
     */

    public void setSkipConfirmation(Boolean skipConfirmation) {
        this.skipConfirmation = skipConfirmation;
    }

    /**
     * Returns the custom attributes.
     *
     * @return the result
     */

    public List<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    /**
     * Sets the custom attributes.
     *
     * @param customAttributes the custom attributes value
     */

    public void setCustomAttributes(List<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }

    /**
     * Sets the bio and returns this instance.
     *
     * @param bio the bio value
     * @return the result
     */

    public User withBio(String bio) {
        this.bio = bio;
        return this;
    }

    /**
     * Sets the can create group and returns this instance.
     *
     * @param canCreateGroup the can create group value
     * @return the result
     */

    public User withCanCreateGroup(Boolean canCreateGroup) {
        this.canCreateGroup = canCreateGroup;
        return this;
    }

    /**
     * Sets the can create project and returns this instance.
     *
     * @param canCreateProject the can create project value
     * @return the result
     */

    public User withCanCreateProject(Boolean canCreateProject) {
        this.canCreateProject = canCreateProject;
        return this;
    }

    /**
     * Sets the color scheme id and returns this instance.
     *
     * @param colorSchemeId the color scheme id value
     * @return the result
     */

    public User withColorSchemeId(Integer colorSchemeId) {
        this.colorSchemeId = colorSchemeId;
        return this;
    }

    /**
     * Sets the confirmed at and returns this instance.
     *
     * @param confirmedAt the confirmed at value
     * @return the result
     */

    public User withConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
        return this;
    }

    /**
     * Sets the current sign in at and returns this instance.
     *
     * @param currentSignInAt the current sign in at value
     * @return the result
     */

    public User withCurrentSignInAt(Date currentSignInAt) {
        this.currentSignInAt = currentSignInAt;
        return this;
    }

    /**
     * Sets the external and returns this instance.
     *
     * @param external the external value
     * @return the result
     */

    public User withExternal(Boolean external) {
        this.external = external;
        return this;
    }

    /**
     * Sets the extern uid and returns this instance.
     *
     * @param externUid the extern uid value
     * @return the result
     */

    public User withExternUid(String externUid) {
        this.externUid = externUid;
        return this;
    }

    /**
     * Sets the extra shared runners minutes limit and returns this instance.
     *
     * @param extraSharedRunnersMinutesLimit the extra shared runners minutes limit value
     * @return the result
     */

    public User withExtraSharedRunnersMinutesLimit(Integer extraSharedRunnersMinutesLimit) {
        this.extraSharedRunnersMinutesLimit = extraSharedRunnersMinutesLimit;
        return this;
    }

    /**
     * Sets the identities and returns this instance.
     *
     * @param identities the identities value
     * @return the result
     */

    public User withIdentities(List<Identity> identities) {
        this.identities = identities;
        return this;
    }

    /**
     * Sets the is admin and returns this instance.
     *
     * @param isAdmin the is admin value
     * @return the result
     */

    public User withIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
        return this;
    }

    /**
     * Sets the last activity on and returns this instance.
     *
     * @param lastActivityOn the last activity on value
     * @return the result
     */

    public User withLastActivityOn(Date lastActivityOn) {
        this.lastActivityOn = lastActivityOn;
        return this;
    }

    /**
     * Sets the last sign in at and returns this instance.
     *
     * @param lastSignInAt the last sign in at value
     * @return the result
     */

    public User withLastSignInAt(Date lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
        return this;
    }

    /**
     * Sets the linkedin and returns this instance.
     *
     * @param linkedin the linkedin value
     * @return the result
     */

    public User withLinkedin(String linkedin) {
        this.linkedin = linkedin;
        return this;
    }

    /**
     * Sets the location and returns this instance.
     *
     * @param location the location value
     * @return the result
     */

    public User withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Sets the organization and returns this instance.
     *
     * @param organization the organization value
     * @return the result
     */

    public User withOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    /**
     * Sets the private profile and returns this instance.
     *
     * @param privateProfile the private profile value
     * @return the result
     */

    public User withPrivateProfile(Boolean privateProfile) {
        this.privateProfile = privateProfile;
        return this;
    }

    /**
     * Sets the projects limit and returns this instance.
     *
     * @param projectsLimit the projects limit value
     * @return the result
     */

    public User withProjectsLimit(Integer projectsLimit) {
        this.projectsLimit = projectsLimit;
        return this;
    }

    /**
     * Sets the provider and returns this instance.
     *
     * @param provider the provider value
     * @return the result
     */

    public User withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Sets the public email and returns this instance.
     *
     * @param publicEmail the public email value
     * @return the result
     */

    public User withPublicEmail(String publicEmail) {
        this.publicEmail = publicEmail;
        return this;
    }

    /**
     * Sets the shared runners minutes limit and returns this instance.
     *
     * @param sharedRunnersMinutesLimit the shared runners minutes limit value
     * @return the result
     */

    public User withSharedRunnersMinutesLimit(Integer sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit = sharedRunnersMinutesLimit;
        return this;
    }

    /**
     * Sets the skype and returns this instance.
     *
     * @param skype the skype value
     * @return the result
     */

    public User withSkype(String skype) {
        this.skype = skype;
        return this;
    }

    /**
     * Sets the theme id and returns this instance.
     *
     * @param themeId the theme id value
     * @return the result
     */

    public User withThemeId(Integer themeId) {
        this.themeId = themeId;
        return this;
    }

    /**
     * Sets the twitter and returns this instance.
     *
     * @param twitter the twitter value
     * @return the result
     */

    public User withTwitter(String twitter) {
        this.twitter = twitter;
        return this;
    }

    /**
     * Sets the two factor enabled and returns this instance.
     *
     * @param twoFactorEnabled the two factor enabled value
     * @return the result
     */

    public User withTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
        return this;
    }

    /**
     * Sets the website url and returns this instance.
     *
     * @param websiteUrl the website url value
     * @return the result
     */

    public User withWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }

    /**
     * Sets the skip confirmation and returns this instance.
     *
     * @param skipConfirmation the skip confirmation value
     * @return the result
     */

    public User withSkipConfirmation(Boolean skipConfirmation) {
        this.skipConfirmation = skipConfirmation;
        return this;
    }

    /**
     * Sets the custom attributes and returns this instance.
     *
     * @param customAttributes the custom attributes value
     * @return the result
     */

    public User withCustomAttributes(List<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
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
