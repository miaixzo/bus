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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * This class contains the sizing information from the project. To get this information, ProjectApi.getProject() has to
 * be called with parameter statistics=true which is only allowed for GitLab admins.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProjectStatistics implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852275036520L;

    long commitCount;
    long storageSize;
    long repositorySize;
    long wikiSize;
    long lfsObjectsSize;
    long jobArtifactsSize;
    long packagesSize;

    /**
     * Returns the commit count.
     *
     * @return the result
     */

    public long getCommitCount() {
        return commitCount;
    }

    /**
     * Sets the commit count.
     *
     * @param commitCount the commit count value
     */

    public void setCommitCount(long commitCount) {
        this.commitCount = commitCount;
    }

    /**
     * Returns the storage size.
     *
     * @return the result
     */

    public long getStorageSize() {
        return storageSize;
    }

    /**
     * Sets the storage size.
     *
     * @param storageSize the storage size value
     */

    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }

    /**
     * Returns the repository size.
     *
     * @return the result
     */

    public long getRepositorySize() {
        return repositorySize;
    }

    /**
     * Sets the repository size.
     *
     * @param repositorySize the repository size value
     */

    public void setRepositorySize(long repositorySize) {
        this.repositorySize = repositorySize;
    }

    /**
     * Returns the wiki size.
     *
     * @return the result
     */

    public long getWikiSize() {
        return wikiSize;
    }

    /**
     * Sets the wiki size.
     *
     * @param wikiSize the wiki size value
     */

    public void setWikiSize(long wikiSize) {
        this.wikiSize = wikiSize;
    }

    /**
     * Returns the lfs objects size.
     *
     * @return the result
     */

    public long getLfsObjectsSize() {
        return lfsObjectsSize;
    }

    /**
     * Sets the lfs objects size.
     *
     * @param lfsObjectsSize the lfs objects size value
     */

    public void setLfsObjectsSize(long lfsObjectsSize) {
        this.lfsObjectsSize = lfsObjectsSize;
    }

    /**
     * Returns the job artifacts size.
     *
     * @return the result
     */

    public long getJobArtifactsSize() {
        return jobArtifactsSize;
    }

    /**
     * Sets the job artifacts size.
     *
     * @param jobArtifactsSize the job artifacts size value
     */

    public void setJobArtifactsSize(long jobArtifactsSize) {
        this.jobArtifactsSize = jobArtifactsSize;
    }

    /**
     * Returns the packages size.
     *
     * @return the result
     */

    public long getPackagesSize() {
        return packagesSize;
    }

    /**
     * Sets the packages size.
     *
     * @param packagesSize the packages size value
     */

    public void setPackagesSize(long packagesSize) {
        this.packagesSize = packagesSize;
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
