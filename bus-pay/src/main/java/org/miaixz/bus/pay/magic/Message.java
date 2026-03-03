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
package org.miaixz.bus.pay.magic;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Unified authorization response class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends org.miaixz.bus.core.basic.entity.Message {

    @Serial
    private static final long serialVersionUID = 2852292670363L;

    /**
     * The body of the message.
     */
    private String body;
    /**
     * The body of the message as a byte array.
     */
    private byte[] bodyByte;
    /**
     * The HTTP status code of the response.
     */
    private int status;
    /**
     * The headers of the message.
     */
    private Map<String, List<String>> headers;

    /**
     * Retrieves the first header value for the given header name.
     *
     * @param name The name of the header.
     * @return The first header value, or null if the header is not found or empty.
     */
    public String getHeader(String name) {
        List<String> values = this.headerList(name);
        return CollKit.isEmpty(values) ? null : values.get(0);
    }

    /**
     * Retrieves a list of header values for the given header name, case-insensitively.
     *
     * @param name The name of the header.
     * @return A list of header values, or null if the header is not found.
     */
    private List<String> headerList(String name) {
        if (StringKit.isBlank(name)) {
            return null;
        } else {
            CaseInsensitiveMap<String, List<String>> headersIgnoreCase = new CaseInsensitiveMap<>(getHeaders());
            return headersIgnoreCase.get(name.trim());
        }
    }

}
