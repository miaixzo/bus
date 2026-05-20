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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The created child epic class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CreatedChildEpic extends AbstractMinimalEpic<CreatedChildEpic> {

    @Serial
    private static final long serialVersionUID = 2852250855125L;

    private Boolean hasChildren;
    private Boolean hasIssues;
    private String relationUrl;

    /**
     * Returns the has children.
     *
     * @return the result
     */

    public Boolean getHasChildren() {
        return hasChildren;
    }

    /**
     * Sets the has children.
     *
     * @param hasChildren the has children value
     */

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    /**
     * Returns the has issues.
     *
     * @return the result
     */

    public Boolean getHasIssues() {
        return hasIssues;
    }

    /**
     * Sets the has issues.
     *
     * @param hasIssues the has issues value
     */

    public void setHasIssues(Boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    /**
     * Returns the relation url.
     *
     * @return the result
     */

    public String getRelationUrl() {
        return relationUrl;
    }

    /**
     * Sets the relation url.
     *
     * @param relationUrl the relation url value
     */

    public void setRelationUrl(String relationUrl) {
        this.relationUrl = relationUrl;
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
