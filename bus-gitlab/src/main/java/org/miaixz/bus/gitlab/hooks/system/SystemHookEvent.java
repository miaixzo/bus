/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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
package org.miaixz.bus.gitlab.hooks.system;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serial;

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

    String getEventName();

    void setRequestUrl(String requestUrl);

    @JsonIgnore
    String getRequestUrl();

    void setRequestQueryString(String requestQueryString);

    @JsonIgnore
    String getRequestQueryString();

    void setRequestSecretToken(String requestSecretToken);

    @JsonIgnore
    String getRequestSecretToken();
}

// All of the following class definitions are needed to make the above work.
// Jackson has a tough time mapping the same class to multiple IDs
class CreateProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228078820L;
}

class DestroyProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228251132L;
}

class RenameProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228370569L;
}

class TransferProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228576607L;
}

class UpdateProjectSystemHookEvent extends ProjectSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228633920L;
}

class NewTeamMemberSystemHookEvent extends TeamMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228695353L;
}

class RemoveTeamMemberSystemHookEvent extends TeamMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228711072L;
}

class CreateUserSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228769923L;
}

class DestroyUserSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228833780L;
}

class RenameUserSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852228918310L;
}

class UserFailedLoginSystemHookEvent extends UserSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229056099L;
}

class CreateKeySystemHookEvent extends KeySystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229066130L;
}

class DestroyKeySystemHookEvent extends KeySystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229156110L;
}

class CreateGroupSystemHookEvent extends GroupSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229213926L;
}

class DestroyGroupSystemHookEvent extends GroupSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229266109L;
}

class RenameGroupSystemHookEvent extends GroupSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229331677L;
}

class NewGroupMemberSystemHookEvent extends GroupMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229395085L;
}

class RemoveGroupMemberSystemHookEvent extends GroupMemberSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229651810L;

}
