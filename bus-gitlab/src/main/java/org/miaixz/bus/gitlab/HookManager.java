/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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
    default public boolean isValidSecretToken(String secretToken) {
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
    default public boolean isValidSecretToken(HttpServletRequest request) {

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
    void handleEvent(HttpServletRequest request) throws GitLabApiException;

}