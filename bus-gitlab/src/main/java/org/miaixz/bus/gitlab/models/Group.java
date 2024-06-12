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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.miaixz.bus.gitlab.Constants.DefaultBranchProtectionLevel;
import org.miaixz.bus.gitlab.Constants.ProjectCreationLevel;
import org.miaixz.bus.gitlab.Constants.SubgroupCreationLevel;
import org.miaixz.bus.gitlab.support.JacksonJson;

import java.util.Date;
import java.util.List;

public class Group extends AbstractGroup<Group> {
    private static final long serialVersionUID = 1L;

    public class Statistics {
        private Long storageSize;
        private Long repositorySize;
        private Long lfsObjectsSize;
        private Long jobArtifactsSize;

        public Long getStorageSize() {
            return storageSize;
        }

        public void setStorageSize(Long storageSize) {
            this.storageSize = storageSize;
        }

        public Long getRepositorySize() {
            return repositorySize;
        }

        public void setRepositorySize(Long repositorySize) {
            this.repositorySize = repositorySize;
        }

        public Long getLfsObjectsSize() {
            return lfsObjectsSize;
        }

        public void setLfsObjectsSize(Long lfsObjectsSize) {
            this.lfsObjectsSize = lfsObjectsSize;
        }

        public Long getJobArtifactsSize() {
            return jobArtifactsSize;
        }

        public void setJobArtifactsSize(Long jobArtifactsSize) {
            this.jobArtifactsSize = jobArtifactsSize;
        }
    }


    private String path;
    private String description;
    private Visibility visibility;
    private Boolean lfsEnabled;
    private Boolean requestAccessEnabled;
    private Long parentId;
    private Integer sharedRunnersMinutesLimit;
    private Statistics statistics;
    private List<Project> projects;
    private List<Project> sharedProjects;
    private Date createdAt;
    private List<SharedGroup> sharedWithGroups;
    private List<CustomAttribute> customAttributes;
    private String runnersToken;
    private Boolean preventSharingGroupsOutsideHierarchy;
    private Boolean preventForkingOutsideGroup;
    private ProjectCreationLevel projectCreationLevel;
    private SubgroupCreationLevel subgroupCreationLevel;
    private DefaultBranchProtectionLevel defaultBranchProtection;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date markedForDeletionOn;

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Boolean getLfsEnabled() {
        return lfsEnabled;
    }

    public void setLfsEnabled(Boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
    }

    public Boolean getRequestAccessEnabled() {
        return requestAccessEnabled;
    }

    public void setRequestAccessEnabled(Boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSharedRunnersMinutesLimit() {
        return sharedRunnersMinutesLimit;
    }

    public void setSharedRunnersMinutesLimit(Integer sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit = sharedRunnersMinutesLimit;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public List<Project> getProjects() {
        return (projects);
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Project> getSharedProjects() {
        return (sharedProjects);
    }

    public void setSharedProjects(List<Project> sharedProjects) {
        this.sharedProjects = sharedProjects;
    }

    public Date getMarkedForDeletionOn() {
        return markedForDeletionOn;
    }

    public void setMarkedForDeletionOn(Date markedForDeletionOn) {
        this.markedForDeletionOn = markedForDeletionOn;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<SharedGroup> getSharedWithGroups() {
        return sharedWithGroups;
    }

    public void setSharedWithGroups(List<SharedGroup> sharedWithGroups) {
        this.sharedWithGroups = sharedWithGroups;
    }

    public String getRunnersToken() {
        return runnersToken;
    }

    public void setRunnersToken(String runnersToken) {
        this.runnersToken = runnersToken;
    }

    public Boolean getPreventSharingGroupsOutsideHierarchy() {
        return preventSharingGroupsOutsideHierarchy;
    }

    public void setPreventSharingGroupsOutsideHierarchy(Boolean preventSharingGroupsOutsideHierarchy) {
        this.preventSharingGroupsOutsideHierarchy = preventSharingGroupsOutsideHierarchy;
    }

    public Boolean getPreventForkingOutsideGroup() {
        return preventForkingOutsideGroup;
    }

    public void setPreventForkingOutsideGroup(Boolean preventForkingOutsideGroup) {
        this.preventForkingOutsideGroup = preventForkingOutsideGroup;
    }

    public ProjectCreationLevel getProjectCreationLevel() {
        return this.projectCreationLevel;
    }

    public void setProjectCreationLevel(ProjectCreationLevel projectCreationLevel) {
        this.projectCreationLevel = projectCreationLevel;
    }

    public SubgroupCreationLevel getSubgroupCreationLevel() {
        return this.subgroupCreationLevel;
    }

    public void setSubgroupCreationLevel(SubgroupCreationLevel subgroupCreationLevel) {
        this.subgroupCreationLevel = subgroupCreationLevel;
    }

    public DefaultBranchProtectionLevel getDefaultBranchProtection() {
        return this.defaultBranchProtection;
    }

    public void setDefaultBranchProtection(DefaultBranchProtectionLevel defaultBranchProtection) {
        this.defaultBranchProtection = defaultBranchProtection;
    }

    public List<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(List<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public Group withPath(String path) {
        this.path = path;
        return this;
    }

    public Group withDescription(String description) {
        this.description = description;
        return this;
    }

    public Group withVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public Group withlfsEnabled(boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
        return this;
    }

    public Group withRequestAccessEnabled(boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
        return this;
    }

    public Group withParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Group withSharedRunnersMinutesLimit(Integer minutesLimit) {
        this.sharedRunnersMinutesLimit = minutesLimit;
        return this;
    }

    public Group withStatistics(Statistics statistics) {
        this.statistics = statistics;
        return this;
    }

    public Group withProjects(List<Project> projects) {
        this.projects = projects;
        return this;
    }

    public Group withSharedProjects(List<Project> sharedProjects) {
        this.sharedProjects = sharedProjects;
        return this;
    }

    public Group withPreventSharingGroupsOutsideHierarchy(Boolean preventSharingGroupsOutsideHierarchy) {
        this.preventSharingGroupsOutsideHierarchy = preventSharingGroupsOutsideHierarchy;
        return this;
    }

    public Group withPreventForkingOutsideGroup(Boolean preventForkingOutsideGroup) {
        this.preventForkingOutsideGroup = preventForkingOutsideGroup;
        return this;
    }

    public Group withProjectCreationLevel(ProjectCreationLevel projectCreationLevel) {
        this.projectCreationLevel = projectCreationLevel;
        return this;
    }

    public Group withSubgroupCreationLevel(SubgroupCreationLevel subgroupCreationLevel) {
        this.subgroupCreationLevel = subgroupCreationLevel;
        return this;
    }

    public Group withDefaultBranchProtection(DefaultBranchProtectionLevel defaultBranchProtection) {
        this.defaultBranchProtection = defaultBranchProtection;
        return this;
    }

    public Group withCustomAttributes(List<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
        return this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
