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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The constants interface.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Constants {

    /**
     * The total number of items HTTP header key.
     */
    public static final String TOTAL_HEADER = "X-Total";

    /**
     * The total number of pages HTTP header key.
     */
    public static final String TOTAL_PAGES_HEADER = "X-Total-Pages";

    /**
     * The number of items per page HTTP header key.
     */
    public static final String PER_PAGE = "X-Per-Page";

    /**
     * The index of the current page (starting at 1) HTTP header key.
     */
    public static final String PAGE_HEADER = "X-Page";

    /**
     * The index of the next page HTTP header key.
     */
    public static final String NEXT_PAGE_HEADER = "X-Next-Page";

    /**
     * The index of the previous page HTTP header key.
     */
    public static final String PREV_PAGE_HEADER = "X-Prev-Page";

    /**
     * Items per page param HTTP header key.
     */
    public static final String PER_PAGE_PARAM = "per_page";

    /**
     * Page param HTTP header key.
     */
    public static final String PAGE_PARAM = "page";

    /**
     * Used to specify the type of authentication token.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum TokenType {
        /**
         * The access token type.
         */
        ACCESS,
        /**
         * The oauth2 access token type.
         */
        OAUTH2_ACCESS,
        /**
         * The job token token type.
         */
        JOB_TOKEN,
        /**
         * The private token type.
         */
        PRIVATE;

    }

    /**
     * Enum to specify encoding of file contents.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Encoding {

        /**
         * The text encoding.
         */
        TEXT,
        /**
         * The base64 encoding.
         */
        BASE64;

        private static JacksonJsonEnumHelper<Encoding> enumHelper = new JacksonJsonEnumHelper<>(Encoding.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static Encoding forValue(String value) {
            return enumHelper.forValue((value != null ? value.toLowerCase() : value));
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
     * Enum to use for ordering the results of various API calls.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum SortOrder {

        /**
         * The asc sort order.
         */
        ASC,
        /**
         * The desc sort order.
         */
        DESC;

        private static JacksonJsonEnumHelper<SortOrder> enumHelper = new JacksonJsonEnumHelper<>(SortOrder.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static SortOrder forValue(String value) {
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
     * Enum to use for ordering the results of getEpics().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum EpicOrderBy {

        /**
         * The created at epic order by.
         */
        CREATED_AT,
        /**
         * The updated at epic order by.
         */
        UPDATED_AT;

        private static JacksonJsonEnumHelper<EpicOrderBy> enumHelper = new JacksonJsonEnumHelper<>(EpicOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static EpicOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getIssues().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IssueOrderBy {

        /**
         * The created at issue order by.
         */
        CREATED_AT,
        /**
         * The updated at issue order by.
         */
        UPDATED_AT;

        private static JacksonJsonEnumHelper<IssueOrderBy> enumHelper = new JacksonJsonEnumHelper<>(IssueOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static IssueOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getPackages().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum PackageOrderBy {

        /**
         * The name package order by.
         */
        NAME,
        /**
         * The created at package order by.
         */
        CREATED_AT,
        /**
         * The version package order by.
         */
        VERSION,
        /**
         * The type package order by.
         */
        TYPE,
        /**
         * The project path package order by.
         */
        PROJECT_PATH;

        private static JacksonJsonEnumHelper<PackageOrderBy> enumHelper = new JacksonJsonEnumHelper<>(
                PackageOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static PackageOrderBy forValue(String value) {
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
     * Enum to use for filtering the results of getPackages().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum PackageStatus {

        /**
         * The default package status.
         */
        DEFAULT,
        /**
         * The hidden package status.
         */
        HIDDEN,
        /**
         * The processing package status.
         */
        PROCESSING;

        private static JacksonJsonEnumHelper<PackageStatus> enumHelper = new JacksonJsonEnumHelper<>(
                PackageStatus.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static PackageStatus forValue(String value) {
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
     * Enum to use for ordering the results of getProjects().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProjectOrderBy {

        /**
         * The id project order by.
         */
        ID,
        /**
         * The name project order by.
         */
        NAME,
        /**
         * The path project order by.
         */
        PATH,
        /**
         * The created at project order by.
         */
        CREATED_AT,
        /**
         * The updated at project order by.
         */
        UPDATED_AT,
        /**
         * The last activity at project order by.
         */
        LAST_ACTIVITY_AT;

        private static JacksonJsonEnumHelper<ProjectOrderBy> enumHelper = new JacksonJsonEnumHelper<>(
                ProjectOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ProjectOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getPipelines().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum PipelineOrderBy {

        /**
         * The id pipeline order by.
         */
        ID,
        /**
         * The status pipeline order by.
         */
        STATUS,
        /**
         * The ref pipeline order by.
         */
        REF,
        /**
         * The updated at pipeline order by.
         */
        UPDATED_AT,
        /**
         * The user id pipeline order by.
         */
        USER_ID;

        private static JacksonJsonEnumHelper<PipelineOrderBy> enumHelper = new JacksonJsonEnumHelper<>(
                PipelineOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static PipelineOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getMergeRequests().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MergeRequestOrderBy {

        /**
         * The created at merge request order by.
         */
        CREATED_AT,
        /**
         * The updated at merge request order by.
         */
        UPDATED_AT;

        private static JacksonJsonEnumHelper<MergeRequestOrderBy> enumHelper = new JacksonJsonEnumHelper<>(
                MergeRequestOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static MergeRequestOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getGroups() and getSubGroups().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum GroupOrderBy {

        /**
         * The name group order by.
         */
        NAME,
        /**
         * The path group order by.
         */
        PATH,
        /**
         * The id group order by.
         */
        ID,
        /**
         * The similarity group order by.
         */
        SIMILARITY;

        private static JacksonJsonEnumHelper<GroupOrderBy> enumHelper = new JacksonJsonEnumHelper<>(GroupOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static GroupOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getTags().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum TagOrderBy {

        /**
         * The name tag order by.
         */
        NAME,
        /**
         * The updated tag order by.
         */
        UPDATED;

        private static JacksonJsonEnumHelper<TagOrderBy> enumHelper = new JacksonJsonEnumHelper<>(TagOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static TagOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getDeployments.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static enum DeploymentOrderBy {

        /**
         * The id deployment order by.
         */
        ID,
        /**
         * The iid deployment order by.
         */
        IID,
        /**
         * The created at deployment order by.
         */
        CREATED_AT,
        /**
         * The updated at deployment order by.
         */
        UPDATED_AT,
        /**
         * The ref deployment order by.
         */
        REF;

        private static JacksonJsonEnumHelper<DeploymentOrderBy> enumHelper = new JacksonJsonEnumHelper<>(
                DeploymentOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static DeploymentOrderBy forValue(String value) {
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
     * Enum to use for ordering the results of getContibutors().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ContributorOrderBy {

        /**
         * The name contributor order by.
         */
        NAME,
        /**
         * The email contributor order by.
         */
        EMAIL,
        /**
         * The commits contributor order by.
         */
        COMMITS;

        private static JacksonJsonEnumHelper<ContributorOrderBy> enumHelper = new JacksonJsonEnumHelper<>(
                ContributorOrderBy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ContributorOrderBy forValue(String value) {
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
     * Enum to use for specifying the scope when calling getPipelines().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum PipelineScope {

        /**
         * The running pipeline scope.
         */
        RUNNING,
        /**
         * The pending pipeline scope.
         */
        PENDING,
        /**
         * The finished pipeline scope.
         */
        FINISHED,
        /**
         * The branches pipeline scope.
         */
        BRANCHES,
        /**
         * The tags pipeline scope.
         */
        TAGS;

        private static JacksonJsonEnumHelper<PipelineScope> enumHelper = new JacksonJsonEnumHelper<>(
                PipelineScope.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static PipelineScope forValue(String value) {
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
     * Enum to use for specifying the source when calling getPipelines().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum PipelineSource {

        /**
         * The push pipeline source.
         */
        PUSH,
        /**
         * The web pipeline source.
         */
        WEB,
        /**
         * The trigger pipeline source.
         */
        TRIGGER,
        /**
         * The schedule pipeline source.
         */
        SCHEDULE,
        /**
         * The api pipeline source.
         */
        API,
        /**
         * The external pipeline source.
         */
        EXTERNAL,
        /**
         * The pipeline pipeline source.
         */
        PIPELINE,
        /**
         * The chat pipeline source.
         */
        CHAT,
        /**
         * The webide pipeline source.
         */
        WEBIDE,
        /**
         * The merge request event pipeline source.
         */
        MERGE_REQUEST_EVENT,
        /**
         * The external pull request event pipeline source.
         */
        EXTERNAL_PULL_REQUEST_EVENT;

        private static JacksonJsonEnumHelper<PipelineSource> enumHelper = new JacksonJsonEnumHelper<>(
                PipelineSource.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static PipelineSource forValue(String value) {
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
     * Enum to use for specifying the scope when calling getJobs().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum JobScope {

        /**
         * The created job scope.
         */
        CREATED,
        /**
         * The pending job scope.
         */
        PENDING,
        /**
         * The running job scope.
         */
        RUNNING,
        /**
         * The failed job scope.
         */
        FAILED,
        /**
         * The success job scope.
         */
        SUCCESS,
        /**
         * The canceled job scope.
         */
        CANCELED,
        /**
         * The skipped job scope.
         */
        SKIPPED,
        /**
         * The manual job scope.
         */
        MANUAL,
        /**
         * The waiting for resource job scope.
         */
        WAITING_FOR_RESOURCE;

        private static JacksonJsonEnumHelper<JobScope> enumHelper = new JacksonJsonEnumHelper<>(JobScope.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static JobScope forValue(String value) {
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
     * Enum to use for specifying the scope when calling the various get issue methods.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IssueScope {

        /**
         * The created by me issue scope.
         */
        CREATED_BY_ME,
        /**
         * The assigned to me issue scope.
         */
        ASSIGNED_TO_ME,
        /**
         * The all issue scope.
         */
        ALL;

        private static JacksonJsonEnumHelper<IssueScope> enumHelper = new JacksonJsonEnumHelper<>(IssueScope.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static IssueScope forValue(String value) {
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
     * Enum to use for specifying the scope for getMergeRequests methods.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MergeRequestScope {

        /**
         * The created by me merge request scope.
         */
        CREATED_BY_ME,
        /**
         * The assigned to me merge request scope.
         */
        ASSIGNED_TO_ME,
        /**
         * The all merge request scope.
         */
        ALL;

        private static JacksonJsonEnumHelper<MergeRequestScope> enumHelper = new JacksonJsonEnumHelper<>(
                MergeRequestScope.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static MergeRequestScope forValue(String value) {
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
     * Enum to use for querying the state of a MergeRequest
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MergeRequestState {

        /**
         * The opened merge request state.
         */
        OPENED,
        /**
         * The closed merge request state.
         */
        CLOSED,
        /**
         * The locked merge request state.
         */
        LOCKED,
        /**
         * The merged merge request state.
         */
        MERGED,
        /**
         * The all merge request state.
         */
        ALL;

        private static JacksonJsonEnumHelper<MergeRequestState> enumHelper = new JacksonJsonEnumHelper<>(
                MergeRequestState.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static MergeRequestState forValue(String value) {
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
     * Enum to use for specifying the scope of the search attribute when calling getMergeRequests().
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MergeRequestSearchIn {

        /**
         * The title merge request search in.
         */
        TITLE,
        /**
         * The description merge request search in.
         */
        DESCRIPTION;

        private static JacksonJsonEnumHelper<MergeRequestSearchIn> enumHelper = new JacksonJsonEnumHelper<>(
                MergeRequestSearchIn.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static MergeRequestSearchIn forValue(String value) {
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
     * Enum to use for specifying the state of a merge request or issue update.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum StateEvent {

        /**
         * The close state event.
         */
        CLOSE,
        /**
         * The reopen state event.
         */
        REOPEN;

        private static JacksonJsonEnumHelper<StateEvent> enumHelper = new JacksonJsonEnumHelper<>(StateEvent.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static StateEvent forValue(String value) {
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
     * Enum to used to store the state of an issue.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IssueState {

        /**
         * The opened issue state.
         */
        OPENED,
        /**
         * The closed issue state.
         */
        CLOSED,
        /**
         * The reopened issue state.
         */
        REOPENED;

        private static JacksonJsonEnumHelper<IssueState> enumHelper = new JacksonJsonEnumHelper<>(IssueState.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static IssueState forValue(String value) {
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
     * The milestone state enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MilestoneState {

        /**
         * The active milestone state.
         */
        ACTIVE,
        /**
         * The closed milestone state.
         */
        CLOSED,
        /**
         * The activate milestone state.
         */
        ACTIVATE,
        /**
         * The close milestone state.
         */
        CLOSE;

        private static JacksonJsonEnumHelper<MilestoneState> enumHelper = new JacksonJsonEnumHelper<>(
                MilestoneState.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static MilestoneState forValue(String value) {
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
     * Enum to use for specifying the event action_type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ActionType {

        /**
         * The created action type.
         */
        CREATED,
        /**
         * The updated action type.
         */
        UPDATED,
        /**
         * The opened action type.
         */
        OPENED,
        /**
         * The closed action type.
         */
        CLOSED,
        /**
         * The reopened action type.
         */
        REOPENED,
        /**
         * The pushed action type.
         */
        PUSHED,
        /**
         * The commented action type.
         */
        COMMENTED,
        /**
         * The merged action type.
         */
        MERGED,
        /**
         * The joined action type.
         */
        JOINED,
        /**
         * The left action type.
         */
        LEFT,
        /**
         * The destroyed action type.
         */
        DESTROYED,
        /**
         * The expired action type.
         */
        EXPIRED,
        /**
         * The removed action type.
         */
        REMOVED,
        /**
         * The deleted action type.
         */
        DELETED,
        /**
         * The approved action type.
         */
        APPROVED,
        /**
         * The accepted action type.
         */
        ACCEPTED,
        /**
         * The imported action type.
         */
        IMPORTED;

        private static JacksonJsonEnumHelper<ActionType> enumHelper = new JacksonJsonEnumHelper<>(ActionType.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ActionType forValue(String value) {
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
     * Enum to use for specifying the event target_type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum TargetType {

        /**
         * The issue target type.
         */
        ISSUE,
        /**
         * The milestone target type.
         */
        MILESTONE,
        /**
         * The merge request target type.
         */
        MERGE_REQUEST,
        /**
         * The note target type.
         */
        NOTE,
        /**
         * The project target type.
         */
        PROJECT,
        /**
         * The snippet target type.
         */
        SNIPPET,
        /**
         * The user target type.
         */
        USER;

        private static JacksonJsonEnumHelper<TargetType> enumHelper = new JacksonJsonEnumHelper<>(TargetType.class,
                true, false, true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static TargetType forValue(String value) {
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
     * Enum to use for specifying the line type for a commit comment.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum LineType {

        /**
         * The old line type.
         */
        OLD,
        /**
         * The new line type.
         */
        NEW;

        private static JacksonJsonEnumHelper<LineType> enumHelper = new JacksonJsonEnumHelper<>(LineType.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static LineType forValue(String value) {
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
     * Enum to specify the state of an ImpersonationToken.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ImpersonationState {

        /**
         * The all impersonation state.
         */
        ALL,
        /**
         * The active impersonation state.
         */
        ACTIVE,
        /**
         * The inactive impersonation state.
         */
        INACTIVE;

        private static JacksonJsonEnumHelper<ImpersonationState> enumHelper = new JacksonJsonEnumHelper<>(
                ImpersonationState.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ImpersonationState forValue(String value) {
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
     * Enum to specify the format of a downloaded archive.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ArchiveFormat {

        /**
         * The bz2 archive format.
         */
        BZ2,
        /**
         * The tar archive format.
         */
        TAR,
        /**
         * The tar bz2 archive format.
         */
        TAR_BZ2,
        /**
         * The tar gz archive format.
         */
        TAR_GZ,
        /**
         * The tb2 archive format.
         */
        TB2,
        /**
         * The tbz archive format.
         */
        TBZ,
        /**
         * The tbz2 archive format.
         */
        TBZ2,
        /**
         * The zip archive format.
         */
        ZIP;

        private final String value;

        ArchiveFormat() {
            this.value = name().toLowerCase().replace('_', '.');
        }

        private static Map<String, ArchiveFormat> valuesMap = new HashMap<String, ArchiveFormat>(8);

        static {
            for (ArchiveFormat archiveFormat : ArchiveFormat.values())
                valuesMap.put(archiveFormat.value, archiveFormat);
        }

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        public static ArchiveFormat forValue(String value) {

            if (value == null || value.trim().isEmpty()) {
                return (null);
            }

            ArchiveFormat archiveFormat = valuesMap.get(value);
            if (archiveFormat != null) {
                return (archiveFormat);
            }

            throw new IllegalArgumentException(
                    "Invalid format! Options are tar.gz, tar.bz2, tbz, tbz2, tb2, bz2, tar, and zip.");
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (value);
        }

    }

    /**
     * Enum for the various Commit build status values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum CommitBuildState {

        /**
         * The pending commit build state.
         */
        PENDING,
        /**
         * The running commit build state.
         */
        RUNNING,
        /**
         * The success commit build state.
         */
        SUCCESS,
        /**
         * The failed commit build state.
         */
        FAILED,
        /**
         * The canceled commit build state.
         */
        CANCELED,
        /**
         * The skipped commit build state.
         */
        SKIPPED;

        private static JacksonJsonEnumHelper<CommitBuildState> enumHelper = new JacksonJsonEnumHelper<>(
                CommitBuildState.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static CommitBuildState forValue(String value) {
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
     * Enum for the various Application scope values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ApplicationScope {

        /**
         * The api application scope.
         */
        API,
        /**
         * The read api application scope.
         */
        READ_API,
        /**
         * The read user application scope.
         */
        READ_USER,
        /**
         * The create runner application scope.
         */
        CREATE_RUNNER,
        /**
         * The manage runner application scope.
         */
        MANAGE_RUNNER,
        /**
         * The k8 s proxy application scope.
         */
        K8S_PROXY,
        /**
         * The read repository application scope.
         */
        READ_REPOSITORY,
        /**
         * The write repository application scope.
         */
        WRITE_REPOSITORY,
        /**
         * The read observability application scope.
         */
        READ_OBSERVABILITY,
        /**
         * The write observability application scope.
         */
        WRITE_OBSERVABILITY,
        /**
         * The ai features application scope.
         */
        AI_FEATURES,
        /**
         * The sudo application scope.
         */
        SUDO,
        /**
         * The admin mode application scope.
         */
        ADMIN_MODE,
        /**
         * The read service ping application scope.
         */
        READ_SERVICE_PING,
        /**
         * The openid application scope.
         */
        OPENID,
        /**
         * The profile application scope.
         */
        PROFILE,
        /**
         * The email application scope.
         */
        EMAIL,
        /**
         * The read registry application scope.
         */
        READ_REGISTRY,
        /**
         * The write registry application scope.
         */
        WRITE_REGISTRY,
        /**
         * The read virtual registry application scope.
         */
        READ_VIRTUAL_REGISTRY,
        /**
         * The write virtual registry application scope.
         */
        WRITE_VIRTUAL_REGISTRY,
        /**
         * The self rotate application scope.
         */
        SELF_ROTATE;

        private static JacksonJsonEnumHelper<ApplicationScope> enumHelper = new JacksonJsonEnumHelper<>(
                ApplicationScope.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ApplicationScope forValue(String value) {
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
     * Enum to use for specifying the state when doing a getTodos() with the TodosApi.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum TodoState {

        /**
         * The pending todo state.
         */
        PENDING,
        /**
         * The done todo state.
         */
        DONE;

        private static JacksonJsonEnumHelper<TodoState> enumHelper = new JacksonJsonEnumHelper<>(TodoState.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static TodoState forValue(String value) {
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
     * Enum to use for specifying the type when doing a getTodos() with the TodosApi.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum TodoType {

        /**
         * The issue todo type.
         */
        ISSUE,
        /**
         * The merge request todo type.
         */
        MERGE_REQUEST;

        private static JacksonJsonEnumHelper<TodoType> enumHelper = new JacksonJsonEnumHelper<>(TodoType.class, true,
                true);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static TodoType forValue(String value) {
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
     * Enum to use for specifying the deploy token scope.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum DeployTokenScope {

        /**
         * The read repository deploy token scope.
         */
        READ_REPOSITORY,
        /**
         * The read registry deploy token scope.
         */
        READ_REGISTRY,
        /**
         * The write registry deploy token scope.
         */
        WRITE_REGISTRY,
        /**
         * The read virtual registry deploy token scope.
         */
        READ_VIRTUAL_REGISTRY,
        /**
         * The write virtual registry deploy token scope.
         */
        WRITE_VIRTUAL_REGISTRY,
        /**
         * The read package registry deploy token scope.
         */
        READ_PACKAGE_REGISTRY,
        /**
         * The write package registry deploy token scope.
         */
        WRITE_PACKAGE_REGISTRY;

        /**
         * JSON enum conversion helper.
         */
        private static JacksonJsonEnumHelper<DeployTokenScope> enumHelper = new JacksonJsonEnumHelper<>(
                DeployTokenScope.class);

        /**
         * Converts a GitLab API value into a deploy token scope.
         *
         * @param value the GitLab API value
         * @return the matching deploy token scope
         */
        @JsonCreator
        public static DeployTokenScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Converts this deploy token scope to the GitLab API value.
         *
         * @return the GitLab API value
         */
        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Converts this deploy token scope to the GitLab API value.
         *
         * @return the GitLab API value
         */
        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    /**
     * Enum to use for specifying the action when doing a getTodos() with the TodosApi.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum TodoAction {

        /**
         * The assigned todo action.
         */
        ASSIGNED,
        /**
         * The mentioned todo action.
         */
        MENTIONED,
        /**
         * The build failed todo action.
         */
        BUILD_FAILED,
        /**
         * The marked todo action.
         */
        MARKED,
        /**
         * The approval required todo action.
         */
        APPROVAL_REQUIRED,
        /**
         * The unmergeable todo action.
         */
        UNMERGEABLE,
        /**
         * The directly addressed todo action.
         */
        DIRECTLY_ADDRESSED;

        private static JacksonJsonEnumHelper<TodoAction> enumHelper = new JacksonJsonEnumHelper<>(TodoAction.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static TodoAction forValue(String value) {
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
     * Enum for the build_git_strategy of the project instance.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum SquashOption {

        /**
         * The never squash option.
         */
        NEVER,
        /**
         * The always squash option.
         */
        ALWAYS,
        /**
         * The default on squash option.
         */
        DEFAULT_ON,
        /**
         * The default off squash option.
         */
        DEFAULT_OFF;

        private static JacksonJsonEnumHelper<SquashOption> enumHelper = new JacksonJsonEnumHelper<>(SquashOption.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static SquashOption forValue(String value) {
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
     * The project feature visibility access level enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProjectFeatureVisibilityAccessLevel {

        /**
         * The disabled project feature visibility access level.
         */
        DISABLED,
        /**
         * The private project feature visibility access level.
         */
        PRIVATE,
        /**
         * The enabled project feature visibility access level.
         */
        ENABLED,
        /**
         * The public project feature visibility access level.
         */
        PUBLIC;

        private static final JacksonJsonEnumHelper<ProjectFeatureVisibilityAccessLevel> enumHelper = new JacksonJsonEnumHelper<>(
                ProjectFeatureVisibilityAccessLevel.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ProjectFeatureVisibilityAccessLevel forValue(String value) {
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
     * Enum to use for specifying the status of a deployment.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum DeploymentStatus {

        /**
         * The created deployment status.
         */
        CREATED,
        /**
         * The running deployment status.
         */
        RUNNING,
        /**
         * The success deployment status.
         */
        SUCCESS,
        /**
         * The failed deployment status.
         */
        FAILED,
        /**
         * The canceled deployment status.
         */
        CANCELED;

        private static JacksonJsonEnumHelper<DeploymentStatus> enumHelper = new JacksonJsonEnumHelper<>(
                DeploymentStatus.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static DeploymentStatus forValue(String value) {
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
     * The auto cancel pending pipelines enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum AutoCancelPendingPipelines {

        /**
         * The disabled auto cancel pending pipelines.
         */
        DISABLED,
        /**
         * The enabled auto cancel pending pipelines.
         */
        ENABLED;

        private static final JacksonJsonEnumHelper<AutoCancelPendingPipelines> enumHelper = new JacksonJsonEnumHelper<>(
                AutoCancelPendingPipelines.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static AutoCancelPendingPipelines forValue(String value) {
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
     * Enum to use for specifying the project token scope.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProjectAccessTokenScope {

        /**
         * The api project access token scope.
         */
        API,
        /**
         * The read api project access token scope.
         */
        READ_API,
        /**
         * The read registry project access token scope.
         */
        READ_REGISTRY,
        /**
         * The write registry project access token scope.
         */
        WRITE_REGISTRY,
        /**
         * The read repository project access token scope.
         */
        READ_REPOSITORY,
        /**
         * The write repository project access token scope.
         */
        WRITE_REPOSITORY,
        /**
         * The create runner project access token scope.
         */
        CREATE_RUNNER,
        /**
         * The manage runner project access token scope.
         */
        MANAGE_RUNNER,
        /**
         * The ai features project access token scope.
         */
        AI_FEATURES,
        /**
         * The k8 s proxy project access token scope.
         */
        K8S_PROXY,
        /**
         * The self rotate project access token scope.
         */
        SELF_ROTATE;

        /**
         * JSON enum conversion helper.
         */
        private static JacksonJsonEnumHelper<ProjectAccessTokenScope> enumHelper = new JacksonJsonEnumHelper<>(
                ProjectAccessTokenScope.class);

        /**
         * Converts a GitLab API value into a project access token scope.
         *
         * @param value the GitLab API value
         * @return the matching project access token scope
         */
        @JsonCreator
        public static ProjectAccessTokenScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Converts this project access token scope to the GitLab API value.
         *
         * @return the GitLab API value
         */
        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Converts this project access token scope to the GitLab API value.
         *
         * @return the GitLab API value
         */
        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    /**
     * Enum for the search scope when doing a globalSearch() with the SearchApi.
     *
     * @author Kimi Liu
     * @since Java 21+
     * @param <T> the value type
     */
    public static class SearchScope<T> {

        /**
         * Search scope for projects.
         */
        public static final SearchScope<Project> PROJECTS = new SearchScope<>("projects", Project.class);
        /**
         * Search scope for issues.
         */
        public static final SearchScope<Issue> ISSUES = new SearchScope<>("issues", Issue.class);
        /**
         * Search scope for merge requests.
         */
        public static final SearchScope<MergeRequest> MERGE_REQUESTS = new SearchScope<>("merge_requests",
                MergeRequest.class);
        /**
         * Search scope for milestones.
         */
        public static final SearchScope<Milestone> MILESTONES = new SearchScope<>("milestones", Milestone.class);
        /**
         * Search scope for snippet titles.
         */
        public static final SearchScope<Snippet> SNIPPET_TITLES = new SearchScope<>("snippet_titles", Snippet.class);
        /**
         * Search scope for snippet blobs.
         */
        public static final SearchScope<Snippet> SNIPPET_BLOBS = new SearchScope<>("snippet_blobs", Snippet.class);
        /**
         * Search scope for users.
         */
        public static final SearchScope<User> USERS = new SearchScope<>("users", User.class);
        /**
         * Search scope for blobs.
         */
        public static final SearchScope<SearchBlob> BLOBS = new SearchScope<>("blobs", SearchBlob.class);
        /**
         * Search scope for commits.
         */
        public static final SearchScope<Commit> COMMITS = new SearchScope<>("commits", Commit.class);
        /**
         * Search scope for wiki blobs.
         */
        public static final SearchScope<SearchBlob> WIKI_BLOBS = new SearchScope<>("wiki_blobs", SearchBlob.class);
        private static final Map jsonLookup = Arrays
                .stream(
                        new SearchScope[] { PROJECTS, ISSUES, MERGE_REQUESTS, MILESTONES, SNIPPET_TITLES, SNIPPET_BLOBS,
                                USERS, BLOBS, COMMITS, WIKI_BLOBS })
                .collect(Collectors.toMap(searchScope -> searchScope.jsonName, Function.identity()));
        private final String jsonName;
        private final Class<T> resultType;

        private SearchScope(String jsonName, Class<T> resultType) {
            this.jsonName = jsonName;
            this.resultType = resultType;
        }

        /**
         * Returns the value.
         *
         * @param <T>   the search result type
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static <T> SearchScope<T> forValue(String value) {
            return (SearchScope<T>) jsonLookup.get(value);
        }

        /**
         * Executes the values operation.
         *
         * @return the result
         */

        public static Set<String> values() {
            return jsonLookup.keySet();
        }

        /**
         * Returns the result type.
         *
         * @return the result
         */

        public Class<T> getResultType() {
            return resultType;
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return jsonName;
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return jsonName;
        }

    }

    /**
     * Enum for the build_git_strategy of the project instance.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum BuildGitStrategy {

        /**
         * The fetch build git strategy.
         */
        FETCH,
        /**
         * The clone build git strategy.
         */
        CLONE;

        private static JacksonJsonEnumHelper<BuildGitStrategy> enumHelper = new JacksonJsonEnumHelper<>(
                BuildGitStrategy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static BuildGitStrategy forValue(String value) {
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
     * The auto devops deploy strategy enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum AutoDevopsDeployStrategy {

        /**
         * The continuous auto devops deploy strategy.
         */
        CONTINUOUS,
        /**
         * The manual auto devops deploy strategy.
         */
        MANUAL,
        /**
         * The timed incremental auto devops deploy strategy.
         */
        TIMED_INCREMENTAL;

        private static JacksonJsonEnumHelper<AutoDevopsDeployStrategy> enumHelper = new JacksonJsonEnumHelper<>(
                AutoDevopsDeployStrategy.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static AutoDevopsDeployStrategy forValue(String value) {
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
     * Enum to use for specifying the Event scope.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum EventScope {

        ALL;

        private static JacksonJsonEnumHelper<EventScope> enumHelper = new JacksonJsonEnumHelper<>(EventScope.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static EventScope forValue(String value) {
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
     * Constant to specify the project_creation_level for the group.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProjectCreationLevel {

        /**
         * The noone project creation level.
         */
        NOONE,
        /**
         * The developer project creation level.
         */
        DEVELOPER,
        /**
         * The maintainer project creation level.
         */
        MAINTAINER;

        private static JacksonJsonEnumHelper<ProjectCreationLevel> enumHelper = new JacksonJsonEnumHelper<>(
                ProjectCreationLevel.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static ProjectCreationLevel forValue(String value) {
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
     * Constant to specify the subgroup_creation_level for the group.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum SubgroupCreationLevel {

        /**
         * The owner subgroup creation level.
         */
        OWNER,
        /**
         * The maintainer subgroup creation level.
         */
        MAINTAINER;

        private static JacksonJsonEnumHelper<SubgroupCreationLevel> enumHelper = new JacksonJsonEnumHelper<>(
                SubgroupCreationLevel.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static SubgroupCreationLevel forValue(String value) {
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
     * The default branch protection level enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum DefaultBranchProtectionLevel {

        /**
         * The not protected default branch protection level.
         */
        NOT_PROTECTED(0),
        /**
         * The partially protected default branch protection level.
         */
        PARTIALLY_PROTECTED(1),
        /**
         * The fully protected default branch protection level.
         */
        FULLY_PROTECTED(2),
        /**
         * The protected against pushes default branch protection level.
         */
        PROTECTED_AGAINST_PUSHES(3),
        /**
         * The full protection after initial push default branch protection level.
         */
        FULL_PROTECTION_AFTER_INITIAL_PUSH(4);

        @JsonValue
        private final int value;

        private DefaultBranchProtectionLevel(int value) {
            this.value = value;
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return Integer.toString(value);
        }

    }

    /**
     * Enum for the search scope when doing a groupSearch() with the SearchApi.
     *
     * @author Kimi Liu
     * @since Java 21+
     * @param <T> the value type
     */
    public static class GroupSearchScope<T> {

        /**
         * Group search scope for projects.
         */
        public static final GroupSearchScope<Project> PROJECTS = new GroupSearchScope<>("projects", Project.class);
        /**
         * Group search scope for issues.
         */
        public static final GroupSearchScope<Issue> ISSUES = new GroupSearchScope<>("issues", Issue.class);
        /**
         * Group search scope for merge requests.
         */
        public static final GroupSearchScope<MergeRequest> MERGE_REQUESTS = new GroupSearchScope<>("merge_requests",
                MergeRequest.class);
        /**
         * Group search scope for milestones.
         */
        public static final GroupSearchScope<Milestone> MILESTONES = new GroupSearchScope<>("milestones",
                Milestone.class);
        /**
         * Group search scope for wiki blobs.
         */
        public static final GroupSearchScope<SearchBlob> WIKI_BLOBS = new GroupSearchScope<>("wiki_blobs",
                SearchBlob.class);
        /**
         * Group search scope for commits.
         */
        public static final GroupSearchScope<Commit> COMMITS = new GroupSearchScope<>("commits", Commit.class);
        /**
         * Group search scope for blobs.
         */
        public static final GroupSearchScope<SearchBlob> BLOBS = new GroupSearchScope<>("blobs", SearchBlob.class);
        /**
         * Group search scope for notes.
         */
        public static final GroupSearchScope<Note> NOTES = new GroupSearchScope<>("notes", Note.class);
        /**
         * Group search scope for users.
         */
        public static final GroupSearchScope<User> USERS = new GroupSearchScope<>("users", User.class);
        private static final Map jsonLookup = Arrays
                .stream(
                        new GroupSearchScope[] { PROJECTS, ISSUES, MERGE_REQUESTS, MILESTONES, WIKI_BLOBS, COMMITS,
                                BLOBS, NOTES, USERS, })
                .collect(Collectors.toMap(searchScope -> searchScope.jsonName, Function.identity()));
        private final String jsonName;
        private final Class<T> resultType;

        /**
         * Creates a group search scope.
         *
         * @param jsonName   the json name value
         * @param resultType the result type value
         */

        public GroupSearchScope(String jsonName, Class<T> resultType) {
            this.jsonName = jsonName;
            this.resultType = resultType;
        }

        /**
         * Returns the value.
         *
         * @param <T>   the search result type
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static <T> GroupSearchScope<T> forValue(String value) {
            return (GroupSearchScope<T>) jsonLookup.get(value);
        }

        /**
         * Executes the values operation.
         *
         * @return the result
         */

        public static Set<String> values() {
            return jsonLookup.keySet();
        }

        /**
         * Returns the result type.
         *
         * @return the result
         */

        public Class<T> getResultType() {
            return resultType;
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return jsonName;
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return jsonName;
        }

    }

    /**
     * Enum for the search scope when doing a projectSearch() with the SearchApi.
     *
     * @author Kimi Liu
     * @since Java 21+
     * @param <T> the value type
     */
    public static class ProjectSearchScope<T> {

        /**
         * Project search scope for blobs.
         */
        public static final ProjectSearchScope<SearchBlob> BLOBS = new ProjectSearchScope<>("blobs", SearchBlob.class);
        /**
         * Project search scope for commits.
         */
        public static final ProjectSearchScope<Commit> COMMITS = new ProjectSearchScope<>("commits", Commit.class);
        /**
         * Project search scope for issues.
         */
        public static final ProjectSearchScope<Issue> ISSUES = new ProjectSearchScope<>("issues", Issue.class);
        /**
         * Project search scope for merge requests.
         */
        public static final ProjectSearchScope<MergeRequest> MERGE_REQUESTS = new ProjectSearchScope<>("merge_requests",
                MergeRequest.class);
        /**
         * Project search scope for milestones.
         */
        public static final ProjectSearchScope<Milestone> MILESTONES = new ProjectSearchScope<>("milestones",
                Milestone.class);
        /**
         * Project search scope for notes.
         */
        public static final ProjectSearchScope<Note> NOTES = new ProjectSearchScope<>("notes", Note.class);
        /**
         * Project search scope for wiki blobs.
         */
        public static final ProjectSearchScope<SearchBlob> WIKI_BLOBS = new ProjectSearchScope<>("wiki_blobs",
                SearchBlob.class);
        /**
         * Project search scope for users.
         */
        public static final ProjectSearchScope<User> USERS = new ProjectSearchScope<>("users", User.class);
        private static final Map jsonLookup = Arrays
                .stream(
                        new ProjectSearchScope[] { BLOBS, COMMITS, ISSUES, MERGE_REQUESTS, MILESTONES, NOTES,
                                WIKI_BLOBS, USERS })
                .collect(Collectors.toMap(searchScope -> searchScope.jsonName, Function.identity()));
        private final String jsonName;
        private final Class<T> resultType;

        /**
         * Creates a project search scope.
         *
         * @param jsonName   the json name value
         * @param resultType the result type value
         */

        public ProjectSearchScope(String jsonName, Class<T> resultType) {
            this.jsonName = jsonName;
            this.resultType = resultType;
        }

        /**
         * Returns the value.
         *
         * @param <T>   the search result type
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static <T> ProjectSearchScope<T> forValue(String value) {
            return (ProjectSearchScope<T>) jsonLookup.get(value);
        }

        /**
         * Executes the values operation.
         *
         * @return the result
         */

        public static Set<String> values() {
            return jsonLookup.keySet();
        }

        /**
         * Returns the result type.
         *
         * @return the result
         */

        public Class<T> getResultType() {
            return resultType;
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return jsonName;
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return jsonName;
        }

    }

}
