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
 * The remote mirror class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RemoteMirror implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852279737972L;

    private Long id;
    private Boolean enabled;
    private String lastError;
    private Date lastSuccessfulUpdateAt;
    private Date lastUpdateAt;
    private Date lastUpdateStartedAt;
    private Boolean onlyProtectedBranches;
    private Boolean keepDivergentRefs;
    private String updateStatus;
    private String url;

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
     * Returns the enabled.
     *
     * @return the result
     */

    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the enabled value
     */

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the last error.
     *
     * @return the result
     */

    public String getLastError() {
        return lastError;
    }

    /**
     * Sets the last error.
     *
     * @param lastError the last error value
     */

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Returns the last successful update at.
     *
     * @return the result
     */

    public Date getLastSuccessfulUpdateAt() {
        return lastSuccessfulUpdateAt;
    }

    /**
     * Sets the last successful update at.
     *
     * @param lastSuccessfulUpdateAt the last successful update at value
     */

    public void setLastSuccessfulUpdateAt(Date lastSuccessfulUpdateAt) {
        this.lastSuccessfulUpdateAt = lastSuccessfulUpdateAt;
    }

    /**
     * Returns the last update at.
     *
     * @return the result
     */

    public Date getLastUpdateAt() {
        return lastUpdateAt;
    }

    /**
     * Sets the last update at.
     *
     * @param lastUpdateAt the last update at value
     */

    public void setLastUpdateAt(Date lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

    /**
     * Returns the last update started at.
     *
     * @return the result
     */

    public Date getLastUpdateStartedAt() {
        return lastUpdateStartedAt;
    }

    /**
     * Sets the last update started at.
     *
     * @param lastUpdateStartedAt the last update started at value
     */

    public void setLastUpdateStartedAt(Date lastUpdateStartedAt) {
        this.lastUpdateStartedAt = lastUpdateStartedAt;
    }

    /**
     * Returns the only protected branches.
     *
     * @return the result
     */

    public Boolean getOnlyProtectedBranches() {
        return onlyProtectedBranches;
    }

    /**
     * Sets the only protected branches.
     *
     * @param onlyProtectedBranches the only protected branches value
     */

    public void setOnlyProtectedBranches(Boolean onlyProtectedBranches) {
        this.onlyProtectedBranches = onlyProtectedBranches;
    }

    /**
     * Returns the keep divergent refs.
     *
     * @return the result
     */

    public Boolean getKeepDivergentRefs() {
        return keepDivergentRefs;
    }

    /**
     * Sets the keep divergent refs.
     *
     * @param keepDivergentRefs the keep divergent refs value
     */

    public void setKeepDivergentRefs(Boolean keepDivergentRefs) {
        this.keepDivergentRefs = keepDivergentRefs;
    }

    /**
     * Returns the update status.
     *
     * @return the result
     */

    public String getUpdateStatus() {
        return updateStatus;
    }

    /**
     * Sets the update status.
     *
     * @param updateStatus the update status value
     */

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    /**
     * Returns the url.
     *
     * @return the result
     */

    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url the url value
     */

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return JacksonJson.toJsonString(this);
    }

}
