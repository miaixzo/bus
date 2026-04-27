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
package org.miaixz.bus.core.net;

/**
 * Centralized specifications for network-facing contracts and communication conventions.
 *
 * <p>
 * This class acts as the "source of truth" for the system's network vocabulary, consolidating non-standardized HTTP
 * exchange definitions into a single, reusable registry. It ensures consistency across various bus-level modules and
 * third-party integrations by defining:
 * </p>
 *
 * <ul>
 * <li><b>Protocol Controls:</b> Mandatory parameters for method routing, versioning, and integrity signatures.</li>
 * <li><b>Resource Operations:</b> Standardized path mappings for CRUD actions, batch processing, and registry
 * transfers.</li>
 * <li><b>Observability and Ops:</b> Cloud-native diagnostic endpoints for health, liveness, and metric
 * exportation.</li>
 * <li><b>Identity and Security:</b> Candidate header/parameter aliases for multi-format access token transport.</li>
 * <li><b>Standard Discoveries:</b> RFC 8615 well-known locations and standard static web resources.</li>
 * </ul>
 *
 * <p>
 * By centralizing these "dialects," the library promotes a "Convention over Configuration" approach, reducing
 * hard-coded strings in upper-layer request construction and parsing.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Specifics {

    /**
     * The mandatory parameter name for the logical API method to be invoked (e.g., "user.getProfile").
     */
    public static final String METHOD = "method";

    /**
     * The parameter name for specifying the desired response format (e.g., "json", "xml").
     */
    public static final String FORMAT = "format";

    /**
     * The parameter name for specifying the version of the requested API method (e.g., "v1", "1.0.0").
     */
    public static final String VERSION = "v";

    /**
     * The parameter name for the request signature, used for validation and integrity checks.
     */
    public static final String SIGN = "sign";

    /**
     * The parameter name for the request timestamp (milliseconds since epoch), used for replay attack prevention.
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * Candidate parameter and header names for backward-compatible access-token transport.
     */
    public static final String[] TOKEN_KEYS = { "Authorization", "authorization", "AUTHORIZATION", "X-Access-Token",
            "X-ACCESS-TOKEN", "x-access-token", "X_Access_Token", "X_ACCESS_TOKEN", "x_access_token" };
    /**
     * Candidate parameter and header names for API-key transport.
     */
    public static final String[] API_KEY_KEYS = { "apiKey", "apikey", "api_key", "x_api_key", "api_id", "x_api_id",
            "X-API-ID", "X-API-KEY", "API-KEY", "API-ID" };

    /** --- CRUD & Resource Actions (Explicit Operations) --- */

    /**
     * Resource retrieval ({@code /get}). Explicit path for fetching a single resource.
     */
    public static final String MAPPING_GET = "/get";
    /**
     * Resource creation ({@code /create}). Explicit path for adding new resources.
     */
    public static final String MAPPING_CREATE = "/create";
    /**
     * Resource deletion ({@code /delete}). Explicit path for removing resources.
     */
    public static final String MAPPING_DELETE = "/delete";
    /**
     * Resource removal ({@code /remove}). Synonym for delete, common in many APIs.
     */
    public static final String MAPPING_REMOVE = "/remove";
    /**
     * Resource update ({@code /update}). Explicit path for modification.
     */
    public static final String MAPPING_UPDATE = "/update";
    /**
     * Resource save ({@code /save}). Often used for upsert (create or update) operations.
     */
    public static final String MAPPING_SAVE = "/save";
    /**
     * Resource edit ({@code /edit}). Common in CMS or form-based systems.
     */
    public static final String MAPPING_EDIT = "/edit";
    /**
     * Resource detail ({@code /view}). Explicit path for viewing a resource.
     */
    public static final String MAPPING_VIEW = "/view";
    /**
     * Resource list ({@code /list}). Standard for collection retrieval.
     */
    public static final String MAPPING_LIST = "/list";
    /**
     * Resource query ({@code /query}). Used for complex filtered searches.
     */
    public static final String MAPPING_QUERY = "/query";
    /**
     * Resource query ({@code /page}). Used for complex filtered searches.
     */
    public static final String MAPPING_PAGE = "/page";
    /**
     * Data extraction ({@code /extract}). Used for pulling specific fields or sub-resources.
     */
    public static final String MAPPING_EXTRACT = "/extract";
    /**
     * Resource preview ({@code /preview}). Standard for seeing changes before committing.
     */
    public static final String MAPPING_PREVIEW = "/preview";

    /** --- Registry & Data Transfer (Storage Standards) --- */

    /**
     * Base path for registry endpoints ({@code /registry}).
     */
    public static final String MAPPING_REGISTRY = "/registry";
    /**
     * Registry push ({@code /push}). Industry standard for pushing data/images.
     */
    public static final String MAPPING_PUSH = "/push";
    /**
     * Registry pull ({@code /pull}). Industry standard for pulling data/images.
     */
    public static final String MAPPING_PULL = "/pull";
    /**
     * Data fetch ({@code /fetch}). Used for retrieving data sets or configurations.
     */
    public static final String MAPPING_FETCH = "/fetch";
    /**
     * Multi-part upload ({@code /upload}). Standard for file or stream uploads.
     */
    public static final String MAPPING_UPLOAD = "/upload";
    /**
     * Binary download ({@code /download}). Standard for resource retrieval.
     */
    public static final String MAPPING_DOWNLOAD = "/download";
    /**
     * Image manifests ({@code /manifests}). OCI/Docker metadata path.
     */
    public static final String MAPPING_MANIFESTS = "/manifests";
    /**
     * Version tags ({@code /tags}). Standard for resource versioning.
     */
    public static final String MAPPING_TAGS = "/tags";
    /**
     * Repository catalog ({@code /catalog}). Standard OCI repository listing.
     */
    public static final String MAPPING_CATALOG = "/catalog";
    /**
     * Upload initiation ({@code /initiate}). S3/TUS protocol standard.
     */
    public static final String MAPPING_INITIATE = "/initiate";
    /**
     * Upload completion ({@code /complete}). Finalizing chunked transfers.
     */
    public static final String MAPPING_COMPLETE = "/complete";
    /**
     * Upload part ({@code /part}). Used for specific chunk uploads.
     */
    public static final String MAPPING_PART = "/part";

    /** --- Observability & Ops (Maintenance & Diagnostics) --- */

    /**
     * Health status ({@code /health}). Main health check endpoint.
     */
    public static final String MAPPING_HEALTH = "/health";
    /**
     * Liveness probe ({@code /liveness}). Kubernetes standard for process running.
     */
    public static final String MAPPING_LIVENESS = "/liveness";
    /**
     * Readiness probe ({@code /readiness}). Kubernetes standard for traffic ready.
     */
    public static final String MAPPING_READINESS = "/readiness";
    /**
     * Metrics export ({@code /metrics}). Prometheus/OTLP standard.
     */
    public static final String MAPPING_METRICS = "/metrics";
    /**
     * System info ({@code /info}). Build, version, or environment metadata.
     */
    public static final String MAPPING_INFO = "/info";
    /**
     * Network ping ({@code /ping}). Minimal reachability check.
     */
    public static final String MAPPING_PING = "/ping";
    /**
     * Log access ({@code /logs}). Diagnostic or audit logs.
     */
    public static final String MAPPING_LOGS = "/logs";
    /**
     * Heap dump ({@code /dump}). JVM or native memory analysis.
     */
    public static final String MAPPING_DUMP = "/dump";
    /**
     * Thread dump ({@code /threaddump}). Diagnostic thread analysis.
     */
    public static final String MAPPING_THREADDUMP = "/threaddump";
    /**
     * Environment config ({@code /env}). View environment variables.
     */
    public static final String MAPPING_ENV = "/env";

    /** --- Control Plane & Business (Jobs & Workflows) --- */

    /**
     * Task trigger ({@code /trigger}). Starting a workflow or background job.
     */
    public static final String MAPPING_TRIGGER = "/trigger";
    /**
     * Status polling ({@code /status}). Checking async job progress.
     */
    public static final String MAPPING_STATUS = "/status";
    /**
     * Job cancellation ({@code /cancel}). Aborting running operations.
     */
    public static final String MAPPING_CANCEL = "/cancel";
    /**
     * Config reload ({@code /config}). Hot-reloading application settings.
     */
    public static final String MAPPING_CONFIG = "/config";
    /**
     * Data sync ({@code /sync}). Node-to-node state synchronization.
     */
    public static final String MAPPING_SYNC = "/sync";
    /**
     * Batch processing ({@code /batch}). Single call for multiple items.
     */
    public static final String MAPPING_BATCH = "/batch";
    /**
     * Bulk import ({@code /import}). Mass data ingestion or migration.
     */
    public static final String MAPPING_IMPORT = "/import";
    /**
     * Bulk export ({@code /export}). Mass data archive generation.
     */
    public static final String MAPPING_EXPORT = "/export";
    /**
     * Operation retry ({@code /retry}). Re-executing failed tasks.
     */
    public static final String MAPPING_RETRY = "/retry";
    /**
     * Operation undo ({@code /rollback}). Reverting changes or transactions.
     */
    public static final String MAPPING_ROLLBACK = "/rollback";

    /** --- Security & Identity (Authentication & Gateway) --- */

    /**
     * Auth entry ({@code /auth}). Login or identity verification.
     */
    public static final String MAPPING_AUTH = "/auth";
    /**
     * Token issue ({@code /token}). OAuth2 access token endpoint.
     */
    public static final String MAPPING_TOKEN = "/token";
    /**
     * Token revoke ({@code /revoke}). OAuth2 token invalidation.
     */
    public static final String MAPPING_REVOKE = "/revoke";
    /**
     * Challenge/Verify ({@code /verify}). MFA or checksum verification.
     */
    public static final String MAPPING_VERIFY = "/verify";
    /**
     * Personal profile ({@code /me}). Currently authenticated user information.
     */
    public static final String MAPPING_ME = "/me";
    /**
     * Public keys ({@code /keys}). JWKS distribution endpoint for JWT validation.
     */
    public static final String MAPPING_KEYS = "/keys";
    /**
     * Authorization ({@code /authorize}). OAuth2 permission grant endpoint.
     */
    public static final String MAPPING_AUTHORIZE = "/authorize";
    /**
     * Token introspection ({@code /introspect}). Validating access tokens.
     */
    public static final String MAPPING_INTROSPECT = "/introspect";
    /**
     * Session management ({@code /session}). Handling user session states.
     */
    public static final String MAPPING_SESSION = "/session";

    /** --- Communication & Interaction (Real-time & Discovery) --- */

    /**
     * Search engine ({@code /search}). Resource discovery and querying.
     */
    public static final String MAPPING_SEARCH = "/search";
    /**
     * Resource metadata ({@code /metadata}). Retrieves properties without content.
     */
    public static final String MAPPING_METADATA = "/metadata";
    /**
     * API Capabilities ({@code /capabilities}). Server feature matrix.
     */
    public static final String MAPPING_CAPABILITIES = "/capabilities";
    /**
     * WebSocket entry ({@code /ws}). Industry standard shorthand for socket connections.
     */
    public static final String MAPPING_WS = "/ws";
    /**
     * Event stream ({@code /events}). Standard for Server-Sent Events (SSE).
     */
    public static final String MAPPING_EVENTS = "/events";
    /**
     * Webhook callback ({@code /webhook}). Entry point for third-party notifications.
     */
    public static final String MAPPING_WEBHOOK = "/webhook";
    /**
     * Resource subscription ({@code /subscribe}). Pub/Sub listener initiation.
     */
    public static final String MAPPING_SUBSCRIBE = "/subscribe";
    /**
     * Message publication ({@code /publish}). Pub/Sub producer endpoint.
     */
    public static final String MAPPING_PUBLISH = "/publish";

    /** --- Protocol Level (RFC 8615 & Static Files) --- */

    /**
     * OIDC config ({@code /.well-known/openid-configuration}). Discovery for identity providers.
     */
    public static final String WELL_KNOWN_OIDC = "/.well-known/openid-configuration";
    /**
     * Security contact ({@code /.well-known/security.txt}). Standard for security policy discovery.
     */
    public static final String WELL_KNOWN_SECURITY = "/.well-known/security.txt";
    /**
     * Robots control ({@code /robots.txt}). Web crawler instructions.
     */
    public static final String ROBOTS_TXT = "/robots.txt";
    /**
     * Sitemap ({@code /sitemap.xml}). Search engine indexing instructions.
     */
    public static final String SITEMAP_XML = "/sitemap.xml";
    /**
     * Favicon ({@code /favicon.ico}). Standard website icon path.
     */
    public static final String FAVICON_ICO = "/favicon.ico";

}
