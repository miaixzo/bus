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
package org.miaixz.bus.starter.elastic;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransportBase;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.http.TransportHttpClient;
import co.elastic.clients.transport.instrumentation.Instrumentation;

/**
 * Elasticsearch transport backed by the Bus Fabric HTTP runtime.
 * <p>
 * The typed Elasticsearch client API is kept unchanged while HTTP execution is delegated to
 * {@link org.miaixz.bus.fabric.Fabric}. As a consequence neither the Apache HttpClient 5 module nor the Elasticsearch
 * REST 5 client module is required on the class path.
 * </p>
 *
 * @author Kimi Liu
 */
final class FabricTransport extends ElasticsearchTransportBase {

    /**
     * Creates a transport with a Fabric-backed HTTP client.
     *
     * @param httpClient Fabric HTTP client
     * @param options    transport options
     * @param mapper     JSON mapper
     */
    FabricTransport(TransportHttpClient httpClient, TransportOptions options, JsonpMapper mapper) {
        super(httpClient, options, mapper);
    }

    /**
     * Creates a transport with explicit instrumentation.
     *
     * @param httpClient      Fabric HTTP client
     * @param options         transport options
     * @param mapper          JSON mapper
     * @param instrumentation instrumentation
     */
    FabricTransport(TransportHttpClient httpClient, TransportOptions options, JsonpMapper mapper,
            Instrumentation instrumentation) {
        super(httpClient, options, mapper, instrumentation);
    }

    /**
     * Copies this transport with replacement options, mapper or instrumentation.
     *
     * @param options         replacement options, or {@code null} to keep the current ones
     * @param mapper          replacement mapper, or {@code null} to keep the current one
     * @param instrumentation replacement instrumentation, or {@code null} to keep the current one
     * @return copied transport
     */
    @Override
    protected ElasticsearchTransportBase cloneWith(
            TransportOptions options,
            JsonpMapper mapper,
            Instrumentation instrumentation) {
        return new FabricTransport(this.httpClient, options == null ? this.transportOptions : options,
                mapper == null ? this.mapper : mapper,
                instrumentation == null ? this.instrumentation : instrumentation);
    }

}
