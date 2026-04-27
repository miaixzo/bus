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
package org.miaixz.bus.cortex;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.builtin.Label;
import org.miaixz.bus.cortex.builtin.LabelMapper;

import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all Cortex assets, covering both registry entries and gateway routing blueprints.
 * <p>
 * Extends {@link Nature} with application-scoped routing fields, expiration control ({@code ttl}), structured metadata,
 * and transient label views. Subclasses add domain-specific metadata accessors on top of the same base fields.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Assets extends Nature {

    /**
     * Creates an empty asset instance.
     */
    public Assets() {

    }

    /**
     * Human-readable name of the asset. Used for display in dashboards and logs.
     */
    private String name;

    /**
     * Optional icon URL or identifier for administrative portals.
     */
    private String icon;

    /**
     * Hostname or IP address of the downstream service.
     */
    private String host;

    /**
     * Network port of the downstream service.
     */
    private Integer port;

    /**
     * Context path on the downstream service, prefixed when forwarding requests.
     */
    private String path;

    /**
     * Actual access address of the target application endpoint.
     */
    private String url;

    /**
     * Application identifier bound to the asset within the current namespace.
     */
    private String app_id;

    /**
     * Logical asset method identifier used for lookup within one namespace and application.
     */
    private String method;

    /**
     * Upstream interaction mode.
     * <ul>
     * <li>{@code 1}: HTTP/HTTPS reverse proxy</li>
     * <li>{@code 2}: Message queue</li>
     * <li>{@code 3}: Model Context Protocol (MCP)</li>
     * <li>{@code 4}: gRPC</li>
     * <li>{@code 5}: WebSocket</li>
     * </ul>
     */
    private Integer protocol;

    /**
     * Streaming output flag. {@code 1} = atomic (buffered), {@code 2} = streaming (flushed).
     */
    private Integer stream;

    /**
     * HTTP request method (verb). {@code 1}=GET, {@code 2}=POST, {@code 3}=HEAD, {@code 4}=PUT, {@code 5}=PATCH,
     * {@code 6}=DELETE, {@code 7}=OPTIONS, {@code 8}=TRACE, {@code 9}=CONNECT.
     */
    private Integer verb;

    /**
     * Access control policy / security level. Valid range: -1 to 6.
     * <ul>
     * <li>{@code 0}: Anonymous</li>
     * <li>{@code 1}: Token</li>
     * <li>{@code 2}: Token + permissions</li>
     * <li>{@code 3}: Token + permissions + license</li>
     * <li>{@code 4}: AppKey</li>
     * <li>{@code 5}: AppKey + permissions</li>
     * <li>{@code 6}: AppKey + permissions + license</li>
     * </ul>
     */
    private Integer policy;

    /**
     * Signature verification flag. {@code 0} = disabled, {@code 1} = enabled.
     */
    private Integer sign;

    /**
     * OAuth2 scope ID / permission level required to access this asset.
     */
    private Integer scope;

    /**
     * Maximum number of automatic retry attempts for transient failures. Default: {@code 3}.
     */
    @Builder.Default
    private Integer retries = 3;

    /**
     * Request timeout in milliseconds. Default: {@code 60}.
     */
    @Builder.Default
    private Integer timeout = 60;

    /**
     * Per-minute request rate limit. {@code null} or {@code 0} disables rate limiting.
     */
    private Integer throttle;

    /**
     * Load-balancing strategy for distributing traffic among upstream instances.
     */
    private Integer balance;

    /**
     * Routing weight relative to other assets in a weighted balancing scenario.
     */
    private Integer weight;

    /**
     * Command line string used when {@code mode} is STDIO.
     */
    private String command;

    /**
     * JSON-serialized environment variables injected into the process when {@code mode} is STDIO.
     */
    private String env;

    /**
     * Display or matching priority. Lower values indicate higher priority.
     */
    private Integer sort;

    /**
     * Mock mode flag. {@code 1} = return mock data from {@link #result}; {@code 0} = normal.
     */
    private Integer mock;

    /**
     * Mock response data returned when {@link #mock} is enabled.
     */
    private String result;

    /**
     * Asset version identifier used for version negotiation and lifecycle management.
     */
    private String version;

    /**
     * JSON-serialized extension metadata for complex or vendor-specific configuration.
     */
    private String metadata;

    /**
     * Human-readable description of the asset's functionality.
     */
    private String description;

    /**
     * Entry expiration time in milliseconds. A value of {@code 0} means no expiration.
     */
    @Transient
    @Builder.Default
    private long ttl = 3600_000L;

    /**
     * Structured key-value labels attached to the entry, used for routing, filtering and discovery.
     */
    @Transient
    private Map<String, String> labels;

    /**
     * Returns whether the asset is currently configured to answer with mock content.
     *
     * @return {@code true} when mock responses are enabled
     */
    public boolean mockEnabled() {
        return mock != null && mock > 0;
    }

    /**
     * Returns the effective retry count.
     *
     * @return positive retry count
     */
    public int retryCount() {
        return retries == null || retries < 0 ? 0 : retries;
    }

    /**
     * Returns the effective timeout in milliseconds.
     *
     * @return positive timeout value
     */
    public int timeoutMs() {
        return timeout == null || timeout <= 0 ? 60 : timeout;
    }

    /**
     * Returns whether the asset contains one exact label.
     *
     * @param key   label key
     * @param value expected label value
     * @return {@code true} when the label matches
     */
    public boolean hasLabel(String key, String value) {
        return labels != null && value != null && value.equals(labels.get(key));
    }

    /**
     * Replaces the raw metadata payload.
     *
     * @param metadata raw metadata JSON
     */
    public void metadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns an immutable label view for routing and filtering code paths.
     *
     * @return immutable label map
     */
    public Map<String, String> labelView() {
        return labels == null ? Collections.emptyMap() : Collections.unmodifiableMap(labels);
    }

    /**
     * Returns management-facing label items converted from runtime labels.
     *
     * @return label items
     */
    public List<Label> labelItems() {
        return LabelMapper.toLabels(labels);
    }

    /**
     * Sets runtime labels from management-facing label items.
     *
     * @param labelItems label items
     */
    public void labelItems(List<Label> labelItems) {
        this.labels = LabelMapper.toMap(labelItems);
    }

}
