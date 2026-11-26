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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.ElasticsearchTransportBase;
import co.elastic.clients.transport.Endpoint;
import co.elastic.clients.transport.RetryConfig;
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
final class FabricTransport implements ElasticsearchTransport {

    /**
     * SDK transport retaining serialization, response decoding, instrumentation and safe-read retry ownership.
     */
    private final ElasticsearchTransportBase delegate;

    /**
     * Resolved SDK instrumentation retained when copying this transport.
     */
    private final Instrumentation instrumentation;

    /**
     * Creates a transport with a Fabric-backed HTTP client.
     *
     * @param httpClient Fabric HTTP client
     * @param options    transport options
     * @param mapper     JSON mapper
     */
    FabricTransport(TransportHttpClient httpClient, TransportOptions options, JsonpMapper mapper) {
        this(httpClient, options, mapper, null);
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
        var sdk = new ElasticsearchTransportBase(httpClient, options, mapper, instrumentation) {

            /**
             * Returns the instrumentation resolved by the SDK constructor.
             *
             * @return instrumentation used by the SDK transport delegate
             */
            Instrumentation resolvedInstrumentation() {
                return this.instrumentation;
            }

        };
        this.delegate = sdk;
        this.instrumentation = sdk.resolvedInstrumentation();
    }

    /**
     * Executes a typed Elasticsearch request synchronously using the applicable replay policy.
     *
     * @param <RequestT>  endpoint request type
     * @param <ResponseT> successful response type
     * @param <ErrorT>    endpoint error response type
     * @param request     typed Elasticsearch request
     * @param endpoint    endpoint definition used to encode and decode the exchange
     * @param options     per-request options, or {@code null} to use the transport defaults
     * @return decoded Elasticsearch response
     * @throws IOException if the HTTP exchange or response decoding fails
     */
    @Override
    public <RequestT, ResponseT, ErrorT> ResponseT performRequest(
            RequestT request,
            Endpoint<RequestT, ResponseT, ErrorT> endpoint,
            TransportOptions options) throws IOException {
        return this.delegate.performRequest(request, endpoint, requestOptions(request, endpoint, options));
    }

    /**
     * Executes a typed Elasticsearch request asynchronously using the applicable replay policy.
     *
     * @param <RequestT>  endpoint request type
     * @param <ResponseT> successful response type
     * @param <ErrorT>    endpoint error response type
     * @param request     typed Elasticsearch request
     * @param endpoint    endpoint definition used to encode and decode the exchange
     * @param options     per-request options, or {@code null} to use the transport defaults
     * @return future that completes with the decoded response or the request failure
     */
    @Override
    public <RequestT, ResponseT, ErrorT> CompletableFuture<ResponseT> performRequestAsync(
            RequestT request,
            Endpoint<RequestT, ResponseT, ErrorT> endpoint,
            TransportOptions options) {
        final TransportOptions guarded;
        try {
            guarded = requestOptions(request, endpoint, options);
        } catch (Exception failure) {
            return CompletableFuture.failedFuture(failure);
        }
        return this.delegate.performRequestAsync(request, endpoint, guarded);
    }

    /**
     * Disables SDK replay for writes before the SDK can install its retry wrapper. The HTTP adapter still permits
     * switching nodes when authoritative delivery facts establish that the write was not processed.
     *
     * @param <RequestT>  endpoint request type
     * @param <ResponseT> endpoint response type
     * @param <ErrorT>    endpoint error type
     * @param request     request whose HTTP method determines replay eligibility
     * @param endpoint    SDK endpoint definition
     * @param options     per-request options, or {@code null} to use the defaults
     * @return options for the SDK call, or {@code null} to let the SDK use its defaults
     */
    private <RequestT, ResponseT, ErrorT> TransportOptions requestOptions(
            RequestT request,
            Endpoint<RequestT, ResponseT, ErrorT> endpoint,
            TransportOptions options) {
        TransportOptions effective = options == null ? options() : options;
        RetryConfig retry = effective.retryConfig();
        if (retry == null || !retry.isEnabled()
                || FabricHttpTransportClient.safeReplay(endpoint.id(), endpoint.method(request))) {
            return options;
        }
        return effective.toBuilder().retryConfig(RetryConfig.disabled()).build();
    }

    /**
     * Returns the mapper used for Elasticsearch JSON payloads.
     *
     * @return mapper used by the SDK transport delegate
     */
    @Override
    public JsonpMapper jsonpMapper() {
        return this.delegate.jsonpMapper();
    }

    /**
     * Returns the options used when a request supplies no override.
     *
     * @return default SDK transport options
     */
    @Override
    public TransportOptions options() {
        return this.delegate.options();
    }

    /**
     * Returns the HTTP client used by the SDK transport delegate.
     *
     * @return underlying Fabric HTTP transport client
     */
    public TransportHttpClient httpClient() {
        return this.delegate.httpClient();
    }

    /**
     * Closes the SDK transport and its underlying Fabric HTTP client.
     *
     * @throws IOException if the SDK transport cannot be closed
     */
    @Override
    public void close() throws IOException {
        this.delegate.close();
    }

    /**
     * Copies this transport with replacement options, mapper or instrumentation.
     *
     * @param options         replacement options, or {@code null} to keep the current ones
     * @param mapper          replacement mapper, or {@code null} to keep the current one
     * @param instrumentation replacement instrumentation, or {@code null} to keep the current one
     * @return copied transport
     */
    protected FabricTransport cloneWith(TransportOptions options, JsonpMapper mapper, Instrumentation instrumentation) {
        return new FabricTransport(httpClient(), options == null ? options() : options,
                mapper == null ? jsonpMapper() : mapper,
                instrumentation == null ? this.instrumentation : instrumentation);
    }

}
