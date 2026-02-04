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
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.Todo;

import jakarta.ws.rs.core.Response;

/**
 * This class implements the client side API for the GitLab Todos API.
 */
public class TodosApi extends AbstractApi {

    public TodosApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get a List of pending todos for the current user.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @return a List of pending Todos for the current user
     * @throws GitLabApiException if any exception occurs
     */
    public List<Todo> getPendingTodos() throws GitLabApiException {
        return (getTodos(null, null, null, null, TodoState.PENDING, null, getDefaultPerPage()).all());
    }

    /**
     * Get a Pager of pending todos for the current user.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @param itemsPerPage the number of todo that will be fetched per page
     * @return a Pager containing the pending Todos for the current user
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Todo> getPendingTodos(int itemsPerPage) throws GitLabApiException {
        return (getTodos(null, null, null, null, TodoState.PENDING, null, itemsPerPage));
    }

    /**
     * Get a Stream of pending todos for the current user.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @return a Stream containing the pending Todos for the user
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Todo> getPendingTodosStream() throws GitLabApiException {
        return (getTodos(null, null, null, null, TodoState.PENDING, null, getDefaultPerPage()).stream());
    }

    /**
     * Get a List of done todos for the current user.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @return a List of done Todos for the current user
     * @throws GitLabApiException if any exception occurs
     */
    public List<Todo> getDoneTodos() throws GitLabApiException {
        return (getTodos(null, null, null, null, TodoState.DONE, null, getDefaultPerPage()).all());
    }

    /**
     * Get a Pager of done todos for the current user.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @param itemsPerPage the number of todo that will be fetched per page
     * @return a Pager containing the done Todos for the current user
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Todo> getDoneTodos(int itemsPerPage) throws GitLabApiException {
        return (getTodos(null, null, null, null, TodoState.DONE, null, itemsPerPage));
    }

    /**
     * Get a Stream of done todos for the current user.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @return a Stream containing the done Todos for the current user
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Todo> getDoneTodosStream() throws GitLabApiException {
        return (getTodos(null, null, null, null, TodoState.DONE, null, getDefaultPerPage()).stream());
    }

    /**
     * Get a List of all todos that match the provided filter params.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @param action    the action to be filtered. Can be assigned, mentioned, build_failed, marked, approval_required,
     *                  unmergeable or directly_addressed.
     * @param authorId  the ID of an author
     * @param projectId the ID of a project
     * @param groupId   the ID of a group
     * @param state     the state of the todo. Can be either pending or done
     * @param type      the type of a todo. Can be either Issue or MergeRequest
     * @return Stream of Todo instances
     * @throws GitLabApiException if any exception occurs
     */
    public List<Todo> getTodos(
            TodoAction action,
            Long authorId,
            Long projectId,
            Long groupId,
            TodoState state,
            TodoType type) throws GitLabApiException {
        return (getTodos(action, authorId, projectId, groupId, state, type, getDefaultPerPage()).all());
    }

    /**
     * Get a List of all todos that match the provided filter params.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @param action    the action to be filtered. Can be assigned, mentioned, build_failed, marked, approval_required,
     *                  unmergeable or directly_addressed.
     * @param authorId  the ID of an author
     * @param projectId the ID of a project
     * @param groupId   the ID of a group
     * @param state     the state of the todo. Can be either pending or done
     * @param type      the type of a todo. Can be either Issue or MergeRequest
     * @return Stream of Todo instances
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Todo> getTodosStream(
            TodoAction action,
            Long authorId,
            Long projectId,
            Long groupId,
            TodoState state,
            TodoType type) throws GitLabApiException {
        return (getTodos(action, authorId, projectId, groupId, state, type, getDefaultPerPage()).stream());
    }

    /**
     * Returns a Pager of todos that match the provided filter params. When no filter params are provided, will returns
     * all pending todos for the current user.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /todos</code>
     * </pre>
     *
     * @param action       the action to be filtered. Can be assigned, mentioned, build_failed, marked,
     *                     approval_required, unmergeable or directly_addressed.
     * @param authorId     the ID of an author
     * @param projectId    the ID of a project
     * @param groupId      the ID of a group
     * @param state        the state of the todo. Can be either pending or done
     * @param type         the type of a todo. Can be either Issue or MergeRequest
     * @param itemsPerPage the number of todo that will be fetched per page
     * @return a list of pages in todo for the specified range
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Todo> getTodos(
            TodoAction action,
            Long authorId,
            Long projectId,
            Long groupId,
            TodoState state,
            TodoType type,
            int itemsPerPage) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("action", action, false)
                .withParam("author_id", authorId, false).withParam("project_id", projectId, false)
                .withParam("group_id", groupId, false).withParam("state", state, false).withParam("type", type, false);
        return (new Pager<Todo>(this, Todo.class, itemsPerPage, formData.asMap(), "todos"));
    }

    /**
     * Marks a single pending todo given by its ID for the current user as done. The todo marked as done is returned in
     * the response.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /todos/:id/mark_as_done</code>
     * </pre>
     *
     * @param todoId the ID of a todo
     * @return todo instance with info on the created page
     * @throws GitLabApiException if any exception occurs
     */
    public Todo markAsDone(Long todoId) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm();
        Response response = post(Response.Status.OK, formData, "todos", todoId, "mark_as_done");
        return (response.readEntity(Todo.class));
    }

    /**
     * Marks all pending todos for the current user as done.
     *
     * <pre>
     * <code>GitLab Endpoint: POST /todos/mark_as_done</code>
     * </pre>
     *
     * @throws GitLabApiException if any exception occurs
     */
    public void markAllAsDone() throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm();
        post(Response.Status.NO_CONTENT, formData, "todos", "mark_as_done");
    }

}
