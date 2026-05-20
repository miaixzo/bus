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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.ISO8601;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The iteration filter class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IterationFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852259338991L;

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
        return new GitLabForm().withParam("state", state).withParam("search", search).withParam("in", in)
                .withParam("include_ancestors", includeAncestors)
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false));
    }

    /**
     * Return opened, upcoming, current, closed, or all iterations.
     */
    private IterationFilterState state;

    /**
     * Return only iterations with a title matching the provided string.
     */
    private String search;

    /**
     * Fields in which fuzzy search should be performed with the query given in the argument search.
     */
    private IterationFilterIn in;

    /**
     * Include iterations from parent group and its ancestors. Defaults to true.
     */
    private Boolean includeAncestors;

    /**
     * Return iterations updated after the specified date. Expected in ISO 8601 format (2019-03-15T08:00:00Z).
     */
    private Date updatedAfter;

    /**
     * Return iterations updated before the specified date. Expected in ISO 8601 format (2019-03-15T08:00:00Z).
     */
    private Date updatedBefore;

    /**
     * Returns the state.
     *
     * @return the result
     */

    public IterationFilterState getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(IterationFilterState state) {
        this.state = state;
    }

    /**
     * Returns the search.
     *
     * @return the result
     */

    public String getSearch() {
        return search;
    }

    /**
     * Sets the search.
     *
     * @param search the search value
     */

    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Returns the in.
     *
     * @return the result
     */

    public IterationFilterIn getIn() {
        return in;
    }

    /**
     * Sets the in.
     *
     * @param in the in value
     */

    public void setIn(IterationFilterIn in) {
        this.in = in;
    }

    /**
     * Returns the include ancestors.
     *
     * @return the result
     */

    public Boolean getIncludeAncestors() {
        return includeAncestors;
    }

    /**
     * Sets the include ancestors.
     *
     * @param includeAncestors the include ancestors value
     */

    public void setIncludeAncestors(Boolean includeAncestors) {
        this.includeAncestors = includeAncestors;
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
     * Sets the state and returns this instance.
     *
     * @param state the state value
     * @return the result
     */

    public IterationFilter withState(IterationFilterState state) {
        this.state = state;
        return (this);
    }

    /**
     * Sets the search and returns this instance.
     *
     * @param search the search value
     * @return the result
     */

    public IterationFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    /**
     * Sets the in and returns this instance.
     *
     * @param in the in value
     * @return the result
     */

    public IterationFilter withIn(IterationFilterIn in) {
        this.in = in;
        return (this);
    }

    /**
     * Sets the include ancestors and returns this instance.
     *
     * @param includeAncestors the include ancestors value
     * @return the result
     */

    public IterationFilter withIncludeAncestors(Boolean includeAncestors) {
        this.includeAncestors = includeAncestors;
        return (this);
    }

    /**
     * Sets the updated after and returns this instance.
     *
     * @param updatedAfter the updated after value
     * @return the result
     */

    public IterationFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    /**
     * Sets the updated before and returns this instance.
     *
     * @param updatedBefore the updated before value
     * @return the result
     */

    public IterationFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    /**
     * The iteration filter state enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IterationFilterState {

        /**
         * The opened iteration filter state.
         */
        OPENED,
        /**
         * The upcoming iteration filter state.
         */
        UPCOMING,
        /**
         * The current iteration filter state.
         */
        CURRENT,
        /**
         * The closed iteration filter state.
         */
        CLOSED,
        /**
         * The all iteration filter state.
         */
        ALL;

        private static JacksonJsonEnumHelper<IterationFilterState> enumHelper = new JacksonJsonEnumHelper<>(
                IterationFilterState.class, false, true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static IterationFilterState forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    /**
     * The iteration filter in enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IterationFilterIn {

        /**
         * The title iteration filter in.
         */
        TITLE,
        /**
         * The cadence title iteration filter in.
         */
        CADENCE_TITLE;

        private static JacksonJsonEnumHelper<IterationFilterIn> enumHelper = new JacksonJsonEnumHelper<>(
                IterationFilterIn.class, false, false, true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static IterationFilterIn forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

}
