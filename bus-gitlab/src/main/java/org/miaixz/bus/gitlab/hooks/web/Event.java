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
package org.miaixz.bus.gitlab.hooks.web;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "object_kind")
@JsonSubTypes({ @JsonSubTypes.Type(value = BuildEvent.class, name = BuildEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = IssueEvent.class, name = IssueEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = WorkItemEvent.class, name = WorkItemEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = JobEvent.class, name = JobEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = MergeRequestEvent.class, name = MergeRequestEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = NoteEvent.class, name = NoteEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = PipelineEvent.class, name = PipelineEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = PushEvent.class, name = PushEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = TagPushEvent.class, name = TagPushEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = WikiPageEvent.class, name = WikiPageEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = DeploymentEvent.class, name = DeploymentEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = ReleaseEvent.class, name = ReleaseEvent.OBJECT_KIND) })
public interface Event extends Serializable {

    String getObjectKind();

    void setRequestUrl(String url);

    @JsonIgnore
    String getRequestUrl();

    void setRequestQueryString(String queryString);

    @JsonIgnore
    String getRequestQueryString();

    void setRequestSecretToken(String secretToken);

    @JsonIgnore
    String getRequestSecretToken();

}
