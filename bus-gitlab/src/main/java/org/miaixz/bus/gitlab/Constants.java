/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.gitlab;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import java.util.HashMap;
import java.util.Map;

public interface Constants {

    /**
     * The total number of items HTTP header key.
     */
    String TOTAL_HEADER = "X-Total";

    /**
     * The total number of pages HTTP header key.
     */
    String TOTAL_PAGES_HEADER = "X-Total-Pages";

    /**
     * The number of items per page HTTP header key.
     */
    String PER_PAGE = "X-Per-Page";

    /**
     * The index of the current page (starting at 1) HTTP header key.
     */
    String PAGE_HEADER = "X-Page";

    /**
     * The index of the next page HTTP header key.
     */
    String NEXT_PAGE_HEADER = "X-Next-Page";

    /**
     * The index of the previous page HTTP header key.
     */
    String PREV_PAGE_HEADER = "X-Prev-Page";

    /**
     * Items per page param HTTP header key.
     */
    String PER_PAGE_PARAM = "per_page";

    /**
     * Page param HTTP header key.
     */
    String PAGE_PARAM = "page";

    /**
     * Used to specify the type of authentication token.
     */
    enum TokenType {
        ACCESS, OAUTH2_ACCESS, PRIVATE
    }

    /**
     * Enum to specify encoding of file contents.
     */
    enum Encoding {
        TEXT, BASE64;

        private static final JacksonJsonEnumHelper<Encoding> enumHelper = new JacksonJsonEnumHelper<>(Encoding.class);

        @JsonCreator
        public static Encoding forValue(String value) {
            return enumHelper.forValue((value != null ? value.toLowerCase() : value));
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of various API calls.
     */
    enum SortOrder {

        ASC, DESC;

        private static final JacksonJsonEnumHelper<SortOrder> enumHelper = new JacksonJsonEnumHelper<>(SortOrder.class);

        @JsonCreator
        public static SortOrder forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }


    /**
     * Enum to use for ordering the results of getEpics().
     */
    enum EpicOrderBy {

        CREATED_AT, UPDATED_AT;

        private static final JacksonJsonEnumHelper<EpicOrderBy> enumHelper = new JacksonJsonEnumHelper<>(EpicOrderBy.class);

        @JsonCreator
        public static EpicOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getIssues().
     */
    enum IssueOrderBy {

        CREATED_AT, UPDATED_AT;

        private static final JacksonJsonEnumHelper<IssueOrderBy> enumHelper = new JacksonJsonEnumHelper<>(IssueOrderBy.class);

        @JsonCreator
        public static IssueOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getPackages().
     */
    enum PackageOrderBy {

        NAME, CREATED_AT, VERSION, TYPE, PROJECT_PATH;

        private static final JacksonJsonEnumHelper<PackageOrderBy> enumHelper = new JacksonJsonEnumHelper<>(PackageOrderBy.class);

        @JsonCreator
        public static PackageOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for filtering the results of getPackages().
     */
    enum PackageStatus {

        DEFAULT, HIDDEN, PROCESSING;

        private static final JacksonJsonEnumHelper<PackageStatus> enumHelper = new JacksonJsonEnumHelper<>(PackageStatus.class);

        @JsonCreator
        public static PackageStatus forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getProjects().
     */
    enum ProjectOrderBy {

        ID, NAME, PATH, CREATED_AT, UPDATED_AT, LAST_ACTIVITY_AT;
        private static final JacksonJsonEnumHelper<ProjectOrderBy> enumHelper = new JacksonJsonEnumHelper<>(ProjectOrderBy.class);

        @JsonCreator
        public static ProjectOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getPipelines().
     */
    enum PipelineOrderBy {

        ID, STATUS, REF, UPDATED_AT, USER_ID;

        private static final JacksonJsonEnumHelper<PipelineOrderBy> enumHelper = new JacksonJsonEnumHelper<>(PipelineOrderBy.class);

        @JsonCreator
        public static PipelineOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getMergeRequests().
     */
    enum MergeRequestOrderBy {

        CREATED_AT, UPDATED_AT;

        private static final JacksonJsonEnumHelper<MergeRequestOrderBy> enumHelper = new JacksonJsonEnumHelper<>(MergeRequestOrderBy.class);

        @JsonCreator
        public static MergeRequestOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getGroups() and getSubGroups().
     */
    enum GroupOrderBy {

        NAME, PATH, ID, SIMILARITY;
        private static final JacksonJsonEnumHelper<GroupOrderBy> enumHelper = new JacksonJsonEnumHelper<>(GroupOrderBy.class);

        @JsonCreator
        public static GroupOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getTags().
     */
    enum TagOrderBy {

        NAME, UPDATED;
        private static final JacksonJsonEnumHelper<TagOrderBy> enumHelper = new JacksonJsonEnumHelper<>(TagOrderBy.class);

        @JsonCreator
        public static TagOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getDeployments.
     */
    enum DeploymentOrderBy {

        ID, IID, CREATED_AT, UPDATED_AT, REF;
        private static final JacksonJsonEnumHelper<DeploymentOrderBy> enumHelper = new JacksonJsonEnumHelper<>(DeploymentOrderBy.class);

        @JsonCreator
        public static DeploymentOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for ordering the results of getContibutors().
     */
    enum ContributorOrderBy {

        NAME, EMAIL, COMMITS;

        private static final JacksonJsonEnumHelper<ContributorOrderBy> enumHelper = new JacksonJsonEnumHelper<>(ContributorOrderBy.class);

        @JsonCreator
        public static ContributorOrderBy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the scope when calling getPipelines().
     */
    enum PipelineScope {

        RUNNING, PENDING, FINISHED, BRANCHES, TAGS;

        private static final JacksonJsonEnumHelper<PipelineScope> enumHelper = new JacksonJsonEnumHelper<>(PipelineScope.class);

        @JsonCreator
        public static PipelineScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the scope when calling getJobs().
     */
    enum JobScope {

        CREATED, PENDING, RUNNING, FAILED, SUCCESS, CANCELED, SKIPPED, MANUAL;

        private static final JacksonJsonEnumHelper<JobScope> enumHelper = new JacksonJsonEnumHelper<>(JobScope.class);

        @JsonCreator
        public static JobScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the scope when calling the various get issue methods.
     */
    enum IssueScope {

        CREATED_BY_ME, ASSIGNED_TO_ME, ALL;

        private static final JacksonJsonEnumHelper<IssueScope> enumHelper = new JacksonJsonEnumHelper<>(IssueScope.class);

        @JsonCreator
        public static IssueScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the scope for getMergeRequests methods.
     */
    enum MergeRequestScope {

        CREATED_BY_ME, ASSIGNED_TO_ME, ALL;

        private static final JacksonJsonEnumHelper<MergeRequestScope> enumHelper = new JacksonJsonEnumHelper<>(MergeRequestScope.class);

        @JsonCreator
        public static MergeRequestScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for querying the state of a MergeRequest
     */
    enum MergeRequestState {

        OPENED, CLOSED, LOCKED, MERGED, ALL;

        private static final JacksonJsonEnumHelper<MergeRequestState> enumHelper = new JacksonJsonEnumHelper<>(MergeRequestState.class);

        @JsonCreator
        public static MergeRequestState forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the scope of the search attribute when calling getMergeRequests().
     */
    enum MergeRequestSearchIn {

        TITLE, DESCRIPTION;

        private static final JacksonJsonEnumHelper<MergeRequestSearchIn> enumHelper = new JacksonJsonEnumHelper<>(MergeRequestSearchIn.class);

        @JsonCreator
        public static MergeRequestSearchIn forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the state of a merge request or issue update.
     */
    enum StateEvent {

        CLOSE, REOPEN;

        private static final JacksonJsonEnumHelper<StateEvent> enumHelper = new JacksonJsonEnumHelper<>(StateEvent.class);

        @JsonCreator
        public static StateEvent forValue(String value) {
            return enumHelper.forValue(value);
        }


        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to used to store the state of an issue.
     */
    enum IssueState {

        OPENED, CLOSED, REOPENED;

        private static final JacksonJsonEnumHelper<IssueState> enumHelper = new JacksonJsonEnumHelper<>(IssueState.class);

        @JsonCreator
        public static IssueState forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    enum MilestoneState {

        ACTIVE, CLOSED, ACTIVATE, CLOSE;

        private static final JacksonJsonEnumHelper<MilestoneState> enumHelper = new JacksonJsonEnumHelper<>(MilestoneState.class);

        @JsonCreator
        public static MilestoneState forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the event action_type.
     */
    enum ActionType {

        CREATED, UPDATED, OPENED, CLOSED, REOPENED, PUSHED, COMMENTED, MERGED, JOINED, LEFT, DESTROYED, EXPIRED, REMOVED, DELETED, APPROVED, ACCEPTED, IMPORTED;

        private static final JacksonJsonEnumHelper<ActionType> enumHelper = new JacksonJsonEnumHelper<>(ActionType.class);

        @JsonCreator
        public static ActionType forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the event target_type.
     */
    enum TargetType {

        ISSUE, MILESTONE, MERGE_REQUEST, NOTE, PROJECT, SNIPPET, USER;

        private static final JacksonJsonEnumHelper<TargetType> enumHelper = new JacksonJsonEnumHelper<>(TargetType.class, true, false, true);

        @JsonCreator
        public static TargetType forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the line type for a commit comment.
     */
    enum LineType {

        OLD, NEW;

        private static final JacksonJsonEnumHelper<LineType> enumHelper = new JacksonJsonEnumHelper<>(LineType.class);

        @JsonCreator
        public static LineType forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to specify the state of an ImpersonationToken.
     */
    enum ImpersonationState {

        ALL, ACTIVE, INACTIVE;

        private static final JacksonJsonEnumHelper<ImpersonationState> enumHelper = new JacksonJsonEnumHelper<>(ImpersonationState.class);

        @JsonCreator
        public static ImpersonationState forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to specify the format of a downloaded archive.
     */
    enum ArchiveFormat {

        BZ2, TAR, TAR_BZ2, TAR_GZ, TB2, TBZ, TBZ2, ZIP;

        private static final Map<String, ArchiveFormat> valuesMap = new HashMap<String, ArchiveFormat>(8);

        static {
            for (ArchiveFormat archiveFormat : ArchiveFormat.values())
                valuesMap.put(archiveFormat.value, archiveFormat);
        }

        private final String value;

        ArchiveFormat() {
            this.value = name().toLowerCase().replace('_', '.');
        }

        public static ArchiveFormat forValue(String value) throws GitLabApiException {

            if (value == null || value.trim().isEmpty()) {
                return (null);
            }

            ArchiveFormat archiveFormat = valuesMap.get(value);
            if (archiveFormat != null) {
                return (archiveFormat);
            }

            throw new GitLabApiException("Invalid format! Options are tar.gz, tar.bz2, tbz, tbz2, tb2, bz2, tar, and zip.");
        }

        @Override
        public String toString() {
            return (value);
        }
    }

    /**
     * Enum for the various Commit build status values.
     */
    enum CommitBuildState {

        PENDING, RUNNING, SUCCESS, FAILED, CANCELED, SKIPPED;

        private static final JacksonJsonEnumHelper<CommitBuildState> enumHelper = new JacksonJsonEnumHelper<>(CommitBuildState.class);

        @JsonCreator
        public static CommitBuildState forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum for the various Application scope values.
     */
    enum ApplicationScope {

        /**
         * Access the authenticated user's API
         */
        API,

        /**
         * Read the authenticated user's personal information
         */
        READ_USER,

        /**
         * Perform API actions as any user in the system
         */
        SUDO,

        /**
         * Allows read-access to the repository
         */
        READ_REPOSITORY,

        /**
         * Authenticate using OpenID Connect
         */
        OPENID,

        /**
         * Allows read-only access to the user's personal information using OpenID Connect
         */
        PROFILE,

        /**
         * Allows read-only access to the user's primary email address using OpenID Connect
         */
        EMAIL;

        private static final JacksonJsonEnumHelper<ApplicationScope> enumHelper = new JacksonJsonEnumHelper<>(ApplicationScope.class);

        @JsonCreator
        public static ApplicationScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum for the search scope when doing a globalSearch() with the SearchApi.
     */
    enum SearchScope {

        PROJECTS, ISSUES, MERGE_REQUESTS, MILESTONES, SNIPPET_TITLES, SNIPPET_BLOBS, USERS,
        BLOBS, COMMITS, WIKI_BLOBS;

        private static final JacksonJsonEnumHelper<SearchScope> enumHelper = new JacksonJsonEnumHelper<>(SearchScope.class);

        @JsonCreator
        public static SearchScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum for the search scope when doing a groupSearch() with the SearchApi.
     */
    enum GroupSearchScope {

        PROJECTS, ISSUES, MERGE_REQUESTS, MILESTONES, WIKI_BLOBS, COMMITS, BLOBS, NOTES, USERS;

        private static final JacksonJsonEnumHelper<GroupSearchScope> enumHelper = new JacksonJsonEnumHelper<>(GroupSearchScope.class);

        @JsonCreator
        public static GroupSearchScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum for the search scope when doing a projectSearch() with the SearchApi.
     */
    enum ProjectSearchScope {

        BLOBS, COMMITS, ISSUES, MERGE_REQUESTS, MILESTONES, NOTES, WIKI_BLOBS, USERS;

        private static final JacksonJsonEnumHelper<ProjectSearchScope> enumHelper = new JacksonJsonEnumHelper<>(ProjectSearchScope.class);

        @JsonCreator
        public static ProjectSearchScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the action when doing a getTodos() with the TodosApi.
     */
    enum TodoAction {

        ASSIGNED, MENTIONED, BUILD_FAILED, MARKED, APPROVAL_REQUIRED, UNMERGEABLE, DIRECTLY_ADDRESSED;

        private static final JacksonJsonEnumHelper<TodoAction> enumHelper = new JacksonJsonEnumHelper<>(TodoAction.class);

        @JsonCreator
        public static TodoAction forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the state when doing a getTodos() with the TodosApi.
     */
    enum TodoState {

        PENDING, DONE;

        private static final JacksonJsonEnumHelper<TodoState> enumHelper = new JacksonJsonEnumHelper<>(TodoState.class);

        @JsonCreator
        public static TodoState forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the type when doing a getTodos() with the TodosApi.
     */
    enum TodoType {

        ISSUE, MERGE_REQUEST;

        private static final JacksonJsonEnumHelper<TodoType> enumHelper = new JacksonJsonEnumHelper<>(TodoType.class, true, true);

        @JsonCreator
        public static TodoType forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the status of a deployment.
     */
    enum DeploymentStatus {
        /**
         * After some tests, {@link #CREATED} value is not a valid value.
         */
        CREATED, RUNNING, SUCCESS, FAILED, CANCELED;

        private static final JacksonJsonEnumHelper<DeploymentStatus> enumHelper = new JacksonJsonEnumHelper<>(DeploymentStatus.class);

        @JsonCreator
        public static DeploymentStatus forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the deploy token scope.
     */
    enum DeployTokenScope {
        READ_REPOSITORY, READ_REGISTRY;

        private static final JacksonJsonEnumHelper<DeployTokenScope> enumHelper = new JacksonJsonEnumHelper<>(DeployTokenScope.class);

        @JsonCreator
        public static DeployTokenScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the project token scope.
     */
    enum ProjectAccessTokenScope {
        API, READ_API, READ_REGISTRY, WRITE_REGISTRY, READ_REPOSITORY, WRITE_REPOSITORY, CREATE_RUNNER;

        private static final JacksonJsonEnumHelper<ProjectAccessTokenScope> enumHelper = new JacksonJsonEnumHelper<>(ProjectAccessTokenScope.class);

        @JsonCreator
        public static ProjectAccessTokenScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum for the build_git_strategy of the project instance.
     */
    enum SquashOption {

        NEVER, ALWAYS, DEFAULT_ON, DEFAULT_OFF;

        private static final JacksonJsonEnumHelper<SquashOption> enumHelper = new JacksonJsonEnumHelper<>(SquashOption.class);

        @JsonCreator
        public static SquashOption forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum for the build_git_strategy of the project instance.
     */
    enum BuildGitStrategy {

        FETCH, CLONE;

        private static final JacksonJsonEnumHelper<BuildGitStrategy> enumHelper = new JacksonJsonEnumHelper<>(BuildGitStrategy.class);

        @JsonCreator
        public static BuildGitStrategy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    enum AutoDevopsDeployStrategy {
        CONTINUOUS, MANUAL, TIMED_INCREMENTAL;

        private static final JacksonJsonEnumHelper<AutoDevopsDeployStrategy> enumHelper = new JacksonJsonEnumHelper<>(AutoDevopsDeployStrategy.class);

        @JsonCreator
        public static AutoDevopsDeployStrategy forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Enum to use for specifying the Event scope.
     */
    enum EventScope {
        ALL;

        private static final JacksonJsonEnumHelper<EventScope> enumHelper = new JacksonJsonEnumHelper<>(EventScope.class);

        @JsonCreator
        public static EventScope forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Constant to specify the project_creation_level for the group.
     */
    enum ProjectCreationLevel {
        NOONE, DEVELOPER, MAINTAINER;

        private static final JacksonJsonEnumHelper<ProjectCreationLevel> enumHelper = new JacksonJsonEnumHelper<>(ProjectCreationLevel.class);

        @JsonCreator
        public static ProjectCreationLevel forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    /**
     * Constant to specify the subgroup_creation_level for the group.
     */
    enum SubgroupCreationLevel {
        OWNER, MAINTAINER;

        private static final JacksonJsonEnumHelper<SubgroupCreationLevel> enumHelper = new JacksonJsonEnumHelper<>(SubgroupCreationLevel.class);

        @JsonCreator
        public static SubgroupCreationLevel forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    enum DefaultBranchProtectionLevel {
        NOT_PROTECTED(0),
        PARTIALLY_PROTECTED(1),
        FULLY_PROTECTED(2),
        PROTECTED_AGAINST_PUSHES(3),
        FULL_PROTECTION_AFTER_INITIAL_PUSH(4);

        @JsonValue
        private final int value;

        DefaultBranchProtectionLevel(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
}

