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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.models.Constants.PipelineOrderBy;
import org.miaixz.bus.gitlab.models.Constants.PipelineScope;
import org.miaixz.bus.gitlab.models.Constants.PipelineSource;
import org.miaixz.bus.gitlab.models.Constants.SortOrder;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * This class is used to filter Pipelines when getting lists of them.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PipelineFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852269565389L;

    /**
     * pipelines, one of: running, pending, finished, branches, tags
     */
    private PipelineScope scope;

    /**
     * {@link Constants.PipelineScope} The status of pipelines, one of: running, pending, success, failed, canceled,
     * skipped, created
     */
    private PipelineStatus status;

    /**
     * The ref of pipelines
     */
    private PipelineSource source;

    /**
     * The ref of pipelines.
     */
    private String ref;

    /**
     * The SHA of pipelines.
     */
    private String sha;

    /**
     * If true, returns pipelines with invalid configurations.
     */
    private Boolean yamlErrors;

    /**
     * The name of the user who triggered pipelines.
     */
    private String name;

    /**
     * The username of the user who triggered pipelines
     */
    private String username;

    /**
     * Return pipelines updated after the specified date.
     */
    private Date updatedAfter;

    /**
     * Return pipelines updated before the specified date.
     */
    private Date updatedBefore;

    /**
     * {@link Constants.PipelineOrderBy} Order pipelines by id, status, ref, updated_at or user_id (default: id).
     */
    private PipelineOrderBy orderBy;

    /**
     * {@link Constants.SortOrder} Return issues sorted in asc or desc order. Default is desc.
     */
    private SortOrder sort;

    /**
     * Sets the scope.
     *
     * @param scope the scope value
     */

    public void setScope(PipelineScope scope) {
        this.scope = scope;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }

    /**
     * Sets the source.
     *
     * @param source the source value
     */

    public void setSource(PipelineSource source) {
        this.source = source;
    }

    /**
     * Sets the ref.
     *
     * @param ref the ref value
     */

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Sets the sha.
     *
     * @param sha the sha value
     */

    public void setSha(String sha) {
        this.sha = sha;
    }

    /**
     * Sets the yaml errors.
     *
     * @param yamlErrors the yaml errors value
     */

    public void setYamlErrors(Boolean yamlErrors) {
        this.yamlErrors = yamlErrors;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the username.
     *
     * @param username the username value
     */

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the updated after.
     *
     * @param updatedAfter the updated after value
     */

    public void setUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    /**
     * Sets the updated before.
     *
     * @param updatedBefore the updated before value
     */

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    /**
     * Sets the order by.
     *
     * @param orderBy the order by value
     */

    public void setOrderBy(PipelineOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * Sets the sort.
     *
     * @param sort the sort value
     */

    public void setSort(SortOrder sort) {
        this.sort = sort;
    }

    /**
     * Sets the scope and returns this instance.
     *
     * @param scope the scope value
     * @return the result
     */

    public PipelineFilter withScope(PipelineScope scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Sets the status and returns this instance.
     *
     * @param status the status value
     * @return the result
     */

    public PipelineFilter withStatus(PipelineStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the source and returns this instance.
     *
     * @param source the source value
     * @return the result
     */

    public PipelineFilter withSource(PipelineSource source) {
        this.source = source;
        return this;
    }

    /**
     * Sets the ref and returns this instance.
     *
     * @param ref the ref value
     * @return the result
     */

    public PipelineFilter withRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * Sets the sha and returns this instance.
     *
     * @param sha the sha value
     * @return the result
     */

    public PipelineFilter withSha(String sha) {
        this.sha = sha;
        return this;
    }

    /**
     * Sets the yaml errors and returns this instance.
     *
     * @param yamlErrors the yaml errors value
     * @return the result
     */

    public PipelineFilter withYamlErrors(Boolean yamlErrors) {
        this.yamlErrors = yamlErrors;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public PipelineFilter withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the username and returns this instance.
     *
     * @param username the username value
     * @return the result
     */

    public PipelineFilter withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the updated after and returns this instance.
     *
     * @param updatedAfter the updated after value
     * @return the result
     */

    public PipelineFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return this;
    }

    /**
     * Sets the updated before and returns this instance.
     *
     * @param updatedBefore the updated before value
     * @return the result
     */

    public PipelineFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return this;
    }

    /**
     * Sets the order by and returns this instance.
     *
     * @param orderBy the order by value
     * @return the result
     */

    public PipelineFilter withOrderBy(PipelineOrderBy orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * Sets the sort and returns this instance.
     *
     * @param sort the sort value
     * @return the result
     */

    public PipelineFilter withSort(SortOrder sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Returns the query params.
     *
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getQueryParams() {
        return (new GitLabForm().withParam("scope", scope).withParam("status", status).withParam("source", source)
                .withParam("ref", ref).withParam("sha", sha).withParam("yaml_errors", yamlErrors)
                .withParam("name", name).withParam("username", username).withParam("updated_after", updatedAfter)
                .withParam("updated_before", updatedBefore).withParam("order_by", orderBy).withParam("sort", sort));
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
