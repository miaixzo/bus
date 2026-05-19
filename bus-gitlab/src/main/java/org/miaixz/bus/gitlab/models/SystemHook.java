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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The system hook class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SystemHook implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852281631008L;

    private Long id;
    private String name;
    private String description;
    private String url;
    private Date createdAt;
    private Boolean pushEvents;
    private Boolean tagPushEvents;
    private Boolean enableSslVerification;
    private Boolean repositoryUpdateEvents;
    private Boolean mergeRequestsEvents;
    private List<SystemHook.UrlVariable> urlVariables;

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
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return name;
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
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
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
     * Returns the push events.
     *
     * @return the result
     */

    public Boolean getPushEvents() {
        return pushEvents;
    }

    /**
     * Sets the push events.
     *
     * @param pushEvents the push events value
     */

    public void setPushEvents(Boolean pushEvents) {
        this.pushEvents = pushEvents;
    }

    /**
     * Returns the tag push events.
     *
     * @return the result
     */

    public Boolean getTagPushEvents() {
        return tagPushEvents;
    }

    /**
     * Sets the tag push events.
     *
     * @param tagPushEvents the tag push events value
     */

    public void setTagPushEvents(Boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents;
    }

    /**
     * Returns the enable ssl verification.
     *
     * @return the result
     */

    public Boolean getEnableSslVerification() {
        return enableSslVerification;
    }

    /**
     * Sets the enable ssl verification.
     *
     * @param enableSslVerification the enable ssl verification value
     */

    public void setEnableSslVerification(Boolean enableSslVerification) {
        this.enableSslVerification = enableSslVerification;
    }

    /**
     * Sets the repository update events.
     *
     * @param repositoryUpdateEvents the repository update events value
     */

    public void setRepositoryUpdateEvents(Boolean repositoryUpdateEvents) {
        this.repositoryUpdateEvents = repositoryUpdateEvents;
    }

    /**
     * Returns the repository update events.
     *
     * @return the result
     */

    public Boolean getRepositoryUpdateEvents() {
        return repositoryUpdateEvents;
    }

    /**
     * Sets the merge requests events.
     *
     * @param mergeRequestsEvents the merge requests events value
     */

    public void setMergeRequestsEvents(Boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents;
    }

    /**
     * Returns the merge requests events.
     *
     * @return the result
     */

    public Boolean getMergeRequestsEvents() {
        return mergeRequestsEvents;
    }

    /**
     * Returns the url variables.
     *
     * @return the result
     */

    public List<SystemHook.UrlVariable> getUrlVariables() {
        return urlVariables;
    }

    /**
     * Sets the url variables.
     *
     * @param urlVariables the url variables value
     */

    public void setUrlVariables(List<SystemHook.UrlVariable> urlVariables) {
        this.urlVariables = urlVariables;
    }

    /**
     * Sets the id and returns this instance.
     *
     * @param id the id value
     * @return the result
     */

    public SystemHook withId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public SystemHook withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public SystemHook withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the url and returns this instance.
     *
     * @param url the url value
     * @return the result
     */

    public SystemHook withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets the created at and returns this instance.
     *
     * @param createdAt the created at value
     * @return the result
     */

    public SystemHook withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Sets the push events and returns this instance.
     *
     * @param pushEvents the push events value
     * @return the result
     */

    public SystemHook withPushEvents(Boolean pushEvents) {
        this.pushEvents = pushEvents;
        return this;
    }

    /**
     * Sets the tag push events and returns this instance.
     *
     * @param tagPushEvents the tag push events value
     * @return the result
     */

    public SystemHook withTagPushEvents(Boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents;
        return this;
    }

    /**
     * Sets the enable ssl verification and returns this instance.
     *
     * @param enableSslVerification the enable ssl verification value
     * @return the result
     */

    public SystemHook withEnableSslVerification(Boolean enableSslVerification) {
        this.enableSslVerification = enableSslVerification;
        return this;
    }

    /**
     * Sets the repository update events and returns this instance.
     *
     * @param repositoryUpdateEvents the repository update events value
     * @return the result
     */

    public SystemHook withRepositoryUpdateEvents(Boolean repositoryUpdateEvents) {
        this.repositoryUpdateEvents = repositoryUpdateEvents;
        return this;
    }

    /**
     * Sets the merge requests events and returns this instance.
     *
     * @param mergeRequestsEvents the merge requests events value
     * @return the result
     */

    public SystemHook withMergeRequestsEvents(Boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents;
        return this;
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

    /**
     * The URL variable class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class UrlVariable implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852281710398L;

        private String key;

        /**
         * Returns the key.
         *
         * @return the result
         */

        public String getKey() {
            return key;
        }

        /**
         * Sets the key.
         *
         * @param key the key value
         */

        public void setKey(String key) {
            this.key = key;
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

}
