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

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.miaixz.bus.gitlab.models.Constants.TodoAction;
import org.miaixz.bus.gitlab.models.Constants.TodoState;
import org.miaixz.bus.gitlab.models.Constants.TodoType;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The todo class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Todo implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282035620L;

    private Long id;
    private Project project;
    private Author author;
    private TodoAction actionName;
    private TodoType targetType;

    @JsonDeserialize(using = TargetDeserializer.class)
    private Object target;

    private String targetUrl;
    private String body;
    private TodoState state;
    private Date createdAt;

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
     * Returns the project.
     *
     * @return the result
     */

    public Project getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Returns the author.
     *
     * @return the result
     */

    public Author getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author the author value
     */

    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * Returns the action name.
     *
     * @return the result
     */

    public TodoAction getActionName() {
        return actionName;
    }

    /**
     * Sets the action name.
     *
     * @param actionName the action name value
     */

    public void setActionName(TodoAction actionName) {
        this.actionName = actionName;
    }

    /**
     * Returns the target type.
     *
     * @return the result
     */

    public TodoType getTargetType() {
        return targetType;
    }

    /**
     * Sets the target type.
     *
     * @param targetType the target type value
     */

    public void setTargetType(TodoType targetType) {
        this.targetType = targetType;
    }

    /**
     * Returns the target url.
     *
     * @return the result
     */

    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * Sets the target url.
     *
     * @param targetUrl the target url value
     */

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    /**
     * Returns the target.
     *
     * @return the result
     */

    public Object getTarget() {
        return target;
    }

    /**
     * Sets the target.
     *
     * @param target the target value
     */

    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * Returns the body.
     *
     * @return the result
     */

    public String getBody() {
        return body;
    }

    /**
     * Sets the body.
     *
     * @param body the body value
     */

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public TodoState getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(TodoState state) {
        this.state = state;
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
     * Returns the issue target.
     *
     * @return the result
     */

    @JsonIgnore
    public Issue getIssueTarget() {
        return (targetType == TodoType.ISSUE ? (Issue) target : null);
    }

    /**
     * Returns the merge request target.
     *
     * @return the result
     */

    @JsonIgnore
    public MergeRequest getMergeRequestTarget() {
        return (targetType == TodoType.MERGE_REQUEST ? (MergeRequest) target : null);
    }

    /**
     * Returns whether the issue todo is enabled.
     *
     * @return the result
     */

    @JsonIgnore
    public boolean isIssueTodo() {
        return (targetType == TodoType.ISSUE);
    }

    /**
     * Returns whether the merge request todo is enabled.
     *
     * @return the result
     */

    @JsonIgnore
    public boolean isMergeRequestTodo() {
        return (targetType == TodoType.MERGE_REQUEST);
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

    // This deserializer will determine the target type and deserialize to the correct class (either MergeRequest or
    // Issue).
    /**
     * The target deserializer class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class TargetDeserializer extends JsonDeserializer<Object> {

        /**
         * Executes the deserialize operation.
         *
         * @param jp      the jp value
         * @param context the context value
         * @return the result
         * @throws IOException             if the operation fails
         * @throws JsonProcessingException if the operation fails
         */

        @Override
        public Object deserialize(JsonParser jp, DeserializationContext context)
                throws IOException, JsonProcessingException {

            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            ObjectNode root = (ObjectNode) mapper.readTree(jp);
            boolean isMergeRequestTarget = root.has("source_branch");
            if (isMergeRequestTarget) {
                return mapper.treeToValue(root, MergeRequest.class);
            } else {
                return mapper.treeToValue(root, Issue.class);
            }
        }

    }

}
