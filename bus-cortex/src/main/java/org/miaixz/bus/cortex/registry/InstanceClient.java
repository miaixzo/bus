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
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.cortex.guard.token.TrustToken;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.crypto.Builder;
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
     * Configured registry URL, application code and signing material; never emitted in diagnostics.
     */
    private final String endpoint, code, credential;

    /**
     * Process-owned instance address and registration metadata.
     */
    private final Instance process;

    /**
     * Transport deadline applied to each instance registration or lease renewal request.
     */
    private final Duration timeout;

    /**
     * Last server acknowledgement, including canonical App ID and current service lease.
     */
    private Instance acknowledged;

    /**
     * Creates a service client and normalizes the effective port for registration and lease acknowledgements.
     *
     * @param endpoint   exact instance registration endpoint
     * @param code       application directory code
     * @param encodedKey configured registry master key
     * @param process    current process address
     * @param timeout    request timeout
     */
    public InstanceClient(String endpoint, String code, String encodedKey, Instance process, Duration timeout) {
        ServiceRoutes.validate(process);
        this.endpoint = endpoint;
        this.code = code;
        this.process = BeanKit.toBean(process, Instance.class);
        this.process.setPort(ServiceRoutes.address(process).getPort());
        this.timeout = timeout;
        byte[] key = Base64.getUrlDecoder().decode(encodedKey);
        try {
            if (key.length != Normal._32 || code == null || code.isBlank())
                throw new IllegalArgumentException("Complete registry service credentials required");
            var encoder = Base64.getUrlEncoder().withoutPadding();
            this.credential = org.miaixz.bus.cortex.Builder.REGISTRY_CREDENTIAL_VERSION + Symbol.DOT
                    + encoder.encodeToString(code.getBytes(Charset.UTF_8)) + Symbol.DOT
                    + encoder.encodeToString(
                            Builder.hmacSha256(key).digest(
                                    (org.miaixz.bus.cortex.Builder.REGISTRY_FORWARD_CONTEXT + code)
                                            .getBytes(Charset.UTF_8)));
        } finally {
            Arrays.fill(key, (byte) Normal._0);
        }
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
            exchange(endpoint + Http.Path.REMOVE, acknowledged);
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
        try (Context context = Context.create()) {
            var request = Fabric.http(context).post(url).header(Http.Auth.API_KEY_HEADER, credential);
            final String token = TrustToken.registry();
            if (token != null) {
                request.header(Http.Auth.SERVICE_TOKEN_HEADER, token);
            }
            try (var response = request.json(JsonKit.toJsonString(Map.of("code", code, "instance", instance)))
                    .timeout(timeout).execute()) {
                Object decoded = JsonKit.toJavaValue(JsonKit.readValue(response.text().getBytes(Charset.UTF_8)));
                if (!response.successful() || !(decoded instanceof Map<?, ?> body)
                        || !ErrorCode._SUCCESS.getKey().equals(String.valueOf(body.get("code")))
                        || !(body.get("data") instanceof Map<?, ?> data) || !code.equals(data.get("code"))
                        || !(data.get("instance") instanceof Map<?, ?>))
                    throw new IllegalStateException("Service registry rejected announcement");
                Instance result = JsonKit.toPojo(JsonKit.toJsonString(data.get("instance")), Instance.class);
                if (result.getId() == null || result.getApp_id() == null || !result.getApp_id().equals(data.get("app_id"))
                        || !process.getHost().equals(result.getHost()) || !process.getPort().equals(result.getPort()))
                    throw new IllegalStateException("Service registry acknowledgement identity mismatch");
                return result;
            }
        } catch (Exception failure) {
            throw new IllegalStateException("Service registry announcement failed", failure);
        }
    }

}
