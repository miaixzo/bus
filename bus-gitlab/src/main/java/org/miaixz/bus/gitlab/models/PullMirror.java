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
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * Pull mirror configuration and status for a project.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PullMirror implements Serializable {

    /**
     * Serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852260153907L;

    /**
     * Pull mirror identifier.
     */
    private Long id;

    /**
     * Last error reported by the pull mirror update.
     */
    private String lastError;

    /**
     * Time of the last successful pull mirror update.
     */
    private Date lastSuccessfulUpdateAt;

    /**
     * Time of the last pull mirror update.
     */
    private Date lastUpdateAt;

    /**
     * Time when the last pull mirror update started.
     */
    private Date lastUpdateStartedAt;

    /**
     * Current pull mirror update status.
     */
    private String updateStatus;

    /**
     * Source repository URL for the pull mirror.
     */
    private String url;

    /**
     * Flag indicating whether the pull mirror is enabled.
     */
    private Boolean enabled;

    /**
     * Flag indicating whether pull mirror updates trigger builds.
     */
    private Boolean mirrorTriggerBuilds;

    /**
     * Flag indicating whether only protected branches are mirrored.
     */
    private Boolean onlyMirrorProtectedBranches;

    /**
     * Flag indicating whether diverged branches are overwritten by the mirror.
     */
    private Boolean mirrorOverwritesDivergedBranches;

    /**
     * Regular expression used to limit mirrored branches.
     */
    private String mirrorBranchRegex;

    /**
     * Gets the pull mirror identifier.
     *
     * @return the pull mirror identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the pull mirror identifier.
     *
     * @param id the pull mirror identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the last error reported by the pull mirror update.
     *
     * @return the last update error
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Sets the last error reported by the pull mirror update.
     *
     * @param lastError the last update error
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Gets the time of the last successful pull mirror update.
     *
     * @return the last successful update time
     */
    public Date getLastSuccessfulUpdateAt() {
        return lastSuccessfulUpdateAt;
    }

    /**
     * Sets the time of the last successful pull mirror update.
     *
     * @param lastSuccessfulUpdateAt the last successful update time
     */
    public void setLastSuccessfulUpdateAt(Date lastSuccessfulUpdateAt) {
        this.lastSuccessfulUpdateAt = lastSuccessfulUpdateAt;
    }

    /**
     * Gets the time of the last pull mirror update.
     *
     * @return the last update time
     */
    public Date getLastUpdateAt() {
        return lastUpdateAt;
    }

    /**
     * Sets the time of the last pull mirror update.
     *
     * @param lastUpdateAt the last update time
     */
    public void setLastUpdateAt(Date lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

    /**
     * Gets the time when the last pull mirror update started.
     *
     * @return the last update start time
     */
    public Date getLastUpdateStartedAt() {
        return lastUpdateStartedAt;
    }

    /**
     * Sets the time when the last pull mirror update started.
     *
     * @param lastUpdateStartedAt the last update start time
     */
    public void setLastUpdateStartedAt(Date lastUpdateStartedAt) {
        this.lastUpdateStartedAt = lastUpdateStartedAt;
    }

    /**
     * Gets the current pull mirror update status.
     *
     * @return the update status
     */
    public String getUpdateStatus() {
        return updateStatus;
    }

    /**
     * Sets the current pull mirror update status.
     *
     * @param updateStatus the update status
     */
    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    /**
     * Gets the source repository URL for the pull mirror.
     *
     * @return the source repository URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the source repository URL for the pull mirror.
     *
     * @param url the source repository URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets whether the pull mirror is enabled.
     *
     * @return {@code true} if the pull mirror is enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets whether the pull mirror is enabled.
     *
     * @param enabled {@code true} if the pull mirror is enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets whether pull mirror updates trigger builds.
     *
     * @return {@code true} if pull mirror updates trigger builds
     */
    public Boolean getMirrorTriggerBuilds() {
        return mirrorTriggerBuilds;
    }

    /**
     * Sets whether pull mirror updates trigger builds.
     *
     * @param mirrorTriggerBuilds {@code true} if pull mirror updates trigger builds
     */
    public void setMirrorTriggerBuilds(Boolean mirrorTriggerBuilds) {
        this.mirrorTriggerBuilds = mirrorTriggerBuilds;
    }

    /**
     * Gets whether only protected branches are mirrored.
     *
     * @return {@code true} if only protected branches are mirrored
     */
    public Boolean getOnlyMirrorProtectedBranches() {
        return onlyMirrorProtectedBranches;
    }

    /**
     * Sets whether only protected branches are mirrored.
     *
     * @param onlyMirrorProtectedBranches {@code true} if only protected branches are mirrored
     */
    public void setOnlyMirrorProtectedBranches(Boolean onlyMirrorProtectedBranches) {
        this.onlyMirrorProtectedBranches = onlyMirrorProtectedBranches;
    }

    /**
     * Gets whether diverged branches are overwritten by the mirror.
     *
     * @return {@code true} if diverged branches are overwritten
     */
    public Boolean getMirrorOverwritesDivergedBranches() {
        return mirrorOverwritesDivergedBranches;
    }

    /**
     * Sets whether diverged branches are overwritten by the mirror.
     *
     * @param mirrorOverwritesDivergedBranches {@code true} if diverged branches are overwritten
     */
    public void setMirrorOverwritesDivergedBranches(Boolean mirrorOverwritesDivergedBranches) {
        this.mirrorOverwritesDivergedBranches = mirrorOverwritesDivergedBranches;
    }

    /**
     * Gets the regular expression used to limit mirrored branches.
     *
     * @return the mirrored branch regular expression
     */
    public String getMirrorBranchRegex() {
        return mirrorBranchRegex;
    }

    /**
     * Sets the regular expression used to limit mirrored branches.
     *
     * @param mirrorBranchRegex the mirrored branch regular expression
     */
    public void setMirrorBranchRegex(String mirrorBranchRegex) {
        this.mirrorBranchRegex = mirrorBranchRegex;
    }

    /**
     * Returns this pull mirror as a JSON string.
     *
     * @return this pull mirror serialized as JSON
     */
    @Override
    public String toString() {
        return JacksonJson.toJsonString(this);
    }

}
