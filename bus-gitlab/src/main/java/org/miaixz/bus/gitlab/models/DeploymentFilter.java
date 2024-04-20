/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.miaixz.bus.gitlab.Constants;
import org.miaixz.bus.gitlab.Constants.DeploymentOrderBy;
import org.miaixz.bus.gitlab.Constants.DeploymentStatus;
import org.miaixz.bus.gitlab.Constants.SortOrder;
import org.miaixz.bus.gitlab.GitLabApiForm;
import org.miaixz.bus.gitlab.support.ISO8601;

import java.io.Serializable;
import java.util.Date;

public class DeploymentFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Return deployments ordered by either one of id, iid, created_at, updated_at or ref fields. Default is id.
     */
    private DeploymentOrderBy orderBy;

    /**
     * Return deployments sorted in asc or desc order. Default is asc.
     */
    private SortOrder sortOrder;

    /**
     * Return deployments updated after the specified date. Expected in ISO 8601 format (2019-03-15T08:00:00Z).
     */
    private Date updatedAfter;

    /**
     * Return deployments updated before the specified date. Expected in ISO 8601 format (2019-03-15T08:00:00Z).
     */
    private Date updatedBefore;

    /**
     * The name of the environment to filter deployments by.
     */
    private String environment;

    /**
     * The status to filter deployments by.
     */
    private DeploymentStatus status;

    public DeploymentOrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(DeploymentOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Date getUpdatedAfter() {
        return updatedAfter;
    }

    public void setUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    public Date getUpdatedBefore() {
        return updatedBefore;
    }

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    /**
     * @deprecated use {@link #getUpdatedAfter()}
     */
    @Deprecated
    public Date getFinishedAfter() {
        return updatedAfter;
    }

    /**
     * @deprecated use {@link #setUpdatedAfter(Date)}
     */
    @Deprecated
    public void setFinishedAfter(Date finishedAfter) {
        this.updatedAfter = finishedAfter;
    }

    /**
     * @deprecated use {@link #getUpdatedBefore()}
     */
    @Deprecated
    public Date getFinishedBefore() {
        return updatedBefore;
    }

    /**
     * @deprecated use {@link #setUpdatedBefore(Date)}
     */
    @Deprecated
    public void setFinishedBefore(Date finishedBefore) {
        this.updatedBefore = finishedBefore;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public DeploymentFilter withOrderBy(DeploymentOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    public DeploymentFilter withSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return (this);
    }

    public DeploymentFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    public DeploymentFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    /**
     * @deprecated use {@link #withUpdatedAfter(Date)}
     */
    @Deprecated
    public DeploymentFilter withFinishedAfter(Date finishedAfter) {
        this.updatedAfter = finishedAfter;
        return (this);
    }

    /**
     * @deprecated use {@link #withUpdatedBefore(Date)}
     */
    @Deprecated
    public DeploymentFilter withFinishedBefore(Date finishedBefore) {
        this.updatedBefore = finishedBefore;
        return (this);
    }

    public DeploymentFilter withEnvironment(String environment) {
        this.environment = environment;
        return (this);
    }

    public DeploymentFilter withStatus(DeploymentStatus status) {
        this.status = status;
        return (this);
    }

    @JsonIgnore
    public GitLabApiForm getQueryParams(int page, int perPage) {
        return (getQueryParams()
                .withParam(Constants.PAGE_PARAM, page)
                .withParam(Constants.PER_PAGE_PARAM, perPage));
    }

    @JsonIgnore
    public GitLabApiForm getQueryParams() {
        return (new GitLabApiForm()
                .withParam("order_by", orderBy)
                .withParam("sort", sortOrder)
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false))
                .withParam("environment", environment)
                .withParam("status", status));
    }

}
