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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The merge request version class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MergeRequestVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852265139183L;

    private Long id;
    private String headCommitSha;
    private String baseCommitSha;
    private String startCommitSha;
    private Date createdAt;
    private Long mergeRequestId;
    private String state;
    private String realSize;

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
     * Returns the head commit sha.
     *
     * @return the result
     */

    public String getHeadCommitSha() {
        return headCommitSha;
    }

    /**
     * Sets the head commit sha.
     *
     * @param headCommitSha the head commit sha value
     */

    public void setHeadCommitSha(String headCommitSha) {
        this.headCommitSha = headCommitSha;
    }

    /**
     * Returns the base commit sha.
     *
     * @return the result
     */

    public String getBaseCommitSha() {
        return baseCommitSha;
    }

    /**
     * Sets the base commit sha.
     *
     * @param baseCommitSha the base commit sha value
     */

    public void setBaseCommitSha(String baseCommitSha) {
        this.baseCommitSha = baseCommitSha;
    }

    /**
     * Returns the start commit sha.
     *
     * @return the result
     */

    public String getStartCommitSha() {
        return startCommitSha;
    }

    /**
     * Sets the start commit sha.
     *
     * @param startCommitSha the start commit sha value
     */

    public void setStartCommitSha(String startCommitSha) {
        this.startCommitSha = startCommitSha;
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
     * Returns the merge request id.
     *
     * @return the result
     */

    public Long getMergeRequestId() {
        return mergeRequestId;
    }

    /**
     * Sets the merge request id.
     *
     * @param mergeRequestId the merge request id value
     */

    public void setMergeRequestId(Long mergeRequestId) {
        this.mergeRequestId = mergeRequestId;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the real size.
     *
     * @return the result
     */

    public String getRealSize() {
        return realSize;
    }

    /**
     * Sets the real size.
     *
     * @param realSize the real size value
     */

    public void setRealSize(String realSize) {
        this.realSize = realSize;
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
