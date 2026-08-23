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
package org.miaixz.bus.auth;

import java.time.Duration;

import org.miaixz.bus.core.lang.Normal;

/**
 * Defines constants shared across bus-auth packages.
 *
 * @author Kimi Liu
 */
public class Builder {

    /**
     * Limitation stating that returned contacts follow the application's configured visibility scope.
     */
    public static final String REALM_LIMITATION_APPLICATION_VISIBLE_CONTACT_SCOPE = "application-visible-contact-scope";
    /**
     * Limitation stating that hierarchical continuation pages replay traversal from the root.
     */
    public static final String REALM_LIMITATION_HIERARCHY_PAGES_REPLAY_FROM_ROOT = "hierarchy-pages-replay-from-root";
    /**
     * Limitation stating that an upstream unpaged endpoint is fully reread for each continuation page.
     */
    public static final String REALM_LIMITATION_UNPAGED_REPLAY = "unpaged-endpoint-requires-full-response-replay";
    /**
     * Limitation stating that a changed replay projection invalidates the current snapshot continuation.
     */
    public static final String REALM_LIMITATION_REPLAY_CHANGE_FAILURE = "snapshot-replay-fails-when-source-projection-changes";
    /**
     * Limitation stating that a Variant provides snapshots without a recoverable change feed.
     */
    public static final String REALM_LIMITATION_SNAPSHOT_ONLY = "snapshot-only-no-change-feed";
    /**
     * Limitation stating that an identical resource key may be emitted again on later snapshot pages.
     */
    public static final String REALM_LIMITATION_REPEATED_RESOURCES = "snapshot-may-repeat-identical-resource-keys-across-pages";
    /**
     * Management target key for listing Realm users.
     */
    public static final String REALM_USERS = "users";
    /**
     * Management target key for retrieving one Realm user.
     */
    public static final String REALM_USER = "user";
    /**
     * Management target key for listing Realm organizations.
     */
    public static final String REALM_ORGANIZATIONS = "organizations";
    /**
     * Management target key for retrieving one Realm organization.
     */
    public static final String REALM_ORGANIZATION = "organization";
    /**
     * Management target key for listing users belonging to an organization.
     */
    public static final String REALM_ORGANIZATION_USERS = "organization-users";
    /**
     * Management target key for listing Realm groups.
     */
    public static final String REALM_GROUPS = "groups";
    /**
     * Management target key for retrieving one Realm group.
     */
    public static final String REALM_GROUP = "group";
    /**
     * Management target key for listing members of a Realm group.
     */
    public static final String REALM_GROUP_MEMBERS = "group-members";
    /**
     * Management target key for listing Realm roles.
     */
    public static final String REALM_ROLES = "roles";
    /**
     * Management target key for retrieving one Realm role.
     */
    public static final String REALM_ROLE = "role";
    /**
     * Management target key for listing members assigned to a role.
     */
    public static final String REALM_ROLE_MEMBERS = "role-members";
    /**
     * Management target key for listing role-assignment resources.
     */
    public static final String REALM_ROLE_ASSIGNMENTS = "role-assignments";
    /**
     * Management target key for listing service accounts or service principals.
     */
    public static final String REALM_SERVICE_ACCOUNTS = "service-accounts";
    /**
     * Management target key for reading a recoverable Realm change feed.
     */
    public static final String REALM_CHANGES = "changes";
    /**
     * Management target key for listing organization assignments.
     */
    public static final String REALM_ORGANIZATION_ASSIGNMENTS = "organization-assignments";
    /**
     * Cursor envelope field carrying the stable Vendor identifier.
     */
    public static final String VENDOR_FIELD = "vendor";
    /**
     * Cursor envelope field carrying the stable Variant identifier.
     */
    public static final String VARIANT_FIELD = "variant";
    /**
     * Cursor envelope field carrying the Realm operation identifier.
     */
    public static final String OPERATION_FIELD = "operation";
    /**
     * Cursor envelope field carrying the current finite pagination phase.
     */
    public static final String CURSOR_PHASE_FIELD = "phase";
    /**
     * Cursor envelope field carrying the resource kind selected for the phase.
     */
    public static final String CURSOR_KIND_FIELD = "kind";
    /**
     * Cursor envelope field containing the operation-specific continuation position.
     */
    public static final String CURSOR_POSITION_FIELD = "position";
    /**
     * Cursor position field containing a validated upstream token, offset, page index, marker, or next URL.
     */
    public static final String CURSOR_NEXT_FIELD = "next";
    /**
     * Replay cursor position field containing the next stable object offset.
     */
    public static final String CURSOR_OFFSET_FIELD = "offset";
    /**
     * Replay cursor position field containing the next relation offset within the current object.
     */
    public static final String CURSOR_RELATION_OFFSET_FIELD = "relation_offset";
    /**
     * Replay cursor position field binding continuation to the current stable parent identifier.
     */
    public static final String CURSOR_PARENT_ID_FIELD = "parent_id";
    /**
     * Replay cursor position field carrying the lowercase SHA-256 projection fingerprint.
     */
    public static final String CURSOR_FINGERPRINT_FIELD = "fingerprint";
    /**
     * Stable outcome-detail field carrying an upstream HTTP status.
     */
    public static final String HTTP_STATUS_FIELD = "http_status";
    /**
     * Stable outcome-detail field carrying a non-sensitive upstream error code.
     */
    public static final String ERROR_CODE_FIELD = "error_code";
    /**
     * Stable outcome-detail field carrying a parsed retry delay in seconds.
     */
    public static final String RETRY_AFTER_SECONDS_FIELD = "retry_after_seconds";
    /**
     * Maximum number of resources or relations returned in one Realm page collection.
     */
    public static final int MAXIMUM_REALM_PAGE_SIZE = Normal._500;
    /**
     * Maximum encoded character length accepted for an opaque Realm cursor.
     */
    public static final int MAXIMUM_REALM_CURSOR_LENGTH = Normal._8192;
    /**
     * Maximum JSON nesting depth accepted from Realm management APIs.
     */
    public static final int MAXIMUM_REALM_JSON_DEPTH = Normal._64;
    /**
     * Lifetime assigned to locally constructed upstream JWT assertions.
     */
    public static final Duration UPSTREAM_ASSERTION_LIFETIME = Duration.ofHours(Normal._1);
    /**
     * Source-private cache key used for one upstream access token.
     */
    public static final String UPSTREAM_ACCESS_TOKEN_CACHE_KEY = "upstream-access-token";
    /**
     * Maximum safety interval subtracted from an upstream access-token lifetime before caching.
     */
    public static final Duration UPSTREAM_ACCESS_TOKEN_MAXIMUM_SKEW = Duration.ofSeconds(Normal._60);
    /**
     * Divisor used to derive the proportional access-token expiration safety interval.
     */
    public static final int UPSTREAM_ACCESS_TOKEN_SKEW_DIVISOR = Normal._10;
    /**
     * Maximum size accepted for a bounded JSON or remote response document.
     */
    public static final long MAXIMUM_DOCUMENT_BYTES = Normal.MEBI;
    /**
     * Maximum retry count for optimistic create, update, and state transitions.
     */
    public static final int MAXIMUM_RETRY_ATTEMPTS = Normal._3;
    /**
     * Stable failure-detail key carrying an OAuth error code.
     */
    public static final String OAUTH_ERROR = "oauth_error";
    /**
     * Stable failure-detail key recording that redirect validation has completed.
     */
    public static final String REDIRECT_VALIDATED = "redirect_validated";
    /**
     * Registered JOSE public-key use for signatures.
     */
    public static final String SIGNATURE = "sig";
    /**
     * Framework key-material purpose for signing operations.
     */
    public static final String SIGNING = "signing";
    /**
     * Registered JOSE key operation for signature verification.
     */
    public static final String VERIFY = "verify";
    /**
     * Stable capability key for beginning external Source authentication.
     */
    public static final String SOURCE_AUTHENTICATION_INITIATE = "source_authentication.initiate";
    /**
     * Stable capability key for completing external Source authentication.
     */
    public static final String SOURCE_AUTHENTICATION_COMPLETE = "source_authentication.complete";
    /**
     * Diagnostic marker for an absent value.
     */
    public static final String ABSENT_VALUE = "[ABSENT]";
    /**
     * Diagnostic marker for a configured value whose material must not be rendered.
     */
    public static final String CONFIGURED_VALUE = "[CONFIGURED]";
    /**
     * Diagnostic marker for redacted sensitive material.
     */
    public static final String REDACTED_VALUE = "[REDACTED]";
    /**
     * Shared diagnostic fragment for Source option values whose secrets must not be rendered.
     */
    public static final String REDACTED_SOURCE_OPTIONS = ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=";
    /**
     * Shared diagnostic fragment preceding a Vendor variant identifier.
     */
    public static final String VARIANT = ", variant=";
    /**
     * Shared diagnostic prefix for access-token results.
     */
    public static final String REDACTED_ACCESS_TOKEN = "Access[accessToken=[REDACTED], expiresIn=";

    /**
     * Creates an authentication constant holder with no retained state.
     */
    public Builder() {
        // No initialization required.
    }

    /**
     * Derives the common lowercase SHA-256 hexadecimal representation used for irreversible protocol indexes.
     *
     * @param value non-secret or opaque protocol value
     * @return lowercase SHA-256 hexadecimal digest
     */
    public static String sha256Hex(final String value) {
        return org.miaixz.bus.crypto.Builder.sha256Hex(value);
    }

}
