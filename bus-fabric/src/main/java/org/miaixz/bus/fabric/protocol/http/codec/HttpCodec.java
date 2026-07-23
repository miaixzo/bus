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
package org.miaixz.bus.fabric.protocol.http.codec;

import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * HTTP codec contract for request and response transfer over a bound route.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface HttpCodec {

    /**
     * Writes a request to the bound transport.
     *
     * @param request HTTP request whose start line, headers, and optional body are written
     */
    void writeRequest(HttpRequest request);

    /**
     * Reads a response from the bound transport.
     *
     * @param request originating request to associate with the decoded response
     * @return response decoded from the bound transport
     */
    HttpResponse readResponse(HttpRequest request);

    /**
     * Cancels in-progress codec work and the transport resources bound to it.
     */
    void cancel();

    /**
     * Returns whether the bound connection can be reused.
     *
     * @return true when the bound connection remains eligible for another exchange
     */
    boolean reusable();

}
