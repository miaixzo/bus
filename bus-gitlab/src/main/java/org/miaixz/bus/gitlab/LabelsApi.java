/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.Label;

import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab API project and group label calls.
 *
 * @see <a href="https://docs.gitlab.com/ce/api/labels.html">Labels API at GitLab</a>
 * @see <a href="https://docs.gitlab.com/ce/api/group_labels.html">Group Labels API at GitLab</a>
 */
public class LabelsApi extends AbstractApi {

    public LabelsApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get all labels of the specified project.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @return a list of project's labels
     * @throws GitLabApiException if any exception occurs
     */
    public List<Label> getProjectLabels(Object projectIdOrPath) throws GitLabApiException {
        return (getProjectLabels(projectIdOrPath, getDefaultPerPage()).all());
    }

    /**
     * Get a Pager of all labels of the specified project.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param itemsPerPage    the number of items per page
     * @return a list of project's labels in the specified range
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Label> getProjectLabels(Object projectIdOrPath, int itemsPerPage) throws GitLabApiException {
        return (new Pager<Label>(this, Label.class, itemsPerPage, null, "projects", getProjectIdOrPath(projectIdOrPath),
                "labels"));
    }

    /**
     * Get a Stream of all labels of the specified project.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @return a Stream of project's labels
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Label> getProjectLabelsStream(Object projectIdOrPath) throws GitLabApiException {
        return (getProjectLabels(projectIdOrPath, getDefaultPerPage()).stream());
    }

    /**
     * Get a single project label.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param labelIdOrName   the label in the form of an Long(ID), String(name), or Label instance
     * @return a Label instance holding the information for the group label
     * @throws GitLabApiException if any exception occurs
     */
    public Label getProjectLabel(Object projectIdOrPath, Object labelIdOrName) throws GitLabApiException {
        Response response = get(
                Response.Status.OK,
                null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName));
        return (response.readEntity(Label.class));
    }

    /**
     * Get a single project label as the value of an Optional.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param labelIdOrName   the label in the form of an Long(ID), String(name), or Label instance
     * @return a Optional instance with a Label instance as its value
     * @throws GitLabApiException if any exception occurs
     */
    public Optional<Label> getOptionalProjectLabel(Object projectIdOrPath, Object labelIdOrName)
            throws GitLabApiException {
        try {
            return (Optional.ofNullable(getProjectLabel(projectIdOrPath, labelIdOrName)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

    /**
     * Create a project label. A Label instance is used to set the label properties. withXXX() methods are provided to
     * set the properties of the label to create:
     * 
     * <pre>
     * <code>
     *   // name and color properties are required
     *   Label labelProperties = new Label()
     *          .withName("a-pink-project-label")
     *          .withColor("pink")
     *          .withDescription("A new pink project label")
     *       .withPriority(10);
     *   gitLabApi.getLabelsApi().createProjectLabel(projectId, labelProperties);
     * </code>
     * </pre>
     *
     * <pre>
     * <code>GitLab Endpoint: POST /groups/:id/labels</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param labelProperties a Label instance holding the properties for the new group label
     * @return the created Label instance
     * @throws GitLabApiException if any exception occurs
     */
    public Label createProjectLabel(Object projectIdOrPath, Label labelProperties) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm(labelProperties.getForm(true));
        Response response = post(
                Response.Status.CREATED,
                formData,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "labels");
        return (response.readEntity(Label.class));
    }

    /**
     * Update the specified project label. The name, color, and description can be updated. A Label instance is used to
     * set the properties of the label to update, withXXX() methods are provided to set the properties to update:
     * 
     * <pre>
     * <code>
     *   Label labelUpdates = new Label()
     *        .withName("a-new-name")
     *        .withColor("red")
     *        .withDescription("A red group label");
     *   gitLabApi.getLabelsApi().updateGroupLabel(projectId, labelId, labelUpdates);
     * </code>
     * </pre>
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /projects/:id/labels/:label_id</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param labelIdOrName   the label in the form of an Long(ID), String(name), or Label instance
     * @param labelConfig     a Label instance holding the label properties to update
     * @return the updated Label instance
     * @throws GitLabApiException if any exception occurs
     */
    public Label updateProjectLabel(Object projectIdOrPath, Object labelIdOrName, Label labelConfig)
            throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm(labelConfig.getForm(false));
        Response response = putWithFormData(
                Response.Status.OK,
                formData,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName));
        return (response.readEntity(Label.class));
    }

    /**
     * Delete the specified project label.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param labelIdOrName   the label in the form of an Long(ID), String(name), or Label instance
     * @throws GitLabApiException if any exception occurs
     */
    public void deleteProjectLabel(Object projectIdOrPath, Object labelIdOrName) throws GitLabApiException {
        delete(
                Response.Status.OK,
                null,
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName));
    }

    /**
     * Subscribe a specified project label.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param labelIdOrName   the label in the form of an Long(ID), String(name), or Label instance
     * @return HttpStatusCode 503
     * @throws GitLabApiException if any exception occurs
     */
    public Label subscribeProjectLabel(Object projectIdOrPath, Object labelIdOrName) throws GitLabApiException {
        Response response = post(
                Response.Status.NOT_MODIFIED,
                getDefaultPerPageParam(),
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName),
                "subscribe");
        return (response.readEntity(Label.class));
    }

    /**
     * Unsubscribe a specified project label.
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param labelIdOrName   the label in the form of an Long(ID), String(name), or Label instance
     * @return HttpStatusCode 503
     * @throws GitLabApiException if any exception occurs
     */
    public Label unsubscribeProjectLabel(Object projectIdOrPath, Object labelIdOrName) throws GitLabApiException {
        Response response = post(
                Response.Status.NOT_MODIFIED,
                getDefaultPerPageParam(),
                "projects",
                getProjectIdOrPath(projectIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName),
                "unsubscribe");
        return (response.readEntity(Label.class));
    }

    /**
     * Get all labels of the specified group.
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @return a list of group's labels
     * @throws GitLabApiException if any exception occurs
     */
    public List<Label> getGroupLabels(Object groupIdOrPath) throws GitLabApiException {
        return (getGroupLabels(groupIdOrPath, getDefaultPerPage()).all());
    }

    /**
     * Get a Pager of all labels of the specified group.
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @param itemsPerPage  the number of items per page
     * @return a list of group's labels in the specified range
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Label> getGroupLabels(Object groupIdOrPath, int itemsPerPage) throws GitLabApiException {
        return (new Pager<Label>(this, Label.class, itemsPerPage, null, "groups", getGroupIdOrPath(groupIdOrPath),
                "labels"));
    }

    /**
     * Get a Stream of all labels of the specified group.
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @return a Stream of group's labels
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Label> getGroupLabelsStream(Object groupIdOrPath) throws GitLabApiException {
        return (getGroupLabels(groupIdOrPath, getDefaultPerPage()).stream());
    }

    /**
     * Get a single group label.
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @param labelIdOrName the label in the form of an Long(ID), String(name), or Label instance
     * @return a Label instance holding the information for the group label
     * @throws GitLabApiException if any exception occurs
     */
    public Label getGroupLabel(Object groupIdOrPath, Object labelIdOrName) throws GitLabApiException {
        Response response = get(
                Response.Status.OK,
                null,
                "groups",
                getGroupIdOrPath(groupIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName));
        return (response.readEntity(Label.class));
    }

    /**
     * Get a single group label as the value of an Optional.
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @param labelIdOrName the label in the form of an Long(ID), String(name), or Label instance
     * @return a Optional instance with a Label instance as its value
     * @throws GitLabApiException if any exception occurs
     */
    public Optional<Label> getOptionalGroupLabel(Object groupIdOrPath, Object labelIdOrName) throws GitLabApiException {
        try {
            return (Optional.ofNullable(getGroupLabel(groupIdOrPath, labelIdOrName)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

    /**
     * Create a group label. A Label instance is used to set the label properties. withXXX() methods are provided to set
     * the properties of the label to create:
     * 
     * <pre>
     * <code>
     *   Label labelProperties = new Label()
     *       .withName("a-name")
     *       .withColor("green")
     *       .withDescription("A new green group label");
     *   gitLabApi.getLabelsApi().createGroupLabel(projectId, labelProperties);
     * </code>
     * </pre>
     *
     * <pre>
     * <code>GitLab Endpoint: POST /groups/:id/labels</code>
     * </pre>
     *
     * @param groupIdOrPath   the group in the form of an Long(ID), String(path), or Group instance
     * @param labelProperties a Label instance holding the properties for the new group label
     * @return the created Label instance
     * @throws GitLabApiException if any exception occurs
     */
    public Label createGroupLabel(Object groupIdOrPath, Label labelProperties) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm(labelProperties.getForm(true));
        Response response = post(
                Response.Status.CREATED,
                formData,
                "groups",
                getGroupIdOrPath(groupIdOrPath),
                "labels");
        return (response.readEntity(Label.class));
    }

    /**
     * Update the specified label. The name, color, and description can be updated. A Label instance is used to set the
     * properties of the label to update, withXXX() methods are provided to set the properties to update:
     * 
     * <pre>
     * <code>
     *   Label labelUpdates = new Label()
     *       .withName("a-new-name")
     *       .withColor("red")
     *       .withDescription("A red group label");
     *   gitLabApi.getLabelsApi().updateGroupLabel(projectId, labelId, labelUpdates);
     * </code>
     * </pre>
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /groups/:id/labels/:label_id</code>
     * </pre>
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @param labelIdOrName the label in the form of an Long(ID), String(name), or Label instance
     * @param labelConfig   a Label instance holding the label properties to update
     * @return the updated Label instance
     * @throws GitLabApiException if any exception occurs
     */
    public Label updateGroupLabel(Object groupIdOrPath, Object labelIdOrName, Label labelConfig)
            throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm(labelConfig.getForm(false));
        Response response = putWithFormData(
                Response.Status.OK,
                formData,
                "groups",
                getGroupIdOrPath(groupIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName));
        return (response.readEntity(Label.class));
    }

    /**
     * Delete the specified label
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @param labelIdOrName the label in the form of an Long(ID), String(name), or Label instance
     * @throws GitLabApiException if any exception occurs
     */
    public void deleteGroupLabel(Object groupIdOrPath, Object labelIdOrName) throws GitLabApiException {
        delete(
                Response.Status.OK,
                null,
                "groups",
                getGroupIdOrPath(groupIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName));
    }

    /**
     * Subscribe a specified group label.
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @param labelIdOrName the label in the form of an Long(ID), String(name), or Label instance
     * @return HttpStatusCode 503
     * @throws GitLabApiException if any exception occurs
     */
    public Label subscribeGroupLabel(Object groupIdOrPath, Object labelIdOrName) throws GitLabApiException {
        Response response = post(
                Response.Status.NOT_MODIFIED,
                getDefaultPerPageParam(),
                "groups",
                getGroupIdOrPath(groupIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName),
                "subscribe");
        return (response.readEntity(Label.class));
    }

    /**
     * Unsubscribe a specified group label.
     *
     * @param groupIdOrPath the group in the form of an Long(ID), String(path), or Group instance
     * @param labelIdOrName the label in the form of an Long(ID), String(name), or Label instance
     * @return HttpStatusCode 503
     * @throws GitLabApiException if any exception occurs
     */
    public Label unsubscribeGroupLabel(Object groupIdOrPath, Object labelIdOrName) throws GitLabApiException {
        Response response = post(
                Response.Status.NOT_MODIFIED,
                getDefaultPerPageParam(),
                "groups",
                getGroupIdOrPath(groupIdOrPath),
                "labels",
                getLabelIdOrName(labelIdOrName),
                "unsubscribe");
        return (response.readEntity(Label.class));
    }

}
