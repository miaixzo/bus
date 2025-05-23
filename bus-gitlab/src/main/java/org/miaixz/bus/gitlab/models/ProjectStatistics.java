/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * This class contains the sizing information from the project. To get this information, ProjectApi.getProject() has to
 * be called with parameter statistics=true which is only allowed for GitLab admins.
 */
public class ProjectStatistics implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852382665297L;

    long commitCount;
    long storageSize;
    long repositorySize;
    long wikiSize;
    long lfsObjectsSize;
    long jobArtifactsSize;
    long packagesSize;

    public long getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(long commitCount) {
        this.commitCount = commitCount;
    }

    public long getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }

    public long getRepositorySize() {
        return repositorySize;
    }

    public void setRepositorySize(long repositorySize) {
        this.repositorySize = repositorySize;
    }

    public long getWikiSize() {
        return wikiSize;
    }

    public void setWikiSize(long wikiSize) {
        this.wikiSize = wikiSize;
    }

    public long getLfsObjectsSize() {
        return lfsObjectsSize;
    }

    public void setLfsObjectsSize(long lfsObjectsSize) {
        this.lfsObjectsSize = lfsObjectsSize;
    }

    public long getJobArtifactsSize() {
        return jobArtifactsSize;
    }

    public void setJobArtifactsSize(long jobArtifactsSize) {
        this.jobArtifactsSize = jobArtifactsSize;
    }

    public long getPackagesSize() {
        return packagesSize;
    }

    public void setPackagesSize(long packagesSize) {
        this.packagesSize = packagesSize;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
