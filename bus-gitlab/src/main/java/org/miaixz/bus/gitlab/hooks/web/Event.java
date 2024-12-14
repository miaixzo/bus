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
package org.miaixz.bus.gitlab.hooks.web;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "object_kind")
@JsonSubTypes({ @JsonSubTypes.Type(value = BuildEvent.class, name = BuildEvent.OBJECT_KIND),
        @JsonSubTypes.Type(value = IssueEvent.class, name = IssueEvent.OBJECT_KIND),
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

    @JsonIgnore
    String getRequestUrl();

    void setRequestUrl(String url);

    @JsonIgnore
    String getRequestQueryString();

    void setRequestQueryString(String queryString);

    @JsonIgnore
    String getRequestSecretToken();

    void setRequestSecretToken(String secretToken);

}
