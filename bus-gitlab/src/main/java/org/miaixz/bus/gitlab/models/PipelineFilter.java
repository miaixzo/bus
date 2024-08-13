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

import java.io.Serializable;
import java.util.Date;

import org.miaixz.bus.gitlab.Constants;
import org.miaixz.bus.gitlab.Constants.PipelineOrderBy;
import org.miaixz.bus.gitlab.Constants.PipelineScope;
import org.miaixz.bus.gitlab.Constants.SortOrder;
import org.miaixz.bus.gitlab.GitLabApiForm;
import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class is used to filter Pipelines when getting lists of them.
 */
public class PipelineFilter implements Serializable {

    private static final long serialVersionUID = -1L;

    /** {@link Constants.PipelineScope} The scope of pipelines, one of: running, pending, finished, branches, tags */
    private PipelineScope scope;

    /**
     * {@link Constants.PipelineScope} The status of pipelines, one of: running, pending, success, failed, canceled,
     * skipped, created
     */
    private PipelineStatus status;

    /** The ref of pipelines. */
    private String ref;

    /** The SHA of pipelines. */
    private String sha;

    /** If true, returns pipelines with invalid configurations. */
    private Boolean yamlErrors;

    /** The name of the user who triggered pipelines. */
    private String name;

    /** The username of the user who triggered pipelines */
    private String username;

    /** Return pipelines updated after the specified date. */
    private Date updatedAfter;

    /** Return pipelines updated before the specified date. */
    private Date updatedBefore;

    /**
     * {@link Constants.PipelineOrderBy} Order pipelines by id, status, ref, updated_at or user_id (default: id).
     */
    private PipelineOrderBy orderBy;

    /**
     * {@link Constants.SortOrder} Return issues sorted in asc or desc order. Default is desc.
     */
    private SortOrder sort;

    public void setScope(PipelineScope scope) {
        this.scope = scope;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public void setYamlErrors(Boolean yamlErrors) {
        this.yamlErrors = yamlErrors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    public void setOrderBy(PipelineOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public void setSort(SortOrder sort) {
        this.sort = sort;
    }

    public PipelineFilter withScope(PipelineScope scope) {
        this.scope = scope;
        return this;
    }

    public PipelineFilter withStatus(PipelineStatus status) {
        this.status = status;
        return this;
    }

    public PipelineFilter withRef(String ref) {
        this.ref = ref;
        return this;
    }

    public PipelineFilter withSha(String sha) {
        this.sha = sha;
        return this;
    }

    public PipelineFilter withYamlErrors(Boolean yamlErrors) {
        this.yamlErrors = yamlErrors;
        return this;
    }

    public PipelineFilter withName(String name) {
        this.name = name;
        return this;
    }

    public PipelineFilter withUsername(String username) {
        this.username = username;
        return this;
    }

    public PipelineFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return this;
    }

    public PipelineFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return this;
    }

    public PipelineFilter withOrderBy(PipelineOrderBy orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public PipelineFilter withSort(SortOrder sort) {
        this.sort = sort;
        return this;
    }

    @JsonIgnore
    public GitLabApiForm getQueryParams() {
        return (new GitLabApiForm().withParam("scope", scope).withParam("status", status).withParam("ref", ref)
                .withParam("sha", sha).withParam("yaml_errors", yamlErrors).withParam("name", name)
                .withParam("username", username).withParam("updated_after", updatedAfter)
                .withParam("updated_before", updatedBefore).withParam("order_by", orderBy).withParam("sort", sort));
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
