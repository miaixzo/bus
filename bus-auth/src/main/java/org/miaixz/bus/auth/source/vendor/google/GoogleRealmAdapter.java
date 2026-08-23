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
package org.miaixz.bus.auth.source.vendor.google;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.FabricX.UrlBuilder;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.auth.worker.loader.KeyLoader;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Payload;

/**
 * Implements the implementation-neutral Google Workspace realm administration surface.
 * <p>
 * Snapshot pagination advances through six finite phases in this order: organization units, users, groups, group
 * members, administrator roles, and role assignments. Organization units use complete unpaged replay with a stable
 * projection fingerprint; official page tokens drive every other collection. Group-member continuation stores only the
 * stable current group and its official member page token. A domain-wide delegated service-account assertion is signed
 * through the shared Bus JOSE service, while externally loaded private-key material remains local to the synchronous
 * signing method.
 * </p>
 *
 * @author Kimi Liu
 */
public class GoogleRealmAdapter implements VendorAdapter {

    /**
     * Empty immutable JSON object used where the frozen mapping exposes no attributes.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Exact resource categories exposed by Google Workspace.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set
            .of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.GROUP, Realm.Kind.ROLE);

    /**
     * Ordered management-target key closure required from the Workspace manifest.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.REALM_USERS,
            Builder.REALM_USER,
            Builder.REALM_ORGANIZATIONS,
            Builder.REALM_ORGANIZATION,
            Builder.REALM_GROUPS,
            Builder.REALM_GROUP,
            Builder.REALM_GROUP_MEMBERS,
            Builder.REALM_ROLES,
            Builder.REALM_ROLE_MEMBERS,
            Builder.REALM_ROLE_ASSIGNMENTS);

    /**
     * RFC 7523 JWT bearer grant type sent to the Google OAuth token endpoint.
     */
    private static final String JWT_BEARER_GRANT = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    /**
     * OAuth form field carrying the signed service-account assertion.
     */
    private static final String ASSERTION = "assertion";

    /**
     * Exact Google OAuth audience used in both the assertion and token request.
     */
    private static final String TOKEN_AUDIENCE = "https://oauth2.googleapis.com/token";

    /**
     * Protected JOSE type value required for the service-account assertion.
     */
    private static final String JWT_TYPE = "JWT";

    /**
     * Selected immutable Google Workspace Variant.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated Google Workspace deployment options.
     */
    private final GoogleOptions options;

    /**
     * Caller-owned execution services used without taking lifecycle ownership.
     */
    private final SourceServices services;

    /**
     * Resolved token and Admin SDK endpoints declared by the selected manifest.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Source-private cache containing only a short-lived delegated access token.
     */
    private final CacheX<String, Access> accessCache;

    /**
     * Shared Bus JWS implementation constrained to the sole RS256 algorithm.
     */
    private final JwsService jwsService;

    /**
     * Creates one Source-isolated Google Workspace realm adapter.
     *
     * @param spaceId  Source space used for key isolation
     * @param sourceId Source identifier used for ownership validation
     * @param manifest exact Google manifest
     * @param variant  exact selected Workspace Variant
     * @param options  validated Workspace options
     * @param services capability-limited Source services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if manifest, Variant, protocol, targets, or options are inconsistent
     */
    public GoogleRealmAdapter(final String spaceId, final String sourceId, final GoogleManifest manifest,
            final VendorManifest.Variant variant, final GoogleOptions options, final SourceServices services) {
        Assert.notBlank(spaceId, "Google Workspace space id must not be blank");
        Assert.notBlank(sourceId, "Google Workspace Source id must not be blank");
        final GoogleManifest selectedManifest = Assert.notNull(manifest, "Google manifest must not be null");
        this.variant = Assert.notNull(variant, "Google Workspace Variant must not be null");
        this.options = Assert.notNull(options, "Google Workspace options must not be null");
        this.services = Assert.notNull(services, "Google Workspace services must not be null");
        if (!GoogleManifest.ID.equals(selectedManifest.vendor())
                || !GoogleManifest.WORKSPACE.equals(this.variant.variant())
                || !selectedManifest.variant(this.variant.variant()).equals(this.variant)
                || !GoogleManifest.ID.equals(this.variant.platform()) || this.variant.protocol() != Protocol.HTTPS
                || !GoogleManifest.ID.equals(this.options.vendor())
                || !GoogleManifest.WORKSPACE.equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || this.options.credential().type() != org.miaixz.bus.auth.Credential.Type.PRIVATE_KEY
                || !this.variant.defaultScopes().equals(this.options.scopes())) {
            throw new ValidateException("Google realm adapter requires the frozen Workspace HTTPS Variant");
        }
        this.services.policies().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (this.targets.token().isEmpty()
                || !List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("Google Workspace manifest has an invalid management target set");
        }
        this.accessCache = new MemoryCache<>(FabricX.clock()::millis);
        this.jwsService = new JwsService(new org.miaixz.bus.auth.guard.AlgorithmGuard(),
                Set.of(JwaAlgorithm.RS256.name()));
    }

    /**
     * Reports whether one authenticated operation was rejected for an invalid delegated token.
     *
     * @param outcome completed authenticated operation outcome
     * @return whether the outcome carries the shared 401 error
     */
    private static boolean unauthorized(final Outcome<?> outcome) {
        return outcome instanceof Outcome.Rejected<?> rejected && ErrorCode._401.equals(rejected.failure().error());
    }

    /**
     * Creates the standard Google page query with an optional official page token.
     *
     * @param limit requested maximum page size
     * @param next  optional official continuation token
     * @return mutable ordered query used only to add endpoint-specific fixed members
     */
    private static Map<String, String> pageQuery(final int limit, final Optional<String> next) {
        final Map<String, String> query = new LinkedHashMap<>();
        query.put("maxResults", Integer.toString(limit));
        next.ifPresent(token -> query.put("pageToken", token));
        return query;
    }

    /**
     * Converts one minimal Google user projection to a implementation-neutral resource.
     *
     * @param user       validated user projection
     * @param observedAt shared observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()),
                orderedIdentifiers("primaryEmail", user.primaryEmail()), user.name(),
                user.suspended() ? Realm.State.INACTIVE : Realm.State.ACTIVE, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one minimal Google organization projection to a implementation-neutral resource.
     *
     * @param organization validated organization projection
     * @param observedAt   shared observation instant
     * @return immutable organization resource
     */
    private static Realm.Resource organizationResource(final Organization organization, final Instant observedAt) {
        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        attributes.put("orgUnitPath", new JsonValue.StringValue(organization.path()));
        return new Realm.Resource(new Realm.Key(Realm.Kind.ORGANIZATION, organization.id()), Map.of(),
                organization.name(), Realm.State.UNKNOWN, new JsonValue.ObjectValue(attributes), observedAt);
    }

    /**
     * Converts one minimal Google group projection to a implementation-neutral resource.
     *
     * @param group      validated group projection
     * @param observedAt shared observation instant
     * @return immutable group resource
     */
    private static Realm.Resource groupResource(final Group group, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.GROUP, group.id()),
                orderedIdentifiers("email", group.email()), group.name(), Realm.State.UNKNOWN, EMPTY_ATTRIBUTES,
                observedAt);
    }

    /**
     * Converts one minimal Google administrator role to a implementation-neutral resource.
     *
     * @param role       validated role projection
     * @param observedAt shared observation instant
     * @return immutable role resource
     */
    private static Realm.Resource roleResource(final Role role, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.ROLE, role.id()), Map.of(), role.name(), Realm.State.UNKNOWN,
                EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one organization parent relation.
     *
     * @param organization child organization projection
     * @param observedAt   shared observation instant
     * @return immutable parent relation
     */
    private static Realm.Relation parentRelation(final Organization organization, final Instant observedAt) {
        return relation(
                Realm.RelationKind.PARENT,
                Realm.Kind.ORGANIZATION,
                organization.id(),
                Realm.Kind.ORGANIZATION,
                organization.parentId().getOrNull(),
                observedAt);
    }

    /**
     * Creates one user membership relation to an organization or group.
     *
     * @param userId     stable user identifier
     * @param targetKind organization or group category
     * @param targetId   stable target identifier
     * @param observedAt shared observation instant
     * @return immutable membership relation
     */
    private static Realm.Relation memberRelation(
            final String userId,
            final Realm.Kind targetKind,
            final String targetId,
            final Instant observedAt) {
        return relation(Realm.RelationKind.MEMBER, Realm.Kind.USER, userId, targetKind, targetId, observedAt);
    }

    /**
     * Creates one user-to-role membership relation from an official role assignment.
     *
     * @param assignment validated role assignment projection
     * @param observedAt shared observation instant
     * @return immutable role-member relation
     */
    private static Realm.Relation roleMemberRelation(final RoleAssignment assignment, final Instant observedAt) {
        return relation(
                Realm.RelationKind.ROLE_MEMBER,
                Realm.Kind.USER,
                assignment.userId(),
                Realm.Kind.ROLE,
                assignment.roleId(),
                observedAt);
    }

    /**
     * Creates one implementation-neutral relation without allow-listed attributes.
     *
     * @param relationKind normalized relation category
     * @param fromKind     normalized source resource category
     * @param fromId       stable source identifier
     * @param toKind       normalized target resource category
     * @param toId         stable target identifier
     * @param observedAt   shared observation instant
     * @return immutable implementation-neutral relation
     */
    private static Realm.Relation relation(
            final Realm.RelationKind relationKind,
            final Realm.Kind fromKind,
            final String fromId,
            final Realm.Kind toKind,
            final String toId,
            final Instant observedAt) {
        return new Realm.Relation(
                new Realm.RelationKey(relationKind, new Realm.Key(fromKind, fromId), new Realm.Key(toKind, toId)),
                EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one single-entry ordered identifier map.
     *
     * @param name  normalized identifier name
     * @param value exact identifier value
     * @return immutable ordered identifier map
     */
    private static Map<String, String> orderedIdentifiers(final String name, final String value) {
        final Map<String, String> identifiers = new LinkedHashMap<>();
        identifiers.put(name, value);
        return Collections.unmodifiableMap(identifiers);
    }

    /**
     * Adds one page resource, de-duplicating equal values and rejecting conflicting stable keys.
     *
     * @param resources page-local resource map
     * @param resource  normalized resource
     * @param label     safe projection label
     */
    private static void putResource(
            final Map<Realm.Key, Realm.Resource> resources,
            final Realm.Resource resource,
            final String label) {
        final Realm.Resource previous = resources.putIfAbsent(resource.key(), resource);
        if (previous != null && !previous.equals(resource)) {
            throw new ValidateException(label + " contains a conflicting resource key");
        }
    }

    /**
     * Adds one page relation, de-duplicating equal values and rejecting conflicting stable keys.
     *
     * @param relations page-local relation map
     * @param relation  normalized relation
     * @param label     safe projection label
     */
    private static void putRelation(
            final Map<Realm.RelationKey, Realm.Relation> relations,
            final Realm.Relation relation,
            final String label) {
        final Realm.Relation previous = relations.putIfAbsent(relation.key(), relation);
        if (previous != null && !previous.equals(relation)) {
            throw new ValidateException(label + " contains a conflicting relation key");
        }
    }

    /**
     * Parses one minimal Google organization-unit projection.
     *
     * @param value decoded organization-unit object
     * @return validated minimal organization projection
     */
    private static Organization organization(final JsonValue.ObjectValue value) {
        return new Organization(requiredIdentifier(value, "orgUnitId"), requiredString(value, "name"),
                optionalIdentifier(value, "parentOrgUnitId"), requiredString(value, "orgUnitPath"));
    }

    /**
     * Parses one minimal Google user projection.
     *
     * @param value decoded user object
     * @return validated minimal user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        return new User(requiredIdentifier(value, "id"), requiredString(value, "primaryEmail"),
                requiredString(requiredObject(value, "name"), "fullName"), requiredBoolean(value, "suspended"),
                requiredString(value, "orgUnitPath"));
    }

    /**
     * Parses one minimal Google group projection.
     *
     * @param value decoded group object
     * @return validated minimal group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        return new Group(requiredIdentifier(value, "id"), requiredString(value, "email"),
                requiredString(value, "name"));
    }

    /**
     * Parses one minimal Google group-member projection.
     *
     * @param value decoded member object
     * @return validated minimal member projection
     */
    private static Member member(final JsonValue.ObjectValue value) {
        return new Member(requiredIdentifier(value, "id"));
    }

    /**
     * Parses one minimal Google administrator-role projection.
     *
     * @param value decoded role object
     * @return validated minimal role projection
     */
    private static Role role(final JsonValue.ObjectValue value) {
        return new Role(requiredIdentifier(value, "roleId"), requiredString(value, "roleName"));
    }

    /**
     * Parses one minimal Google role-assignment projection.
     *
     * @param value decoded role-assignment object
     * @return validated minimal role-assignment projection
     */
    private static RoleAssignment roleAssignment(final JsonValue.ObjectValue value) {
        return new RoleAssignment(requiredIdentifier(value, "assignedTo"), requiredIdentifier(value, "roleId"));
    }

    /**
     * Selects the next declared finite phase enabled by the requested kinds.
     *
     * @param current completed phase
     * @param kinds   exact requested kinds in stable code order
     * @return next enabled phase or {@code null}
     */
    private static Phase nextPhase(final Phase current, final List<Realm.Kind> kinds) {
        Phase candidate = current.next();
        while (candidate != null && !kinds.contains(candidate.kind())) {
            candidate = candidate.next();
        }
        return candidate;
    }

    /**
     * Encodes the exact position member closure owned by one finite phase.
     *
     * @param phase    finite snapshot phase
     * @param position validated phase position
     * @return immutable ordered JSON position object
     */
    private static JsonValue.ObjectValue position(final Phase phase, final Position position) {
        position.validate(phase);
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        if (phase == Phase.ORG_UNITS) {
            values.put(Builder.CURSOR_OFFSET_FIELD, number(position.offset()));
            values.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(position.relationOffset()));
            values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
            values.put(Builder.CURSOR_FINGERPRINT_FIELD, nullable(position.fingerprint()));
        } else if (phase == Phase.GROUP_MEMBERS) {
            values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
            values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
        } else {
            values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Decodes the exact position shape owned by one finite phase.
     *
     * @param phase finite snapshot phase
     * @param value decoded JSON position object
     * @return validated internal position
     */
    private static Position position(final Phase phase, final JsonValue.ObjectValue value) {
        final Position position;
        if (phase == Phase.ORG_UNITS) {
            exactMembers(
                    value,
                    Set.of(
                            Builder.CURSOR_OFFSET_FIELD,
                            Builder.CURSOR_RELATION_OFFSET_FIELD,
                            Builder.CURSOR_PARENT_ID_FIELD,
                            Builder.CURSOR_FINGERPRINT_FIELD),
                    "Google unpaged cursor position");
            position = Position.unpaged(
                    nonNegativeInt(value, Builder.CURSOR_OFFSET_FIELD),
                    nullableString(value, Builder.CURSOR_FINGERPRINT_FIELD).getOrNull());
            if (nonNegativeInt(value, Builder.CURSOR_RELATION_OFFSET_FIELD) != 0
                    || nullableString(value, Builder.CURSOR_PARENT_ID_FIELD).isPresent()) {
                throw new ValidateException("Google organization cursor contains an inapplicable replay field");
            }
        } else if (phase == Phase.GROUP_MEMBERS) {
            exactMembers(
                    value,
                    Set.of(Builder.CURSOR_NEXT_FIELD, Builder.CURSOR_PARENT_ID_FIELD),
                    "Google group-member cursor position");
            position = Position.parent(
                    nullableString(value, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                    nullableString(value, Builder.CURSOR_NEXT_FIELD).getOrNull());
        } else {
            exactMembers(value, Set.of(Builder.CURSOR_NEXT_FIELD), "Google page-token cursor position");
            position = new Position(nullableString(value, Builder.CURSOR_NEXT_FIELD), 0, 0, Optional.empty(),
                    Optional.empty());
        }
        position.validate(phase);
        return position;
    }

    /**
     * Decodes the stable requested-kind code array without using enum ordinal values.
     *
     * @param values decoded JSON kind array
     * @return immutable requested kinds in stable code order
     */
    private static List<Realm.Kind> kinds(final List<JsonValue> values) {
        final LinkedHashSet<Realm.Kind> kinds = new LinkedHashSet<>();
        int previous = 0;
        for (JsonValue value : values) {
            if (!(value instanceof JsonValue.NumberValue number)) {
                throw new ValidateException("Google cursor kind must be an integer code");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("Google cursor kind code is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !kinds.add(kind)) {
                throw new ValidateException("Google cursor kinds are not in canonical code order");
            }
            previous = code;
        }
        if (kinds.isEmpty()) {
            throw new ValidateException("Google cursor kinds must not be empty");
        }
        return List.copyOf(kinds);
    }

    /**
     * Resolves one stable Realm kind code.
     *
     * @param code persisted kind code
     * @return exact Realm kind
     */
    private static Realm.Kind kind(final int code) {
        for (Realm.Kind kind : Realm.Kind.values()) {
            if (kind.code() == code) {
                return kind;
            }
        }
        throw new ValidateException("Google cursor contains an unknown kind code");
    }

    /**
     * Validates one requested kind set against the fixed Google Workspace closure.
     *
     * @param kinds caller-requested resource kinds
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (!SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("Google snapshot contains an unsupported resource kind");
        }
    }

    /**
     * Reads one required JSON object member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return required object member
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        return requiredObject(object.values().get(name), name);
    }

    /**
     * Narrows one decoded JSON value to a required object.
     *
     * @param value decoded JSON value
     * @param label safe field label
     * @return required object
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue value, final String label) {
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException(label + " must be a JSON object");
        }
        return object;
    }

    /**
     * Reads one required JSON array member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return immutable array values
     */
    private static List<JsonValue> requiredArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException(name + " must be a JSON array");
        }
        return array.values();
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return original validated string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string");
        }
        return requireText(text.value(), name);
    }

    /**
     * Reads one required stable identifier represented as a string or exact integral JSON number.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return canonical lexical identifier
     */
    private static String requiredIdentifier(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.StringValue text) {
            return requireText(text.value(), name);
        }
        if (value instanceof JsonValue.NumberValue number) {
            try {
                return Long.toString(number.value().longValueExact());
            } catch (ArithmeticException cause) {
                throw new ValidateException(name + " must be an exact integral identifier", cause);
            }
        }
        throw new ValidateException(name + " must be a string or integral identifier");
    }

    /**
     * Reads one optional stable identifier represented as a string, number, null, or absence.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return optional canonical lexical identifier
     */
    private static Optional<String> optionalIdentifier(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (value instanceof JsonValue.StringValue text && text.value().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(requiredIdentifier(object, name));
    }

    /**
     * Reads one optional non-blank JSON string as empty when absent, null, or empty.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return optional original string
     */
    private static Optional<String> optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string or null");
        }
        if (text.value().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(requireText(text.value(), name));
    }

    /**
     * Reads one canonical optional cursor string whose absence is explicit JSON null.
     *
     * @param object decoded cursor object
     * @param name   member name
     * @return optional validated string
     */
    private static Optional<String> nullableString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string or null");
        }
        return Optional.of(requireText(text.value(), name));
    }

    /**
     * Reads one required JSON boolean member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return exact boolean value
     */
    private static boolean requiredBoolean(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.BooleanValue bool)) {
            throw new ValidateException(name + " must be a JSON boolean");
        }
        return bool.value();
    }

    /**
     * Reads one required exact JSON long member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return exact long value
     */
    private static long requiredLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException(name + " must be a JSON number");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException(name + " must be an exact integer", cause);
        }
    }

    /**
     * Reads one required positive JSON long member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return positive exact long value
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final long value = requiredLong(object, name);
        if (value <= 0L) {
            throw new ValidateException(name + " must be positive");
        }
        return value;
    }

    /**
     * Reads one exact JSON int member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return exact int value
     */
    private static int requiredInt(final JsonValue.ObjectValue object, final String name) {
        final long value = requiredLong(object, name);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ValidateException(name + " exceeds the integer range");
        }
        return (int) value;
    }

    /**
     * Reads one non-negative JSON int member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return non-negative int value
     */
    private static int nonNegativeInt(final JsonValue.ObjectValue object, final String name) {
        final int value = requiredInt(object, name);
        if (value < 0) {
            throw new ValidateException(name + " must not be negative");
        }
        return value;
    }

    /**
     * Verifies an object's exact member-name closure.
     *
     * @param object   decoded object
     * @param expected exact member set
     * @param label    safe object label
     */
    private static void exactMembers(
            final JsonValue.ObjectValue object,
            final Set<String> expected,
            final String label) {
        if (!object.values().keySet().equals(expected)) {
            throw new ValidateException(label + " contains an invalid member set");
        }
    }

    /**
     * Converts one optional string into a JSON string or explicit null.
     *
     * @param value optional string
     * @return JSON string or null
     */
    private static JsonValue nullable(final Optional<String> value) {
        return value.isPresent() ? new JsonValue.StringValue(value.getOrNull()) : JsonValue.NullValue.instance();
    }

    /**
     * Creates one exact JSON integer.
     *
     * @param value integral value
     * @return immutable JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Validates required text without silently trimming it.
     *
     * @param value caller or upstream supplied text
     * @param label safe semantic label
     * @return original validated text
     */
    private static String requireText(final String value, final String label) {
        final String text;
        try {
            text = Assert.notBlank(value, label + " must not be blank");
        } catch (IllegalArgumentException cause) {
            throw new ValidateException(label + " must not be blank", cause);
        }
        if (!text.equals(StringKit.trim(text))) {
            throw new ValidateException(label + " must not contain surrounding whitespace");
        }
        return text;
    }

    /**
     * Narrows one delegated outcome through the declared capability response type.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared response class
     * @param <S>          expected success type
     * @return type-safe delegated outcome
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "Google delegated operation returned an unsupported outcome");
        });
    }

    /**
     * Propagates one non-success outcome across an internal type boundary.
     *
     * @param outcome outcome that must not contain a success value
     * @param <T>     target success type
     * @return original rejection or failure
     */
    private static <T> Outcome<T> propagate(final Outcome<?> outcome) {
        return switch (outcome) {
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "Google internal outcome could not be propagated");
        };
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a missing-capability rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejection
     */
    private static <T> CompletionStage<Outcome<T>> missing() {
        return completed(rejected(ErrorCode._400, "Google Realm capability is not declared by the selected manifest"));
    }

    /**
     * Creates a request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejection
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(
                rejected(ErrorCode._400, "Google Realm request does not match the selected capability contract"));
    }

    /**
     * Creates one expected rejection without structured details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final Errors code, final String description) {
        return rejected(code, description, Map.of());
    }

    /**
     * Creates one expected rejection with safe details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param details     allow-listed scalar details
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.rejected(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Creates one operational failure without structured details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates one operational failure with safe details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param details     allow-listed scalar details
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns the exact Realm capability manifest selected at Source compilation.
     *
     * @return immutable describe, snapshot, and retrieve capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes one exact Google Workspace capability and request type.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific Realm request
     * @param context    immutable non-secret invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return asynchronous typed Realm outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Google Realm capability must not be null");
        Assert.notNull(context, "Google Realm context must not be null");
        Assert.notNull(timeout, "Google Realm timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
        }
        if (capability.equals(Realm.DESCRIBE) && request instanceof Realm.Describe) {
            return completed(Outcome.succeeded(capability.responseType().cast(GoogleManifest.realmDescription())));
        }
        if (capability.equals(Realm.SNAPSHOT) && request instanceof Realm.Snapshot snapshot) {
            return narrow(snapshot(snapshot, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.RETRIEVE) && request instanceof Realm.Retrieve retrieve) {
            return narrow(retrieve(retrieve, context, timeout), capability.responseType());
        }
        return mismatch();
    }

    /**
     * Clears and closes the Source-private upstream-token cache without closing shared services.
     */
    @Override
    public void close() {
        accessCache.clear();
        accessCache.close();
    }

    /**
     * Validates and starts one snapshot invocation with a single observation instant.
     *
     * @param request exact implementation-neutral snapshot request
     * @param context immutable context used only for key loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous normalized page outcome
     */
    private CompletionStage<Outcome<Realm.Page>> snapshot(
            final Realm.Snapshot request,
            final Context context,
            final Timeout timeout) {
        final CursorState state;
        try {
            requireKinds(request.kinds());
            state = request.cursor().isPresent() ? decode(request.cursor().getOrNull(), request.kinds())
                    : CursorState.initial(request.kinds());
        } catch (RuntimeException ignored) {
            return completed(rejected(ErrorCode._400, "Google Workspace snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Google Workspace snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
        return authenticated(context, timeout, access -> snapshot(access, request, state, observedAt, timeout));
    }

    /**
     * Dispatches one validated snapshot state to its finite Admin SDK phase.
     *
     * @param access     valid delegated Google access
     * @param request    validated snapshot request
     * @param state      canonical snapshot cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return normalized page or safely classified failure
     */
    private Outcome<Realm.Page> snapshot(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        try {
            return switch (state.phase()) {
                case ORG_UNITS -> organizations(access, request, state, observedAt, timeout);
                case USERS -> users(access, request, state, observedAt, timeout);
                case GROUPS -> groups(access, request, state, observedAt, timeout);
                case GROUP_MEMBERS -> groupMembers(access, request, state, observedAt, timeout);
                case ROLES -> roles(access, request, state, observedAt, timeout);
                case ROLE_ASSIGNMENTS -> roleAssignments(access, request, state, observedAt, timeout);
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "Google Admin SDK returned an invalid snapshot projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Google Workspace snapshot processing failed locally");
        }
    }

    /**
     * Reads one bounded organization page from the completely replayed organization-unit response.
     *
     * @param access     valid delegated Google access
     * @param request    validated snapshot request
     * @param state      organization replay state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded organization resource and parent-relation page
     */
    private Outcome<Realm.Page> organizations(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<List<Organization>> fetched = organizationList(access, "snapshot", timeout);
        if (!(fetched instanceof Outcome.Succeeded<List<Organization>> success)) {
            return propagate(fetched);
        }
        final List<Organization> organizations = success.value();
        final String fingerprint = organizationFingerprint(organizations);
        final Position position = state.position();
        if (position.offset() > organizations.size()) {
            return failed(ErrorCode._502, "Google organization replay offset exceeds the current projection");
        }
        if (position.fingerprint().isPresent() && !fingerprint.equals(position.fingerprint().getOrNull())) {
            return failed(ErrorCode._502, "Google organization projection changed between snapshot pages");
        }
        if (position.offset() > 0 && position.fingerprint().isEmpty()) {
            return failed(ErrorCode._502, "Google organization continuation omits its projection fingerprint");
        }
        final List<Realm.Resource> resources = new ArrayList<>();
        final List<Realm.Relation> relations = new ArrayList<>();
        int offset = position.offset();
        while (offset < organizations.size()) {
            final Organization organization = organizations.get(offset++);
            resources.add(organizationResource(organization, observedAt));
            if (organization.parentId().isPresent()) {
                relations.add(parentRelation(organization, observedAt));
            }
            if ((resources.size() >= request.limit() || relations.size() >= request.limit())
                    && offset < organizations.size()) {
                return page(resources, relations, state, Position.unpaged(offset, fingerprint));
            }
        }
        return completedPhase(resources, relations, state);
    }

    /**
     * Reads one official user page and resolves organization membership through the complete organization projection.
     *
     * @param access     valid delegated Google access
     * @param request    validated snapshot request
     * @param state      user page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded user and membership page
     */
    private Outcome<Realm.Page> users(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<List<Organization>> organizations = organizationList(access, "snapshot", timeout);
        if (!(organizations instanceof Outcome.Succeeded<List<Organization>> organizationSuccess)) {
            return propagate(organizations);
        }
        final Map<String, String> pathIds = new LinkedHashMap<>();
        for (Organization organization : organizationSuccess.value()) {
            final String previous = pathIds.putIfAbsent(organization.path(), organization.id());
            if (previous != null && !previous.equals(organization.id())) {
                return failed(ErrorCode._502, "Google organization paths do not identify unique resources");
            }
        }
        final Map<String, String> query = new LinkedHashMap<>();
        query.put("customer", options.customer());
        query.put("orderBy", "email");
        query.put("maxResults", Integer.toString(request.limit()));
        query.put("projection", "basic");
        state.position().next().ifPresent(token -> query.put("pageToken", token));
        final Outcome<WirePage<User>> fetched = wirePage(
                url(Builder.REALM_USERS, null, query),
                access,
                timeout,
                "snapshot",
                "users",
                GoogleRealmAdapter::user);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > request.limit()) {
            return failed(ErrorCode._502, "Google users response exceeds the requested page size");
        }
        final List<Realm.Resource> resources = new ArrayList<>();
        final List<Realm.Relation> relations = new ArrayList<>();
        final Map<Realm.Key, Realm.Resource> uniqueResources = new LinkedHashMap<>();
        final Map<Realm.RelationKey, Realm.Relation> uniqueRelations = new LinkedHashMap<>();
        for (User user : success.value().items()) {
            putResource(uniqueResources, userResource(user, observedAt), "Google user page");
            final String organizationId = pathIds.get(user.organizationPath());
            if (organizationId != null) {
                putRelation(
                        uniqueRelations,
                        memberRelation(user.id(), Realm.Kind.ORGANIZATION, organizationId, observedAt),
                        "Google user membership page");
            } else if (!Symbol.SLASH.equals(user.organizationPath())) {
                return failed(
                        ErrorCode._502,
                        "Google user references an organization path absent from the complete projection");
            }
        }
        resources.addAll(uniqueResources.values());
        relations.addAll(uniqueRelations.values());
        return continuedOrCompleted(resources, relations, state, success.value().next());
    }

    /**
     * Reads one official group collection page.
     *
     * @param access     valid delegated Google access
     * @param request    validated snapshot request
     * @param state      group page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded group resource page
     */
    private Outcome<Realm.Page> groups(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Map<String, String> query = pageQuery(request.limit(), state.position().next());
        query.put("customer", options.customer());
        final Outcome<WirePage<Group>> fetched = wirePage(
                url(Builder.REALM_GROUPS, null, query),
                access,
                timeout,
                "snapshot",
                "groups",
                GoogleRealmAdapter::group);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > request.limit()) {
            return failed(ErrorCode._502, "Google groups response exceeds the requested page size");
        }
        final Map<Realm.Key, Realm.Resource> unique = new LinkedHashMap<>();
        for (Group group : success.value().items()) {
            putResource(unique, groupResource(group, observedAt), "Google group page");
        }
        return continuedOrCompleted(List.copyOf(unique.values()), List.of(), state, success.value().next());
    }

    /**
     * Reads one official member page for the replayed current group.
     *
     * @param access     valid delegated Google access
     * @param request    validated snapshot request
     * @param state      group-member page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded group-member relation page
     */
    private Outcome<Realm.Page> groupMembers(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<String>> parent = groupParent(access, state.position().parentId(), false, timeout);
        if (!(parent instanceof Outcome.Succeeded<Optional<String>> parentSuccess)) {
            return propagate(parent);
        }
        if (parentSuccess.value().isEmpty()) {
            return completedPhase(List.of(), List.of(), state);
        }
        final String groupId = parentSuccess.value().getOrNull();
        final Map<String, String> query = pageQuery(request.limit(), state.position().next());
        final Outcome<WirePage<Member>> fetched = wirePage(
                url(Builder.REALM_GROUP_MEMBERS, List.of(groupId, "members"), query),
                access,
                timeout,
                "snapshot",
                "members",
                GoogleRealmAdapter::member);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Member>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > request.limit()) {
            return failed(ErrorCode._502, "Google group members response exceeds the requested page size");
        }
        final Map<Realm.RelationKey, Realm.Relation> unique = new LinkedHashMap<>();
        for (Member member : success.value().items()) {
            putRelation(
                    unique,
                    memberRelation(member.id(), Realm.Kind.GROUP, groupId, observedAt),
                    "Google group-member page");
        }
        if (success.value().next().isPresent()) {
            return page(
                    List.of(),
                    List.copyOf(unique.values()),
                    state,
                    Position.parent(groupId, success.value().next().getOrNull()));
        }
        final Outcome<Optional<String>> following = groupParent(access, Optional.of(groupId), true, timeout);
        if (!(following instanceof Outcome.Succeeded<Optional<String>> followingSuccess)) {
            return propagate(following);
        }
        if (followingSuccess.value().isPresent()) {
            return page(
                    List.of(),
                    List.copyOf(unique.values()),
                    state,
                    Position.parent(followingSuccess.value().getOrNull(), null));
        }
        return completedPhase(List.of(), List.copyOf(unique.values()), state);
    }

    /**
     * Reads one official administrator-role page.
     *
     * @param access     valid delegated Google access
     * @param request    validated snapshot request
     * @param state      role page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded role resource page
     */
    private Outcome<Realm.Page> roles(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Map<String, String> query = pageQuery(request.limit(), state.position().next());
        final Outcome<WirePage<Role>> fetched = wirePage(
                url(Builder.REALM_ROLES, List.of(options.customer(), "roles"), query),
                access,
                timeout,
                "snapshot",
                "items",
                GoogleRealmAdapter::role);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Role>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > request.limit()) {
            return failed(ErrorCode._502, "Google roles response exceeds the requested page size");
        }
        final Map<Realm.Key, Realm.Resource> unique = new LinkedHashMap<>();
        for (Role role : success.value().items()) {
            putResource(unique, roleResource(role, observedAt), "Google role page");
        }
        return continuedOrCompleted(List.copyOf(unique.values()), List.of(), state, success.value().next());
    }

    /**
     * Reads one official role-assignment page and maps user-to-role relations.
     *
     * @param access     valid delegated Google access
     * @param request    validated snapshot request
     * @param state      role-assignment page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded role-member relation page
     */
    private Outcome<Realm.Page> roleAssignments(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Map<String, String> query = pageQuery(request.limit(), state.position().next());
        final Outcome<WirePage<RoleAssignment>> fetched = wirePage(
                url(Builder.REALM_ROLE_ASSIGNMENTS, List.of(options.customer(), "roleassignments"), query),
                access,
                timeout,
                "snapshot",
                "items",
                GoogleRealmAdapter::roleAssignment);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<RoleAssignment>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > request.limit()) {
            return failed(ErrorCode._502, "Google role assignments response exceeds the requested page size");
        }
        final Map<Realm.RelationKey, Realm.Relation> unique = new LinkedHashMap<>();
        for (RoleAssignment assignment : success.value().items()) {
            putRelation(unique, roleMemberRelation(assignment, observedAt), "Google role-assignment page");
        }
        return continuedOrCompleted(List.of(), List.copyOf(unique.values()), state, success.value().next());
    }

    /**
     * Validates and starts one direct stable-key retrieval with a single observation instant.
     *
     * @param request exact implementation-neutral retrieval request
     * @param context immutable context used only for key loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous retrieval outcome
     */
    private CompletionStage<Outcome<Realm.Retrieved>> retrieve(
            final Realm.Retrieve request,
            final Context context,
            final Timeout timeout) {
        if (!SUPPORTED_KINDS.contains(request.key().kind())) {
            return completed(rejected(ErrorCode._400, "Google Workspace retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Google Workspace retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
        return authenticated(context, timeout, access -> retrieve(access, request.key(), observedAt, timeout));
    }

    /**
     * Dispatches one retrieval to the fixed Admin SDK resource path.
     *
     * @param access     valid delegated Google access
     * @param key        stable implementation-neutral resource key
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved resource, explicit absence, or classified failure
     */
    private Outcome<Realm.Retrieved> retrieve(
            final Access access,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        return switch (key.kind()) {
            case USER -> retrieveUser(access, key.externalId(), observedAt, timeout);
            case ORGANIZATION -> retrieveOrganization(access, key.externalId(), observedAt, timeout);
            case GROUP -> retrieveGroup(access, key.externalId(), observedAt, timeout);
            case ROLE -> retrieveRole(access, key.externalId(), observedAt, timeout);
            default -> rejected(ErrorCode._400, "Google Workspace retrieve kind is unsupported");
        };
    }

    /**
     * Retrieves one user by its stable Directory API identifier.
     *
     * @param access     valid delegated Google access
     * @param id         stable user identifier
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved user or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveUser(
            final Access access,
            final String id,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = get(
                url(Builder.REALM_USER, List.of(id), Map.of("projection", "basic")),
                access,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (success.value() == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final User user = user(success.value());
        if (!id.equals(user.id())) {
            return failed(ErrorCode._502, "Google user retrieval returned a different stable identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(userResource(user, observedAt))));
    }

    /**
     * Locates one organization path through the unpaged projection and retrieves the exact organization resource.
     *
     * @param access     valid delegated Google access
     * @param id         stable organization-unit identifier
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved organization or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveOrganization(
            final Access access,
            final String id,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<List<Organization>> listed = organizationList(access, "retrieve", timeout);
        if (!(listed instanceof Outcome.Succeeded<List<Organization>> success)) {
            return propagate(listed);
        }
        Organization selected = null;
        for (Organization organization : success.value()) {
            if (id.equals(organization.id())) {
                selected = organization;
                break;
            }
        }
        if (selected == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Outcome<JsonValue.ObjectValue> fetched = get(
                url(Builder.REALM_ORGANIZATION, List.of(options.customer(), "orgunits", selected.path()), Map.of()),
                access,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> retrieveSuccess)) {
            return propagate(fetched);
        }
        if (retrieveSuccess.value() == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Organization organization = organization(retrieveSuccess.value());
        if (!selected.equals(organization)) {
            return failed(ErrorCode._502, "Google organization projection changed during retrieval");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(organizationResource(organization, observedAt))));
    }

    /**
     * Retrieves one group by its stable Directory API identifier.
     *
     * @param access     valid delegated Google access
     * @param id         stable group identifier
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved group or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveGroup(
            final Access access,
            final String id,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = get(
                url(Builder.REALM_GROUP, List.of(id), Map.of()),
                access,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (success.value() == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Group group = group(success.value());
        if (!id.equals(group.id())) {
            return failed(ErrorCode._502, "Google group retrieval returned a different stable identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(groupResource(group, observedAt))));
    }

    /**
     * Retrieves one administrator role by its stable Directory API identifier.
     *
     * @param access     valid delegated Google access
     * @param id         stable role identifier
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved role or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveRole(
            final Access access,
            final String id,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = get(
                url(Builder.REALM_ROLE, List.of(options.customer(), "roles", id), Map.of()),
                access,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (success.value() == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Role role = role(success.value());
        if (!id.equals(role.id())) {
            return failed(ErrorCode._502, "Google role retrieval returned a different stable identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(roleResource(role, observedAt))));
    }

    /**
     * Acquires a cached or freshly exchanged delegated token and retries one 401 result exactly once.
     *
     * @param context   immutable context used by the project Key Loader
     * @param timeout   shared end-to-end timeout
     * @param operation authenticated synchronous Admin SDK operation
     * @param <T>       successful operation value type
     * @return asynchronous authenticated outcome
     */
    private <T> CompletionStage<Outcome<T>> authenticated(
            final Context context,
            final Timeout timeout,
            final Function<Access, Outcome<T>> operation) {
        return access(context, timeout).thenCompose(resolved -> switch (resolved) {
            case Outcome.Succeeded<Access> success -> execute(success.value(), timeout, operation)
                    .thenCompose(first -> {
                        if (!unauthorized(first)) {
                            return completed(first);
                        }
                        return accessCache.delete(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY)
                                .handle(
                                        (ignored, cause) -> cause == null ? Outcome.<Boolean>succeeded(Boolean.TRUE)
                                                : GoogleRealmAdapter.<Boolean>failed(
                                                        ErrorCode._500,
                                                        "Google upstream-token cache deletion failed"))
                                .thenCompose(deleted -> {
                                    if (!(deleted instanceof Outcome.Succeeded<Boolean>)) {
                                        return completed(propagate(deleted));
                                    }
                                    return exchange(context, timeout).thenCompose(refreshed -> switch (refreshed) {
                                        case Outcome.Succeeded<Access> retry -> execute(
                                                retry.value(),
                                                timeout,
                                                operation);
                                        case Outcome.Rejected<Access> rejected -> completed(
                                                Outcome.rejected(rejected.failure()));
                                        case Outcome.Failed<Access> failed -> completed(
                                                Outcome.failed(failed.failure()));
                                        default -> completed(
                                                failed(
                                                        ErrorCode._500,
                                                        "Google token refresh returned an unsupported outcome"));
                                    });
                                });
                    });
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
            default -> completed(failed(ErrorCode._500, "Google token lookup returned an unsupported outcome"));
        });
    }

    /**
     * Executes one authenticated operation on the Source executor and closes transport exceptions into Outcomes.
     *
     * @param access    valid delegated Google access
     * @param timeout   shared end-to-end timeout
     * @param operation authenticated synchronous operation
     * @param <T>       successful value type
     * @return asynchronous safely closed operation outcome
     */
    private <T> CompletionStage<Outcome<T>> execute(
            final Access access,
            final Timeout timeout,
            final Function<Access, Outcome<T>> operation) {
        return CompletableFuture.<Outcome<T>>supplyAsync(() -> {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Google Workspace operation has no remaining timeout");
            }
            try {
                return Assert.notNull(operation.apply(access), "Google Workspace operation returned no outcome");
            } catch (TimeoutException ignored) {
                return failed(ErrorCode._408, "Google Workspace operation timed out");
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._503, "Google Admin SDK transport is unavailable");
            }
        }, services.executor());
    }

    /**
     * Reads the Source-private token cache before exchanging a new delegated token.
     *
     * @param context immutable context used by the project Key Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous cached or newly exchanged access outcome
     */
    private CompletionStage<Outcome<Access>> access(final Context context, final Timeout timeout) {
        return accessCache.get(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY)
                .<CompletionStage<Outcome<Access>>>handle((cached, cause) -> {
                    if (cause != null) {
                        return completed(failed(ErrorCode._500, "Google upstream-token cache lookup failed"));
                    }
                    return cached == null ? exchange(context, timeout) : completed(Outcome.succeeded(cached));
                }).thenCompose(Function.identity());
    }

    /**
     * Loads and validates the external RSA private key before signing the delegated assertion synchronously.
     *
     * @param context immutable context supplied to the project Key Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous delegated access-token outcome
     */
    private CompletionStage<Outcome<Access>> exchange(final Context context, final Timeout timeout) {
        final Instant now = timeout.clock().now();
        final KeyLoader.Request query = new KeyLoader.Request(services.entry(), options.clientId(),
                Optional.of(options.credential().id()), Builder.SIGNATURE, JwaAlgorithm.RS256.name(), now);
        final CompletionStage<Outcome<KeyMaterial>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.keyLoader().load(query, context, timeout),
                    loaded -> services.keyParser().parse(services.entry(), query, loaded));
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "Google signing-key resolution failed before returning a stage"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._500, "Google signing-key resolution returned no stage"));
        }
        return resolution.<Outcome<String>>handle((outcome, cause) -> {
            if (cause != null || outcome == null) {
                return GoogleRealmAdapter.<String>failed(ErrorCode._500, "Google signing-key resolution failed");
            }
            return switch (outcome) {
                case Outcome.Succeeded<KeyMaterial> success -> assertion(success.value(), now);
                case Outcome.Rejected<KeyMaterial> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<KeyMaterial> failed -> Outcome.failed(failed.failure());
                default -> GoogleRealmAdapter.<String>failed(
                        ErrorCode._500,
                        "Google signing-key resolution returned an unsupported outcome");
            };
        }).thenCompose(assertion -> switch (assertion) {
            case Outcome.Succeeded<String> success -> token(success.value(), timeout).thenCompose(this::cache);
            case Outcome.Rejected<String> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<String> failed -> completed(Outcome.failed(failed.failure()));
            default -> completed(failed(ErrorCode._500, "Google assertion signing returned an unsupported outcome"));
        });
    }

    /**
     * Signs the exact Google service-account assertion while retaining KeyMaterial only in this synchronous scope.
     *
     * @param resolved externally loaded and parsed signing-key material
     * @param now      shared-clock assertion issue instant
     * @return compact RS256 assertion or a safe rejection/failure
     */
    private Outcome<String> assertion(final KeyMaterial resolved, final Instant now) {
        if (!options.credential().id().equals(resolved.keyId())
                || !JwaAlgorithm.RS256.name().equals(resolved.algorithm())
                || !(resolved.key() instanceof RSAPrivateKey privateKey) || now.isBefore(resolved.notBefore())
                || !now.isBefore(resolved.notAfter())
                || resolved.notAfter().isBefore(now.plus(Builder.UPSTREAM_ASSERTION_LIFETIME))) {
            return rejected(ErrorCode._401, "Google signing key does not match the configured Workspace key");
        }
        byte[] payload = null;
        try {
            final Map<String, JsonValue> headers = new LinkedHashMap<>();
            headers.put(JoseHeader.ALGORITHM, new JsonValue.StringValue(JwaAlgorithm.RS256.name()));
            headers.put(JoseHeader.TYPE, new JsonValue.StringValue(JWT_TYPE));
            final Map<String, JsonValue> claims = new LinkedHashMap<>();
            claims.put(JwtClaims.ISSUER, new JsonValue.StringValue(options.clientId()));
            claims.put(JwtClaims.SUBJECT, new JsonValue.StringValue(options.delegatedAdmin()));
            claims.put(OAuth2.Parameters.SCOPE, new JsonValue.StringValue(String.join(Symbol.SPACE, options.scopes())));
            claims.put(JwtClaims.AUDIENCE, new JsonValue.StringValue(TOKEN_AUDIENCE));
            claims.put(JwtClaims.ISSUED_AT, number(now.getEpochSecond()));
            claims.put(JwtClaims.EXPIRATION, number(now.plus(Builder.UPSTREAM_ASSERTION_LIFETIME).getEpochSecond()));
            payload = JsonKit.writeValue(new JsonValue.ObjectValue(claims));
            final JwsService.Signature signature = jwsService
                    .sign(JoseHeader.protectedOnly(new JsonValue.ObjectValue(headers)), payload, privateKey);
            return Outcome.succeeded(jwsService.compact(new JwsService.Jws(payload, List.of(signature))));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Google delegated service-account assertion signing failed");
        } finally {
            if (payload != null) {
                Arrays.fill(payload, (byte) 0);
            }
        }
    }

    /**
     * Schedules the bounded Google OAuth assertion exchange on the shared Source executor.
     *
     * @param assertion compact signed JWT bearer assertion
     * @param timeout   shared end-to-end timeout
     * @return asynchronous fresh access outcome
     */
    private CompletionStage<Outcome<Access>> token(final String assertion, final Timeout timeout) {
        try {
            return CompletableFuture
                    .<Outcome<Access>>supplyAsync(() -> tokenRequest(assertion, timeout), services.executor()).handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : GoogleRealmAdapter
                                            .<Access>failed(ErrorCode._503, "Google token exchange task failed"));
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "Google token exchange task could not be scheduled"));
        }
    }

    /**
     * Exchanges one signed assertion through the fixed Google OAuth token endpoint.
     *
     * @param assertion compact signed JWT bearer assertion
     * @param timeout   shared end-to-end timeout
     * @return delegated access or safely classified failure
     */
    private Outcome<Access> tokenRequest(final String assertion, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Google token exchange has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(Protocol.HTTPS, timeout, services.policies())
                    .url(targets.token().getOrNull().url().toString()).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .form(OAuth2.Parameters.GRANT_TYPE, JWT_BEARER_GRANT).form(ASSERTION, assertion).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Google token exchange timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Google token endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return tokenHttpFailure(response);
            }
            final JsonValue.ObjectValue envelope = object(response);
            final String token = requiredString(envelope, "access_token");
            final long seconds = requiredPositiveLong(envelope, "expires_in");
            final String tokenType = requiredString(envelope, "token_type");
            if (!"Bearer".equalsIgnoreCase(tokenType)) {
                return failed(ErrorCode._502, "Google token endpoint returned an unsupported token type");
            }
            final long now = FabricX.clock().millis();
            long expiresAt = 0L;
            try {
                expiresAt = Math.addExact(now, Duration.ofSeconds(seconds).toMillis());
            } catch (ArithmeticException ignored) {
                expiresAt = 0L;
            }
            if (expiresAt <= now) {
                expiresAt = 0L;
            }
            return Outcome.succeeded(new Access(token, expiresAt));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Google token endpoint returned an invalid response");
        }
    }

    /**
     * Stores one cacheable delegated token with the mandatory early-expiration skew.
     *
     * @param outcome freshly exchanged access outcome
     * @return original access after cache creation or a safe cache failure
     */
    private CompletionStage<Outcome<Access>> cache(final Outcome<Access> outcome) {
        if (!(outcome instanceof Outcome.Succeeded<Access> success) || success.value().expiresAtMillis() == 0L) {
            return completed(outcome);
        }
        final long now = FabricX.clock().millis();
        final long lifetime = success.value().expiresAtMillis() - now;
        if (lifetime <= 0L) {
            return completed(Outcome.succeeded(new Access(success.value().token(), 0L)));
        }
        final long skew = Math.min(
                Builder.UPSTREAM_ACCESS_TOKEN_MAXIMUM_SKEW.toMillis(),
                lifetime / Builder.UPSTREAM_ACCESS_TOKEN_SKEW_DIVISOR);
        final long ttl = lifetime - skew;
        if (ttl <= 0L) {
            return completed(Outcome.succeeded(new Access(success.value().token(), 0L)));
        }
        return accessCache.create(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY, success.value(), ttl).handle(
                (ignored, cause) -> cause == null ? outcome
                        : failed(ErrorCode._500, "Google upstream-token cache creation failed"));
    }

    /**
     * Maps one non-successful Google OAuth token response.
     *
     * @param response owned non-successful token response
     * @return credential rejection, rate-limit failure, or upstream failure
     */
    private Outcome<Access> tokenHttpFailure(final Response response) {
        final int status = response.code();
        final Map<String, JsonValue> details = details("token", status, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.UNAUTHORIZED
                || status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._401, "Google rejected the delegated service-account assertion", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Google token endpoint is rate limited", details);
        }
        return failed(ErrorCode._502, "Google token endpoint returned an upstream error", details);
    }

    /**
     * Executes one Google Admin SDK GET and decodes a successful JSON object.
     *
     * @param url           manifest-derived immutable request URL
     * @param access        valid delegated Google access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe Realm operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @return decoded object, {@code null} for absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> get(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound) {
        return request(url, access, timeout, operation, allowNotFound, this::object);
    }

    /**
     * Executes the official unpaged organization-unit GET using complete payload replay.
     *
     * @param url       manifest-derived immutable request URL
     * @param access    valid delegated Google access
     * @param timeout   shared end-to-end timeout
     * @param operation safe Realm operation label
     * @return completely decoded organization-unit response or classified failure
     */
    private Outcome<JsonValue.ObjectValue> getUnpaged(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation) {
        return request(url, access, timeout, operation, false, this::unpagedObject);
    }

    /**
     * Executes one Google Admin SDK GET through an operation-specific response reader.
     *
     * @param url           manifest-derived immutable request URL
     * @param access        valid delegated Google access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe Realm operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @param reader        bounded or explicit unpaged response reader
     * @return decoded object, {@code null} for absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> request(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound,
            final Function<Response, JsonValue.ObjectValue> reader) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Google Admin SDK request has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(Protocol.HTTPS, timeout, services.policies()).url(url.toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(Http.Header.AUTHORIZATION, "Bearer " + access.token()).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Google Admin SDK request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Google Admin SDK endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return adminHttpFailure(response, operation, allowNotFound);
            }
            return Outcome.succeeded(reader.apply(response));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Google Admin SDK returned an invalid response");
        }
    }

    /**
     * Maps one non-successful Admin SDK response without retaining its body or headers.
     *
     * @param response      owned non-successful response
     * @param operation     safe Realm operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @param <T>           expected success type
     * @return classified rejection, absence, or failure
     */
    private <T> Outcome<T> adminHttpFailure(
            final Response response,
            final String operation,
            final boolean allowNotFound) {
        final int status = response.code();
        final Map<String, JsonValue> details = details(operation, status, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "Google Admin SDK rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "Google Admin SDK rejected the delegated access token", details);
        }
        if (status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._403, "Google Admin SDK visibility or permission is insufficient", details);
        }
        if (status == Http.Status.NOT_FOUND && allowNotFound) {
            return Outcome.succeeded(null);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Google Admin SDK is rate limited", details);
        }
        return failed(ErrorCode._502, "Google Admin SDK returned an upstream error", details);
    }

    /**
     * Completely reads, validates, de-duplicates, and stable-sorts the G-02 organization-unit response.
     *
     * @param access    valid delegated Google access
     * @param operation safe Realm operation label
     * @param timeout   shared end-to-end timeout
     * @return complete minimal organization projection
     */
    private Outcome<List<Organization>> organizationList(
            final Access access,
            final String operation,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = getUnpaged(
                url(Builder.REALM_ORGANIZATIONS, List.of(options.customer(), "orgunits"), Map.of("type", "all")),
                access,
                timeout,
                operation);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final Map<String, Organization> organizations = new LinkedHashMap<>();
        for (JsonValue value : requiredArray(success.value(), "organizationUnits")) {
            final Organization organization = organization(requiredObject(value, "organization-unit item"));
            final Organization previous = organizations.putIfAbsent(organization.id(), organization);
            if (previous != null && !previous.equals(organization)) {
                return failed(ErrorCode._502, "Google organization response contains a conflicting stable identifier");
            }
        }
        final List<Organization> result = new ArrayList<>(organizations.values());
        result.sort(Comparator.comparing(Organization::id));
        return Outcome.succeeded(List.copyOf(result));
    }

    /**
     * Reads one official Google collection envelope into its minimal typed projection.
     *
     * @param url       manifest-derived request URL
     * @param access    valid delegated Google access
     * @param timeout   shared end-to-end timeout
     * @param operation safe Realm operation label
     * @param member    exact collection member containing items
     * @param parser    minimal projection parser
     * @param <T>       minimal item type
     * @return decoded page and optional official next-page token
     */
    private <T> Outcome<WirePage<T>> wirePage(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final String member,
            final Function<JsonValue.ObjectValue, T> parser) {
        final Outcome<JsonValue.ObjectValue> fetched = get(url, access, timeout, operation, false);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final List<T> items = new ArrayList<>();
        for (JsonValue value : requiredArray(success.value(), member)) {
            items.add(parser.apply(requiredObject(value, member + " item")));
        }
        return Outcome.succeeded(new WirePage<>(items, optionalString(success.value(), "nextPageToken")));
    }

    /**
     * Replays the official group collection and selects the current or following stable group identifier.
     *
     * @param access    valid delegated Google access
     * @param current   empty for the first group or the current stable group identifier
     * @param following whether to return the group following the supplied identifier
     * @param timeout   shared end-to-end timeout
     * @return selected group identifier or natural exhaustion
     */
    private Outcome<Optional<String>> groupParent(
            final Access access,
            final Optional<String> current,
            final boolean following,
            final Timeout timeout) {
        Optional<String> token = Optional.empty();
        boolean found = current.isEmpty();
        while (true) {
            final Map<String, String> query = pageQuery(Builder.MAXIMUM_REALM_PAGE_SIZE, token);
            query.put("customer", options.customer());
            final Outcome<WirePage<Group>> fetched = wirePage(
                    url(Builder.REALM_GROUPS, null, query),
                    access,
                    timeout,
                    "snapshot",
                    "groups",
                    GoogleRealmAdapter::group);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
                return propagate(fetched);
            }
            for (Group group : success.value().items()) {
                if (found) {
                    return Outcome.succeeded(Optional.of(group.id()));
                }
                if (current.getOrNull().equals(group.id())) {
                    if (!following) {
                        return Outcome.succeeded(Optional.of(group.id()));
                    }
                    found = true;
                }
            }
            if (success.value().next().isEmpty()) {
                if (!found) {
                    return failed(ErrorCode._502, "Google group replay parent no longer exists");
                }
                return Outcome.succeeded(Optional.empty());
            }
            token = success.value().next();
        }
    }

    /**
     * Builds one URL from a manifest-owned endpoint, safely encoded path segments, and fixed query values.
     *
     * @param targetName exact root Builder management target key
     * @param segments   optional stable path segments appended to the target base
     * @param query      exact query members
     * @return immutable Google HTTPS URL
     */
    private Url url(final String targetName, final List<String> segments, final Map<String, String> query) {
        final Url base = target(targetName).url();
        final StringBuilder path = new StringBuilder(base.path());
        if (segments != null) {
            for (String segment : segments) {
                path.append('/')
                        .append(RFC3986.SEGMENT.encode(requireText(segment, "Google path segment"), Charset.UTF_8));
            }
        }
        final UrlBuilder builder = Url.builder().scheme(base.scheme()).host(base.host()).path(path.toString());
        query.forEach(builder::query);
        return builder.build();
    }

    /**
     * Resolves one required manifest-owned management target.
     *
     * @param name fixed root Builder management key
     * @return exact resolved HTTPS endpoint
     * @throws ValidateException if the selected manifest omits the required target
     */
    private Endpoint target(final String name) {
        final Endpoint endpoint = targets.management().get(name);
        if (endpoint == null) {
            throw new ValidateException("Google Workspace manifest omits a required management target");
        }
        return endpoint;
    }

    /**
     * Computes the deterministic organization projection fingerprint in frozen field order.
     *
     * @param organizations stable-ID-sorted organization projection
     * @return 64-character lowercase SHA-256 fingerprint
     */
    private String organizationFingerprint(final List<Organization> organizations) {
        final List<JsonValue> values = new ArrayList<>(organizations.size());
        for (Organization organization : organizations) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put("orgUnitId", new JsonValue.StringValue(organization.id()));
            item.put("name", new JsonValue.StringValue(organization.name()));
            item.put("parentOrgUnitId", nullable(organization.parentId()));
            item.put("orgUnitPath", new JsonValue.StringValue(organization.path()));
            values.add(new JsonValue.ObjectValue(item));
        }
        final byte[] encoded = JsonKit.writeValue(new JsonValue.ArrayValue(values));
        return Builder.sha256Hex(new String(encoded, Charset.UTF_8));
    }

    /**
     * Creates one page with an encoded continuation cursor.
     *
     * @param resources normalized resources in output order
     * @param relations normalized relations in output order
     * @param state     current finite cursor state
     * @param position  recoverable position for the same phase
     * @return successful normalized page outcome
     */
    private Outcome<Realm.Page> page(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Position position) {
        final Realm.Cursor cursor = encode(new CursorState(state.phase(), state.kinds(), position));
        return Outcome.succeeded(new Realm.Page(resources, relations, Optional.of(cursor)));
    }

    /**
     * Continues one official page-token phase or advances after natural upstream exhaustion.
     *
     * @param resources normalized resources
     * @param relations normalized relations
     * @param state     current finite cursor state
     * @param next      optional official next-page token
     * @return continued or phase-completed page outcome
     */
    private Outcome<Realm.Page> continuedOrCompleted(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Optional<String> next) {
        return next.isPresent() ? page(resources, relations, state, Position.page(next.getOrNull()))
                : completedPhase(resources, relations, state);
    }

    /**
     * Completes one finite phase and selects the next phase enabled by the original kind set.
     *
     * @param resources resources produced by the completed phase
     * @param relations relations produced by the completed phase
     * @param state     completed finite cursor state
     * @return successful page with the next phase cursor or natural completion
     */
    private Outcome<Realm.Page> completedPhase(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state) {
        final Phase next = nextPhase(state.phase(), state.kinds());
        final Optional<Realm.Cursor> cursor = next == null ? Optional.empty()
                : Optional.of(encode(new CursorState(next, state.kinds(), Position.initial())));
        return Outcome.succeeded(new Realm.Page(resources, relations, cursor));
    }

    /**
     * Encodes one canonical six-field Google Workspace snapshot cursor.
     *
     * @param state validated finite pagination state
     * @return opaque unpadded Base64 URL-safe cursor
     */
    private Realm.Cursor encode(final CursorState state) {
        final Map<String, JsonValue> envelope = new LinkedHashMap<>();
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(GoogleManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(GoogleManifest.WORKSPACE.value()));
        envelope.put(Builder.OPERATION_FIELD, number(Realm.Operation.SNAPSHOT.code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code()));
        final List<JsonValue> kinds = new ArrayList<>(state.kinds().size());
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        envelope.put(Builder.CURSOR_POSITION_FIELD, position(state.phase(), state.position()));
        final byte[] json = JsonKit.writeValue(new JsonValue.ObjectValue(envelope));
        return new Realm.Cursor(Base64.encodeUrlSafe(json));
    }

    /**
     * Decodes, validates, and canonicalizes one Google Workspace snapshot cursor.
     *
     * @param cursor opaque caller-supplied cursor
     * @param kinds  exact kinds requested by the current snapshot call
     * @return validated finite pagination state
     * @throws ValidateException if encoding, fields, types, context, position, or canonical form is invalid
     */
    private CursorState decode(final Realm.Cursor cursor, final Set<Realm.Kind> kinds) {
        try {
            final byte[] decoded = Base64.decode(cursor.value());
            final JsonValue value = JsonKit.readValue(decoded, Builder.MAXIMUM_REALM_JSON_DEPTH, true);
            final JsonValue.ObjectValue envelope = requiredObject(value, "cursor envelope");
            exactMembers(
                    envelope,
                    Set.of(
                            Builder.VENDOR_FIELD,
                            Builder.VARIANT_FIELD,
                            Builder.OPERATION_FIELD,
                            Builder.CURSOR_PHASE_FIELD,
                            Builder.CURSOR_KIND_FIELD,
                            Builder.CURSOR_POSITION_FIELD),
                    "Google cursor envelope");
            if (!GoogleManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !GoogleManifest.WORKSPACE.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredLong(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("Google cursor does not belong to this Workspace snapshot");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("Google cursor kinds do not match the snapshot request");
            }
            final CursorState state = new CursorState(phase, decodedKinds,
                    position(phase, requiredObject(envelope, Builder.CURSOR_POSITION_FIELD)));
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("Google cursor is not in canonical form");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("Google cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded JSON response object with duplicate-member rejection.
     *
     * @param response owned successful HTTP response
     * @return decoded top-level JSON object
     */
    private JsonValue.ObjectValue object(final Response response) {
        return requiredObject(
                JsonKit.readValue(
                        response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES),
                        Builder.MAXIMUM_REALM_JSON_DEPTH,
                        true),
                "Google response");
    }

    /**
     * Decodes the complete official unpaged organization response without a local materialization threshold.
     *
     * @param response owned successful G-02 response
     * @return decoded top-level JSON object
     */
    private JsonValue.ObjectValue unpagedObject(final Response response) {
        final Buffer buffer = new Buffer();
        Payload.copyTo(response.body().payload(), buffer);
        return requiredObject(
                JsonKit.readValue(buffer.readByteArray(), Builder.MAXIMUM_REALM_JSON_DEPTH, true),
                "Google unpaged organization response");
    }

    /**
     * Builds safe allow-listed outcome details for one upstream response.
     *
     * @param operation safe Realm operation label
     * @param status    upstream HTTP status
     * @param headers   headers used only for Retry-After normalization
     * @return immutable allow-listed scalar details
     */
    private Map<String, JsonValue> details(final String operation, final int status, final FabricX.Headers headers) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(GoogleManifest.ID.value()));
        details.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(variant.variant().value()));
        details.put(Builder.OPERATION_FIELD, new JsonValue.StringValue(operation));
        details.put(Builder.HTTP_STATUS_FIELD, number(status));
        final String retryAfter = headers.get(Http.Header.RETRY_AFTER);
        if (retryAfter != null) {
            try {
                final long seconds = Long.parseLong(retryAfter);
                if (seconds >= 0L) {
                    details.put(Builder.RETRY_AFTER_SECONDS_FIELD, number(seconds));
                }
            } catch (NumberFormatException ignored) {
                details.remove(Builder.RETRY_AFTER_SECONDS_FIELD);
            }
        }
        return Collections.unmodifiableMap(details);
    }

    /**
     * Defines the complete finite Google Workspace snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Completely replays organization units and emits organization resources and parent relations.
         */
        ORG_UNITS(1, Realm.Kind.ORGANIZATION),

        /**
         * Reads official user pages and emits user resources plus resolvable organization memberships.
         */
        USERS(2, Realm.Kind.USER),

        /**
         * Reads official group pages and emits group resources.
         */
        GROUPS(3, Realm.Kind.GROUP),

        /**
         * Replays group parents and reads their official member pages.
         */
        GROUP_MEMBERS(4, Realm.Kind.GROUP),

        /**
         * Reads official administrator-role pages and emits role resources.
         */
        ROLES(5, Realm.Kind.ROLE),

        /**
         * Reads official role-assignment pages and emits user-to-role membership relations.
         */
        ROLE_ASSIGNMENTS(6, Realm.Kind.ROLE);

        /**
         * Stable persisted phase code independent of enum ordinal.
         */
        private final int code;

        /**
         * Resource category that enables this phase.
         */
        private final Realm.Kind kind;

        /**
         * Creates one stable Google Workspace phase.
         *
         * @param code stable persisted phase code
         * @param kind resource category enabling the phase
         */
        Phase(final int code, final Realm.Kind kind) {
            this.code = code;
            this.kind = kind;
        }

        /**
         * Resolves one stable phase code without using ordinal values.
         *
         * @param code persisted phase code
         * @return exact finite phase
         */
        private static Phase from(final int code) {
            for (Phase phase : values()) {
                if (phase.code == code) {
                    return phase;
                }
            }
            throw new ValidateException("Google cursor contains an unknown phase code");
        }

        /**
         * Returns the stable persisted phase code.
         *
         * @return phase code
         */
        private int code() {
            return code;
        }

        /**
         * Returns the resource category enabling this phase.
         *
         * @return owning resource category
         */
        private Realm.Kind kind() {
            return kind;
        }

        /**
         * Returns the next declared snapshot phase.
         *
         * @return following phase or {@code null}
         */
        private Phase next() {
            return switch (this) {
                case ORG_UNITS -> USERS;
                case USERS -> GROUPS;
                case GROUPS -> GROUP_MEMBERS;
                case GROUP_MEMBERS -> ROLES;
                case ROLES -> ROLE_ASSIGNMENTS;
                case ROLE_ASSIGNMENTS -> null;
            };
        }
    }

    /**
     * Carries one canonical Google Workspace snapshot cursor state.
     *
     * @param phase    exact finite phase
     * @param kinds    requested kinds in stable code order
     * @param position recoverable phase-specific position
     * @author Kimi Liu
     */
    private record CursorState(Phase phase, List<Realm.Kind> kinds, Position position) {

        /**
         * Validates and freezes one canonical cursor state.
         *
         * @param phase    exact finite phase
         * @param kinds    requested kinds
         * @param position recoverable position
         */
        private CursorState {
            phase = Assert.notNull(phase, "Google cursor phase must not be null");
            Assert.notNull(kinds, "Google cursor kinds must not be null");
            final List<Realm.Kind> copy = new ArrayList<>(kinds.size());
            int previous = 0;
            for (Realm.Kind kind : kinds) {
                final Realm.Kind checked = Assert.notNull(kind, "Google cursor kind must not be null");
                if (!SUPPORTED_KINDS.contains(checked) || checked.code() <= previous) {
                    throw new ValidateException("Google cursor kinds must be supported and in stable code order");
                }
                previous = checked.code();
                copy.add(checked);
            }
            if (copy.isEmpty()) {
                throw new ValidateException("Google cursor kinds must not be empty");
            }
            kinds = List.copyOf(copy);
            if (!kinds.contains(phase.kind())) {
                throw new ValidateException("Google cursor phase is not enabled by its requested kinds");
            }
            position = Assert.notNull(position, "Google cursor position must not be null");
            position.validate(phase);
        }

        /**
         * Creates the first enabled snapshot phase for one validated kind set.
         *
         * @param kinds exact requested kinds in stable code order
         * @return canonical initial cursor state
         */
        private static CursorState initial(final Set<Realm.Kind> kinds) {
            final List<Realm.Kind> ordered = List.copyOf(kinds);
            Phase phase = Phase.ORG_UNITS;
            while (phase != null && !ordered.contains(phase.kind())) {
                phase = phase.next();
            }
            if (phase == null) {
                throw new ValidateException("Google snapshot kind set has no enabled phase");
            }
            return new CursorState(phase, ordered, Position.initial());
        }
    }

    /**
     * Carries one recoverable Google Workspace phase position.
     *
     * @param next           official page token for paged phases
     * @param offset         next organization index for unpaged replay
     * @param relationOffset fixed zero relation offset retained by the unpaged position contract
     * @param parentId       current stable group identifier for the member phase
     * @param fingerprint    complete lowercase organization projection fingerprint
     * @author Kimi Liu
     */
    private record Position(Optional<String> next, int offset, int relationOffset, Optional<String> parentId,
            Optional<String> fingerprint) {

        /**
         * Validates and freezes one recoverable position.
         *
         * @param next           optional official page token
         * @param offset         non-negative organization replay index
         * @param relationOffset non-negative relation replay index
         * @param parentId       optional stable group identifier
         * @param fingerprint    optional lowercase projection fingerprint
         */
        private Position {
            next = optional(next, "Google cursor next token");
            parentId = optional(parentId, "Google cursor parent identifier");
            fingerprint = optional(fingerprint, "Google cursor fingerprint");
            if (offset < 0 || relationOffset < 0) {
                throw new ValidateException("Google cursor offsets must not be negative");
            }
            if (fingerprint.isPresent() && !fingerprint(fingerprint.getOrNull())) {
                throw new ValidateException("Google cursor fingerprint must be lowercase SHA-256 hexadecimal");
            }
        }

        /**
         * Creates one empty initial position.
         *
         * @return canonical empty position
         */
        private static Position initial() {
            return new Position(Optional.empty(), 0, 0, Optional.empty(), Optional.empty());
        }

        /**
         * Creates one official top-level page-token continuation.
         *
         * @param next official next-page token
         * @return canonical paged position
         */
        private static Position page(final String next) {
            return new Position(Optional.of(next), 0, 0, Optional.empty(), Optional.empty());
        }

        /**
         * Creates one organization unpaged-replay continuation.
         *
         * @param offset      next stable organization index
         * @param fingerprint complete projection fingerprint
         * @return canonical unpaged position
         */
        private static Position unpaged(final int offset, final String fingerprint) {
            return new Position(Optional.empty(), offset, 0, Optional.empty(), Optional.ofNullable(fingerprint));
        }

        /**
         * Creates one group-member parent and page-token continuation.
         *
         * @param parentId stable current or following group identifier
         * @param next     official child page token or {@code null}
         * @return canonical dependent position
         */
        private static Position parent(final String parentId, final String next) {
            return new Position(Optional.ofNullable(next), 0, 0, Optional.ofNullable(parentId), Optional.empty());
        }

        /**
         * Validates one Bus optional text container without changing its lexical value.
         *
         * @param value optional text container
         * @param label safe semantic label
         * @return detached optional original text
         */
        private static Optional<String> optional(final Optional<String> value, final String label) {
            Assert.notNull(value, label + " container must not be null");
            return value.isPresent() ? Optional.of(requireText(value.getOrNull(), label)) : Optional.empty();
        }

        /**
         * Reports whether one string is exactly 64 lowercase hexadecimal characters.
         *
         * @param value candidate fingerprint
         * @return whether the candidate is canonical SHA-256 hexadecimal
         */
        private static boolean fingerprint(final String value) {
            if (value.length() != Normal._64) {
                return false;
            }
            for (int index = 0; index < value.length(); index++) {
                final char character = value.charAt(index);
                if (!((character >= Symbol.C_ZERO && character <= Symbol.C_NINE)
                        || (character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_F))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Verifies phase-specific ownership of every position component.
         *
         * @param phase exact finite snapshot phase
         */
        private void validate(final Phase phase) {
            if (phase == Phase.ORG_UNITS) {
                if (next.isPresent() || parentId.isPresent() || relationOffset != 0
                        || (offset == 0) == fingerprint.isPresent()) {
                    throw new ValidateException("Google organization replay cursor is inconsistent");
                }
                return;
            }
            if (offset != 0 || relationOffset != 0 || fingerprint.isPresent()) {
                throw new ValidateException("Google paged cursor contains an unpaged replay value");
            }
            if (phase == Phase.GROUP_MEMBERS) {
                if (next.isPresent() && parentId.isEmpty()) {
                    throw new ValidateException("Google group-member page token requires a parent identifier");
                }
            } else if (parentId.isPresent()) {
                throw new ValidateException("Google top-level page cursor must not contain a parent identifier");
            }
        }
    }

    /**
     * Carries one decoded official Google collection page.
     *
     * @param items minimal page items
     * @param next  official next-page token or empty
     * @param <T>   minimal item type
     * @author Kimi Liu
     */
    private record WirePage<T>(List<T> items, Optional<String> next) {

        /**
         * Freezes one decoded Google collection page.
         *
         * @param items minimal page items
         * @param next  optional official next-page token
         */
        private WirePage {
            items = List.copyOf(Assert.notNull(items, "Google page items must not be null"));
            next = Position.optional(next, "Google next-page token");
        }
    }

    /**
     * Minimal Google organization-unit projection.
     *
     * @param id       stable organization-unit identifier
     * @param name     exact display name
     * @param parentId optional stable parent organization-unit identifier
     * @param path     exact organization-unit path used by direct retrieval and user membership resolution
     * @author Kimi Liu
     */
    private record Organization(String id, String name, Optional<String> parentId, String path) {

        /**
         * Validates one organization-unit projection.
         *
         * @param id       stable organization-unit identifier
         * @param name     exact display name
         * @param parentId optional stable parent identifier
         * @param path     exact organization-unit path
         */
        private Organization {
            id = requireText(id, "Google organization identifier");
            name = requireText(name, "Google organization display name");
            parentId = Position.optional(parentId, "Google parent organization identifier");
            if (parentId.isPresent() && id.equals(parentId.getOrNull())) {
                throw new ValidateException("Google organization must not reference itself as parent");
            }
            path = requireText(path, "Google organization path");
        }
    }

    /**
     * Minimal Google user projection.
     *
     * @param id               stable user identifier
     * @param primaryEmail     primary email identifier
     * @param name             exact full display name
     * @param suspended        official suspended account flag
     * @param organizationPath exact organization path used for membership resolution
     * @author Kimi Liu
     */
    private record User(String id, String primaryEmail, String name, boolean suspended, String organizationPath) {

        /**
         * Validates one user projection.
         *
         * @param id               stable user identifier
         * @param primaryEmail     primary email identifier
         * @param name             exact full display name
         * @param suspended        official suspended flag
         * @param organizationPath exact organization path
         */
        private User {
            id = requireText(id, "Google user identifier");
            primaryEmail = requireText(primaryEmail, "Google user primary email");
            name = requireText(name, "Google user display name");
            organizationPath = requireText(organizationPath, "Google user organization path");
        }
    }

    /**
     * Minimal Google group projection.
     *
     * @param id    stable group identifier
     * @param email group email identifier
     * @param name  exact group display name
     * @author Kimi Liu
     */
    private record Group(String id, String email, String name) {

        /**
         * Validates one group projection.
         *
         * @param id    stable group identifier
         * @param email group email identifier
         * @param name  exact group display name
         */
        private Group {
            id = requireText(id, "Google group identifier");
            email = requireText(email, "Google group email");
            name = requireText(name, "Google group display name");
        }
    }

    /**
     * Minimal Google group-member projection.
     *
     * @param id stable user identifier returned by the member collection
     * @author Kimi Liu
     */
    private record Member(String id) {

        /**
         * Validates one group-member projection.
         *
         * @param id stable member identifier
         */
        private Member {
            id = requireText(id, "Google group-member identifier");
        }
    }

    /**
     * Minimal Google administrator-role projection.
     *
     * @param id   stable role identifier
     * @param name exact role display name
     * @author Kimi Liu
     */
    private record Role(String id, String name) {

        /**
         * Validates one administrator-role projection.
         *
         * @param id   stable role identifier
         * @param name exact role display name
         */
        private Role {
            id = requireText(id, "Google role identifier");
            name = requireText(name, "Google role display name");
        }
    }

    /**
     * Minimal Google role-assignment projection.
     *
     * @param userId stable assigned user identifier
     * @param roleId stable assigned role identifier
     * @author Kimi Liu
     */
    private record RoleAssignment(String userId, String roleId) {

        /**
         * Validates one role-assignment projection.
         *
         * @param userId stable assigned user identifier
         * @param roleId stable assigned role identifier
         */
        private RoleAssignment {
            userId = requireText(userId, "Google role-assignment user identifier");
            roleId = requireText(roleId, "Google role-assignment role identifier");
        }
    }

    /**
     * Holds one short-lived delegated Google access token inside the Source-private cache.
     *
     * @param token           non-blank upstream token
     * @param expiresAtMillis absolute expiration in Fabric clock milliseconds or zero when uncacheable
     * @author Kimi Liu
     */
    private record Access(String token, long expiresAtMillis) {

        /**
         * Validates one upstream access value.
         *
         * @param token           non-blank upstream token
         * @param expiresAtMillis positive absolute expiration or zero
         */
        private Access {
            token = requireText(token, "Google delegated access token");
            if (expiresAtMillis < 0L) {
                throw new ValidateException("Google access expiration must not be negative");
            }
        }

        /**
         * Returns a fixed representation that cannot disclose token or lifetime.
         *
         * @return root redacted marker
         */
        @Override
        public String toString() {
            return Builder.REDACTED_VALUE;
        }
    }

}
