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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Parses multipart headers with continuation-line support.
 *
 * @author Kimi Liu
 */
public class MultipartHeaderParser {

    /**
     * Creates a new instance.
     */
    public MultipartHeaderParser() {
        // No initialization required.
    }

    /**
     * Parses the headers.
     *
     * @param headerContent the header content.
     * @return the operation result.
     */
    public static Map<String, String> parseHeaders(String headerContent) {
        Objects.requireNonNull(headerContent, "Header content cannot be null");
        Map<String, String> headers = new LinkedHashMap<>();
        String currentFieldName = null;
        StringBuilder currentField = new StringBuilder();

        for (String line : headerContent.split(Symbol.CRLF)) {
            if (line.isEmpty()) {
                break;
            }
            if (line.startsWith(Symbol.SPACE) || line.startsWith(Symbol.HT)) {
                if (currentFieldName != null) {
                    currentField.append(Symbol.C_SPACE).append(line.trim());
                }
                continue;
            }
            if (currentFieldName != null) {
                merge(headers, currentFieldName, currentField.toString());
            }
            int colonIndex = line.indexOf(Symbol.C_COLON);
            if (colonIndex > 0) {
                currentFieldName = line.substring(0, colonIndex).trim();
                currentField = new StringBuilder(line.substring(colonIndex + 1).trim());
            } else {
                currentFieldName = null;
                currentField.setLength(0);
            }
        }
        if (currentFieldName != null) {
            merge(headers, currentFieldName, currentField.toString());
        }
        return headers;
    }

    /**
     * Executes the merge operation.
     *
     * @param headers the headers.
     * @param name    the name.
     * @param value   the value.
     */
    private static void merge(Map<String, String> headers, String name, String value) {
        headers.merge(name, value, (oldVal, newVal) -> oldVal + Symbol.COMMA + newVal);
    }

}
