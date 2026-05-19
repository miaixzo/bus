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

import org.miaixz.bus.gitlab.models.Constants.DeploymentOrderBy;
import org.miaixz.bus.gitlab.models.Constants.DeploymentStatus;
import org.miaixz.bus.gitlab.models.Constants.SortOrder;
import org.miaixz.bus.gitlab.support.ISO8601;

/**
 * The deployment filter class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DeploymentFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852251252095L;

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

    /**
     * Returns the order by.
     *
     * @return the result
     */

    public DeploymentOrderBy getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order by.
     *
     * @param orderBy the order by value
     */

    public void setOrderBy(DeploymentOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * Returns the sort order.
     *
     * @return the result
     */

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the sort order.
     *
     * @param sortOrder the sort order value
     */

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Returns the updated after.
     *
     * @return the result
     */

    public Date getUpdatedAfter() {
        return updatedAfter;
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
     * Returns the updated before.
     *
     * @return the result
     */

    public Date getUpdatedBefore() {
        return updatedBefore;
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
     * Returns the environment.
     *
     * @return the result
     */

    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment.
     *
     * @param environment the environment value
     */

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Returns the status.
     *
     * @return the result
     */

    public DeploymentStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    /**
     * Sets the order by and returns this instance.
     *
     * @param orderBy the order by value
     * @return the result
     */

    public DeploymentFilter withOrderBy(DeploymentOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    /**
     * Sets the sort order and returns this instance.
     *
     * @param sortOrder the sort order value
     * @return the result
     */

    public DeploymentFilter withSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return (this);
    }

    /**
     * Sets the updated after and returns this instance.
     *
     * @param updatedAfter the updated after value
     * @return the result
     */

    public DeploymentFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    /**
     * Sets the updated before and returns this instance.
     *
     * @param updatedBefore the updated before value
     * @return the result
     */

    public DeploymentFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    /**
     * Sets the environment and returns this instance.
     *
     * @param environment the environment value
     * @return the result
     */

    public DeploymentFilter withEnvironment(String environment) {
        this.environment = environment;
        return (this);
    }

    /**
     * Sets the status and returns this instance.
     *
     * @param status the status value
     * @return the result
     */

    public DeploymentFilter withStatus(DeploymentStatus status) {
        this.status = status;
        return (this);
    }

    /**
     * Returns the query params.
     *
     * @param page    the page value
     * @param perPage the per page value
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getQueryParams(int page, int perPage) {
        return (getQueryParams().withParam(Constants.PAGE_PARAM, page).withParam(Constants.PER_PAGE_PARAM, perPage));
    }

    /**
     * Returns the query params.
     *
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getQueryParams() {
        return (new GitLabForm().withParam("order_by", orderBy).withParam("sort", sortOrder)
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false))
                .withParam("environment", environment).withParam("status", status));
    }

}
