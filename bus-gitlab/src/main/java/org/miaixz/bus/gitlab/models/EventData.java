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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event data class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventData implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852253272657L;

    private String after;
    private String before;
    private List<Commit> commits;
    private String ref;
    private Repository repository;
    private Integer totalCommitsCount;
    private Long userId;
    private String userName;

    /**
     * Returns the after.
     *
     * @return the result
     */

    public String getAfter() {
        return this.after;
    }

    /**
     * Sets the after.
     *
     * @param after the after value
     */

    public void setAfter(String after) {
        this.after = after;
    }

    /**
     * Returns the before.
     *
     * @return the result
     */

    public String getBefore() {
        return this.before;
    }

    /**
     * Sets the before.
     *
     * @param before the before value
     */

    public void setBefore(String before) {
        this.before = before;
    }

    /**
     * Returns the commits.
     *
     * @return the result
     */

    public List<Commit> getCommits() {
        return this.commits;
    }

    /**
     * Sets the commits.
     *
     * @param commits the commits value
     */

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    /**
     * Returns the ref.
     *
     * @return the result
     */

    public String getRef() {
        return this.ref;
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
     * Returns the repository.
     *
     * @return the result
     */

    public Repository getRepository() {
        return this.repository;
    }

    /**
     * Sets the repository.
     *
     * @param repository the repository value
     */

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Returns the total commits count.
     *
     * @return the result
     */

    public Integer getTotalCommitsCount() {
        return this.totalCommitsCount;
    }

    /**
     * Sets the total commits count.
     *
     * @param totalCommitsCount the total commits count value
     */

    public void setTotalCommitsCount(Integer totalCommitsCount) {
        this.totalCommitsCount = totalCommitsCount;
    }

    /**
     * Returns the user id.
     *
     * @return the result
     */

    public Long getUserId() {
        return this.userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the user id value
     */

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the user name.
     *
     * @return the result
     */

    public String getUserName() {
        return this.userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName the user name value
     */

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Sets the after and returns this instance.
     *
     * @param after the after value
     * @return the result
     */

    public EventData withAfter(String after) {
        this.after = after;
        return this;
    }

    /**
     * Sets the before and returns this instance.
     *
     * @param before the before value
     * @return the result
     */

    public EventData withBefore(String before) {
        this.before = before;
        return this;
    }

    /**
     * Sets the commits and returns this instance.
     *
     * @param commits the commits value
     * @return the result
     */

    public EventData withCommits(List<Commit> commits) {
        this.commits = commits;
        return this;
    }

    /**
     * Sets the ref and returns this instance.
     *
     * @param ref the ref value
     * @return the result
     */

    public EventData withRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * Sets the repository and returns this instance.
     *
     * @param repository the repository value
     * @return the result
     */

    public EventData withRepository(Repository repository) {
        this.repository = repository;
        return this;
    }

    /**
     * Sets the total commits count and returns this instance.
     *
     * @param totalCommitsCount the total commits count value
     * @return the result
     */

    public EventData withTotalCommitsCount(Integer totalCommitsCount) {
        this.totalCommitsCount = totalCommitsCount;
        return this;
    }

    /**
     * Sets the user id and returns this instance.
     *
     * @param userId the user id value
     * @return the result
     */

    public EventData withUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Sets the user name and returns this instance.
     *
     * @param userName the user name value
     * @return the result
     */

    public EventData withUserName(String userName) {
        this.userName = userName;
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

}
