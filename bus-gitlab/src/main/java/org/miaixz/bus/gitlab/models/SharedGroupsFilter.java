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

import org.miaixz.bus.gitlab.models.Constants.GroupOrderBy;
import org.miaixz.bus.gitlab.models.Constants.SortOrder;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * Filter used when listing groups that a specified group has been invited to.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SharedGroupsFilter implements Serializable {

    /**
     * Serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2852256129064L;

    /**
     * Group IDs to exclude from the response.
     */
    private List<Long> skipGroups;

    /**
     * Search text used to match group names or paths.
     */
    private String search;

    /**
     * Field used to order returned groups.
     */
    private GroupOrderBy orderBy;

    /**
     * Direction used to sort returned groups.
     */
    private SortOrder sort;

    /**
     * Visibility level used to limit returned groups.
     */
    private Visibility visibility;

    /**
     * Minimum access level required for the current user.
     */
    private AccessLevel minAccessLevel;

    /**
     * Flag indicating whether custom attributes should be included.
     */
    private Boolean withCustomAttributes;

    /**
     * Excludes the specified group IDs from the response.
     *
     * @param skipGroups the group IDs to exclude
     * @return the reference to this {@code SharedGroupsFilter} instance
     */
    public SharedGroupsFilter withSkipGroups(List<Long> skipGroups) {
        this.skipGroups = skipGroups;
        return (this);
    }

    /**
     * Limits the response to groups matching the specified search text.
     *
     * @param search the search text
     * @return the reference to this {@code SharedGroupsFilter} instance
     */
    public SharedGroupsFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    /**
     * Orders returned groups by the specified field.
     *
     * @param orderBy the field used to order returned groups
     * @return the reference to this {@code SharedGroupsFilter} instance
     */
    public SharedGroupsFilter withOrderBy(GroupOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    /**
     * Sorts returned groups using the specified direction.
     *
     * @param sort the sort direction
     * @return the reference to this {@code SharedGroupsFilter} instance
     */
    public SharedGroupsFilter withSortOder(SortOrder sort) {
        this.sort = sort;
        return (this);
    }

    /**
     * Limits the response to groups with the specified visibility.
     *
     * @param visibility the visibility to match
     * @return the reference to this {@code SharedGroupsFilter} instance
     */
    public SharedGroupsFilter withVisibility(Visibility visibility) {
        this.visibility = visibility;
        return (this);
    }

    /**
     * Limits the response to groups where the current user has at least the specified access level.
     *
     * @param minAccessLevel the minimum access level to match
     * @return the reference to this {@code SharedGroupsFilter} instance
     */
    public SharedGroupsFilter withMinAccessLevel(AccessLevel minAccessLevel) {
        this.minAccessLevel = minAccessLevel;
        return (this);
    }

    /**
     * Includes custom attributes in the response when requested.
     *
     * @param withCustomAttributes {@code true} to include custom attributes
     * @return the reference to this {@code SharedGroupsFilter} instance
     */
    public SharedGroupsFilter withCustomAttributes(Boolean withCustomAttributes) {
        this.withCustomAttributes = withCustomAttributes;
        return (this);
    }

    /**
     * Builds the query parameters represented by this filter.
     *
     * @return query parameters for this filter
     */
    public GitLabForm getQueryParams() {
        return (new GitLabForm().withParam("skip_groups", skipGroups).withParam("search", search)
                .withParam("order_by", orderBy).withParam("sort", sort).withParam("visibility", visibility)
                .withParam("simple", minAccessLevel).withParam("with_custom_attributes", withCustomAttributes));
    }

    /**
     * Returns this filter as a JSON string.
     *
     * @return this filter serialized as JSON
     */
    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
