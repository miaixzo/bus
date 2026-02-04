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
package org.miaixz.bus.gitlab;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This interface provides a base class handler for processing GitLab Web Hook and System Hook callouts.
 */
public interface HookManager {

    /**
     * Get the secret token that received hook events should be validated against.
     *
     * @return the secret token that received hook events should be validated against
     */
    String getSecretToken();

    /**
     * Set the secret token that received hook events should be validated against.
     *
     * @param secretToken the secret token to verify against
     */
    void setSecretToken(String secretToken);

    /**
     * Validate the provided secret token against the reference secret token. Returns true if the secret token is valid
     * or there is no reference secret token to validate against, otherwise returns false.
     *
     * @param secretToken the token to validate
     * @return true if the secret token is valid or there is no reference secret token to validate against
     */
    public default boolean isValidSecretToken(String secretToken) {
        String ourSecretToken = getSecretToken();
        return (ourSecretToken == null || ourSecretToken.trim().isEmpty() || ourSecretToken.equals(secretToken) ? true
                : false);
    }

    /**
     * Validate the provided secret token found in the HTTP header against the reference secret token. Returns true if
     * the secret token is valid or there is no reference secret token to validate against, otherwise returns false.
     *
     * @param request the HTTP request to verify the secret token
     * @return true if the secret token is valid or there is no reference secret token to validate against
     */
    public default boolean isValidSecretToken(HttpServletRequest request) {

        if (getSecretToken() != null) {
            String secretToken = request.getHeader("X-Gitlab-Token");
            return (isValidSecretToken(secretToken));
        }

        return (true);
    }

    /**
     * Parses and verifies an Event instance from the HTTP request and fires it off to the registered listeners.
     *
     * @param request the HttpServletRequest to read the Event instance from
     * @throws GitLabApiException if the parsed event is not supported
     */
    public void handleEvent(HttpServletRequest request) throws GitLabApiException;

}
