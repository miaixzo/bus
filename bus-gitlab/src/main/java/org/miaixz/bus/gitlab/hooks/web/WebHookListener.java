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
     * This method is called when a WebHook release event has been received.
     *
     * @param releaseEvent the ReleaseEvent instance
     */
    default void onReleaseEvent(ReleaseEvent releaseEvent) {
    }

}
