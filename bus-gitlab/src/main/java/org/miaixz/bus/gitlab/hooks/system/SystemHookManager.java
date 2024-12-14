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
package org.miaixz.bus.gitlab.hooks.system;

import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.miaixz.bus.gitlab.GitLabApiException;
import org.miaixz.bus.gitlab.HookManager;
import org.miaixz.bus.gitlab.support.HttpRequest;
import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This class provides a handler for processing GitLab System Hook callouts.
 */
public class SystemHookManager implements HookManager {

    public static final String SYSTEM_HOOK_EVENT = "System Hook";
    private final static Logger LOGGER = Logger.getLogger(SystemHookManager.class.getName());
    private final JacksonJson jacksonJson = new JacksonJson();

    // Collection of objects listening for System Hook events.
    private final List<SystemHookListener> systemHookListeners = new CopyOnWriteArrayList<>();

    private String secretToken;

    /**
     * Create a HookManager to handle GitLab system hook events.
     */
    public SystemHookManager() {
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
        if (eventName == null || eventName.trim().isEmpty()) {
            String message = "X-Gitlab-Event header is missing!";
            LOGGER.warning(message);
            return (null);
        }

        if (!isValidSecretToken(request)) {
            String message = "X-Gitlab-Token mismatch!";
            LOGGER.warning(message);
            throw new GitLabApiException(message);
        }

        LOGGER.info("handleEvent: X-Gitlab-Event=" + eventName);
        if (!SYSTEM_HOOK_EVENT.equals(eventName)) {
            String message = "Unsupported X-Gitlab-Event, event Name=" + eventName;
            LOGGER.warning(message);
            throw new GitLabApiException(message);
        }

        // Get the JSON as a JsonNode tree. We do not directly unmarshal the input as special handling must
        // be done for "merge_request" events.
        JsonNode tree;
        try {

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(HttpRequest.getShortRequestDump("System Hook", true, request));
                String postData = HttpRequest.getPostDataAsString(request);
                LOGGER.fine("Raw POST data:\n" + postData);
                tree = jacksonJson.readTree(postData);
            } else {
                InputStreamReader reader = new InputStreamReader(request.getInputStream());
                tree = jacksonJson.readTree(reader);
            }

        } catch (Exception e) {
            LOGGER.warning(
                    "Error reading JSON data, exception=" + e.getClass().getSimpleName() + ", error=" + e.getMessage());
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
                LOGGER.warning(message);
                throw new GitLabApiException(message);
            }
        }

        // Unmarshal the tree to a concrete instance of a SystemHookEvent and fire the event to any listeners
        SystemHookEvent event;
        try {

            event = jacksonJson.unmarshal(SystemHookEvent.class, tree);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(event.getEventName() + "\n" + jacksonJson.marshal(event) + "\n");
            }

            StringBuffer requestUrl = request.getRequestURL();
            event.setRequestUrl(requestUrl != null ? requestUrl.toString() : null);
            event.setRequestQueryString(request.getQueryString());

            String secretToken = request.getHeader("X-Gitlab-Token");
            event.setRequestSecretToken(secretToken);

        } catch (Exception e) {
            LOGGER.warning(String.format("Error processing JSON data, exception=%s, error=%s",
                    e.getClass().getSimpleName(), e.getMessage()));
            throw new GitLabApiException(e);
        }

        try {

            fireEvent(event);
            return (event);

        } catch (Exception e) {
            LOGGER.warning(String.format("Error processing event, exception=%s, error=%s", e.getClass().getSimpleName(),
                    e.getMessage()));
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
            LOGGER.info("handleEvent:" + event.getClass().getSimpleName() + ", eventName=" + event.getEventName());
            fireEvent(event);
        } else {
            LOGGER.warning("handleEvent: provided event cannot be null!");
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
            LOGGER.warning(message);
            throw new GitLabApiException(message);
        }
    }

    protected void fireProjectEvent(ProjectSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onProjectEvent(event);
        }
    }

    protected void fireTeamMemberEvent(TeamMemberSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onTeamMemberEvent(event);
        }
    }

    protected void fireUserEvent(UserSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onUserEvent(event);
        }
    }

    protected void fireKeyEvent(KeySystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onKeyEvent(event);
        }
    }

    protected void fireGroupEvent(GroupSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onGroupEvent(event);
        }
    }

    protected void fireGroupMemberEvent(GroupMemberSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onGroupMemberEvent(event);
        }
    }

    protected void firePushEvent(PushSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onPushEvent(event);
        }
    }

    protected void fireTagPushEvent(TagPushSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onTagPushEvent(event);
        }
    }

    protected void fireRepositoryEvent(RepositorySystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onRepositoryEvent(event);
        }
    }

    protected void fireMergeRequestEvent(MergeRequestSystemHookEvent event) {
        for (SystemHookListener listener : systemHookListeners) {
            listener.onMergeRequestEvent(event);
        }
    }

}
