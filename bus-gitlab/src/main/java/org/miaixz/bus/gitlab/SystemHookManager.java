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
package org.miaixz.bus.gitlab;

import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.miaixz.bus.gitlab.hooks.system.*;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.logger.Logger;

/**
 * This class provides a handler for processing GitLab System Hook callouts.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SystemHookManager implements HookManager {

    /**
     * The system hook event value.
     */

    public static final String SYSTEM_HOOK_EVENT = "System Hook";
    private final JacksonJson jacksonJson = new JacksonJson();

    // Collection of objects listening for System Hook events.
    private final List<SystemHookListener> systemHookListeners = new CopyOnWriteArrayList<SystemHookListener>();

    private String secretToken;

    /**
     * Create a HookManager to handle GitLab system hook events.
     */
    public SystemHookManager() {
        // No initialization required.
    }

    /**
     * Create a HookManager to handle GitLab system hook events which will be verified against the specified
     * secretToken.
     *
     * @param secretToken the secret token to verify against
     */
    public SystemHookManager(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * Get the secret token that received hook events should be validated against.
     *
     * @return the secret token that received hook events should be validated against
     */
    public String getSecretToken() {
        return (secretToken);
    }

    /**
     * Set the secret token that received hook events should be validated against.
     *
     * @param secretToken the secret token to verify against
     */
    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * Parses and verifies an SystemHookEvent instance from the HTTP request and fires it off to the registered
     * listeners.
     *
     * @param request the HttpServletRequest to read the Event instance from
     * @throws GitLabApiException if the parsed event is not supported
     */
    public void handleEvent(HttpServletRequest request) throws GitLabApiException {
        handleRequest(request);
    }

    /**
     * Parses and verifies an SystemHookEvent instance from the HTTP request and fires it off to the registered
     * listeners.
     *
     * @param request the HttpServletRequest to read the Event instance from
     * @return the processed SystemHookEvent instance read from the request,null if the request not contain a system
     *         hook event
     * @throws GitLabApiException if the parsed event is not supported
     */
    public SystemHookEvent handleRequest(HttpServletRequest request) throws GitLabApiException {

        String eventName = request.getHeader("X-Gitlab-Event");
        Logger.info(
                true,
                "GitLab",
                "System hook request received: eventName={}, requestUri={}, queryPresent={}",
                eventName,
                request.getRequestURI(),
                request.getQueryString() != null);
        if (eventName == null || eventName.trim().isEmpty()) {
            String message = "X-Gitlab-Event header is missing!";
            Logger.warn(
                    false,
                    "GitLab",
                    "System hook request rejected: reason={}, requestUri={}, queryPresent={}",
                    "missingEventHeader",
                    request.getRequestURI(),
                    request.getQueryString() != null);
            return (null);
        }

        if (!isValidSecretToken(request)) {
            String message = "X-Gitlab-Token mismatch!";
            Logger.warn(
                    false,
                    "GitLab",
                    "System hook request rejected: eventName={}, reason={}, requestUri={}",
                    eventName,
                    "credentialMismatch",
                    request.getRequestURI());
            throw new GitLabApiException(message);
        }

        Logger.info(
                true,
                "GitLab",
                "System hook event validation started: eventName={}, requestUri={}",
                eventName,
                request.getRequestURI());
        if (!SYSTEM_HOOK_EVENT.equals(eventName)) {
            String message = "Unsupported X-Gitlab-Event, event Name=" + eventName;
            Logger.warn(
                    false,
                    "GitLab",
                    "System hook event rejected: eventName={}, reason={}",
                    eventName,
                    "unsupportedEventName");
            throw new GitLabApiException(message);
        }

        // Get the JSON as a JsonNode tree. We do not directly unmarshal the input as special handling must
        // be done for "merge_request" events.
        JsonNode tree;
        try {

            Logger.debug(
                    true,
                    "GitLab",
                    "System hook JSON parsing started: eventName={}, requestUri={}",
                    eventName,
                    request.getRequestURI());
            InputStreamReader reader = new InputStreamReader(request.getInputStream());
            tree = jacksonJson.readTree(reader);
            Logger.debug(
                    false,
                    "GitLab",
                    "System hook JSON parsed: eventName={}, objectKindPresent={}",
                    eventName,
                    tree.has("object_kind"));

        } catch (Exception e) {
            Logger.warn(
                    false,
                    "GitLab",
                    e,
                    "System hook JSON parsing failed: eventName={}, requestUri={}, exception={}",
                    eventName,
                    request.getRequestURI(),
                    e.getClass().getSimpleName());
            throw new GitLabApiException(e);
        }

        // NOTE: This is a hack based on the GitLab documentation and actual content of the "merge_request" event
        // showing that the "event_name" property is missing from the merge_request system hook event. The hack is
        // to inject the "event_name" node so that the polymorphic deserialization of a SystemHookEvent works correctly
        // when the system hook event is a "merge_request" event.
        if (!tree.has("event_name") && tree.has("object_kind")) {

            String objectKind = tree.get("object_kind").asText();
            if (MergeRequestSystemHookEvent.MERGE_REQUEST_EVENT.equals(objectKind)) {
                ObjectNode node = (ObjectNode) tree;
                node.put("event_name", MergeRequestSystemHookEvent.MERGE_REQUEST_EVENT);
            } else {
                String message = "Unsupported object_kind for system hook event, object_kind=" + objectKind;
                Logger.warn(
                        false,
                        "GitLab",
                        "System hook event rejected: objectKind={}, reason={}",
                        objectKind,
                        "unsupportedObjectKind");
                throw new GitLabApiException(message);
            }
        }

        // Unmarshal the tree to a concrete instance of a SystemHookEvent and fire the event to any listeners
        SystemHookEvent event;
        try {

            event = jacksonJson.unmarshal(SystemHookEvent.class, tree);
            Logger.debug(
                    false,
                    "GitLab",
                    "System hook event decoded: eventName={}, eventType={}",
                    event.getEventName(),
                    event.getClass().getSimpleName());

            StringBuffer requestUrl = request.getRequestURL();
            event.setRequestUrl(requestUrl != null ? requestUrl.toString() : null);
            event.setRequestQueryString(request.getQueryString());

            String secretToken = request.getHeader("X-Gitlab-Token");
            event.setRequestSecretToken(secretToken);

        } catch (Exception e) {
            Logger.warn(
                    false,
                    "GitLab",
                    e,
                    "System hook event decoding failed: eventName={}, exception={}",
                    eventName,
                    e.getClass().getSimpleName());
            throw new GitLabApiException(e);
        }

        try {

            Logger.info(
                    true,
                    "GitLab",
                    "System hook listener dispatch started: eventName={}, eventType={}, listeners={}",
                    event.getEventName(),
                    event.getClass().getSimpleName(),
                    systemHookListeners.size());
            fireEvent(event);
            Logger.info(
                    false,
                    "GitLab",
                    "System hook listener dispatch completed: eventName={}, eventType={}, listeners={}",
                    event.getEventName(),
                    event.getClass().getSimpleName(),
                    systemHookListeners.size());
            return (event);

        } catch (Exception e) {
            Logger.warn(
                    false,
                    "GitLab",
                    e,
                    "System hook listener dispatch failed: eventName={}, eventType={}, listeners={}, exception={}",
                    event.getEventName(),
                    event.getClass().getSimpleName(),
                    systemHookListeners.size(),
                    e.getClass().getSimpleName());
            throw new GitLabApiException(e);
        }
    }

    /**
     * Verifies the provided Event and fires it off to the registered listeners.
     *
     * @param event the Event instance to handle
     * @throws GitLabApiException if the event is not supported
     */
    public void handleEvent(SystemHookEvent event) throws GitLabApiException {
        if (event != null) {
            Logger.info(
                    true,
                    "GitLab",
                    "System hook event dispatch requested: eventName={}, eventType={}, listeners={}",
                    event.getEventName(),
                    event.getClass().getSimpleName(),
                    systemHookListeners.size());
            fireEvent(event);
            Logger.info(
                    false,
                    "GitLab",
                    "System hook event dispatch completed: eventName={}, eventType={}, listeners={}",
                    event.getEventName(),
                    event.getClass().getSimpleName(),
                    systemHookListeners.size());
        } else {
            Logger.warn(false, "GitLab", "System hook event rejected: reason={}", "nullEvent");
        }
    }

    /**
     * Adds a System Hook event listener.
     *
     * @param listener the SystemHookListener to add
     */
    public void addListener(SystemHookListener listener) {

        if (!systemHookListeners.contains(listener)) {
            systemHookListeners.add(listener);
        }
    }

    /**
     * Removes a System Hook event listener.
     *
     * @param listener the SystemHookListener to remove
     */
    public void removeListener(SystemHookListener listener) {
        systemHookListeners.remove(listener);
    }

    /**
     * Fire the event to the registered listeners.
     *
     * @param event the SystemHookEvent instance to fire to the registered event listeners
     * @throws GitLabApiException if the event is not supported
     */
    public void fireEvent(SystemHookEvent event) throws GitLabApiException {

        if (event instanceof ProjectSystemHookEvent) {
            fireProjectEvent((ProjectSystemHookEvent) event);
        } else if (event instanceof TeamMemberSystemHookEvent) {
            fireTeamMemberEvent((TeamMemberSystemHookEvent) event);
        } else if (event instanceof UserSystemHookEvent) {
            fireUserEvent((UserSystemHookEvent) event);
        } else if (event instanceof KeySystemHookEvent) {
            fireKeyEvent((KeySystemHookEvent) event);
        } else if (event instanceof GroupSystemHookEvent) {
            fireGroupEvent((GroupSystemHookEvent) event);
        } else if (event instanceof GroupMemberSystemHookEvent) {
            fireGroupMemberEvent((GroupMemberSystemHookEvent) event);
        } else if (event instanceof PushSystemHookEvent) {
            firePushEvent((PushSystemHookEvent) event);
        } else if (event instanceof TagPushSystemHookEvent) {
            fireTagPushEvent((TagPushSystemHookEvent) event);
        } else if (event instanceof RepositorySystemHookEvent) {
            fireRepositoryEvent((RepositorySystemHookEvent) event);
        } else if (event instanceof MergeRequestSystemHookEvent) {
            fireMergeRequestEvent((MergeRequestSystemHookEvent) event);
        } else {
            String message = "Unsupported event, event_named=" + event.getEventName();
            Logger.warn(
                    false,
                    "GitLab",
                    "System hook event rejected: eventName={}, eventType={}, reason={}",
                    event.getEventName(),
                    event.getClass().getSimpleName(),
                    "unsupportedEventType");
            throw new GitLabApiException(message);
        }
    }

    /**
     * Executes the fire project event operation.
     *
     * @param event the event value
     */

    protected void fireProjectEvent(ProjectSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onProjectEvent(event);
        }
    }

    /**
     * Executes the fire team member event operation.
     *
     * @param event the event value
     */

    protected void fireTeamMemberEvent(TeamMemberSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onTeamMemberEvent(event);
        }
    }

    /**
     * Executes the fire user event operation.
     *
     * @param event the event value
     */

    protected void fireUserEvent(UserSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onUserEvent(event);
        }
    }

    /**
     * Executes the fire key event operation.
     *
     * @param event the event value
     */

    protected void fireKeyEvent(KeySystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onKeyEvent(event);
        }
    }

    /**
     * Executes the fire group event operation.
     *
     * @param event the event value
     */

    protected void fireGroupEvent(GroupSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onGroupEvent(event);
        }
    }

    /**
     * Executes the fire group member event operation.
     *
     * @param event the event value
     */

    protected void fireGroupMemberEvent(GroupMemberSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onGroupMemberEvent(event);
        }
    }

    /**
     * Executes the fire push event operation.
     *
     * @param event the event value
     */

    protected void firePushEvent(PushSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onPushEvent(event);
        }
    }

    /**
     * Executes the fire tag push event operation.
     *
     * @param event the event value
     */

    protected void fireTagPushEvent(TagPushSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onTagPushEvent(event);
        }
    }

    /**
     * Executes the fire repository event operation.
     *
     * @param event the event value
     */

    protected void fireRepositoryEvent(RepositorySystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onRepositoryEvent(event);
        }
    }

    /**
     * Executes the fire merge request event operation.
     *
     * @param event the event value
     */

    protected void fireMergeRequestEvent(MergeRequestSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onMergeRequestEvent(event);
        }
    }

}
