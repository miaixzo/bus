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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class is used to filter commit status when getting lists of them.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommitStatusFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852250336272L;

    private String ref;
    private String stage;
    private String name;
    private Boolean all;

    /**
     * Sets the ref and returns this instance.
     *
     * @param ref the ref value
     * @return the result
     */

    public CommitStatusFilter withRef(String ref) {
        this.ref = ref;
        return this;
    }

    /**
     * Sets the stage and returns this instance.
     *
     * @param stage the stage value
     * @return the result
     */

    public CommitStatusFilter withStage(String stage) {
        this.stage = stage;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public CommitStatusFilter withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the all and returns this instance.
     *
     * @param all the all value
     * @return the result
     */

    public CommitStatusFilter withAll(Boolean all) {
        this.all = all;
        return this;
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
        return (new GitLabForm().withParam("ref", ref).withParam("stage", stage).withParam("name", name)
                .withParam("all", all));
    }

}
