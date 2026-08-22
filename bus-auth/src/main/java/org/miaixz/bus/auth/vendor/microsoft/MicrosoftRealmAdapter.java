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
package org.miaixz.bus.auth.vendor.microsoft;

import java.math.BigDecimal;
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
import org.miaixz.bus.auth.Realm;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the provider-neutral Microsoft Graph realm surface for the isolated global and China clouds.
 * <p>
 * Snapshot pagination advances through nine finite phases: organization, users, managers, groups, group members, roles,
 * role members, service principals, and application assignments. Dependent phases replay their official parent list
 * from its fixed initial URL until the stable {@code parent_id} is reached; only the current child nextLink is
 * persisted. Changes support one USER, GROUP, or SERVICE_ACCOUNT feed per invocation and preserve only a validated
 * official nextLink or deltaLink. Every persisted URL is checked against the selected cloud host, an allowed
 * {@code /v1.0} path, and the exact Graph query-name closure before use.
 * </p>
 *
 * @author Kimi Liu
 */
public final class MicrosoftRealmAdapter implements VendorAdapter {

    /**
     * Empty immutable JSON object used by resources and relations without allow-listed attributes.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Exact resource categories exposed by both Microsoft enterprise variants.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set.of(
            Realm.Kind.USER,
            Realm.Kind.ORGANIZATION,
            Realm.Kind.GROUP,
            Realm.Kind.ROLE,
            Realm.Kind.SERVICE_ACCOUNT);

    /**
     * Exact resource categories with an official Graph delta feed in this contract.
     */
    private static final Set<Realm.Kind> CHANGE_KINDS = Set
            .of(Realm.Kind.USER, Realm.Kind.GROUP, Realm.Kind.SERVICE_ACCOUNT);

    /**
     * Ordered management-target key closure required from each selected manifest.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.ENTERPRISE_USERS,
            Builder.ENTERPRISE_USER,
            Builder.ENTERPRISE_ORGANIZATION,
            Builder.ENTERPRISE_GROUPS,
            Builder.ENTERPRISE_GROUP,
            Builder.ENTERPRISE_GROUP_MEMBERS,
            Builder.ENTERPRISE_ROLES,
            Builder.ENTERPRISE_ROLE_MEMBERS,
            Builder.ENTERPRISE_ROLE_ASSIGNMENTS,
            Builder.ENTERPRISE_SERVICE_ACCOUNTS,
            Builder.ENTERPRISE_CHANGES);

    /**
     * Query names that an official Graph nextLink or deltaLink may carry.
     */
    private static final Set<String> GRAPH_QUERY_NAMES = Set
            .of("$select", "$top", "$skip", "$skiptoken", "$deltatoken");

    /**
     * Selected immutable Microsoft enterprise Variant.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated Microsoft enterprise deployment options.
     */
    private final MicrosoftOptions options;

    /**
     * Caller-owned services used without taking lifecycle ownership.
     */
    private final DriverServices services;

    /**
     * Resolved token and Graph endpoints declared by the selected manifest.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Exact Graph host allowed for requests and persisted pagination URLs.
     */
    private final String graphHost;

    /**
     * Source-private cache containing only a short-lived Graph access token.
     */
    private final CacheX<String, Access> accessCache;

    /**
     * Creates one Source-isolated Microsoft realm adapter.
     *
     * @param spaceId  registration space used for credential isolation
     * @param sourceId registered Source identifier used for ownership validation
     * @param manifest exact Microsoft manifest
     * @param variant  exact selected Microsoft enterprise Variant
     * @param options  validated Microsoft enterprise options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if manifest, Variant, protocol, targets, or options are inconsistent
     */
    public MicrosoftRealmAdapter(final String spaceId, final String sourceId, final MicrosoftManifest manifest,
            final VariantManifest.Variant variant, final MicrosoftOptions options, final DriverServices services) {
        Assert.notBlank(spaceId, "Microsoft enterprise space id must not be blank");
        Assert.notBlank(sourceId, "Microsoft enterprise Source id must not be blank");
        final MicrosoftManifest selectedManifest = Assert
                .notNull(manifest, "Microsoft enterprise manifest must not be null");
        this.variant = Assert.notNull(variant, "Microsoft enterprise Variant must not be null");
        this.options = Assert.notNull(options, "Microsoft enterprise options must not be null");
        this.services = Assert.notNull(services, "Microsoft enterprise services must not be null");
        final boolean enterprise = MicrosoftManifest.ENTERPRISE_GLOBAL.equals(this.variant.variant())
                || MicrosoftManifest.ENTERPRISE_CHINA.equals(this.variant.variant());
        if (!enterprise || !MicrosoftManifest.ID.equals(selectedManifest.vendor())
                || !selectedManifest.variant(this.variant.variant()).equals(this.variant)
                || !MicrosoftManifest.ID.equals(this.variant.platform()) || this.variant.protocol() != Protocol.HTTPS
                || !MicrosoftManifest.ID.equals(this.options.vendor())
                || !this.variant.variant().equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || this.options.scopes().size() != Normal._1
                || !this.variant.defaultScopes().equals(this.options.scopes())) {
            throw new ValidateException("Microsoft realm adapter requires one frozen enterprise HTTPS variant");
        }
        this.services.securityBaseline().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (this.targets.token().isEmpty()
                || !List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("Microsoft enterprise manifest has an invalid target set");
        }
        this.graphHost = target(Builder.ENTERPRISE_CHANGES).url().host();
        for (Endpoint endpoint : this.targets.management().values()) {
            if (!this.graphHost.equals(endpoint.url().host())) {
                throw new ValidateException("Microsoft enterprise Graph targets must use one isolated cloud host");
            }
        }
        this.accessCache = new MemoryCache<>(FabricX.clock(this.services.fabric())::millis);
    }

    /**
     * Maps one user delta item using only the explicit {@code @removed} deletion marker.
     *
     * @param value      decoded user delta item
     * @param observedAt shared invocation observation instant
     * @param changes    mutable current-page change projection
     */
    private static void userChanges(
            final JsonValue.ObjectValue value,
            final Instant observedAt,
            final List<Realm.Change> changes) {
        final String id = requiredString(value, "id");
        if (removed(value)) {
            appendChange(changes, new Realm.ResourceDelete(new Realm.Key(Realm.Kind.USER, id), observedAt));
        } else {
            appendChange(changes, new Realm.ResourceUpsert(userResource(user(value), observedAt)));
        }
    }

    /**
     * Maps one group delta item and each explicit {@code members@delta} relation marker.
     *
     * @param value      decoded group delta item
     * @param observedAt shared invocation observation instant
     * @param changes    mutable current-page change projection
     */
    private static void groupChanges(
            final JsonValue.ObjectValue value,
            final Instant observedAt,
            final List<Realm.Change> changes) {
        final String id = requiredString(value, "id");
        if (removed(value)) {
            appendChange(changes, new Realm.ResourceDelete(new Realm.Key(Realm.Kind.GROUP, id), observedAt));
            return;
        }
        final boolean resourceProjection = value.values().containsKey("displayName")
                && value.values().containsKey("mailEnabled") && value.values().containsKey("securityEnabled");
        final boolean memberProjection = value.values().containsKey("members@delta");
        if (!resourceProjection && !memberProjection) {
            throw new ValidateException("Microsoft group delta item has no complete supported projection");
        }
        if (resourceProjection) {
            appendChange(changes, new Realm.ResourceUpsert(groupResource(group(value), observedAt)));
        }
        for (JsonValue memberValue : optionalArray(value, "members@delta")) {
            final JsonValue.ObjectValue member = requiredObject(memberValue, "group delta member");
            final String memberId = requiredString(member, "id");
            final Realm.Relation relation = relation(
                    Realm.RelationKind.MEMBER,
                    Realm.Kind.USER,
                    memberId,
                    Realm.Kind.GROUP,
                    id,
                    observedAt);
            if (removed(member)) {
                appendChange(changes, new Realm.RelationDelete(relation.key(), observedAt));
            } else {
                appendChange(changes, new Realm.RelationUpsert(relation));
            }
        }
    }

    /**
     * Maps one service-principal delta item using only the explicit {@code @removed} marker.
     *
     * @param value      decoded service-principal delta item
     * @param observedAt shared invocation observation instant
     * @param changes    mutable current-page change projection
     */
    private static void serviceAccountChanges(
            final JsonValue.ObjectValue value,
            final Instant observedAt,
            final List<Realm.Change> changes) {
        final String id = requiredString(value, "id");
        if (removed(value)) {
            appendChange(changes, new Realm.ResourceDelete(new Realm.Key(Realm.Kind.SERVICE_ACCOUNT, id), observedAt));
        } else {
            appendChange(changes, new Realm.ResourceUpsert(serviceAccountResource(serviceAccount(value), observedAt)));
        }
    }

    /**
     * Adds one change after enforcing same-page key de-duplication and conflict failure.
     *
     * @param changes mutable current-page changes
     * @param change  candidate change
     */
    private static void appendChange(final List<Realm.Change> changes, final Realm.Change change) {
        final Object key = changeKey(change);
        for (Realm.Change existing : changes) {
            if (key.equals(changeKey(existing))) {
                if (change.equals(existing)) {
                    return;
                }
                throw new ValidateException("Microsoft delta page contains conflicting changes for one key");
            }
        }
        changes.add(change);
    }

    /**
     * Extracts the provider-neutral resource or relation key from one change variant.
     *
     * @param change exact enterprise change
     * @return stable resource or relation key
     */
    private static Object changeKey(final Realm.Change change) {
        return switch (change) {
            case Realm.ResourceUpsert upsert -> upsert.resource().key();
            case Realm.ResourceDelete delete -> delete.key();
            case Realm.RelationUpsert upsert -> upsert.relation().key();
            case Realm.RelationDelete delete -> delete.key();
        };
    }

    /**
     * Reports whether a Graph delta object carries the explicit deletion marker.
     *
     * @param value decoded Graph delta object
     * @return whether {@code @removed} is a JSON object
     */
    private static boolean removed(final JsonValue.ObjectValue value) {
        final JsonValue marker = value.values().get("@removed");
        if (marker == null) {
            return false;
        }
        if (!(marker instanceof JsonValue.ObjectValue)) {
            throw new ValidateException("Microsoft @removed marker must be a JSON object");
        }
        return true;
    }

    /**
     * Reports whether one authenticated operation was rejected for an invalid Graph token.
     *
     * @param outcome completed authenticated outcome
     * @return whether the outcome carries the shared 401 error
     */
    private static boolean unauthorized(final Outcome<?> outcome) {
        return outcome instanceof Outcome.Rejected<?> rejected && ErrorCode._401.equals(rejected.failure().error());
    }

    /**
     * Builds one immutable URL on an already validated manifest-owned Graph origin.
     *
     * @param base  manifest-owned Graph URL
     * @param path  fixed path with encoded external segments
     * @param query exact query values
     * @return immutable URL
     */
    private static Url build(final Url base, final String path, final Map<String, String> query) {
        final UrlBuilder builder = Url.builder().scheme(base.scheme()).host(base.host()).path(path);
        query.forEach(builder::query);
        return builder.build();
    }

    /**
     * Validates phase-specific Graph select, top, skip, and delta query semantics.
     *
     * @param url       normalized official pagination URL
     * @param phase     owning finite phase
     * @param operation snapshot or changes operation
     * @return whether all present query values belong to the frozen request template
     */
    private static boolean validQuery(final Url url, final Phase phase, final Realm.Operation operation) {
        final String selected = url.query("$select");
        final String expected = operation == Realm.Operation.CHANGES && phase == Phase.DELTA_GROUPS
                ? select(phase.kind()) + ",members"
                : select(phase.kind());
        if (selected != null && !expected.equals(selected)) {
            return false;
        }
        if (operation == Realm.Operation.SNAPSHOT && url.query("$deltatoken") != null) {
            return false;
        }
        final String skipToken = url.query("$skiptoken");
        final String deltaToken = url.query("$deltatoken");
        if (skipToken != null && skipToken.isEmpty() || deltaToken != null && deltaToken.isEmpty()) {
            return false;
        }
        final String top = url.query("$top");
        if (top != null && !boundedPositiveInteger(top)) {
            return false;
        }
        final String skip = url.query("$skip");
        return skip == null || nonNegativeInteger(skip);
    }

    /**
     * Reports whether text is a canonical positive integer within the framework page limit.
     *
     * @param value candidate query value
     * @return whether the value is canonical and bounded
     */
    private static boolean boundedPositiveInteger(final String value) {
        try {
            final int number = Integer.parseInt(value);
            return number > 0 && number <= Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE
                    && Integer.toString(number).equals(value);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reports whether text is a canonical non-negative integer.
     *
     * @param value candidate query value
     * @return whether the value is canonical and non-negative
     */
    private static boolean nonNegativeInteger(final String value) {
        try {
            final long number = Long.parseLong(value);
            return number >= 0L && Long.toString(number).equals(value);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reports whether a normalized Graph pagination path belongs to the current phase.
     *
     * @param path      normalized Graph path
     * @param phase     owning finite phase
     * @param operation snapshot or changes operation
     * @return whether the path is in the frozen allow-list
     */
    private static boolean allowedPath(final String path, final Phase phase, final Realm.Operation operation) {
        if (operation == Realm.Operation.CHANGES) {
            return path.equals(switch (phase) {
                case DELTA_USERS -> "/v1.0/users/delta";
                case DELTA_GROUPS -> "/v1.0/groups/delta";
                case DELTA_SERVICE_ACCOUNTS -> "/v1.0/servicePrincipals/delta";
                default -> Normal.EMPTY;
            });
        }
        return switch (phase) {
            case USERS, USER_MANAGERS -> path.equals("/v1.0/users")
                    || phase == Phase.USER_MANAGERS && childPath(path, "/v1.0/users/", "/manager");
            case GROUPS -> path.equals("/v1.0/groups");
            case GROUP_MEMBERS -> path.equals("/v1.0/groups") || childPath(path, "/v1.0/groups/", "/members");
            case ROLES -> path.equals("/v1.0/directoryRoles");
            case ROLE_MEMBERS -> path.equals("/v1.0/directoryRoles")
                    || childPath(path, "/v1.0/directoryRoles/", "/members");
            case SERVICE_ACCOUNTS -> path.equals("/v1.0/servicePrincipals");
            case APPLICATION_ASSIGNMENTS -> path.equals("/v1.0/servicePrincipals")
                    || childPath(path, "/v1.0/servicePrincipals/", "/appRoleAssignments");
            case ORGANIZATION -> path.equals("/v1.0/organization");
            default -> false;
        };
    }

    /**
     * Validates one child collection path containing exactly one encoded stable identifier segment.
     *
     * @param path   normalized candidate path
     * @param prefix fixed collection prefix
     * @param suffix fixed child suffix
     * @return whether exactly one non-empty identifier separates prefix and suffix
     */
    private static boolean childPath(final String path, final String prefix, final String suffix) {
        return path.startsWith(prefix) && path.endsWith(suffix) && path.length() > prefix.length() + suffix.length()
                && path.substring(prefix.length(), path.length() - suffix.length()).indexOf('/') < 0;
    }

    /**
     * Selects the next finite snapshot phase required by the canonical kind list.
     *
     * @param phase current completed snapshot phase
     * @param kinds requested kinds in stable code order
     * @return next required phase or {@code null}
     */
    private static Phase nextPhase(final Phase phase, final List<Realm.Kind> kinds) {
        Phase candidate = phase;
        while (true) {
            candidate = candidate.nextSnapshot();
            if (candidate == null || kinds.contains(candidate.kind())) {
                return candidate;
            }
        }
    }

    /**
     * Returns the frozen Graph select projection for one resource kind.
     *
     * @param kind resource kind
     * @return comma-delimited Graph select projection
     */
    private static String select(final Realm.Kind kind) {
        return switch (kind) {
            case USER -> "id,displayName,userPrincipalName,mail,accountEnabled";
            case ORGANIZATION -> "id,displayName";
            case GROUP -> "id,displayName,mail,mailEnabled,securityEnabled";
            case ROLE -> "id,displayName";
            case SERVICE_ACCOUNT -> "id,appId,displayName,accountEnabled";
        };
    }

    /**
     * Parses one minimal Microsoft organization projection.
     *
     * @param value decoded organization object
     * @return validated organization projection
     */
    private static Organization organization(final JsonValue.ObjectValue value) {
        return new Organization(requiredString(value, "id"), requiredString(value, "displayName"));
    }

    /**
     * Parses one minimal Microsoft user projection.
     *
     * @param value decoded user object
     * @return validated user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        return new User(requiredString(value, "id"), requiredString(value, "displayName"),
                optionalString(value, "userPrincipalName"), optionalString(value, "mail"),
                requiredBoolean(value, "accountEnabled"));
    }

    /**
     * Parses one minimal Microsoft group projection.
     *
     * @param value decoded group object
     * @return validated group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        return new Group(requiredString(value, "id"), requiredString(value, "displayName"),
                optionalString(value, "mail"), requiredBoolean(value, "mailEnabled"),
                requiredBoolean(value, "securityEnabled"));
    }

    /**
     * Parses one minimal Microsoft directory role projection.
     *
     * @param value decoded directory role object
     * @return validated role projection
     */
    private static Role role(final JsonValue.ObjectValue value) {
        return new Role(requiredString(value, "id"), requiredString(value, "displayName"));
    }

    /**
     * Parses one minimal Microsoft service-principal projection.
     *
     * @param value decoded service principal object
     * @return validated service-account projection
     */
    private static ServiceAccount serviceAccount(final JsonValue.ObjectValue value) {
        return new ServiceAccount(requiredString(value, "id"), requiredString(value, "appId"),
                requiredString(value, "displayName"), requiredBoolean(value, "accountEnabled"));
    }

    /**
     * Parses one stable Graph member identifier.
     *
     * @param value decoded member object
     * @return validated member projection
     */
    private static Member member(final JsonValue.ObjectValue value) {
        return new Member(requiredString(value, "id"));
    }

    /**
     * Parses one minimal service-principal application assignment.
     *
     * @param value decoded assignment object
     * @return validated assignment projection
     */
    private static Assignment assignment(final JsonValue.ObjectValue value) {
        return new Assignment(requiredString(value, "id"), requiredString(value, "principalId"),
                requiredString(value, "resourceId"), requiredString(value, "appRoleId"));
    }

    /**
     * Maps one Microsoft organization projection to a provider-neutral resource.
     *
     * @param value      minimal organization projection
     * @param observedAt shared invocation observation instant
     * @return immutable organization resource
     */
    private static Realm.Resource organizationResource(final Organization value, final Instant observedAt) {
        return resource(Realm.Kind.ORGANIZATION, value.id(), Map.of(), value.name(), Realm.State.UNKNOWN, observedAt);
    }

    /**
     * Maps one Microsoft user projection to a provider-neutral resource.
     *
     * @param value      minimal user projection
     * @param observedAt shared invocation observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User value, final Instant observedAt) {
        final Map<String, String> identifiers = new LinkedHashMap<>();
        value.principalName().ifPresent(name -> identifiers.put("userPrincipalName", name));
        value.mail().ifPresent(mail -> identifiers.put("mail", mail));
        return resource(
                Realm.Kind.USER,
                value.id(),
                identifiers,
                value.name(),
                value.active() ? Realm.State.ACTIVE : Realm.State.INACTIVE,
                observedAt);
    }

    /**
     * Maps one Microsoft group projection to a provider-neutral resource.
     *
     * @param value      minimal group projection
     * @param observedAt shared invocation observation instant
     * @return immutable group resource
     */
    private static Realm.Resource groupResource(final Group value, final Instant observedAt) {
        final Map<String, String> identifiers = new LinkedHashMap<>();
        value.mail().ifPresent(mail -> identifiers.put("mail", mail));
        return resource(Realm.Kind.GROUP, value.id(), identifiers, value.name(), Realm.State.UNKNOWN, observedAt);
    }

    /**
     * Maps one Microsoft directory role projection to a provider-neutral resource.
     *
     * @param value      minimal role projection
     * @param observedAt shared invocation observation instant
     * @return immutable role resource
     */
    private static Realm.Resource roleResource(final Role value, final Instant observedAt) {
        return resource(Realm.Kind.ROLE, value.id(), Map.of(), value.name(), Realm.State.UNKNOWN, observedAt);
    }

    /**
     * Maps one service principal to the provider-neutral service-account resource category.
     *
     * @param value      minimal service-principal projection
     * @param observedAt shared invocation observation instant
     * @return immutable service-account resource
     */
    private static Realm.Resource serviceAccountResource(final ServiceAccount value, final Instant observedAt) {
        return resource(
                Realm.Kind.SERVICE_ACCOUNT,
                value.id(),
                Map.of("appId", value.appId()),
                value.name(),
                Realm.State.UNKNOWN,
                observedAt);
    }

    /**
     * Creates one attribute-free provider-neutral resource.
     *
     * @param kind        resource kind
     * @param id          stable external identifier
     * @param identifiers allow-listed alternate identifiers
     * @param name        display name
     * @param state       normalized state
     * @param observedAt  shared invocation observation instant
     * @return immutable resource
     */
    private static Realm.Resource resource(
            final Realm.Kind kind,
            final String id,
            final Map<String, String> identifiers,
            final String name,
            final Realm.State state,
            final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(kind, id), identifiers, name, state, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one attribute-free directed enterprise relation.
     *
     * @param relationKind relation semantic
     * @param fromKind     source resource kind
     * @param fromId       source stable identifier
     * @param toKind       target resource kind
     * @param toId         target stable identifier
     * @param observedAt   shared invocation observation instant
     * @return immutable relation
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
     * Encodes the exact fixed Microsoft cursor position member set.
     *
     * @param position validated position
     * @return ordered JSON position object
     */
    private static JsonValue.ObjectValue position(final Position position) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
        values.put(Builder.CURSOR_OFFSET_FIELD, number(position.offset()));
        values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
        values.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(position.relationOffset()));
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Decodes the exact fixed Microsoft cursor position member set.
     *
     * @param value decoded JSON position
     * @return validated position
     */
    private static Position position(final JsonValue.ObjectValue value) {
        exactMembers(
                value,
                Set.of(
                        Builder.CURSOR_NEXT_FIELD,
                        Builder.CURSOR_OFFSET_FIELD,
                        Builder.CURSOR_PARENT_ID_FIELD,
                        Builder.CURSOR_RELATION_OFFSET_FIELD),
                "Microsoft cursor position");
        return new Position(nullableString(value, Builder.CURSOR_NEXT_FIELD),
                nonNegativeInt(value, Builder.CURSOR_OFFSET_FIELD),
                nullableString(value, Builder.CURSOR_PARENT_ID_FIELD),
                nonNegativeInt(value, Builder.CURSOR_RELATION_OFFSET_FIELD));
    }

    /**
     * Decodes requested-kind codes without relying on enum ordinals.
     *
     * @param values decoded JSON kind codes
     * @return immutable kinds in stable code order
     */
    private static List<Realm.Kind> kinds(final List<JsonValue> values) {
        final LinkedHashSet<Realm.Kind> result = new LinkedHashSet<>();
        int previous = 0;
        for (JsonValue value : values) {
            if (!(value instanceof JsonValue.NumberValue number)) {
                throw new ValidateException("Microsoft cursor kind must be a number");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("Microsoft cursor kind must be an exact integer", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !result.add(kind)) {
                throw new ValidateException("Microsoft cursor kinds are not in canonical order");
            }
            previous = code;
        }
        if (result.isEmpty()) {
            throw new ValidateException("Microsoft cursor kinds must not be empty");
        }
        return List.copyOf(result);
    }

    /**
     * Resolves one stable enterprise kind code.
     *
     * @param code persisted kind code
     * @return exact enterprise kind
     */
    private static Realm.Kind kind(final int code) {
        for (Realm.Kind kind : Realm.Kind.values()) {
            if (kind.code() == code) {
                return kind;
            }
        }
        throw new ValidateException("Microsoft cursor contains an unknown kind code");
    }

    /**
     * Validates one requested kind set against an operation-specific supported closure.
     *
     * @param kinds     caller-requested kinds
     * @param supported operation-specific supported kinds
     * @param operation safe operation label
     */
    private static void requireKinds(
            final Set<Realm.Kind> kinds,
            final Set<Realm.Kind> supported,
            final String operation) {
        if (!supported.containsAll(kinds)) {
            throw new ValidateException("Microsoft " + operation + " contains an unsupported resource kind");
        }
    }

    /**
     * Reads one optional official Graph nextLink.
     *
     * @param object decoded Graph collection envelope
     * @return optional nextLink
     */
    private static Optional<String> next(final JsonValue.ObjectValue object) {
        return optionalString(object, "@odata.nextLink");
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
     * Reads one optional JSON array as empty when absent or null.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return immutable array values or an empty list
     */
    private static List<JsonValue> optionalArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException(name + " must be a JSON array or null");
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
        if (!text.equals(text.trim())) {
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
            default -> failed(ErrorCode._500, "Microsoft delegated operation returned an unsupported outcome");
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
            default -> failed(ErrorCode._500, "Microsoft internal outcome could not be propagated");
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
        return completed(
                rejected(ErrorCode._400, "Microsoft enterprise capability is not declared by the selected manifest"));
    }

    /**
     * Creates a request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejection
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(
                rejected(
                        ErrorCode._400,
                        "Microsoft enterprise request does not match the selected capability contract"));
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
     * Returns the exact enterprise capability manifest selected at Source compilation.
     *
     * @return immutable describe, snapshot, changes, and retrieve capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes one exact Microsoft enterprise capability and request type.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific enterprise request
     * @param context    immutable non-secret invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return asynchronous typed enterprise outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Microsoft enterprise capability must not be null");
        Assert.notNull(context, "Microsoft enterprise context must not be null");
        Assert.notNull(timeout, "Microsoft enterprise timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
        }
        if (capability.equals(Realm.describe(MicrosoftManifest.ID)) && request instanceof Realm.Describe) {
            return completed(
                    Outcome.succeeded(capability.responseType().cast(MicrosoftManifest.enterpriseDescription())));
        }
        if (capability.equals(Realm.snapshot(MicrosoftManifest.ID)) && request instanceof Realm.Snapshot snapshot) {
            return narrow(snapshot(snapshot, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.changes(MicrosoftManifest.ID)) && request instanceof Realm.Changes changes) {
            return narrow(changes(changes, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.retrieve(MicrosoftManifest.ID)) && request instanceof Realm.Retrieve retrieve) {
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
     * @param request exact provider-neutral snapshot request
     * @param context immutable context used only for credential loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous normalized page outcome
     */
    private CompletionStage<Outcome<Realm.Page>> snapshot(
            final Realm.Snapshot request,
            final Context context,
            final Timeout timeout) {
        final CursorState state;
        try {
            requireKinds(request.kinds(), SUPPORTED_KINDS, "snapshot");
            state = request.cursor().isPresent()
                    ? decode(request.cursor().getOrNull(), Realm.Operation.SNAPSHOT, request.kinds())
                    : CursorState.snapshot(request.kinds());
        } catch (RuntimeException ignored) {
            return completed(rejected(ErrorCode._400, "Microsoft enterprise snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Microsoft enterprise snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return authenticated(context, timeout, access -> snapshot(access, request, state, observedAt, timeout));
    }

    /**
     * Dispatches one validated snapshot state to its finite Graph phase.
     *
     * @param access     valid Graph application access
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
                case ORGANIZATION -> organization(access, state, observedAt, timeout);
                case USERS -> resourcePage(
                        access,
                        request,
                        state,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_USERS,
                        MicrosoftRealmAdapter::user,
                        MicrosoftRealmAdapter::userResource);
                case USER_MANAGERS -> managers(access, request, state, observedAt, timeout);
                case GROUPS -> resourcePage(
                        access,
                        request,
                        state,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_GROUPS,
                        MicrosoftRealmAdapter::group,
                        MicrosoftRealmAdapter::groupResource);
                case GROUP_MEMBERS -> members(access, request, state, observedAt, timeout, Phase.GROUP_MEMBERS);
                case ROLES -> resourcePage(
                        access,
                        request,
                        state,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_ROLES,
                        MicrosoftRealmAdapter::role,
                        MicrosoftRealmAdapter::roleResource);
                case ROLE_MEMBERS -> members(access, request, state, observedAt, timeout, Phase.ROLE_MEMBERS);
                case SERVICE_ACCOUNTS -> resourcePage(
                        access,
                        request,
                        state,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_SERVICE_ACCOUNTS,
                        MicrosoftRealmAdapter::serviceAccount,
                        MicrosoftRealmAdapter::serviceAccountResource);
                case APPLICATION_ASSIGNMENTS -> assignments(access, request, state, observedAt, timeout);
                default -> failed(ErrorCode._500, "Microsoft snapshot cursor selected a change phase");
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "Microsoft Graph returned an invalid snapshot projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Microsoft enterprise snapshot processing failed locally");
        }
    }

    /**
     * Reads the single official organization collection response.
     *
     * @param access     valid Graph application access
     * @param state      organization snapshot state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return organization page and next finite phase cursor
     */
    private Outcome<Realm.Page> organization(
            final Access access,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Url url = initial(Builder.ENTERPRISE_ORGANIZATION, Map.of("$select", "id,displayName"));
        final Outcome<JsonValue.ObjectValue> fetched = get(url, access, timeout, "snapshot", false);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final List<JsonValue> values = requiredArray(success.value(), "value");
        if (values.size() != Normal._1 || next(success.value()).isPresent()) {
            return failed(ErrorCode._502, "Microsoft organization response must contain exactly one value");
        }
        final Organization organization = organization(requiredObject(values.get(0), "organization"));
        return completedPhase(
                List.of(organizationResource(organization, observedAt)),
                List.of(),
                state,
                Phase.ORGANIZATION);
    }

    /**
     * Reads one official Graph resource-list page for a top-level snapshot phase.
     *
     * @param access     valid Graph application access
     * @param request    validated snapshot request
     * @param state      current finite snapshot state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @param targetName manifest management target name
     * @param parser     minimal wire projection parser
     * @param mapper     provider-neutral resource mapper
     * @param <T>        minimal wire projection type
     * @return one bounded resource page
     */
    private <T> Outcome<Realm.Page> resourcePage(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout,
            final String targetName,
            final Function<JsonValue.ObjectValue, T> parser,
            final ResourceMapper<T> mapper) {
        final Url requestUrl = state.position().next().isPresent()
                ? pagination(state.position().next().getOrNull(), state.phase(), Realm.Operation.SNAPSHOT)
                : listUrl(targetName, state.phase(), request.limit());
        final Outcome<WirePage<T>> fetched = wirePage(requestUrl, access, timeout, "snapshot", parser);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<T>> success)) {
            return propagate(fetched);
        }
        final List<Realm.Resource> resources = new ArrayList<>(success.value().items().size());
        final Map<Realm.Key, Realm.Resource> unique = new LinkedHashMap<>();
        for (T item : success.value().items()) {
            final Realm.Resource resource = mapper.map(item, observedAt);
            final Realm.Resource previous = unique.putIfAbsent(resource.key(), resource);
            if (previous != null && !previous.equals(resource)) {
                return failed(ErrorCode._502, "Microsoft Graph page contains a conflicting resource key");
            }
        }
        final List<Realm.Resource> all = List.copyOf(unique.values());
        final int offset = state.position().offset();
        if (offset > all.size()) {
            return failed(ErrorCode._502, "Microsoft resource offset exceeds the replayed Graph page");
        }
        final int end = Math.min(all.size(), offset + request.limit());
        resources.addAll(all.subList(offset, end));
        if (end < all.size()) {
            pagination(requestUrl.toString(), state.phase(), Realm.Operation.SNAPSHOT);
            return page(resources, List.of(), state, Position.resource(requestUrl.toString(), end));
        }
        if (success.value().next().isPresent()) {
            pagination(success.value().next().getOrNull(), state.phase(), Realm.Operation.SNAPSHOT);
            return page(resources, List.of(), state, Position.page(success.value().next().getOrNull()));
        }
        return completedPhase(resources, List.of(), state, state.phase());
    }

    /**
     * Reads manager relations while replaying the official user collection to the next stable parent identifier.
     *
     * @param access     valid Graph application access
     * @param request    validated snapshot request
     * @param state      manager phase cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one manager relation page
     */
    private Outcome<Realm.Page> managers(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<String>> parentOutcome = parent(
                access,
                Phase.USER_MANAGERS,
                state.position().parentId(),
                false,
                timeout);
        if (!(parentOutcome instanceof Outcome.Succeeded<Optional<String>> parentSuccess)) {
            return propagate(parentOutcome);
        }
        if (parentSuccess.value().isEmpty()) {
            return completedPhase(List.of(), List.of(), state, Phase.USER_MANAGERS);
        }
        final String userId = parentSuccess.value().getOrNull();
        final Url url = child(Builder.ENTERPRISE_USER, userId, "manager", Map.of("$select", "id"));
        final Outcome<JsonValue.ObjectValue> fetched = get(url, access, timeout, "snapshot", true);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final List<Realm.Relation> relations;
        if (success.value() == null) {
            relations = List.of();
        } else {
            final String managerId = requiredString(success.value(), "id");
            relations = List.of(
                    relation(
                            Realm.RelationKind.MANAGER,
                            Realm.Kind.USER,
                            userId,
                            Realm.Kind.USER,
                            managerId,
                            observedAt));
        }
        final Outcome<Optional<String>> following = parent(
                access,
                Phase.USER_MANAGERS,
                Optional.of(userId),
                true,
                timeout);
        if (!(following instanceof Outcome.Succeeded<Optional<String>> followingSuccess)) {
            return propagate(following);
        }
        if (followingSuccess.value().isPresent()) {
            return page(List.of(), relations, state, Position.parent(followingSuccess.value().getOrNull(), null));
        }
        return completedPhase(List.of(), relations, state, Phase.USER_MANAGERS);
    }

    /**
     * Reads one group-member or role-member page for the replayed current parent.
     *
     * @param access     valid Graph application access
     * @param request    validated snapshot request
     * @param state      dependent member phase cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @param phase      exact group-member or role-member phase
     * @return one bounded relation page
     */
    private Outcome<Realm.Page> members(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout,
            final Phase phase) {
        final Outcome<Optional<String>> parentOutcome = parent(
                access,
                phase,
                state.position().parentId(),
                false,
                timeout);
        if (!(parentOutcome instanceof Outcome.Succeeded<Optional<String>> parentSuccess)) {
            return propagate(parentOutcome);
        }
        if (parentSuccess.value().isEmpty()) {
            return completedPhase(List.of(), List.of(), state, phase);
        }
        final String parentId = parentSuccess.value().getOrNull();
        final String targetName = phase == Phase.GROUP_MEMBERS ? Builder.ENTERPRISE_GROUP_MEMBERS
                : Builder.ENTERPRISE_ROLE_MEMBERS;
        final String select = "id";
        final Url requestUrl = state.position().next().isPresent()
                ? pagination(state.position().next().getOrNull(), phase, Realm.Operation.SNAPSHOT)
                : child(
                        targetName,
                        parentId,
                        "members",
                        Map.of("$select", select, "$top", Integer.toString(request.limit())));
        final Outcome<WirePage<Member>> fetched = wirePage(
                requestUrl,
                access,
                timeout,
                "snapshot",
                MicrosoftRealmAdapter::member);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Member>> success)) {
            return propagate(fetched);
        }
        final Realm.Kind container = phase == Phase.GROUP_MEMBERS ? Realm.Kind.GROUP : Realm.Kind.ROLE;
        final Realm.RelationKind relationKind = phase == Phase.GROUP_MEMBERS ? Realm.RelationKind.MEMBER
                : Realm.RelationKind.ROLE_MEMBER;
        final List<Realm.Relation> relations = new ArrayList<>();
        final Set<Realm.RelationKey> unique = new LinkedHashSet<>();
        for (Member member : success.value().items()) {
            final Realm.Relation relation = relation(
                    relationKind,
                    Realm.Kind.USER,
                    member.id(),
                    container,
                    parentId,
                    observedAt);
            if (unique.add(relation.key())) {
                relations.add(relation);
            }
        }
        if (relations.size() > request.limit()) {
            return failed(ErrorCode._502, "Microsoft Graph returned more member relations than requested");
        }
        if (success.value().next().isPresent()) {
            pagination(success.value().next().getOrNull(), phase, Realm.Operation.SNAPSHOT);
            return page(List.of(), relations, state, Position.parent(parentId, success.value().next().getOrNull()));
        }
        final Outcome<Optional<String>> following = parent(access, phase, Optional.of(parentId), true, timeout);
        if (!(following instanceof Outcome.Succeeded<Optional<String>> followingSuccess)) {
            return propagate(following);
        }
        if (followingSuccess.value().isPresent()) {
            return page(List.of(), relations, state, Position.parent(followingSuccess.value().getOrNull(), null));
        }
        return completedPhase(List.of(), relations, state, phase);
    }

    /**
     * Reads one application-assignment page for the replayed service principal.
     *
     * @param access     valid Graph application access
     * @param request    validated snapshot request
     * @param state      application-assignment cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded application-assignment relation page
     */
    private Outcome<Realm.Page> assignments(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<String>> parentOutcome = parent(
                access,
                Phase.APPLICATION_ASSIGNMENTS,
                state.position().parentId(),
                false,
                timeout);
        if (!(parentOutcome instanceof Outcome.Succeeded<Optional<String>> parentSuccess)) {
            return propagate(parentOutcome);
        }
        if (parentSuccess.value().isEmpty()) {
            return completedPhase(List.of(), List.of(), state, Phase.APPLICATION_ASSIGNMENTS);
        }
        final String parentId = parentSuccess.value().getOrNull();
        final Url requestUrl = state.position().next().isPresent()
                ? pagination(
                        state.position().next().getOrNull(),
                        Phase.APPLICATION_ASSIGNMENTS,
                        Realm.Operation.SNAPSHOT)
                : child(
                        Builder.ENTERPRISE_ROLE_ASSIGNMENTS,
                        parentId,
                        "appRoleAssignments",
                        Map.of(
                                "$select",
                                "id,principalId,resourceId,appRoleId",
                                "$top",
                                Integer.toString(request.limit())));
        final Outcome<WirePage<Assignment>> fetched = wirePage(
                requestUrl,
                access,
                timeout,
                "snapshot",
                MicrosoftRealmAdapter::assignment);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Assignment>> success)) {
            return propagate(fetched);
        }
        final List<Realm.Relation> relations = new ArrayList<>();
        final Set<Realm.RelationKey> unique = new LinkedHashSet<>();
        for (Assignment assignment : success.value().items()) {
            if (!parentId.equals(assignment.principalId())) {
                return failed(ErrorCode._502, "Microsoft assignment returned a different principal identifier");
            }
            final Realm.Relation relation = relation(
                    Realm.RelationKind.APPLICATION_ASSIGNMENT,
                    Realm.Kind.SERVICE_ACCOUNT,
                    assignment.principalId(),
                    Realm.Kind.SERVICE_ACCOUNT,
                    assignment.resourceId(),
                    observedAt);
            if (unique.add(relation.key())) {
                relations.add(relation);
            }
        }
        if (relations.size() > request.limit()) {
            return failed(ErrorCode._502, "Microsoft Graph returned more assignments than requested");
        }
        if (success.value().next().isPresent()) {
            pagination(success.value().next().getOrNull(), Phase.APPLICATION_ASSIGNMENTS, Realm.Operation.SNAPSHOT);
            return page(List.of(), relations, state, Position.parent(parentId, success.value().next().getOrNull()));
        }
        final Outcome<Optional<String>> following = parent(
                access,
                Phase.APPLICATION_ASSIGNMENTS,
                Optional.of(parentId),
                true,
                timeout);
        if (!(following instanceof Outcome.Succeeded<Optional<String>> followingSuccess)) {
            return propagate(following);
        }
        if (followingSuccess.value().isPresent()) {
            return page(List.of(), relations, state, Position.parent(followingSuccess.value().getOrNull(), null));
        }
        return completedPhase(List.of(), relations, state, Phase.APPLICATION_ASSIGNMENTS);
    }

    /**
     * Validates and starts one Graph delta invocation with a single observation instant.
     *
     * @param request exact provider-neutral change request
     * @param context immutable context used only for credential loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous normalized change page outcome
     */
    private CompletionStage<Outcome<Realm.ChangePage>> changes(
            final Realm.Changes request,
            final Context context,
            final Timeout timeout) {
        final CursorState state;
        try {
            requireKinds(request.kinds(), CHANGE_KINDS, "changes");
            if (request.kinds().size() != Normal._1) {
                throw new ValidateException("Microsoft changes requires exactly one delta resource kind");
            }
            state = request.cursor().isPresent()
                    ? decode(request.cursor().getOrNull(), Realm.Operation.CHANGES, request.kinds())
                    : CursorState.changes(request.kinds());
        } catch (RuntimeException ignored) {
            return completed(rejected(ErrorCode._400, "Microsoft enterprise changes request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Microsoft enterprise changes has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return authenticated(context, timeout, access -> changes(access, request, state, observedAt, timeout));
    }

    /**
     * Reads and maps one user, group, or service-principal delta page.
     *
     * @param access     valid Graph application access
     * @param request    validated single-kind changes request
     * @param state      canonical delta cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded change page with a validated nextLink or deltaLink
     */
    private Outcome<Realm.ChangePage> changes(
            final Access access,
            final Realm.Changes request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        try {
            final Url requestUrl = state.position().next().isPresent()
                    ? pagination(state.position().next().getOrNull(), state.phase(), Realm.Operation.CHANGES)
                    : deltaUrl(state.phase());
            final Outcome<JsonValue.ObjectValue> fetched = get(requestUrl, access, timeout, "changes", false);
            if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
                return propagate(fetched);
            }
            final List<Realm.Change> mapped = new ArrayList<>();
            for (JsonValue value : requiredArray(success.value(), "value")) {
                final JsonValue.ObjectValue item = requiredObject(value, "delta item");
                switch (state.phase()) {
                    case DELTA_USERS -> userChanges(item, observedAt, mapped);
                    case DELTA_GROUPS -> groupChanges(item, observedAt, mapped);
                    case DELTA_SERVICE_ACCOUNTS -> serviceAccountChanges(item, observedAt, mapped);
                    default -> throw new ValidateException("Microsoft delta cursor selected a snapshot phase");
                }
            }
            final int offset = state.position().relationOffset();
            if (offset > mapped.size()) {
                return failed(ErrorCode._502, "Microsoft delta change offset exceeds the replayed page");
            }
            final int end = Math.min(mapped.size(), offset + request.limit());
            final List<Realm.Change> changes = List.copyOf(mapped.subList(offset, end));
            if (end < mapped.size()) {
                return Outcome.succeeded(
                        new Realm.ChangePage(changes,
                                Optional.of(
                                        encode(
                                                new CursorState(Realm.Operation.CHANGES, state.phase(), state.kinds(),
                                                        Position.changes(requestUrl.toString(), end))))));
            }
            final Optional<String> next = next(success.value());
            final Optional<String> delta = optionalString(success.value(), "@odata.deltaLink");
            if (next.isPresent() == delta.isPresent()) {
                return failed(
                        ErrorCode._502,
                        "Microsoft delta response must contain exactly one nextLink or deltaLink");
            }
            final String continuation = next.isPresent() ? next.getOrNull() : delta.getOrNull();
            pagination(continuation, state.phase(), Realm.Operation.CHANGES);
            final Realm.Cursor cursor = encode(
                    new CursorState(Realm.Operation.CHANGES, state.phase(), state.kinds(),
                            Position.changes(continuation, 0)));
            return Outcome.succeeded(new Realm.ChangePage(changes, Optional.of(cursor)));
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "Microsoft Graph returned an invalid delta projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Microsoft enterprise delta processing failed locally");
        }
    }

    /**
     * Validates and starts one direct resource retrieval.
     *
     * @param request exact provider-neutral retrieval request
     * @param context immutable context used only for credential loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous retrieval outcome
     */
    private CompletionStage<Outcome<Realm.Retrieved>> retrieve(
            final Realm.Retrieve request,
            final Context context,
            final Timeout timeout) {
        if (!SUPPORTED_KINDS.contains(request.key().kind())) {
            return completed(rejected(ErrorCode._400, "Microsoft enterprise retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Microsoft enterprise retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return authenticated(context, timeout, access -> retrieve(access, request.key(), observedAt, timeout));
    }

    /**
     * Dispatches one direct retrieval to its fixed Graph path or organization scan.
     *
     * @param access     valid Graph application access
     * @param key        requested stable resource key
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved resource, explicit absence, or safely classified failure
     */
    private Outcome<Realm.Retrieved> retrieve(
            final Access access,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        try {
            return switch (key.kind()) {
                case USER -> retrieve(
                        access,
                        key,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_USER,
                        MicrosoftRealmAdapter::user,
                        MicrosoftRealmAdapter::userResource);
                case ORGANIZATION -> retrieveOrganization(access, key.externalId(), observedAt, timeout);
                case GROUP -> retrieve(
                        access,
                        key,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_GROUP,
                        MicrosoftRealmAdapter::group,
                        MicrosoftRealmAdapter::groupResource);
                case ROLE -> retrieve(
                        access,
                        key,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_ROLES,
                        MicrosoftRealmAdapter::role,
                        MicrosoftRealmAdapter::roleResource);
                case SERVICE_ACCOUNT -> retrieve(
                        access,
                        key,
                        observedAt,
                        timeout,
                        Builder.ENTERPRISE_SERVICE_ACCOUNTS,
                        MicrosoftRealmAdapter::serviceAccount,
                        MicrosoftRealmAdapter::serviceAccountResource);
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "Microsoft Graph returned an invalid retrieval projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Microsoft enterprise retrieval processing failed locally");
        }
    }

    /**
     * Retrieves one resource through its stable-ID Graph endpoint.
     *
     * @param access     valid Graph application access
     * @param key        requested provider-neutral key
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @param targetName manifest management target name
     * @param parser     minimal wire projection parser
     * @param mapper     provider-neutral resource mapper
     * @param <T>        minimal wire projection type
     * @return retrieved resource or explicit absence
     */
    private <T> Outcome<Realm.Retrieved> retrieve(
            final Access access,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout,
            final String targetName,
            final Function<JsonValue.ObjectValue, T> parser,
            final ResourceMapper<T> mapper) {
        final Url url = child(targetName, key.externalId(), null, Map.of("$select", select(key.kind())));
        final Outcome<JsonValue.ObjectValue> fetched = get(url, access, timeout, "retrieve", true);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (success.value() == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Realm.Resource resource = mapper.map(parser.apply(success.value()), observedAt);
        if (!key.equals(resource.key())) {
            return failed(ErrorCode._502, "Microsoft retrieve returned a different stable resource key");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(resource)));
    }

    /**
     * Scans the single organization collection for one stable identifier.
     *
     * @param access         valid Graph application access
     * @param organizationId requested organization identifier
     * @param observedAt     shared invocation observation instant
     * @param timeout        shared end-to-end timeout
     * @return retrieved organization or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveOrganization(
            final Access access,
            final String organizationId,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = get(
                initial(Builder.ENTERPRISE_ORGANIZATION, Map.of("$select", "id,displayName")),
                access,
                timeout,
                "retrieve",
                false);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        for (JsonValue value : requiredArray(success.value(), "value")) {
            final Organization organization = organization(requiredObject(value, "organization"));
            if (organizationId.equals(organization.id())) {
                return Outcome
                        .succeeded(new Realm.Retrieved(Optional.of(organizationResource(organization, observedAt))));
            }
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
    }

    /**
     * Acquires a cached or fresh Graph token and retries one 401 result exactly once.
     *
     * @param context   immutable context used by the project Secret Loader
     * @param timeout   shared end-to-end timeout
     * @param operation authenticated synchronous Graph operation
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
                                                : MicrosoftRealmAdapter.<Boolean>failed(
                                                        ErrorCode._500,
                                                        "Microsoft upstream-token cache deletion failed"))
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
                                                        "Microsoft token refresh returned an unsupported outcome"));
                                    });
                                });
                    });
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
            default -> completed(failed(ErrorCode._500, "Microsoft access resolution returned an unsupported outcome"));
        });
    }

    /**
     * Schedules one synchronous Graph operation on the caller-owned executor.
     *
     * @param access    valid Graph application access
     * @param timeout   shared end-to-end timeout
     * @param operation synchronous operation
     * @param <T>       successful operation type
     * @return asynchronous operation outcome
     */
    private <T> CompletionStage<Outcome<T>> execute(
            final Access access,
            final Timeout timeout,
            final Function<Access, Outcome<T>> operation) {
        try {
            return CompletableFuture.<Outcome<T>>supplyAsync(() -> {
                try {
                    return operation.apply(access);
                } catch (TimeoutException ignored) {
                    return failed(ErrorCode._408, "Microsoft enterprise operation timed out");
                } catch (RuntimeException ignored) {
                    return failed(ErrorCode._503, "Microsoft Graph transport is unavailable");
                }
            }, services.executor());
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "Microsoft enterprise operation could not be scheduled"));
        }
    }

    /**
     * Reads the Source-private token cache before exchanging a new token.
     *
     * @param context immutable context used by the project Secret Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous cached or fresh access outcome
     */
    private CompletionStage<Outcome<Access>> access(final Context context, final Timeout timeout) {
        return accessCache.get(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY)
                .<CompletionStage<Outcome<Access>>>handle((cached, cause) -> {
                    if (cause != null) {
                        return completed(failed(ErrorCode._500, "Microsoft upstream-token cache lookup failed"));
                    }
                    return cached == null ? exchange(context, timeout) : completed(Outcome.succeeded(cached));
                }).thenCompose(Function.identity());
    }

    /**
     * Loads the external Client Secret before token exchange.
     *
     * @param context immutable context supplied to the project Secret Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous access-token outcome
     */
    private CompletionStage<Outcome<Access>> exchange(final Context context, final Timeout timeout) {
        final CompletionStage<Outcome<SecretLoader.Record>> loaded;
        try {
            loaded = services.secretLoader()
                    .load(new SecretLoader.Request(services.registration(), options.credential()), context, timeout);
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "Microsoft Secret Loader failed before returning a stage"));
        }
        if (loaded == null) {
            return completed(failed(ErrorCode._500, "Microsoft Secret Loader returned no stage"));
        }
        return loaded.<Outcome<SecretLoader.Record>>handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : failed(ErrorCode._500, "Microsoft Secret Loader stage failed"))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLoader.Record> success -> exchange(success.value(), timeout);
                    case Outcome.Rejected<SecretLoader.Record> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLoader.Record> failed -> completed(Outcome.failed(failed.failure()));
                    default -> completed(
                            failed(ErrorCode._500, "Microsoft Secret Loader returned an unsupported outcome"));
                });
    }

    /**
     * Parses one loaded Secret and binds its lease to one asynchronous terminal close point.
     *
     * @param loaded  project-loaded Secret record
     * @param timeout shared end-to-end timeout
     * @return asynchronous access-token outcome with deterministic Secret erasure
     */
    private CompletionStage<Outcome<Access>> exchange(final SecretLoader.Record loaded, final Timeout timeout) {
        final SecretLease raw = loaded == null ? null : loaded.lease();
        final SecretLease secret;
        try {
            secret = services.secretParser().parse(services.registration(), options.credential(), loaded);
        } catch (RuntimeException ignored) {
            if (raw != null) {
                raw.close();
            }
            return completed(failed(ErrorCode._500, "Microsoft loaded Secret could not be validated"));
        }
        try {
            return CompletableFuture.<Outcome<Access>>supplyAsync(() -> token(secret, timeout), services.executor())
                    .handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : MicrosoftRealmAdapter
                                            .<Access>failed(ErrorCode._503, "Microsoft token exchange task failed"))
                    .whenComplete((ignored, cause) -> secret.close()).thenCompose(this::cache);
        } catch (RuntimeException ignored) {
            secret.close();
            return completed(failed(ErrorCode._500, "Microsoft token exchange task could not be scheduled"));
        }
    }

    /**
     * Stores one cacheable Graph token with the mandatory early-expiration skew.
     *
     * @param outcome freshly exchanged access outcome
     * @return original access after cache creation or a safe cache failure
     */
    private CompletionStage<Outcome<Access>> cache(final Outcome<Access> outcome) {
        if (!(outcome instanceof Outcome.Succeeded<Access> success) || success.value().expiresAtMillis() == 0L) {
            return completed(outcome);
        }
        final long now = FabricX.clock(services.fabric()).millis();
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
                        : failed(ErrorCode._500, "Microsoft upstream-token cache creation failed"));
    }

    /**
     * Exchanges Client ID and leased Client Secret through the tenant-scoped OAuth token endpoint.
     *
     * @param secret  still-open Client Secret lease
     * @param timeout shared end-to-end timeout
     * @return Graph access or safely classified failure
     */
    private Outcome<Access> token(final SecretLease secret, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Microsoft token exchange has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(services.fabric(), Protocol.HTTPS, timeout)
                    .url(targets.token().getOrNull().url().toString()).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .form(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .form(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material()))
                    .form(OAuth2.Parameters.GRANT_TYPE, GrantType.CLIENT_CREDENTIALS.value())
                    .form(OAuth2.Parameters.SCOPE, options.scopes().get(0)).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Microsoft token exchange timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Microsoft token endpoint is unavailable");
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
                return failed(ErrorCode._502, "Microsoft token endpoint returned an unsupported token type");
            }
            final long now = FabricX.clock(services.fabric()).millis();
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
            return failed(ErrorCode._502, "Microsoft token endpoint returned an invalid response");
        }
    }

    /**
     * Maps one non-successful OAuth token response.
     *
     * @param response owned non-successful token response
     * @return credential rejection, rate limit, or upstream failure
     */
    private Outcome<Access> tokenHttpFailure(final Response response) {
        final int status = response.code();
        final Map<String, JsonValue> details = details("token", status, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.UNAUTHORIZED
                || status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._401, "Microsoft rejected the configured application credentials", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Microsoft token endpoint is rate limited", details);
        }
        return failed(ErrorCode._502, "Microsoft token endpoint returned an upstream error", details);
    }

    /**
     * Executes one bounded Graph GET and decodes a successful JSON object.
     *
     * @param url           validated cloud-specific Graph URL
     * @param access        valid Graph application access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @return decoded object, {@code null} for absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> get(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Microsoft Graph request has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(services.fabric(), Protocol.HTTPS, timeout).url(url.toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(Http.Header.AUTHORIZATION, "Bearer " + access.token()).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Microsoft Graph request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Microsoft Graph endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return graphHttpFailure(response, operation, allowNotFound);
            }
            return Outcome.succeeded(object(response));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Microsoft Graph returned an invalid response");
        }
    }

    /**
     * Maps one non-successful Graph response without retaining its body or headers.
     *
     * @param response      owned non-successful Graph response
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @param <T>           expected success type
     * @return classified rejection, absence, or failure
     */
    private <T> Outcome<T> graphHttpFailure(
            final Response response,
            final String operation,
            final boolean allowNotFound) {
        final int status = response.code();
        final Map<String, JsonValue> details = details(operation, status, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "Microsoft Graph rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "Microsoft Graph rejected the access token", details);
        }
        if (status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._403, "Microsoft Graph permission is insufficient", details);
        }
        if (status == Http.Status.NOT_FOUND && allowNotFound) {
            return Outcome.succeeded(null);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Microsoft Graph is rate limited", details);
        }
        if (status == Http.Status.GONE && "changes".equals(operation)) {
            return failed(ErrorCode._502, "Microsoft delta token expired and requires a new baseline", details);
        }
        return failed(ErrorCode._502, "Microsoft Graph returned an upstream error", details);
    }

    /**
     * Decodes one official Graph collection page with duplicate-key validation deferred to its mapper.
     *
     * @param url       validated current Graph request URL
     * @param access    valid Graph application access
     * @param timeout   shared end-to-end timeout
     * @param operation safe enterprise operation label
     * @param parser    minimal item projection parser
     * @param <T>       minimal item type
     * @return decoded bounded page and validated nextLink text
     */
    private <T> Outcome<WirePage<T>> wirePage(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final Function<JsonValue.ObjectValue, T> parser) {
        final Outcome<JsonValue.ObjectValue> fetched = get(url, access, timeout, operation, false);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final List<T> items = new ArrayList<>();
        for (JsonValue value : requiredArray(success.value(), "value")) {
            items.add(parser.apply(requiredObject(value, "Graph collection item")));
        }
        return Outcome.succeeded(new WirePage<>(items, next(success.value())));
    }

    /**
     * Replays a parent collection from its fixed initial URL and selects the first or following stable identifier.
     *
     * @param access    valid Graph application access
     * @param phase     dependent snapshot phase
     * @param current   empty for the first parent or the current parent identifier
     * @param following whether to return the parent following the supplied identifier
     * @param timeout   shared end-to-end timeout
     * @return selected parent identifier or natural exhaustion
     */
    private Outcome<Optional<String>> parent(
            final Access access,
            final Phase phase,
            final Optional<String> current,
            final boolean following,
            final Timeout timeout) {
        final String targetName = switch (phase) {
            case USER_MANAGERS -> Builder.ENTERPRISE_USERS;
            case GROUP_MEMBERS -> Builder.ENTERPRISE_GROUPS;
            case ROLE_MEMBERS -> Builder.ENTERPRISE_ROLES;
            case APPLICATION_ASSIGNMENTS -> Builder.ENTERPRISE_SERVICE_ACCOUNTS;
            default -> throw new ValidateException("Microsoft phase has no parent collection");
        };
        Url url = listUrl(targetName, phase.parentPhase(), Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE);
        boolean found = current.isEmpty();
        while (true) {
            final Outcome<WirePage<Member>> fetched = wirePage(
                    url,
                    access,
                    timeout,
                    "snapshot",
                    MicrosoftRealmAdapter::member);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Member>> success)) {
                return propagate(fetched);
            }
            for (Member member : success.value().items()) {
                if (found) {
                    return Outcome.succeeded(Optional.of(member.id()));
                }
                if (current.getOrNull().equals(member.id())) {
                    if (!following) {
                        return Outcome.succeeded(Optional.of(member.id()));
                    }
                    found = true;
                }
            }
            if (success.value().next().isEmpty()) {
                if (!found) {
                    return failed(ErrorCode._502, "Microsoft parent resource no longer exists during replay");
                }
                return Outcome.succeeded(Optional.empty());
            }
            url = pagination(success.value().next().getOrNull(), phase.parentPhase(), Realm.Operation.SNAPSHOT);
        }
    }

    /**
     * Creates one initial top-level Graph list URL with its frozen select and requested top value.
     *
     * @param targetName manifest management target name
     * @param phase      top-level snapshot phase
     * @param limit      requested bounded page size
     * @return validated initial list URL
     */
    private Url listUrl(final String targetName, final Phase phase, final int limit) {
        final Map<String, String> query = new LinkedHashMap<>();
        query.put("$select", select(phase.kind()));
        if (phase != Phase.ROLES) {
            query.put("$top", Integer.toString(limit));
        }
        return initial(targetName, query);
    }

    /**
     * Creates one initial Graph delta URL for the selected single-kind changes phase.
     *
     * @param phase exact delta phase
     * @return validated fixed Graph delta URL
     */
    private Url deltaUrl(final Phase phase) {
        final String path = switch (phase) {
            case DELTA_USERS -> "/v1.0/users/delta";
            case DELTA_GROUPS -> "/v1.0/groups/delta";
            case DELTA_SERVICE_ACCOUNTS -> "/v1.0/servicePrincipals/delta";
            default -> throw new ValidateException("Microsoft changes selected a non-delta phase");
        };
        final String projection = phase == Phase.DELTA_GROUPS ? select(phase.kind()) + ",members"
                : select(phase.kind());
        return build(target(Builder.ENTERPRISE_CHANGES).url(), path, Map.of("$select", projection));
    }

    /**
     * Creates a query-bearing URL from one fixed manifest management endpoint.
     *
     * @param targetName manifest management target name
     * @param query      exact fixed query values
     * @return immutable Graph URL
     */
    private Url initial(final String targetName, final Map<String, String> query) {
        final Url base = target(targetName).url();
        return build(base, base.path(), query);
    }

    /**
     * Creates one stable-ID child Graph URL without concatenating an untrusted path segment.
     *
     * @param targetName manifest management target name
     * @param identifier stable parent or resource identifier
     * @param suffix     fixed child suffix, or {@code null} for the resource itself
     * @param query      exact fixed query values
     * @return immutable encoded Graph URL
     */
    private Url child(
            final String targetName,
            final String identifier,
            final String suffix,
            final Map<String, String> query) {
        final Url base = target(targetName).url();
        final StringBuilder path = new StringBuilder(base.path()).append('/')
                .append(RFC3986.SEGMENT.encode(requireText(identifier, "Microsoft Graph identifier"), Charset.UTF_8));
        if (suffix != null) {
            path.append('/').append(suffix);
        }
        return build(base, path.toString(), query);
    }

    /**
     * Parses and validates an official nextLink or deltaLink before it can be requested or persisted.
     *
     * @param value     official absolute pagination URL
     * @param phase     exact finite phase that owns the URL
     * @param operation snapshot or changes operation
     * @return validated normalized Graph URL
     */
    private Url pagination(final String value, final Phase phase, final Realm.Operation operation) {
        final Url url;
        try {
            url = Url.parse(requireText(value, "Microsoft Graph pagination URL"));
        } catch (RuntimeException cause) {
            throw new ValidateException("Microsoft Graph pagination URL is invalid", cause);
        }
        if (!Protocol.HTTPS.name.equals(url.scheme()) || !graphHost.equals(url.host())
                || url.port() != Url.defaultPort(Protocol.HTTPS.name) || !url.username().isEmpty()
                || !url.password().isEmpty() || url.fragment() != null || !allowedPath(url.path(), phase, operation)
                || !GRAPH_QUERY_NAMES.containsAll(url.query().keySet()) || !validQuery(url, phase, operation)) {
            throw new ValidateException("Microsoft Graph pagination URL crosses its fixed trust boundary");
        }
        if (url.query().values().stream().anyMatch(values -> values.size() != Normal._1)) {
            throw new ValidateException("Microsoft Graph pagination query names must be unique");
        }
        return url;
    }

    /**
     * Resolves one required manifest-owned management target.
     *
     * @param name fixed root Builder management key
     * @return exact resolved HTTPS endpoint
     */
    private Endpoint target(final String name) {
        final Endpoint endpoint = targets.management().get(name);
        if (endpoint == null) {
            throw new ValidateException("Microsoft enterprise manifest omits a required management target");
        }
        return endpoint;
    }

    /**
     * Creates one snapshot page with a continuation in the current phase.
     *
     * @param resources normalized resources
     * @param relations normalized relations
     * @param state     current snapshot state
     * @param position  recoverable continuation position
     * @return successful snapshot page
     */
    private Outcome<Realm.Page> page(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Position position) {
        final Realm.Cursor cursor = encode(
                new CursorState(Realm.Operation.SNAPSHOT, state.phase(), state.kinds(), position));
        return Outcome.succeeded(new Realm.Page(resources, relations, Optional.of(cursor)));
    }

    /**
     * Completes one snapshot phase and selects the next phase enabled by the requested kinds.
     *
     * @param resources resources produced by the completed phase
     * @param relations relations produced by the completed phase
     * @param state     current snapshot state
     * @param phase     completed finite phase
     * @return page with the next phase cursor or natural completion
     */
    private Outcome<Realm.Page> completedPhase(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Phase phase) {
        final Phase next = nextPhase(phase, state.kinds());
        final Optional<Realm.Cursor> cursor = next == null ? Optional.empty()
                : Optional
                        .of(encode(new CursorState(Realm.Operation.SNAPSHOT, next, state.kinds(), Position.initial())));
        return Outcome.succeeded(new Realm.Page(resources, relations, cursor));
    }

    /**
     * Encodes one canonical six-field Microsoft enterprise cursor.
     *
     * @param state validated cursor state
     * @return opaque unpadded Base64 URL-safe cursor
     */
    private Realm.Cursor encode(final CursorState state) {
        final Map<String, JsonValue> envelope = new LinkedHashMap<>();
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(MicrosoftManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(variant.variant().value()));
        envelope.put(Builder.OPERATION_FIELD, number(state.operation().code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code()));
        final List<JsonValue> kinds = new ArrayList<>(state.kinds().size());
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        envelope.put(Builder.CURSOR_POSITION_FIELD, position(state.position()));
        return new Realm.Cursor(
                Base64.encodeUrlSafe(services.jsonProvider().writeValue(new JsonValue.ObjectValue(envelope))));
    }

    /**
     * Decodes and canonicalizes one Microsoft enterprise cursor.
     *
     * @param cursor    opaque caller-supplied cursor
     * @param operation expected enterprise operation
     * @param kinds     exact kinds requested by the current call
     * @return validated canonical cursor state
     */
    private CursorState decode(
            final Realm.Cursor cursor,
            final Realm.Operation operation,
            final Set<Realm.Kind> kinds) {
        try {
            final JsonValue value = services.jsonProvider()
                    .readValue(Base64.decode(cursor.value()), Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH, true);
            final JsonValue.ObjectValue envelope = requiredObject(value, "Microsoft cursor envelope");
            exactMembers(
                    envelope,
                    Set.of(
                            Builder.VENDOR_FIELD,
                            Builder.VARIANT_FIELD,
                            Builder.OPERATION_FIELD,
                            Builder.CURSOR_PHASE_FIELD,
                            Builder.CURSOR_KIND_FIELD,
                            Builder.CURSOR_POSITION_FIELD),
                    "Microsoft cursor envelope");
            if (!MicrosoftManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !variant.variant().value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredLong(envelope, Builder.OPERATION_FIELD) != operation.code()) {
                throw new ValidateException("Microsoft cursor does not belong to this operation or cloud");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("Microsoft cursor kinds do not match the current request");
            }
            final Position position = position(requiredObject(envelope, Builder.CURSOR_POSITION_FIELD));
            final CursorState state = new CursorState(operation, phase, decodedKinds, position);
            state.validate();
            if (position.next().isPresent()) {
                pagination(position.next().getOrNull(), phase, operation);
            }
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("Microsoft cursor is not in canonical form");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("Microsoft cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded response object with duplicate-member rejection.
     *
     * @param response owned successful HTTP response
     * @return decoded top-level JSON object
     */
    private JsonValue.ObjectValue object(final Response response) {
        return requiredObject(
                services.jsonProvider().readValue(
                        response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES),
                        Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH,
                        true),
                "Microsoft response");
    }

    /**
     * Builds safe allow-listed outcome details for one upstream response.
     *
     * @param operation safe enterprise operation label
     * @param status    upstream HTTP status
     * @param headers   headers used only for Retry-After normalization
     * @return immutable allow-listed scalar details
     */
    private Map<String, JsonValue> details(final String operation, final int status, final FabricX.Headers headers) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(MicrosoftManifest.ID.value()));
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
     * Defines the complete finite Microsoft snapshot and delta phase codes.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Reads the single tenant organization.
         */
        ORGANIZATION(1, Realm.Operation.SNAPSHOT, Realm.Kind.ORGANIZATION),

        /**
         * Reads Graph users.
         */
        USERS(2, Realm.Operation.SNAPSHOT, Realm.Kind.USER),

        /**
         * Replays users and reads each manager.
         */
        USER_MANAGERS(3, Realm.Operation.SNAPSHOT, Realm.Kind.USER),

        /**
         * Reads Graph groups.
         */
        GROUPS(4, Realm.Operation.SNAPSHOT, Realm.Kind.GROUP),

        /**
         * Replays groups and reads their members.
         */
        GROUP_MEMBERS(5, Realm.Operation.SNAPSHOT, Realm.Kind.GROUP),

        /**
         * Reads active directory roles.
         */
        ROLES(6, Realm.Operation.SNAPSHOT, Realm.Kind.ROLE),

        /**
         * Replays directory roles and reads their members.
         */
        ROLE_MEMBERS(7, Realm.Operation.SNAPSHOT, Realm.Kind.ROLE),

        /**
         * Reads service principals as service accounts.
         */
        SERVICE_ACCOUNTS(8, Realm.Operation.SNAPSHOT, Realm.Kind.SERVICE_ACCOUNT),

        /**
         * Replays service principals and reads application assignments.
         */
        APPLICATION_ASSIGNMENTS(9, Realm.Operation.SNAPSHOT, Realm.Kind.SERVICE_ACCOUNT),

        /**
         * Reads the user delta feed.
         */
        DELTA_USERS(10, Realm.Operation.CHANGES, Realm.Kind.USER),

        /**
         * Reads the group and group-member delta feed.
         */
        DELTA_GROUPS(11, Realm.Operation.CHANGES, Realm.Kind.GROUP),

        /**
         * Reads the service-principal delta feed.
         */
        DELTA_SERVICE_ACCOUNTS(12, Realm.Operation.CHANGES, Realm.Kind.SERVICE_ACCOUNT);

        /**
         * Stable persisted phase code.
         */
        private final int code;

        /**
         * Enterprise operation owning this phase.
         */
        private final Realm.Operation operation;

        /**
         * Resource kind owning this phase.
         */
        private final Realm.Kind kind;

        /**
         * Creates one stable Microsoft phase.
         *
         * @param code      stable persisted code
         * @param operation owning operation
         * @param kind      owning resource kind
         */
        Phase(final int code, final Realm.Operation operation, final Realm.Kind kind) {
            this.code = code;
            this.operation = operation;
            this.kind = kind;
        }

        /**
         * Resolves one stable phase code.
         *
         * @param code persisted phase code
         * @return exact phase
         */
        private static Phase from(final int code) {
            for (Phase phase : values()) {
                if (phase.code == code) {
                    return phase;
                }
            }
            throw new ValidateException("Microsoft cursor contains an unknown phase code");
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
         * Returns the resource kind owned by this phase.
         *
         * @return resource kind
         */
        private Realm.Kind kind() {
            return kind;
        }

        /**
         * Returns the parent-list phase for one dependent phase.
         *
         * @return top-level parent-list phase
         */
        private Phase parentPhase() {
            return switch (this) {
                case USER_MANAGERS -> USERS;
                case GROUP_MEMBERS -> GROUPS;
                case ROLE_MEMBERS -> ROLES;
                case APPLICATION_ASSIGNMENTS -> SERVICE_ACCOUNTS;
                default -> throw new ValidateException("Microsoft phase has no parent phase");
            };
        }

        /**
         * Returns the next declared snapshot phase.
         *
         * @return following snapshot phase or {@code null}
         */
        private Phase nextSnapshot() {
            return switch (this) {
                case ORGANIZATION -> USERS;
                case USERS -> USER_MANAGERS;
                case USER_MANAGERS -> GROUPS;
                case GROUPS -> GROUP_MEMBERS;
                case GROUP_MEMBERS -> ROLES;
                case ROLES -> ROLE_MEMBERS;
                case ROLE_MEMBERS -> SERVICE_ACCOUNTS;
                case SERVICE_ACCOUNTS -> APPLICATION_ASSIGNMENTS;
                case APPLICATION_ASSIGNMENTS -> null;
                default -> throw new ValidateException("Microsoft delta phase has no snapshot successor");
            };
        }
    }

    /**
     * Maps one minimal wire projection to a provider-neutral resource.
     *
     * @param <T> minimal wire projection type
     * @author Kimi Liu
     */
    @FunctionalInterface
    private interface ResourceMapper<T> {

        /**
         * Maps one minimal projection at the shared observation instant.
         *
         * @param value      minimal projection
         * @param observedAt shared invocation observation instant
         * @return immutable provider-neutral resource
         */
        Realm.Resource map(T value, Instant observedAt);
    }

    /**
     * Carries one canonical Microsoft snapshot or changes cursor state.
     *
     * @param operation exact owning operation
     * @param phase     exact finite phase
     * @param kinds     requested kinds in stable code order
     * @param position  recoverable pagination position
     * @author Kimi Liu
     */
    private record CursorState(Realm.Operation operation, Phase phase, List<Realm.Kind> kinds, Position position) {

        /**
         * Validates and freezes one canonical cursor state.
         *
         * @param operation exact owning operation
         * @param phase     exact finite phase
         * @param kinds     requested kinds
         * @param position  recoverable position
         */
        private CursorState {
            operation = Assert.notNull(operation, "Microsoft cursor operation must not be null");
            phase = Assert.notNull(phase, "Microsoft cursor phase must not be null");
            Assert.notNull(kinds, "Microsoft cursor kinds must not be null");
            final List<Realm.Kind> copy = new ArrayList<>(kinds.size());
            int previous = 0;
            for (Realm.Kind kind : kinds) {
                final Realm.Kind checked = Assert.notNull(kind, "Microsoft cursor kind must not be null");
                if (checked.code() <= previous) {
                    throw new ValidateException("Microsoft cursor kinds must use stable code order");
                }
                previous = checked.code();
                copy.add(checked);
            }
            if (copy.isEmpty()) {
                throw new ValidateException("Microsoft cursor kinds must not be empty");
            }
            kinds = List.copyOf(copy);
            position = Assert.notNull(position, "Microsoft cursor position must not be null");
            validate(operation, phase, kinds, position);
        }

        /**
         * Creates the first enabled snapshot phase.
         *
         * @param kinds requested snapshot kinds
         * @return canonical initial snapshot state
         */
        private static CursorState snapshot(final Set<Realm.Kind> kinds) {
            final List<Realm.Kind> ordered = List.copyOf(kinds);
            Phase phase = Phase.ORGANIZATION;
            while (!ordered.contains(phase.kind())) {
                phase = phase.nextSnapshot();
            }
            return new CursorState(Realm.Operation.SNAPSHOT, phase, ordered, Position.initial());
        }

        /**
         * Creates the initial single-kind delta phase.
         *
         * @param kinds exact one-kind change set
         * @return canonical initial changes state
         */
        private static CursorState changes(final Set<Realm.Kind> kinds) {
            final List<Realm.Kind> ordered = List.copyOf(kinds);
            final Phase phase = switch (ordered.get(0)) {
                case USER -> Phase.DELTA_USERS;
                case GROUP -> Phase.DELTA_GROUPS;
                case SERVICE_ACCOUNT -> Phase.DELTA_SERVICE_ACCOUNTS;
                default -> throw new ValidateException("Microsoft changes kind has no delta phase");
            };
            return new CursorState(Realm.Operation.CHANGES, phase, ordered, Position.initial());
        }

        /**
         * Validates operation, phase, kind, and position ownership.
         *
         * @param operation owning operation
         * @param phase     finite phase
         * @param kinds     requested kinds
         * @param position  pagination position
         */
        private static void validate(
                final Realm.Operation operation,
                final Phase phase,
                final List<Realm.Kind> kinds,
                final Position position) {
            if (phase.operation != operation || !kinds.contains(phase.kind())) {
                throw new ValidateException("Microsoft cursor phase is not enabled by its operation and kinds");
            }
            if (operation == Realm.Operation.CHANGES && kinds.size() != Normal._1) {
                throw new ValidateException("Microsoft delta cursor requires exactly one resource kind");
            }
            final boolean dependent = phase == Phase.USER_MANAGERS || phase == Phase.GROUP_MEMBERS
                    || phase == Phase.ROLE_MEMBERS || phase == Phase.APPLICATION_ASSIGNMENTS;
            if (!dependent && position.parentId().isPresent()) {
                throw new ValidateException("Microsoft top-level cursor must not contain a parent identifier");
            }
            if (operation == Realm.Operation.SNAPSHOT && position.relationOffset() != 0) {
                throw new ValidateException("Microsoft snapshot cursor must not contain a change offset");
            }
            if ((dependent || operation == Realm.Operation.CHANGES) && position.offset() != 0) {
                throw new ValidateException("Microsoft dependent or delta cursor must not contain a resource offset");
            }
            if (dependent && position.next().isPresent() && position.parentId().isEmpty()) {
                throw new ValidateException("Microsoft child nextLink requires its stable parent identifier");
            }
        }

        /**
         * Revalidates the already normalized state.
         */
        private void validate() {
            validate(operation, phase, kinds, position);
        }
    }

    /**
     * Carries one fixed Microsoft pagination position.
     *
     * @param next           validated official request, next, or delta URL
     * @param offset         next resource index within a replayed top-level Graph page
     * @param parentId       current dependent-phase parent identifier
     * @param relationOffset next flattened change index within a replayed delta page
     * @author Kimi Liu
     */
    private record Position(Optional<String> next, int offset, Optional<String> parentId, int relationOffset) {

        /**
         * Validates and normalizes one fixed position.
         *
         * @param next           optional pagination URL
         * @param offset         non-negative replayed resource offset
         * @param parentId       optional stable parent identifier
         * @param relationOffset non-negative flattened change offset
         */
        private Position {
            next = optional(next, "Microsoft cursor next URL");
            parentId = optional(parentId, "Microsoft cursor parent identifier");
            if (offset < 0 || relationOffset < 0) {
                throw new ValidateException("Microsoft cursor offsets must not be negative");
            }
        }

        /**
         * Creates an empty initial position.
         *
         * @return canonical empty position
         */
        private static Position initial() {
            return new Position(Optional.empty(), 0, Optional.empty(), 0);
        }

        /**
         * Creates one top-level official page continuation.
         *
         * @param next official nextLink
         * @return canonical page position
         */
        private static Position page(final String next) {
            return new Position(Optional.of(next), 0, Optional.empty(), 0);
        }

        /**
         * Creates one replayable top-level page position with a resource offset.
         *
         * @param request official current page request URL
         * @param offset  next resource index
         * @return canonical resource replay position
         */
        private static Position resource(final String request, final int offset) {
            return new Position(Optional.of(request), offset, Optional.empty(), 0);
        }

        /**
         * Creates one dependent parent and child-page continuation.
         *
         * @param parentId stable current or next parent identifier
         * @param next     official child nextLink, or {@code null}
         * @return canonical dependent position
         */
        private static Position parent(final String parentId, final String next) {
            return new Position(Optional.ofNullable(next), 0, Optional.of(parentId), 0);
        }

        /**
         * Creates one delta request URL and flattened change offset continuation.
         *
         * @param next   official current, next, or delta URL
         * @param offset next flattened change index
         * @return canonical changes position
         */
        private static Position changes(final String next, final int offset) {
            return new Position(Optional.of(next), 0, Optional.empty(), offset);
        }

        /**
         * Validates one Bus optional text container.
         *
         * @param value optional text container
         * @param label safe semantic label
         * @return detached optional original text
         */
        private static Optional<String> optional(final Optional<String> value, final String label) {
            Assert.notNull(value, label + " container must not be null");
            return value.isPresent() ? Optional.of(requireText(value.getOrNull(), label)) : Optional.empty();
        }
    }

    /**
     * Carries one decoded official Graph collection page.
     *
     * @param items minimal page items
     * @param next  official nextLink or empty
     * @param <T>   minimal item type
     * @author Kimi Liu
     */
    private record WirePage<T>(List<T> items, Optional<String> next) {

        /**
         * Freezes one decoded Graph page.
         *
         * @param items minimal page items
         * @param next  official nextLink
         */
        private WirePage {
            items = List.copyOf(Assert.notNull(items, "Microsoft Graph page items must not be null"));
            next = Position.optional(next, "Microsoft Graph nextLink");
        }
    }

    /**
     * Minimal Microsoft organization projection.
     *
     * @param id   stable organization identifier
     * @param name exact display name
     * @author Kimi Liu
     */
    private record Organization(String id, String name) {

        /**
         * Validates one organization projection.
         *
         * @param id   stable organization identifier
         * @param name exact display name
         */
        private Organization {
            id = requireText(id, "Microsoft organization identifier");
            name = requireText(name, "Microsoft organization display name");
        }
    }

    /**
     * Minimal Microsoft user projection.
     *
     * @param id            stable user identifier
     * @param name          exact display name
     * @param principalName optional user principal name
     * @param mail          optional mail identifier
     * @param active        official account-enabled state
     * @author Kimi Liu
     */
    private record User(String id, String name, Optional<String> principalName, Optional<String> mail, boolean active) {

        /**
         * Validates one user projection.
         *
         * @param id            stable user identifier
         * @param name          exact display name
         * @param principalName optional user principal name
         * @param mail          optional mail identifier
         * @param active        official account-enabled state
         */
        private User {
            id = requireText(id, "Microsoft user identifier");
            name = requireText(name, "Microsoft user display name");
            principalName = Position.optional(principalName, "Microsoft user principal name");
            mail = Position.optional(mail, "Microsoft user mail");
        }
    }

    /**
     * Minimal Microsoft group projection.
     *
     * @param id              stable group identifier
     * @param name            exact display name
     * @param mail            optional mail identifier
     * @param mailEnabled     official mail-enabled flag retained for projection validation
     * @param securityEnabled official security-enabled flag retained for projection validation
     * @author Kimi Liu
     */
    private record Group(String id, String name, Optional<String> mail, boolean mailEnabled, boolean securityEnabled) {

        /**
         * Validates one group projection.
         *
         * @param id              stable group identifier
         * @param name            exact display name
         * @param mail            optional mail identifier
         * @param mailEnabled     official mail-enabled flag
         * @param securityEnabled official security-enabled flag
         */
        private Group {
            id = requireText(id, "Microsoft group identifier");
            name = requireText(name, "Microsoft group display name");
            mail = Position.optional(mail, "Microsoft group mail");
        }
    }

    /**
     * Minimal Microsoft directory role projection.
     *
     * @param id   stable role identifier
     * @param name exact role display name
     * @author Kimi Liu
     */
    private record Role(String id, String name) {

        /**
         * Validates one directory role projection.
         *
         * @param id   stable role identifier
         * @param name exact display name
         */
        private Role {
            id = requireText(id, "Microsoft role identifier");
            name = requireText(name, "Microsoft role display name");
        }
    }

    /**
     * Minimal Microsoft service-principal projection.
     *
     * @param id      stable service-principal identifier
     * @param appId   stable application identifier
     * @param name    exact display name
     * @param enabled official account-enabled flag retained for projection validation
     * @author Kimi Liu
     */
    private record ServiceAccount(String id, String appId, String name, boolean enabled) {

        /**
         * Validates one service-principal projection.
         *
         * @param id      stable service-principal identifier
         * @param appId   stable application identifier
         * @param name    exact display name
         * @param enabled official account-enabled flag
         */
        private ServiceAccount {
            id = requireText(id, "Microsoft service-principal identifier");
            appId = requireText(appId, "Microsoft service-principal application identifier");
            name = requireText(name, "Microsoft service-principal display name");
        }
    }

    /**
     * Minimal stable Graph member projection.
     *
     * @param id stable directory-object identifier
     * @author Kimi Liu
     */
    private record Member(String id) {

        /**
         * Validates one member projection.
         *
         * @param id stable member identifier
         */
        private Member {
            id = requireText(id, "Microsoft member identifier");
        }
    }

    /**
     * Minimal Microsoft application-assignment projection.
     *
     * @param id          stable assignment identifier
     * @param principalId assigning service-principal identifier
     * @param resourceId  assigned resource service-principal identifier
     * @param appRoleId   official application-role identifier retained for projection validation
     * @author Kimi Liu
     */
    private record Assignment(String id, String principalId, String resourceId, String appRoleId) {

        /**
         * Validates one application-assignment projection.
         *
         * @param id          stable assignment identifier
         * @param principalId assigning service-principal identifier
         * @param resourceId  assigned resource identifier
         * @param appRoleId   official application-role identifier
         */
        private Assignment {
            id = requireText(id, "Microsoft assignment identifier");
            principalId = requireText(principalId, "Microsoft assignment principal identifier");
            resourceId = requireText(resourceId, "Microsoft assignment resource identifier");
            appRoleId = requireText(appRoleId, "Microsoft assignment application role identifier");
        }
    }

    /**
     * Holds one short-lived Graph access token inside the Source-private cache.
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
            token = requireText(token, "Microsoft Graph access token");
            if (expiresAtMillis < 0L) {
                throw new ValidateException("Microsoft Graph access expiration must not be negative");
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
