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
import java.util.Map;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The license class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class License implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852260513290L;

    private Long id;
    private String plan;
    private Date createdAt;
    private Date startsAt;
    private Date expiresAt;
    private Integer historicalMax;
    private Boolean expired;
    private Integer overage;
    private Integer userLimit;
    private Integer activeUsers;
    private Map<String, String> licensee;
    private Map<String, Integer> addOns;

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
     * Returns the plan.
     *
     * @return the result
     */

    public String getPlan() {
        return plan;
    }

    /**
     * Sets the plan.
     *
     * @param plan the plan value
     */

    public void setPlan(String plan) {
        this.plan = plan;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the starts at.
     *
     * @return the result
     */

    public Date getStartsAt() {
        return startsAt;
    }

    /**
     * Sets the starts at.
     *
     * @param startsAt the starts at value
     */

    public void setStartsAt(Date startsAt) {
        this.startsAt = startsAt;
    }

    /**
     * Returns the expires at.
     *
     * @return the result
     */

    public Date getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the expires at.
     *
     * @param expiresAt the expires at value
     */

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the historical max.
     *
     * @return the result
     */

    public Integer getHistoricalMax() {
        return historicalMax;
    }

    /**
     * Sets the historical max.
     *
     * @param historicalMax the historical max value
     */

    public void setHistoricalMax(Integer historicalMax) {
        this.historicalMax = historicalMax;
    }

    /**
     * Returns the expired.
     *
     * @return the result
     */

    public Boolean getExpired() {
        return expired;
    }

    /**
     * Sets the expired.
     *
     * @param expired the expired value
     */

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    /**
     * Returns the overage.
     *
     * @return the result
     */

    public Integer getOverage() {
        return overage;
    }

    /**
     * Sets the overage.
     *
     * @param overage the overage value
     */

    public void setOverage(Integer overage) {
        this.overage = overage;
    }

    /**
     * Returns the user limit.
     *
     * @return the result
     */

    public Integer getUserLimit() {
        return userLimit;
    }

    /**
     * Sets the user limit.
     *
     * @param userLimit the user limit value
     */

    public void setUserLimit(Integer userLimit) {
        this.userLimit = userLimit;
    }

    /**
     * Returns the active users.
     *
     * @return the result
     */

    public Integer getActiveUsers() {
        return activeUsers;
    }

    /**
     * Sets the active users.
     *
     * @param activeUsers the active users value
     */

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    /**
     * Returns the licensee.
     *
     * @return the result
     */

    public Map<String, String> getLicensee() {
        return licensee;
    }

    /**
     * Sets the licensee.
     *
     * @param licensee the licensee value
     */

    public void setLicensee(Map<String, String> licensee) {
        this.licensee = licensee;
    }

    /**
     * Returns the add ons.
     *
     * @return the result
     */

    public Map<String, Integer> getAddOns() {
        return addOns;
    }

    /**
     * Sets the add ons.
     *
     * @param addOns the add ons value
     */

    public void setAddOns(Map<String, Integer> addOns) {
        this.addOns = addOns;
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
