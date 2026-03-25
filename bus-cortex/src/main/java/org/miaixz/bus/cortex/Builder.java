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

import org.miaixz.bus.core.lang.Symbol;

/**
 * Shared constants for bus-cortex.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Builder {

    /**
     * Creates a new Builder.
     */
    private Builder() {

    }

    /**
     * Default namespace used when no explicit namespace is specified.
     */
    public static final String DEFAULT_NAMESPACE = "default";
    /**
     * Default health probe interval in milliseconds (30 s). Recommended value for
     * {@link org.miaixz.bus.cortex.registry.HealthProbeScheduler}.
     */
    public static final long DEFAULT_HEALTH_INTERVAL_MS = 30000L;
    /**
     * Default health probe timeout in milliseconds (5 s). Used by {@link org.miaixz.bus.cortex.health.TcpProber} and
     * recommended for {@link org.miaixz.bus.cortex.registry.HealthProbeScheduler}.
     */
    public static final long DEFAULT_HEALTH_TIMEOUT_MS = 5000L;
    /**
     * Default access token expiry in seconds (24 h). Recommended value for
     * {@link org.miaixz.bus.cortex.guard.token.AccessTokenStore}.
     */
    public static final long DEFAULT_TOKEN_EXPIRE_SECONDS = 86400L;

    /**
     * CacheX key prefix for registry entries ({@code reg:}).
     */
    public static final String REG_PREFIX = "reg" + Symbol.COLON;
    /**
     * CacheX key prefix for configuration entries ({@code cfg:}).
     */
    public static final String CFG_PREFIX = "cfg" + Symbol.COLON;
    /**
     * CacheX key prefix for security and token entries ({@code sec:}).
     */
    public static final String SECURITY_PREFIX = "sec" + Symbol.COLON;
    /**
     * CacheX key prefix for sequence counters ({@code seq:}).
     */
    public static final String SEQUENCE_PREFIX = "seq" + Symbol.COLON;
    /**
     * CacheX key prefix for audit log entries ({@code audit:}).
     */
    public static final String AUDIT_PREFIX = "audit" + Symbol.COLON;

    // -------------------------------------------------------------------------
    // Cortex HTTP endpoint paths — use directly with Spring @RequestMapping
    // -------------------------------------------------------------------------

    /**
     * Base path for all Cortex HTTP endpoints ({@code /cortex}).
     */
    public static final String PATH_CORTEX = "/cortex";

    /**
     * Register an API service instance ({@code /api/register}).
     */
    public static final String PATH_API_REGISTER = "/api/register";
    /**
     * Deregister an API service instance ({@code /api/deregister}).
     */
    public static final String PATH_API_DEREGISTER = "/api/deregister";
    /**
     * Query API service instances ({@code /api/query}).
     */
    public static final String PATH_API_QUERY = "/api/query";

    /**
     * Register an MCP server ({@code /mcp/register}).
     */
    public static final String PATH_MCP_REGISTER = "/mcp/register";
    /**
     * Deregister an MCP server ({@code /mcp/deregister}).
     */
    public static final String PATH_MCP_DEREGISTER = "/mcp/deregister";
    /**
     * Query MCP servers ({@code /mcp/query}).
     */
    public static final String PATH_MCP_QUERY = "/mcp/query";

    /**
     * Register a prompt template ({@code /prompt/register}).
     */
    public static final String PATH_PROMPT_REGISTER = "/prompt/register";
    /**
     * Deregister a prompt template ({@code /prompt/deregister}).
     */
    public static final String PATH_PROMPT_DEREGISTER = "/prompt/deregister";
    /**
     * Query prompt templates ({@code /prompt/query}).
     */
    public static final String PATH_PROMPT_QUERY = "/prompt/query";

    /**
     * Register a version record ({@code /version/register}).
     */
    public static final String PATH_VERSION_REGISTER = "/version/register";
    /**
     * Deregister a version record ({@code /version/deregister}).
     */
    public static final String PATH_VERSION_DEREGISTER = "/version/deregister";
    /**
     * Query version records ({@code /version/query}).
     */
    public static final String PATH_VERSION_QUERY = "/version/query";

    /**
     * Publish a configuration item ({@code /config/publish}).
     */
    public static final String PATH_CONFIG_PUBLISH = "/config/publish";
    /**
     * Retrieve a configuration item ({@code /config/get}).
     */
    public static final String PATH_CONFIG_GET = "/config/get";
    /**
     * Roll back a configuration item to a previous version ({@code /config/rollback}).
     */
    public static final String PATH_CONFIG_ROLLBACK = "/config/rollback";
    /**
     * Subscribe to configuration change notifications ({@code /config/watch}).
     */
    public static final String PATH_CONFIG_WATCH = "/config/watch";
    /**
     * Unsubscribe from configuration change notifications ({@code /config/unwatch}).
     */
    public static final String PATH_CONFIG_UNWATCH = "/config/unwatch";

    /**
     * Subscribe to registry watch events ({@code /watch/subscribe}).
     */
    public static final String PATH_WATCH_SUBSCRIBE = "/watch/subscribe";
    /**
     * Unsubscribe from registry watch events ({@code /watch/unsubscribe}).
     */
    public static final String PATH_WATCH_UNSUBSCRIBE = "/watch/unsubscribe";

    /**
     * Internal registry sync path used by the Vortex bridge ({@code /_internal/registry/sync}).
     */
    public static final String INTERNAL_SYNC_PATH = "/_internal/registry/sync";

}
