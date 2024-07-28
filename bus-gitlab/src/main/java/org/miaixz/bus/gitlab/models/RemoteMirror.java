/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
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

import org.miaixz.bus.gitlab.support.JacksonJson;

import java.io.Serializable;
import java.util.Date;

public class RemoteMirror implements Serializable {
    private static final long serialVersionUID = -1L;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Date getLastSuccessfulUpdateAt() {
        return lastSuccessfulUpdateAt;
    }

    public void setLastSuccessfulUpdateAt(Date lastSuccessfulUpdateAt) {
        this.lastSuccessfulUpdateAt = lastSuccessfulUpdateAt;
    }

    public Date getLastUpdateAt() {
        return lastUpdateAt;
    }

    public void setLastUpdateAt(Date lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

    public Date getLastUpdateStartedAt() {
        return lastUpdateStartedAt;
    }

    public void setLastUpdateStartedAt(Date lastUpdateStartedAt) {
        this.lastUpdateStartedAt = lastUpdateStartedAt;
    }

    public Boolean getOnlyProtectedBranches() {
        return onlyProtectedBranches;
    }

    public void setOnlyProtectedBranches(Boolean onlyProtectedBranches) {
        this.onlyProtectedBranches = onlyProtectedBranches;
    }

    public Boolean getKeepDivergentRefs() {
        return keepDivergentRefs;
    }

    public void setKeepDivergentRefs(Boolean keepDivergentRefs) {
        this.keepDivergentRefs = keepDivergentRefs;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return JacksonJson.toJsonString(this);
    }
}
