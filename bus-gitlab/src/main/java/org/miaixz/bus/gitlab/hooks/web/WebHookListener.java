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

/**
 * This interface defines an event listener for the event fired when a WebHook notification has been received from a
 * GitLab server.
 */
public interface WebHookListener extends java.util.EventListener {

    /**
     * This method is called when a WebHook build event has been received.
     *
     * @param buildEvent the BuildEvent instance
     */
    default void onBuildEvent(BuildEvent buildEvent) {
    }

    /**
     * This method is called when a WebHook issue event has been received.
     *
     * @param event the EventObject instance containing info on the issue
     */
    default void onIssueEvent(IssueEvent event) {
    }

    /**
     * This method is called when a WebHook job event has been received.
     *
     * @param jobEvent the JobEvent instance
     */
    default void onJobEvent(JobEvent jobEvent) {
    }

    /**
     * This method is called when a WebHook merge request event has been received
     *
     * @param event the EventObject instance containing info on the merge request
     */
    default void onMergeRequestEvent(MergeRequestEvent event) {
    }

    /**
     * This method is called when a WebHook note event has been received.
     *
     * @param noteEvent theNoteEvent instance
     */
    default void onNoteEvent(NoteEvent noteEvent) {
    }

    /**
     * This method is called when a WebHook pipeline event has been received.
     *
     * @param pipelineEvent the PipelineEvent instance
     */
    default void onPipelineEvent(PipelineEvent pipelineEvent) {
    }

    /**
     * This method is called when a WebHook push event has been received.
     *
     * @param pushEvent the PushEvent instance
     */
    default void onPushEvent(PushEvent pushEvent) {
    }

    /**
     * This method is called when a WebHook tag push event has been received.
     *
     * @param tagPushEvent the TagPushEvent instance
     */
    default void onTagPushEvent(TagPushEvent tagPushEvent) {
    }

    /**
     * This method is called when a WebHook wiki page event has been received.
     *
     * @param wikiEvent the WikiPageEvent instance
     */
    default void onWikiPageEvent(WikiPageEvent wikiEvent) {
    }

    /**
     * This method is called when a WebHook deployment event has been received.
     *
     * @param deploymentEvent the DeploymentEvent instance
     */
    default void onDeploymentEvent(DeploymentEvent deploymentEvent) {
    }

    /**
     * This method is called when a WebHook work item event has been received.
     *
     * @param workItemEvent the WorkItemEvent instance
     */
    default void onWorkItemEvent(WorkItemEvent workItemEvent) {
    }

    /**
     * This method is called when a WebHook release event has been received.
     *
     * @param releaseEvent the ReleaseEvent instance
     */
    default void onReleaseEvent(ReleaseEvent releaseEvent) {
    }

}
