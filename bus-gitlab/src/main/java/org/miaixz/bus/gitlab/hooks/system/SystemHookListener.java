/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       ~
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

/**
 * This interface defines an event listener for the event fired when
 * a System Hook notification has been received from a GitLab server.
 */
public interface SystemHookListener extends java.util.EventListener {

    /**
     * This method is called when a System Hook prject event has been received.
     *
     * @param event the ProjectSystemHookEvent instance
     */
    default void onProjectEvent(ProjectSystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook team member event has been received.
     *
     * @param event the TeamMemberSystemHookEvent instance containing info on the team member event
     */
    default void onTeamMemberEvent(TeamMemberSystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook user event has been received.
     *
     * @param event the UserSystemHookEvent instance containing info on the user event
     */
    default void onUserEvent(UserSystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook key event has been received.
     *
     * @param event the KeySystemHookEvent instance containing info on the key event
     */
    default void onKeyEvent(KeySystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook group event has been received.
     *
     * @param event the GroupSystemHookEvent instance containing info on the key event
     */
    default void onGroupEvent(GroupSystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook group member event has been received.
     *
     * @param event the GroupMemberSystemHookEvent instance containing info on the key event
     */
    default void onGroupMemberEvent(GroupMemberSystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook push event has been received.
     *
     * @param event the PushSystemHookEvent instance containing info on the key event
     */
    default void onPushEvent(PushSystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook tag push event has been received.
     *
     * @param event the TagPushSystemHookEvent instance containing info on the key event
     */
    default void onTagPushEvent(TagPushSystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook repository event has been received.
     *
     * @param event the RepositorySystemHookEvent instance containing info on the key event
     */
    default void onRepositoryEvent(RepositorySystemHookEvent event) {
    }

    /**
     * This method is called when a System Hook merge_request event has been received.
     *
     * @param event the MergeRequestSystemHookEvent instance containing info on the key event
     */
    default void onMergeRequestEvent(MergeRequestSystemHookEvent event) {
    }
}
