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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.RelevantException;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.logger.Logger;

import tools.jackson.databind.JsonNode;

/**
 * Creates global exceptions from GitLab API failures and resolves messages from error responses.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class GitLabFailure {

    /**
     * Prevents utility class instantiation.
     */
    private GitLabFailure() {
        // No initialization required.
    }

    /**
     * Creates a global I/O exception for a GitLab failure message.
     *
     * @param message failure message
     * @return global exception
     */
    public static RelevantException exception(String message) {
        return failure(message, 0, null);
    }

    /**
     * Creates a global I/O exception for a GitLab failure message and HTTP status.
     *
     * @param message    failure message
     * @param httpStatus HTTP response status
     * @return global exception
     */
    public static RelevantException exception(String message, int httpStatus) {
        return failure(message, httpStatus, null);
    }

    /**
     * Creates a global I/O exception from a GitLab HTTP response.
     *
     * @param response response that caused the failure
     * @return global exception
     */
    public static RelevantException exception(Response response) {
        return failure(resolveMessage(response), response.getStatus(), null);
    }

    /**
     * Wraps a non-GitLab failure in the global I/O exception contract.
     *
     * @param cause underlying failure
     * @return existing global exception or a newly wrapped exception
     */
    public static RelevantException exception(Exception cause) {
        if (cause instanceof RelevantException relevant) {
            return relevant;
        }
        return failure(cause.getMessage(), 0, cause);
    }

    /**
     * Creates the global exception and exposes an HTTP status through its standard error code.
     *
     * @param message    failure message
     * @param httpStatus HTTP status, or zero when unavailable
     * @param cause      underlying failure, or {@code null}
     * @return global exception
     */
    private static RelevantException failure(String message, int httpStatus, Throwable cause) {
        RelevantException failure = new RelevantException(ErrorCode._FAILURE, message, cause);
        if (httpStatus > 0) {
            failure.setErrcode(Integer.toString(httpStatus));
        }
        return failure;
    }

    /**
     * Resolves the most useful error message from a GitLab response.
     *
     * @param response GitLab response
     * @return resolved response message
     */
    private static String resolveMessage(Response response) {
        String message = null;
        if (response.hasEntity()) {
            try {
                message = response.readEntity(String.class);
                MediaType mediaType = response.getMediaType();
                if (mediaType != null && "json".equals(mediaType.getSubtype())) {
                    message = resolveJsonMessage(message);
                }
            } catch (Exception ignore) {
                Logger.debug(
                        false,
                        "GitLab",
                        "GitLab API error response parsing skipped: status={}, mediaType={}, exception={}",
                        response.getStatus(),
                        response.getMediaType(),
                        ignore.getClass().getSimpleName());
            }
        }
        return message != null ? message : response.getStatusInfo().getReasonPhrase();
    }

    /**
     * Resolves GitLab's supported JSON error representations.
     *
     * @param content raw JSON response content
     * @return resolved message, or the original content when no known field exists
     */
    private static String resolveJsonMessage(String content) throws IOException {
        JsonNode json = JacksonJson.toJsonNode(content);
        JsonNode message = json.get("message");
        if (message == null) {
            JsonNode error = json.get("error");
            return error != null ? error.asText() : content;
        }
        if (message.isObject()) {
            return resolveValidationMessage(message, content);
        }
        if (message.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode value : message) {
                values.add(value.asText());
            }
            return values.isEmpty() ? content : String.join(Symbol.LF, values);
        }
        return message.isTextual() ? message.asText() : message.toString();
    }

    /**
     * Formats GitLab field validation errors as a readable message.
     *
     * @param message  JSON validation error object
     * @param fallback fallback content when the object is empty
     * @return formatted validation message
     */
    private static String resolveValidationMessage(JsonNode message, String fallback) {
        Map<String, List<String>> validationErrors = new LinkedHashMap<>();
        Iterator<Entry<String, JsonNode>> fields = message.properties().iterator();
        while (fields.hasNext()) {
            Entry<String, JsonNode> field = fields.next();
            List<String> values = new ArrayList<>();
            validationErrors.put(field.getKey(), values);
            for (JsonNode value : field.getValue()) {
                values.add(value.asText());
            }
        }
        if (validationErrors.isEmpty()) {
            return fallback;
        }
        String validationItemPrefix = Symbol.LF + Symbol.SPACE.repeat(5) + Symbol.MINUS + Symbol.SPACE;
        return "The following fields have validation errors: "
                + String.join(Symbol.COMMA + Symbol.SPACE, validationErrors.keySet()) + Symbol.LF
                + validationErrors.entrySet().stream().map(
                        entry -> Symbol.STAR + Symbol.SPACE + entry.getKey()
                                + entry.getValue().stream().collect(
                                        Collectors.joining(validationItemPrefix, validationItemPrefix, Normal.EMPTY)))
                        .collect(Collectors.joining(Symbol.LF));
    }

}
