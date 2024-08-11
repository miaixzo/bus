/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab;

import jakarta.ws.rs.core.Response;

import org.miaixz.bus.gitlab.models.NotificationSettings;
import org.miaixz.bus.gitlab.models.NotificationSettings.Events;

public class NotificationSettingsApi extends AbstractApi {

    public NotificationSettingsApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get the global notification settings.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /notification_settings</code>
     * </pre>
     *
     * @return a NotificationSettings instance containing the global notification settings
     * @throws GitLabApiException if any exception occurs
     */
    public NotificationSettings getGlobalNotificationSettings() throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "notification_settings");
        return (response.readEntity(NotificationSettings.class));
    }

    /**
     * Update the global notification settings.
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /notification_settings</code>
     * </pre>
     *
     * @param settings a NotificationSettings instance with the new settings
     * @return a NotificationSettings instance containing the updated global notification settings
     * @throws GitLabApiException if any exception occurs
     */
    public NotificationSettings updateGlobalNotificationSettings(NotificationSettings settings)
            throws GitLabApiException {

        GitLabApiForm formData = new GitLabApiForm().withParam("level", settings.getLevel()).withParam("email",
                settings.getEmail());

        Events events = settings.getEvents();
        if (events != null) {
            formData.withParam("new_note", events.getNewNote()).withParam("new_issue", events.getNewIssue())
                    .withParam("reopen_issue", events.getReopenIssue()).withParam("close_issue", events.getCloseIssue())
                    .withParam("reassign_issue", events.getReassignIssue())
                    .withParam("new_merge_request", events.getNewMergeRequest())
                    .withParam("reopen_merge_request", events.getReopenMergeRequest())
                    .withParam("close_merge_request", events.getCloseMergeRequest())
                    .withParam("reassign_merge_request", events.getReassignMergeRequest())
                    .withParam("merge_merge_request", events.getMergeMergeRequest())
                    .withParam("failed_pipeline", events.getFailedPipeline())
                    .withParam("success_pipeline", events.getSuccessPipeline());
        }

        Response response = put(Response.Status.OK, formData.asMap(), "notification_settings");
        return (response.readEntity(NotificationSettings.class));
    }

    /**
     * Get the notification settings for a group.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /groups/:id/notification_settings</code>
     * </pre>
     *
     * @param groupId the group ID to get the notification settings for
     * @return a NotificationSettings instance containing the specified group's notification settings
     * @throws GitLabApiException if any exception occurs
     */
    public NotificationSettings getGroupNotificationSettings(long groupId) throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "groups", groupId, "notification_settings");
        return (response.readEntity(NotificationSettings.class));
    }

    /**
     * Update the notification settings for a group
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /groups/:id/notification_settings</code>
     * </pre>
     *
     * @param groupId  the group ID to update the notification settings for
     * @param settings a NotificationSettings instance with the new settings
     * @return a NotificationSettings instance containing the updated group notification settings
     * @throws GitLabApiException if any exception occurs
     */
    public NotificationSettings updateGroupNotificationSettings(long groupId, NotificationSettings settings)
            throws GitLabApiException {

        GitLabApiForm formData = new GitLabApiForm().withParam("level", settings.getLevel()).withParam("email",
                settings.getEmail());

        Events events = settings.getEvents();
        if (events != null) {
            formData.withParam("new_note", events.getNewNote()).withParam("new_issue", events.getNewIssue())
                    .withParam("reopen_issue", events.getReopenIssue()).withParam("close_issue", events.getCloseIssue())
                    .withParam("reassign_issue", events.getReassignIssue())
                    .withParam("new_merge_request", events.getNewMergeRequest())
                    .withParam("reopen_merge_request", events.getReopenMergeRequest())
                    .withParam("close_merge_request", events.getCloseMergeRequest())
                    .withParam("reassign_merge_request", events.getReassignMergeRequest())
                    .withParam("merge_merge_request", events.getMergeMergeRequest())
                    .withParam("failed_pipeline", events.getFailedPipeline())
                    .withParam("success_pipeline", events.getSuccessPipeline());
        }

        Response response = put(Response.Status.OK, formData.asMap(), "groups", groupId, "notification_settings");
        return (response.readEntity(NotificationSettings.class));
    }

    /**
     * Get the notification settings for a project.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/notification_settings</code>
     * </pre>
     *
     * @param projectId the project ID to get the notification settings for
     * @return a NotificationSettings instance containing the specified project's notification settings
     * @throws GitLabApiException if any exception occurs
     */
    public NotificationSettings getProjectNotificationSettings(long projectId) throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "projects", projectId, "notification_settings");
        return (response.readEntity(NotificationSettings.class));
    }

    /**
     * Update the notification settings for a project
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /projects/:id/notification_settings</code>
     * </pre>
     *
     * @param projectId the project ID to update the notification settings for
     * @param settings  a NotificationSettings instance with the new settings
     * @return a NotificationSettings instance containing the updated project notification settings
     * @throws GitLabApiException if any exception occurs
     */
    public NotificationSettings updateProjectNotificationSettings(long projectId, NotificationSettings settings)
            throws GitLabApiException {

        GitLabApiForm formData = new GitLabApiForm().withParam("level", settings.getLevel()).withParam("email",
                settings.getEmail());

        Events events = settings.getEvents();
        if (events != null) {
            formData.withParam("new_note", events.getNewNote()).withParam("new_issue", events.getNewIssue())
                    .withParam("reopen_issue", events.getReopenIssue()).withParam("close_issue", events.getCloseIssue())
                    .withParam("reassign_issue", events.getReassignIssue())
                    .withParam("new_merge_request", events.getNewMergeRequest())
                    .withParam("reopen_merge_request", events.getReopenMergeRequest())
                    .withParam("close_merge_request", events.getCloseMergeRequest())
                    .withParam("reassign_merge_request", events.getReassignMergeRequest())
                    .withParam("merge_merge_request", events.getMergeMergeRequest())
                    .withParam("failed_pipeline", events.getFailedPipeline())
                    .withParam("success_pipeline", events.getSuccessPipeline());
        }

        Response response = put(Response.Status.OK, formData.asMap(), "projects", projectId, "notification_settings");
        return (response.readEntity(NotificationSettings.class));
    }

}
