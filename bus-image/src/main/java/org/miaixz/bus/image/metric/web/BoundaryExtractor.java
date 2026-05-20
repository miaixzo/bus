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
package org.miaixz.bus.image.metric.web;

import java.nio.charset.StandardCharsets;

/**
 * Utility for extracting multipart boundary values from Content-Type headers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BoundaryExtractor {

    /**
     * Creates a new instance.
     */
    private BoundaryExtractor() {
        // No initialization required.
    }

    /**
     * Executes the extract boundary operation.
     *
     * @param contentType  the content type.
     * @param requiredType the required type.
     * @return the operation result.
     */
    public static byte[] extractBoundary(String contentType, String requiredType) {
        String boundary = extractBoundaryValue(contentType, requiredType);
        return boundary == null ? null : boundary.getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Executes the extract boundary value operation.
     *
     * @param contentType  the content type.
     * @param requiredType the required type.
     * @return the operation result.
     */
    public static String extractBoundaryValue(String contentType, String requiredType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        HeaderFieldValues parser = new HeaderFieldValues(contentType);
        String boundaryValue = parser.getValue("boundary");
        if (boundaryValue == null) {
            return null;
        }
        if (requiredType != null && !hasRequiredType(parser, requiredType)) {
            return null;
        }
        return boundaryValue;
    }

    /**
     * Determines whether required type.
     *
     * @param parser       the parser.
     * @param requiredType the required type.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean hasRequiredType(HeaderFieldValues parser, String requiredType) {
        String normalized = requiredType.toLowerCase(java.util.Locale.ROOT);
        return parser.hasKey(normalized)
                || parser.getValues().stream().anyMatch(map -> map.containsValue(requiredType));
    }

}
