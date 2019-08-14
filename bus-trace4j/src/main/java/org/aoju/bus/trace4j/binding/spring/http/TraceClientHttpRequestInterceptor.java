package org.aoju.bus.trace4j.binding.spring.http;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class TraceClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final TraceBackend backend;
    private final HttpHeaderTransport transportSerialization;
    private final String profile;

    public TraceClientHttpRequestInterceptor() {
        this(Trace.getBackend(), new HttpHeaderTransport(), TraceConsts.DEFAULT);
    }

    public TraceClientHttpRequestInterceptor(String profile) {
        this(Trace.getBackend(), new HttpHeaderTransport(), profile);
    }

    public TraceClientHttpRequestInterceptor(TraceBackend backend, HttpHeaderTransport transportSerialization, String profile) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
        this.profile = profile;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        preRequest(request);
        final ClientHttpResponse response = execution.execute(request, body);
        postResponse(response);
        return response;
    }

    private void preRequest(final HttpRequest request) {
        final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);
        if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingRequest)) {
            final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(), TraceFilterConfiguration.Channel.OutgoingRequest);
            request.getHeaders().add(TraceConsts.TPIC_HEADER, transportSerialization.render(filteredParams));
        }
    }

    private void postResponse(ClientHttpResponse response) {
        final List<String> headers = response.getHeaders().get(TraceConsts.TPIC_HEADER);
        if (headers != null) {
            final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);

            if (filterConfiguration.shouldProcessContext(TraceFilterConfiguration.Channel.IncomingResponse)) {
                backend.putAll(filterConfiguration.filterDeniedParams(transportSerialization.parse(headers), TraceFilterConfiguration.Channel.IncomingResponse));
            }
        }
    }

}
