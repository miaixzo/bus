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
package org.miaixz.bus.cortex.registry;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Fabric;

/**
 * Announces a service process to its registry and renews only the acknowledged application session. API definitions
 * never appear in this protocol. The host owns scheduling and shutdown.
 *
 * @author Kimi Liu
 */
public final class InstanceClient {

    /**
     * Application code.
     */
    private final String code;

    /**
     * Configured registry URL.
     */
    private final String endpoint;

    /**
     * Explicit endpoint used to retire the current process session.
     */
    private final String retirement;

    /**
     * Sends the lease payload and returns the successful response body.
     */
    private final BiFunction<String, String, String> transport;

    /**
     * Process-owned instance address and registration metadata.
     */
    private final Instance process;

    /**
     * Last server acknowledgement, including canonical App ID and current service lease.
     */
    private Instance acknowledged;

    /**
     * Creates a dedicated registry client with separately routed announcement and retirement operations.
     *
     * @param endpoint   exact registration and renewal endpoint
     * @param retirement exact session retirement endpoint
     * @param code       application directory code
     * @param process    current process address
     * @param timeout    transport deadline
     * @param credential credential supplier selected by the caller for these endpoints
     */
    public InstanceClient(String endpoint, String retirement, String code, Instance process, Duration timeout,
            Supplier<String> credential) {
        this((url, json) -> post(url, json, timeout, credential), endpoint, retirement, code, process);
    }

    /**
     * Creates a lease client using the supplied transport for both operations.
     *
     * @param transport  sends the encoded request and returns a successful response body
     * @param endpoint   exact registration and renewal endpoint
     * @param retirement exact session retirement endpoint
     * @param code       application directory code
     * @param process    current process address
     */
    public InstanceClient(BiFunction<String, String, String> transport, String endpoint, String retirement, String code,
            Instance process) {
        ServiceRoutes.validate(process);
        this.endpoint = endpoint;
        this.retirement = retirement;
        this.transport = transport;
        this.code = code;
        this.process = BeanKit.toBean(process, Instance.class);
        this.process.setPort(ServiceRoutes.address(process).getPort());
    }

    /**
     * Registers readiness or renews the exact current server session. Expired local acknowledgements are discarded.
     *
     * @return canonical process acknowledged by the registry
     */
    public synchronized Instance renew() {
        if (acknowledged != null && !acknowledged.isOnline(DateKit.current()))
            acknowledged = null;
        Instance request = BeanKit.toBean(process, Instance.class);
        if (acknowledged != null) {
            request.setId(acknowledged.getId());
            request.setApp_id(acknowledged.getApp_id());
            request.setHealth(acknowledged.getHealth());
        }
        acknowledged = exchange(endpoint, request);
        return BeanKit.toBean(acknowledged, Instance.class);
    }

    /**
     * Retires the acknowledged session; a replaced session cannot retire its successor.
     */
    public synchronized void retire() {
        if (acknowledged != null) {
            exchange(retirement, acknowledged);
            acknowledged = null;
        }
    }

    /**
     * Performs one authenticated bounded announcement and validates the complete ownership acknowledgement.
     *
     * @param url      explicit registry operation
     * @param instance current process announcement
     * @return acknowledged process
     */
    private Instance exchange(String url, Instance instance) {
        try {
            String payload = JsonKit.toJsonString(Map.of("code", code, "instance", instance));
            Object decoded = JsonKit
                    .toJavaValue(JsonKit.readValue(transport.apply(url, payload).getBytes(Charset.UTF_8)));
            if (!(decoded instanceof Map<?, ?> body)
                    || !ErrorCode._SUCCESS.getKey().equals(String.valueOf(body.get("code")))
                    || !(body.get("data") instanceof Map<?, ?> data) || !code.equals(data.get("code"))
                    || !(data.get("instance") instanceof Map<?, ?>))
                throw new IllegalStateException("Service registry rejected announcement");
            Instance result = JsonKit.toPojo(JsonKit.toJsonString(data.get("instance")), Instance.class);
            if (result.getId() == null || result.getApp_id() == null || !result.getApp_id().equals(data.get("app_id"))
                    || !process.getHost().equals(result.getHost()) || !process.getPort().equals(result.getPort()))
                throw new IllegalStateException("Service registry acknowledgement identity mismatch");
            return result;
        } catch (Exception failure) {
            throw new IllegalStateException("Service registry announcement failed", failure);
        }
    }

    /**
     * Posts a registry announcement and closes the exchange after reading the response.
     *
     * @param url        registry operation URL
     * @param payload    serialized announcement
     * @param timeout    request timeout
     * @param credential supplier of the service token, which may return {@code null}
     * @return response body from the registry
     * @throws IllegalStateException if the registry returns an unsuccessful status
     */
    private static String post(String url, String payload, Duration timeout, Supplier<String> credential) {
        try (Context context = Context.create()) {
            var request = Fabric.http(context).post(url);
            String token = credential.get();
            if (token != null)
                request.header(Http.Auth.SERVICE_TOKEN_HEADER, token);
            try (var response = request.json(payload).timeout(timeout).execute()) {
                if (!response.successful())
                    throw new IllegalStateException("Service registry rejected announcement");
                return response.text();
            }
        }
    }

}
