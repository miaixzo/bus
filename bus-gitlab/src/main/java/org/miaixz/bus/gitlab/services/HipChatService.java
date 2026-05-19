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
package org.miaixz.bus.gitlab.services;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.models.GitLabForm;

/**
 * The hip chat service class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HipChatService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852285062156L;
    /**
     * The token prop value.
     */

    public static final String TOKEN_PROP = "token";
    /**
     * The color prop value.
     */
    public static final String COLOR_PROP = "color";
    /**
     * The notify prop value.
     */
    public static final String NOTIFY_PROP = "notify";
    /**
     * The room prop value.
     */
    public static final String ROOM_PROP = "room";
    /**
     * The api version prop value.
     */
    public static final String API_VERSION_PROP = "api_version";
    /**
     * The server prop value.
     */
    public static final String SERVER_PROP = "server";

    /**
     * Get the form data for this service based on it's properties.
     *
     * @return the form data for this service based on it's properties
     */
    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam("push_events", getPushEvents())
                .withParam("issues_events", getIssuesEvents())
                .withParam("confidential_issues_events", getConfidentialIssuesEvents())
                .withParam("merge_requests_events", getMergeRequestsEvents())
                .withParam("tag_push_events", getTagPushEvents()).withParam("note_events", getNoteEvents())
                .withParam("confidential_note_events", getConfidentialNoteEvents())
                .withParam("pipeline_events", getPipelineEvents()).withParam("token", getToken(), true)
                .withParam("color", getColor()).withParam("notify", getNotify()).withParam("room", getRoom())
                .withParam("api_version", getApiVersion()).withParam("server", getServer())
                .withParam("notify_only_broken_pipelines", getNotifyOnlyBrokenPipelines());
        return formData;
    }

    /**
     * Sets the push events and returns this instance.
     *
     * @param pushEvents the push events value
     * @return the result
     */

    public HipChatService withPushEvents(Boolean pushEvents) {
        return withPushEvents(pushEvents, this);
    }

    /**
     * Sets the issues events and returns this instance.
     *
     * @param issuesEvents the issues events value
     * @return the result
     */

    public HipChatService withIssuesEvents(Boolean issuesEvents) {
        return withIssuesEvents(issuesEvents, this);
    }

    /**
     * Sets the confidential issues events and returns this instance.
     *
     * @param confidentialIssuesEvents the confidential issues events value
     * @return the result
     */

    public HipChatService withConfidentialIssuesEvents(Boolean confidentialIssuesEvents) {
        return withConfidentialIssuesEvents(confidentialIssuesEvents, this);
    }

    /**
     * Sets the merge requests events and returns this instance.
     *
     * @param mergeRequestsEvents the merge requests events value
     * @return the result
     */

    public HipChatService withMergeRequestsEvents(Boolean mergeRequestsEvents) {
        return withMergeRequestsEvents(mergeRequestsEvents, this);
    }

    /**
     * Sets the tag push events and returns this instance.
     *
     * @param tagPushEvents the tag push events value
     * @return the result
     */

    public HipChatService withTagPushEvents(Boolean tagPushEvents) {
        return withTagPushEvents(tagPushEvents, this);
    }

    /**
     * Sets the note events and returns this instance.
     *
     * @param noteEvents the note events value
     * @return the result
     */

    public HipChatService withNoteEvents(Boolean noteEvents) {
        return withNoteEvents(noteEvents, this);
    }

    /**
     * Sets the confidential note events and returns this instance.
     *
     * @param confidentialNoteEvents the confidential note events value
     * @return the result
     */

    public HipChatService withConfidentialNoteEvents(Boolean confidentialNoteEvents) {
        return withConfidentialNoteEvents(confidentialNoteEvents, this);
    }

    /**
     * Sets the pipeline events and returns this instance.
     *
     * @param pipelineEvents the pipeline events value
     * @return the result
     */

    public HipChatService withPipelineEvents(Boolean pipelineEvents) {
        return withPipelineEvents(pipelineEvents, this);
    }

    /**
     * Sets the wiki page events and returns this instance.
     *
     * @param wikiPageEvents the wiki page events value
     * @return the result
     */

    public HipChatService withWikiPageEvents(Boolean wikiPageEvents) {
        return withWikiPageEvents(wikiPageEvents, this);
    }

    /**
     * Sets the job events and returns this instance.
     *
     * @param jobEvents the job events value
     * @return the result
     */

    public HipChatService withJobEvents(Boolean jobEvents) {
        return withPipelineEvents(jobEvents, this);
    }

    /**
     * Returns the token.
     *
     * @return the result
     */

    public String getToken() {
        return ((String) getProperty(TOKEN_PROP));
    }

    /**
     * Sets the token.
     *
     * @param token the token value
     */

    public void setToken(String token) {
        setProperty(TOKEN_PROP, token);
    }

    /**
     * Sets the token and returns this instance.
     *
     * @param token the token value
     * @return the result
     */

    public HipChatService withToken(String token) {
        setToken(token);
        return (this);
    }

    /**
     * Returns the color.
     *
     * @return the result
     */

    public String getColor() {
        return getProperty(COLOR_PROP);
    }

    /**
     * Sets the color.
     *
     * @param color the color value
     */

    public void setColor(String color) {
        setProperty(COLOR_PROP, color);
    }

    /**
     * Sets the color and returns this instance.
     *
     * @param color the color value
     * @return the result
     */

    public HipChatService withColor(String color) {
        setColor(color);
        return (this);
    }

    /**
     * Returns the notify.
     *
     * @return the result
     */

    public Boolean getNotify() {
        return (getProperty(NOTIFY_PROP, null));
    }

    /**
     * Sets the notify.
     *
     * @param notify the notify value
     */

    public void setNotify(Boolean notify) {
        setProperty(NOTIFY_PROP, notify);
    }

    /**
     * Sets the notify and returns this instance.
     *
     * @param notify the notify value
     * @return the result
     */

    public HipChatService withNotify(Boolean notify) {
        setNotify(notify);
        return (this);
    }

    /**
     * Returns the room.
     *
     * @return the result
     */

    public String getRoom() {
        return getProperty(ROOM_PROP);
    }

    /**
     * Sets the room.
     *
     * @param room the room value
     */

    public void setRoom(String room) {
        setProperty(ROOM_PROP, room);
    }

    /**
     * Sets the room and returns this instance.
     *
     * @param room the room value
     * @return the result
     */

    public HipChatService withRoom(String room) {
        setRoom(room);
        return (this);
    }

    /**
     * Returns the api version.
     *
     * @return the result
     */

    public String getApiVersion() {
        return getProperty(API_VERSION_PROP);
    }

    /**
     * Sets the api version.
     *
     * @param apiVersion the api version value
     */

    public void setApiVersion(String apiVersion) {
        setProperty(API_VERSION_PROP, apiVersion);
    }

    /**
     * Sets the api version and returns this instance.
     *
     * @param apiVersion the api version value
     * @return the result
     */

    public HipChatService withApiVersion(String apiVersion) {
        setApiVersion(apiVersion);
        return (this);
    }

    /**
     * Returns the server.
     *
     * @return the result
     */

    public String getServer() {
        return getProperty(SERVER_PROP);
    }

    /**
     * Sets the server.
     *
     * @param server the server value
     */

    public void setServer(String server) {
        setProperty(SERVER_PROP, server);
    }

    /**
     * Sets the server and returns this instance.
     *
     * @param server the server value
     * @return the result
     */

    public HipChatService withServer(String server) {
        setServer(server);
        return (this);
    }

    /**
     * Returns the notify only broken pipelines.
     *
     * @return the result
     */

    @JsonIgnore
    public Boolean getNotifyOnlyBrokenPipelines() {
        return ((Boolean) getProperty(NOTIFY_ONLY_BROKEN_PIPELINES_PROP, Boolean.FALSE));
    }

    /**
     * Sets the notify only broken pipelines.
     *
     * @param notifyOnlyBrokenPipelines the notify only broken pipelines value
     */

    public void setNotifyOnlyBrokenPipelines(Boolean notifyOnlyBrokenPipelines) {
        setProperty(NOTIFY_ONLY_BROKEN_PIPELINES_PROP, notifyOnlyBrokenPipelines);
    }

    /**
     * Sets the notify only broken pipelines and returns this instance.
     *
     * @param notifyOnlyBrokenPipelines the notify only broken pipelines value
     * @return the result
     */

    public HipChatService withNotifyOnlyBrokenPipelines(Boolean notifyOnlyBrokenPipelines) {
        setNotifyOnlyBrokenPipelines(notifyOnlyBrokenPipelines);
        return (this);
    }

}
