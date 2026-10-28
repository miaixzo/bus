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
package org.miaixz.bus.gitlab.hooks.system;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The system hook event interface.
 *
 * @author Kimi Liu
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "event_name")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateProjectSystemHookEvent.class, name = ProjectSystemHookEvent.PROJECT_CREATE_EVENT),
        @JsonSubTypes.Type(value = DestroyProjectSystemHookEvent.class, name = ProjectSystemHookEvent.PROJECT_DESTROY_EVENT),
        @JsonSubTypes.Type(value = RenameProjectSystemHookEvent.class, name = ProjectSystemHookEvent.PROJECT_RENAME_EVENT),
        @JsonSubTypes.Type(value = TransferProjectSystemHookEvent.class, name = ProjectSystemHookEvent.PROJECT_TRANSFER_EVENT),
        @JsonSubTypes.Type(value = UpdateProjectSystemHookEvent.class, name = ProjectSystemHookEvent.PROJECT_UPDATE_EVENT),
        @JsonSubTypes.Type(value = NewTeamMemberSystemHookEvent.class, name = TeamMemberSystemHookEvent.NEW_TEAM_MEMBER_EVENT),
        @JsonSubTypes.Type(value = RemoveTeamMemberSystemHookEvent.class, name = TeamMemberSystemHookEvent.TEAM_MEMBER_REMOVED_EVENT),
        @JsonSubTypes.Type(value = CreateUserSystemHookEvent.class, name = UserSystemHookEvent.USER_CREATE_EVENT),
        @JsonSubTypes.Type(value = DestroyUserSystemHookEvent.class, name = UserSystemHookEvent.USER_DESTROY_EVENT),
        @JsonSubTypes.Type(value = UserFailedLoginSystemHookEvent.class, name = UserSystemHookEvent.USER_FAILED_LOGIN_EVENT),
        @JsonSubTypes.Type(value = RenameUserSystemHookEvent.class, name = UserSystemHookEvent.USER_RENAME_EVENT),
        @JsonSubTypes.Type(value = CreateKeySystemHookEvent.class, name = KeySystemHookEvent.KEY_CREATE_EVENT),
        @JsonSubTypes.Type(value = DestroyKeySystemHookEvent.class, name = KeySystemHookEvent.KEY_DESTROY_EVENT),
        @JsonSubTypes.Type(value = CreateGroupSystemHookEvent.class, name = GroupSystemHookEvent.GROUP_CREATE_EVENT),
        @JsonSubTypes.Type(value = DestroyGroupSystemHookEvent.class, name = GroupSystemHookEvent.GROUP_DESTROY_EVENT),
        @JsonSubTypes.Type(value = RenameGroupSystemHookEvent.class, name = GroupSystemHookEvent.GROUP_RENAME_EVENT),
        @JsonSubTypes.Type(value = NewGroupMemberSystemHookEvent.class, name = GroupMemberSystemHookEvent.NEW_GROUP_MEMBER_EVENT),
        @JsonSubTypes.Type(value = RemoveGroupMemberSystemHookEvent.class, name = GroupMemberSystemHookEvent.GROUP_MEMBER_REMOVED_EVENT),
        @JsonSubTypes.Type(value = PushSystemHookEvent.class, name = PushSystemHookEvent.PUSH_EVENT),
        @JsonSubTypes.Type(value = TagPushSystemHookEvent.class, name = TagPushSystemHookEvent.TAG_PUSH_EVENT),
        @JsonSubTypes.Type(value = RepositorySystemHookEvent.class, name = RepositorySystemHookEvent.REPOSITORY_UPDATE_EVENT),
        @JsonSubTypes.Type(value = MergeRequestSystemHookEvent.class, name = MergeRequestSystemHookEvent.MERGE_REQUEST_EVENT) })
public interface SystemHookEvent extends Serializable {

    /**
     * Returns the system hook event name.
     *
     * @return event name
     */
    String getEventName();

    /**
     * Returns the request url.
     *
     * @return the result
     */

    @JsonIgnore
    String getRequestUrl();

    /**
     * Sets the request url.
     *
     * @param requestUrl the request url value
     */
    void setRequestUrl(String requestUrl);

    /**
     * Returns the request query string.
     *
     * @return the result
     */

    @JsonIgnore
    String getRequestQueryString();

    /**
     * Sets the request query string.
     *
     * @param requestQueryString the request query string value
     */
    void setRequestQueryString(String requestQueryString);

    /**
     * Returns the request secret token.
     *
     * @return the result
     */

    @JsonIgnore
    String getRequestSecretToken();

    /**
     * Sets the request secret token.
     *
     * @param requestSecretToken the request secret token value
     */
    void setRequestSecretToken(String requestSecretToken);

}

// All of the following class definitions are needed to make the above work.
// Jackson has a tough time mapping the same class to multiple IDs

/**
 * The create project system hook event class.
 *
 * @author Kimi Liu
 */
class CreateProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550005502321L;

}

/**
 * The destroy project system hook event class.
 *
 * @author Kimi Liu
 */
class DestroyProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550008663801L;

}

/**
 * The rename project system hook event class.
 *
 * @author Kimi Liu
 */
class RenameProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550010806231L;

}

/**
 * The transfer project system hook event class.
 *
 * @author Kimi Liu
 */
class TransferProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550013968001L;

}

/**
 * The update project system hook event class.
 *
 * @author Kimi Liu
 */
class UpdateProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550018001298L;

}

/**
 * The new team member system hook event class.
 *
 * @author Kimi Liu
 */
class NewTeamMemberSystemHookEvent extends TeamMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550020163306L;

}

/**
 * The remove team member system hook event class.
 *
 * @author Kimi Liu
 */
class RemoveTeamMemberSystemHookEvent extends TeamMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550022500001L;

}

/**
 * The create user system hook event class.
 *
 * @author Kimi Liu
 */
class CreateUserSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550025661629L;

}

/**
 * The destroy user system hook event class.
 *
 * @author Kimi Liu
 */
class DestroyUserSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550028803292L;

}

/**
 * The rename user system hook event class.
 *
 * @author Kimi Liu
 */
class RenameUserSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550030966011L;

}

/**
 * The user failed login system hook event class.
 *
 * @author Kimi Liu
 */
class UserFailedLoginSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550033208001L;

}

/**
 * The create key system hook event class.
 *
 * @author Kimi Liu
 */
class CreateKeySystemHookEvent extends KeySystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550036500001L;

}

/**
 * The destroy key system hook event class.
 *
 * @author Kimi Liu
 */
class DestroyKeySystemHookEvent extends KeySystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550050001261L;

}

/**
 * The create group system hook event class.
 *
 * @author Kimi Liu
 */
class CreateGroupSystemHookEvent extends GroupSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550052163801L;

}

/**
 * The destroy group system hook event class.
 *
 * @author Kimi Liu
 */
class DestroyGroupSystemHookEvent extends GroupSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550055315001L;

}

/**
 * The rename group system hook event class.
 *
 * @author Kimi Liu
 */
class RenameGroupSystemHookEvent extends GroupSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550058566052L;

}

/**
 * The new group member system hook event class.
 *
 * @author Kimi Liu
 */
class NewGroupMemberSystemHookEvent extends GroupMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550060818501L;

}

/**
 * The remove group member system hook event class.
 *
 * @author Kimi Liu
 */
class RemoveGroupMemberSystemHookEvent extends GroupMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2550062969562L;

}
