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
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;
import java.io.Serial;

/**
 * This class is utilized by the <code>org.miaixz.bus.gitlab.TopicsApi#createTopic(TopicParams)</code> and
 * <code>org.miaixz.bus.gitlab.TopicsApi#updateTopic(Integer, TopicParams)</code> methods to set the parameters for the
 * call to the GitLab API.
 *
 * Avatar Upload has its own Upload in <code>org.miaixz.bus.gitlab.TopicsApi#updateTopicAvatar(Integer,File)</code>
 */
public class TopicParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282251187L;

    private String name;
    private String title;
    private String description;

    public TopicParams withName(String name) {
        this.name = name;
        return (this);
    }

    public TopicParams withTitle(String title) {
        this.title = title;
        return (this);
    }

    public TopicParams withDescription(String description) {
        this.description = description;
        return (this);
    }

    /**
     * Get the form params for a group create oir update call.
     *
     * @param isCreate set to true for a create group call, false for update
     * @return a GitLabApiForm instance holding the parameters for the group create or update operation
     * @throws RuntimeException if required parameters are missing
     */
    public GitLabForm getForm(boolean isCreate) {

        GitLabForm form = new GitLabForm().withParam("name", name, isCreate).withParam("title", title, isCreate)
                .withParam("description", description);

        return (form);
    }

}
