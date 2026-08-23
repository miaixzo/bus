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
package org.miaixz.bus.auth.source.vendor.github;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
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
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the implementation-neutral GitHub Enterprise realm surface.
 * <p>
 * Snapshot pagination advances through Enterprise Teams, team memberships, and team organization assignments. Teams
 * normalize only to groups, while member and organization pages produce stable resources plus explicit membership
 * relations. Every invocation uses one freshly loaded read-only administrator-token lease, preserves GitHub's official
 * page and Link semantics, and closes the lease at the single terminal stage boundary without caching it.
 * </p>
 *
 * @author Kimi Liu
 */
public class GitHubRealmAdapter implements VendorAdapter {

    /**
     * Frozen GitHub REST API version required by every management request.
     */
    private static final String API_VERSION = "2026-03-10";

    /**
     * GitHub REST API version request header.
     */
    private static final String API_VERSION_HEADER = "X-GitHub-Api-Version";

    /**
     * GitHub versioned JSON representation accepted by management requests.
     */
    private static final String GITHUB_JSON = "application/vnd.github+json";

    /**
     * Empty immutable JSON attributes used by projections without declared extensions.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Exact resource categories exposed by GitHub Enterprise Teams.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set
            .of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.GROUP);

    /**
     * Ordered management-target closure required from the GitHub Enterprise manifest.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.REALM_USERS,
            Builder.REALM_USER,
            Builder.REALM_ORGANIZATIONS,
            Builder.REALM_ORGANIZATION,
            Builder.REALM_GROUPS,
            Builder.REALM_GROUP,
            Builder.REALM_GROUP_MEMBERS,
            Builder.REALM_ORGANIZATION_ASSIGNMENTS);

    /**
     * Selected immutable GitHub Enterprise Variant.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated GitHub Enterprise deployment options.
     */
    private final GitHubOptions options;

    /**
     * Caller-owned execution services.
     */
    private final DriverServices services;

    /**
     * Resolved official GitHub Enterprise resource targets.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Creates one Source-isolated GitHub Enterprise realm adapter.
     *
     * @param spaceId  Source space used for isolation
     * @param sourceId Source identifier
     * @param manifest exact GitHub manifest
     * @param variant  selected Enterprise Variant
     * @param options  validated Enterprise options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or collaborator is {@code null}
     * @throws ValidateException        if the manifest, Variant, options, protocol, or targets are inconsistent
     */
    public GitHubRealmAdapter(final String spaceId, final String sourceId, final GitHubManifest manifest,
            final VendorManifest.Variant variant, final GitHubOptions options, final DriverServices services) {
        Assert.notBlank(spaceId, "GitHub Enterprise space id must not be blank");
        Assert.notBlank(sourceId, "GitHub Enterprise Source id must not be blank");
        final GitHubManifest selectedManifest = Assert.notNull(manifest, "GitHub manifest must not be null");
        this.variant = Assert.notNull(variant, "GitHub Enterprise Variant must not be null");
        this.options = Assert.notNull(options, "GitHub Enterprise options must not be null");
        this.services = Assert.notNull(services, "GitHub Enterprise services must not be null");
        if (!GitHubManifest.ID.equals(selectedManifest.vendor())
                || !GitHubManifest.ENTERPRISE.equals(this.variant.variant())
                || !selectedManifest.variant(this.variant.variant()).equals(this.variant)
                || this.variant.protocol() != Protocol.HTTPS || !GitHubManifest.ID.equals(this.options.vendor())
                || !GitHubManifest.ENTERPRISE.equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || !this.options.scopes().isEmpty()
                || this.options.credential().type() != org.miaixz.bus.auth.Credential.Type.SHARED_SECRET) {
            throw new ValidateException("GitHub realm adapter requires the frozen management Variant");
        }
        this.services.policies().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (!List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("GitHub Enterprise manifest has an invalid management target set");
        }
    }

    /**
     * Reports whether one Link parameter section declares the exact next relation.
     *
     * @param parameters Link parameters following the target
     * @return whether the relation is next
     */
    private static boolean nextRelation(final String parameters) {
        for (String parameter : parameters.split(";")) {
            final String normalized = StringKit.trim(parameter).toLowerCase(Locale.ROOT);
            if ("rel=next".equals(normalized) || "rel=\"next\"".equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses one minimal GitHub user.
     *
     * @param value decoded object
     * @return validated user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        final String login = requiredString(value, "login");
        return new User(requiredIdentifier(value, "id"), login, fallback(value, "name", () -> login));
    }

    /**
     * Parses one minimal GitHub Enterprise Team.
     *
     * @param value decoded object
     * @return validated group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        return new Group(requiredIdentifier(value, "id"), requiredString(value, "slug"), requiredString(value, "name"));
    }

    /**
     * Parses one minimal GitHub organization.
     *
     * @param value decoded object
     * @return validated organization projection
     */
    private static Organization organization(final JsonValue.ObjectValue value) {
        return new Organization(requiredIdentifier(value, "id"), requiredString(value, "login"));
    }

    /**
     * Converts one user projection to a normalized resource.
     *
     * @param user       user projection
     * @param observedAt observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()), orderedIdentifier("login", user.login()),
                user.displayName(), Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one group projection to a normalized resource.
     *
     * @param group      group projection
     * @param observedAt observation instant
     * @return immutable group resource
     */
    private static Realm.Resource groupResource(final Group group, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.GROUP, group.id()), orderedIdentifier("slug", group.slug()),
                group.name(), Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one organization projection to a normalized resource.
     *
     * @param organization validated organization projection
     * @param observedAt   observation instant
     * @return immutable organization resource
     */
    private static Realm.Resource organizationResource(final Organization organization, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.ORGANIZATION, organization.id()),
                orderedIdentifier("login", organization.login()), organization.login(), Realm.State.UNKNOWN,
                EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one normalized user-to-group membership relation.
     *
     * @param userId     stable user identifier
     * @param groupId    stable group identifier
     * @param observedAt observation instant
     * @return immutable membership relation
     */
    private static Realm.Relation memberRelation(final String userId, final String groupId, final Instant observedAt) {
        return new Realm.Relation(new Realm.RelationKey(Realm.RelationKind.MEMBER,
                new Realm.Key(Realm.Kind.USER, userId), new Realm.Key(Realm.Kind.GROUP, groupId)), EMPTY_ATTRIBUTES,
                observedAt);
    }

    /**
     * Creates one normalized Team-to-organization assignment relation.
     *
     * @param groupId        stable Team identifier
     * @param organizationId stable organization identifier
     * @param observedAt     observation instant
     * @return immutable assignment relation
     */
    private static Realm.Relation organizationRelation(
            final String groupId,
            final String organizationId,
            final Instant observedAt) {
        final Map<String, JsonValue> attributes = Map.of("assignment", new JsonValue.StringValue("organization"));
        return new Realm.Relation(
                new Realm.RelationKey(Realm.RelationKind.MEMBER, new Realm.Key(Realm.Kind.GROUP, groupId),
                        new Realm.Key(Realm.Kind.ORGANIZATION, organizationId)),
                new JsonValue.ObjectValue(attributes), observedAt);
    }

    /**
     * Creates one immutable single identifier map.
     *
     * @param name  identifier name
     * @param value identifier value
     * @return immutable ordered map
     */
    private static Map<String, String> orderedIdentifier(final String name, final String value) {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put(name, value);
        return Collections.unmodifiableMap(result);
    }

    /**
     * Adds one resource with page-local conflict detection.
     *
     * @param values   resource map
     * @param resource candidate resource
     * @param label    projection label
     */
    private static void putResource(
            final Map<Realm.Key, Realm.Resource> values,
            final Realm.Resource resource,
            final String label) {
        final Realm.Resource previous = values.putIfAbsent(resource.key(), resource);
        if (previous != null && !previous.equals(resource)) {
            throw new ValidateException(label + " contains a conflicting resource key");
        }
    }

    /**
     * Adds one relation with page-local conflict detection.
     *
     * @param values   relation map
     * @param relation candidate relation
     * @param label    projection label
     */
    private static void putRelation(
            final Map<Realm.RelationKey, Realm.Relation> values,
            final Realm.Relation relation,
            final String label) {
        final Realm.Relation previous = values.putIfAbsent(relation.key(), relation);
        if (previous != null && !previous.equals(relation)) {
            throw new ValidateException(label + " contains a conflicting relation key");
        }
    }

    /**
     * Reads a required object member.
     *
     * @param object parent object
     * @param name   member name
     * @return object member
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        return requiredObject(object.values().get(name), name);
    }

    /**
     * Narrows one value to an object.
     *
     * @param value decoded value
     * @param label value label
     * @return object value
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue value, final String label) {
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException(label + " must be a JSON object");
        }
        return object;
    }

    /**
     * Reads a required array member.
     *
     * @param object parent object
     * @param name   member name
     * @return array values
     */
    private static List<JsonValue> requiredArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException(name + " must be a JSON array");
        }
        return array.values();
    }

    /**
     * Reads a required non-blank string member.
     *
     * @param object parent object
     * @param name   member name
     * @return validated string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string");
        }
        return requireText(text.value(), name);
    }

    /**
     * Reads one positive stable GitHub numeric identifier without precision loss.
     *
     * @param object parent object
     * @param name   member name
     * @return canonical decimal identifier
     */
    private static String requiredIdentifier(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        final BigInteger identifier;
        try {
            if (value instanceof JsonValue.NumberValue number) {
                identifier = number.value().toBigIntegerExact();
            } else if (value instanceof JsonValue.StringValue text) {
                identifier = new BigInteger(text.value());
                if (!identifier.toString().equals(text.value())) {
                    throw new ValidateException(name + " must be a canonical decimal identifier");
                }
            } else {
                throw new ValidateException(name + " must be a numeric identifier");
            }
        } catch (ArithmeticException | NumberFormatException cause) {
            throw new ValidateException(name + " must be an exact decimal identifier", cause);
        }
        if (identifier.signum() <= 0) {
            throw new ValidateException(name + " must be a positive identifier");
        }
        return identifier.toString();
    }

    /**
     * Reads a required integer member.
     *
     * @param object parent object
     * @param name   member name
     * @return exact integer
     */
    private static int requiredInt(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.NumberValue number) {
            try {
                return number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException(name + " must be an exact integer", cause);
            }
        }
        if (value instanceof JsonValue.StringValue text) {
            try {
                return Integer.parseInt(text.value());
            } catch (NumberFormatException cause) {
                throw new ValidateException(name + " must be an exact integer", cause);
            }
        }
        throw new ValidateException(name + " must be an integer");
    }

    /**
     * Reads a non-negative integer member.
     *
     * @param object parent object
     * @param name   member name
     * @return non-negative integer
     */
    private static int nonNegativeInt(final JsonValue.ObjectValue object, final String name) {
        final int value = requiredInt(object, name);
        if (value < 0) {
            throw new ValidateException(name + " must not be negative");
        }
        return value;
    }

    /**
     * Parses one canonical positive decimal cursor value.
     *
     * @param value lexical decimal
     * @return positive integer
     */
    private static int positiveDecimal(final String value) {
        return positiveDecimal(value, "cursor index");
    }

    /**
     * Parses one canonical positive decimal value with a safe semantic label.
     *
     * @param value lexical decimal
     * @param label semantic label
     * @return positive integer
     */
    private static int positiveDecimal(final String value, final String label) {
        try {
            final int result = Integer.parseInt(value);
            if (result <= 0 || !Integer.toString(result).equals(value)) {
                throw new ValidateException("GitHub " + label + " must be canonical and positive");
            }
            return result;
        } catch (NumberFormatException cause) {
            throw new ValidateException("GitHub " + label + " is invalid", cause);
        }
    }

    /**
     * Reads an explicit nullable cursor string.
     *
     * @param object parent object
     * @param name   member name
     * @return optional string
     */
    private static Optional<String> nullableString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a string or null");
        }
        return Optional.of(requireText(text.value(), name));
    }

    /**
     * Resolves a primary display string or invokes its declared fallback.
     *
     * @param object   parent object
     * @param name     primary member name
     * @param fallback fallback supplier
     * @return validated primary or fallback string
     */
    private static String fallback(
            final JsonValue.ObjectValue object,
            final String name,
            final java.util.function.Supplier<String> fallback) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return fallback.get();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a string or null");
        }
        return StringKit.hasText(text.value()) ? requireText(text.value(), name) : fallback.get();
    }

    /**
     * Validates an exact object member closure.
     *
     * @param object   decoded object
     * @param expected expected names
     */
    private static void exact(final JsonValue.ObjectValue object, final Set<String> expected) {
        if (!object.values().keySet().equals(expected)) {
            throw new ValidateException("GitHub cursor contains an invalid member set");
        }
    }

    /**
     * Decodes canonical requested kinds.
     *
     * @param values kind code values
     * @return stable ordered kinds
     */
    private static List<Realm.Kind> kinds(final List<JsonValue> values) {
        final LinkedHashSet<Realm.Kind> result = new LinkedHashSet<>();
        int previous = 0;
        for (JsonValue value : values) {
            if (!(value instanceof JsonValue.NumberValue number)) {
                throw new ValidateException("GitHub cursor kind must be numeric");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("GitHub cursor kind is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !result.add(kind)) {
                throw new ValidateException("GitHub cursor kinds are not canonical");
            }
            previous = code;
        }
        return List.copyOf(result);
    }

    /**
     * Resolves one supported kind code.
     *
     * @param code stable code
     * @return supported kind
     */
    private static Realm.Kind kind(final int code) {
        for (Realm.Kind kind : SUPPORTED_KINDS) {
            if (kind.code() == code) {
                return kind;
            }
        }
        throw new ValidateException("GitHub cursor contains an unsupported kind");
    }

    /**
     * Validates the requested kind closure.
     *
     * @param kinds requested kinds
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("GitHub snapshot contains an unsupported kind");
        }
    }

    /**
     * Validates text without trimming it.
     *
     * @param value text value
     * @param label semantic label
     * @return original text
     */
    private static String requireText(final String value, final String label) {
        final String result;
        try {
            result = Assert.notBlank(value, label + " must not be blank");
        } catch (IllegalArgumentException cause) {
            throw new ValidateException(label + " must not be blank", cause);
        }
        if (!result.equals(StringKit.trim(result))) {
            throw new ValidateException(label + " must not contain surrounding whitespace");
        }
        return result;
    }

    /**
     * Converts an optional string to JSON null or string.
     *
     * @param value optional string
     * @return JSON value
     */
    private static JsonValue nullable(final Optional<String> value) {
        return value.isPresent() ? new JsonValue.StringValue(value.getOrNull()) : JsonValue.NullValue.instance();
    }

    /**
     * Creates one JSON integer.
     *
     * @param value integer value
     * @return JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Narrows one outcome stage through the declared response class.
     *
     * @param stage        source stage
     * @param responseType response class
     * @param <S>          response type
     * @return narrowed stage
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "GitHub delegated outcome is unsupported");
        });
    }

    /**
     * Propagates a non-success outcome.
     *
     * @param outcome source outcome
     * @param <T>     target type
     * @return propagated outcome
     */
    private static <T> Outcome<T> propagate(final Outcome<?> outcome) {
        return switch (outcome) {
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "GitHub internal outcome cannot be propagated");
        };
    }

    /**
     * Creates a completed stage.
     *
     * @param outcome completed Realm outcome to expose asynchronously
     * @param <T>     value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates one rejection.
     *
     * @param code        error code
     * @param description safe diagnostic description
     * @param <T>         value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final Errors code, final String description) {
        return rejected(code, description, Map.of());
    }

    /**
     * Creates one detailed rejection.
     *
     * @param code        error code
     * @param description safe diagnostic description
     * @param details     safe details
     * @param <T>         value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.rejected(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Creates one failure.
     *
     * @param code        error code
     * @param description safe diagnostic description
     * @param <T>         value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates one detailed failure.
     *
     * @param code        error code
     * @param description safe diagnostic description
     * @param details     safe details
     * @param <T>         value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns the selected Realm capability manifest.
     *
     * @return immutable describe, snapshot, and retrieve manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes one exact GitHub Enterprise management capability.
     *
     * @param capability runtime-selected capability
     * @param request    capability-specific request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        response type
     * @return asynchronous typed outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "GitHub Realm capability must not be null");
        Assert.notNull(context, "GitHub Realm context must not be null");
        Assert.notNull(timeout, "GitHub Realm timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected(ErrorCode._400, "GitHub Realm capability is not declared"));
        }
        if (capability.equals(Realm.DESCRIBE) && request instanceof Realm.Describe) {
            return completed(Outcome.succeeded(capability.responseType().cast(GitHubManifest.realmDescription())));
        }
        if (capability.equals(Realm.SNAPSHOT) && request instanceof Realm.Snapshot value) {
            return narrow(snapshot(value, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.RETRIEVE) && request instanceof Realm.Retrieve value) {
            return narrow(retrieve(value, context, timeout), capability.responseType());
        }
        return completed(rejected(ErrorCode._400, "GitHub Realm request does not match the capability contract"));
    }

    /**
     * Performs no action because this adapter owns no cache or shared service.
     */
    @Override
    public void close() {
    }

    /**
     * Validates and executes one snapshot under a fresh Secret lease.
     *
     * @param request snapshot request
     * @param context invocation context
     * @param timeout shared timeout
     * @return asynchronous snapshot outcome
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
            return completed(rejected(ErrorCode._400, "GitHub Enterprise snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "GitHub Enterprise snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
        return withSecret(context, timeout, secret -> switch (state.phase()) {
            case TEAMS -> teams(secret, request, state, observedAt, timeout);
            case TEAM_MEMBERS -> teamMembers(secret, request, state, observedAt, timeout);
            case TEAM_ORGANIZATIONS -> teamOrganizations(secret, request, state, observedAt, timeout);
        });
    }

    /**
     * Reads one bounded official Enterprise Team page.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      team cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized group page
     */
    private Outcome<Realm.Page> teams(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<WirePage<Group>> fetched = page(
                Phase.TEAMS,
                null,
                state.position().start(),
                request.limit(),
                secret,
                timeout,
                "snapshot",
                GitHubRealmAdapter::group);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        for (Group group : success.value().items()) {
            putResource(resources, groupResource(group, observedAt), "GitHub team page");
        }
        return success.value().next() > 0
                ? output(List.copyOf(resources.values()), List.of(), state, Position.page(success.value().next()))
                : complete(List.copyOf(resources.values()), List.of(), state);
    }

    /**
     * Reads one team-membership page while replaying the owning Team page deterministically.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      team-membership cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized users and membership relations
     */
    private Outcome<Realm.Page> teamMembers(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<ParentPage> replayed = parent(state, request.limit(), secret, timeout, "snapshot");
        if (!(replayed instanceof Outcome.Succeeded<ParentPage> replaySuccess)) {
            return propagate(replayed);
        }
        if (replaySuccess.value().parent().isEmpty()) {
            return complete(List.of(), List.of(), state);
        }
        final Group group = replaySuccess.value().parent().getOrNull();
        final int innerPage = state.position().relationOffset() == 0 ? Normal._1 : state.position().relationOffset();
        final Outcome<WirePage<User>> fetched = page(
                Phase.TEAM_MEMBERS,
                group.slug(),
                innerPage,
                request.limit(),
                secret,
                timeout,
                "snapshot",
                GitHubRealmAdapter::user);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        final Map<Realm.RelationKey, Realm.Relation> relations = new LinkedHashMap<>();
        for (User user : success.value().items()) {
            putResource(resources, userResource(user, observedAt), "GitHub team-member page");
            if (state.kinds().contains(Realm.Kind.GROUP)) {
                putRelation(relations, memberRelation(user.id(), group.id(), observedAt), "GitHub team-member page");
            }
        }
        if (success.value().next() > 0) {
            return output(
                    List.copyOf(resources.values()),
                    List.copyOf(relations.values()),
                    state,
                    Position.group(state.position().start(), group.id(), success.value().next()));
        }
        return advanceParent(resources, relations, state, replaySuccess.value());
    }

    /**
     * Reads one team-organization assignment page while replaying its owning Team page.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      organization-assignment cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized organizations and assignment relations
     */
    private Outcome<Realm.Page> teamOrganizations(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<ParentPage> replayed = parent(state, request.limit(), secret, timeout, "snapshot");
        if (!(replayed instanceof Outcome.Succeeded<ParentPage> replaySuccess)) {
            return propagate(replayed);
        }
        if (replaySuccess.value().parent().isEmpty()) {
            return complete(List.of(), List.of(), state);
        }
        final Group group = replaySuccess.value().parent().getOrNull();
        final int innerPage = state.position().relationOffset() == 0 ? Normal._1 : state.position().relationOffset();
        final Outcome<WirePage<Organization>> fetched = page(
                Phase.TEAM_ORGANIZATIONS,
                group.slug(),
                innerPage,
                request.limit(),
                secret,
                timeout,
                "snapshot",
                GitHubRealmAdapter::organization);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Organization>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        final Map<Realm.RelationKey, Realm.Relation> relations = new LinkedHashMap<>();
        for (Organization organization : success.value().items()) {
            putResource(resources, organizationResource(organization, observedAt), "GitHub team-organization page");
            if (state.kinds().contains(Realm.Kind.GROUP)) {
                putRelation(
                        relations,
                        organizationRelation(group.id(), organization.id(), observedAt),
                        "GitHub team-organization page");
            }
        }
        if (success.value().next() > 0) {
            return output(
                    List.copyOf(resources.values()),
                    List.copyOf(relations.values()),
                    state,
                    Position.group(state.position().start(), group.id(), success.value().next()));
        }
        return advanceParent(resources, relations, state, replaySuccess.value());
    }

    /**
     * Validates and executes one stable-key retrieval under a fresh Secret lease.
     *
     * @param request retrieval request
     * @param context invocation context
     * @param timeout shared timeout
     * @return asynchronous retrieval outcome
     */
    private CompletionStage<Outcome<Realm.Retrieved>> retrieve(
            final Realm.Retrieve request,
            final Context context,
            final Timeout timeout) {
        if (!SUPPORTED_KINDS.contains(request.key().kind())) {
            return completed(rejected(ErrorCode._400, "GitHub Enterprise retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "GitHub Enterprise retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
        return withSecret(context, timeout, secret -> retrieve(secret, request.key(), observedAt, timeout));
    }

    /**
     * Retrieves one official User, Organization, or Enterprise Team resource.
     *
     * @param secret     open administrator-token lease
     * @param key        stable resource key
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return retrieved resource or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieve(
            final SecretLease secret,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        if (key.kind() == Realm.Kind.GROUP) {
            return retrieveGroup(secret, key, observedAt, timeout);
        }
        final String targetName = key.kind() == Realm.Kind.USER ? Builder.REALM_USER : Builder.REALM_ORGANIZATION;
        final Outcome<JsonValue.ObjectValue> fetched = get(
                url(targetName, List.of(key.externalId()), Map.of()),
                secret,
                timeout,
                "retrieve");
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final Realm.Resource resource = key.kind() == Realm.Kind.USER ? userResource(user(success.value()), observedAt)
                : organizationResource(organization(success.value()), observedAt);
        if (!key.equals(resource.key())) {
            return failed(ErrorCode._502, "GitHub Enterprise retrieval returned a different stable identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(resource)));
    }

    /**
     * Scans the official Team collection by stable numeric identifier before using the slug-only retrieve endpoint.
     *
     * @param secret     open administrator-token lease
     * @param key        stable Group key
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return retrieved Team or natural absence
     */
    private Outcome<Realm.Retrieved> retrieveGroup(
            final SecretLease secret,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        int page = Normal._1;
        while (true) {
            final Outcome<WirePage<Group>> fetched = page(
                    Phase.TEAMS,
                    null,
                    page,
                    Normal._100,
                    secret,
                    timeout,
                    "retrieve",
                    GitHubRealmAdapter::group);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
                return propagate(fetched);
            }
            for (Group group : success.value().items()) {
                if (key.externalId().equals(group.id())) {
                    final Outcome<JsonValue.ObjectValue> direct = get(
                            url(Builder.REALM_GROUP, List.of(group.slug()), Map.of()),
                            secret,
                            timeout,
                            "retrieve");
                    if (!(direct instanceof Outcome.Succeeded<JsonValue.ObjectValue> directSuccess)) {
                        return propagate(direct);
                    }
                    final Realm.Resource resource = groupResource(group(directSuccess.value()), observedAt);
                    if (!key.equals(resource.key())) {
                        return failed(
                                ErrorCode._502,
                                "GitHub Enterprise Team retrieval returned a different stable identifier");
                    }
                    return Outcome.succeeded(new Realm.Retrieved(Optional.of(resource)));
                }
            }
            if (success.value().next() == 0) {
                return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
            }
            page = success.value().next();
        }
    }

    /**
     * Replays one official Team page and selects the cursor-owned Team parent.
     *
     * @param state     dependent phase state
     * @param limit     outward page limit
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return selected Team and its outer-page navigation state
     */
    private Outcome<ParentPage> parent(
            final CursorState state,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final Outcome<WirePage<Group>> fetched = page(
                Phase.TEAMS,
                null,
                state.position().start(),
                limit,
                secret,
                timeout,
                operation,
                GitHubRealmAdapter::group);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
            return propagate(fetched);
        }
        final List<Group> groups = success.value().items();
        if (groups.isEmpty()) {
            if (state.position().parentId().isPresent() || success.value().next() > 0) {
                return failed(ErrorCode._502, "GitHub Enterprise Team replay page is inconsistent");
            }
            return Outcome.succeeded(new ParentPage(groups, -1, success.value().next(), Optional.empty()));
        }
        if (state.position().parentId().isEmpty()) {
            return Outcome.succeeded(new ParentPage(groups, 0, success.value().next(), Optional.of(groups.getFirst())));
        }
        for (int index = 0; index < groups.size(); index++) {
            if (state.position().parentId().getOrNull().equals(groups.get(index).id())) {
                return Outcome.succeeded(
                        new ParentPage(groups, index, success.value().next(), Optional.of(groups.get(index))));
            }
        }
        return failed(ErrorCode._502, "GitHub Enterprise replay Team no longer exists on its official page");
    }

    /**
     * Advances a dependent phase to the following Team or official Team collection page.
     *
     * @param resources normalized resources produced for the current Team
     * @param relations normalized relations produced for the current Team
     * @param state     current dependent phase state
     * @param parent    replayed parent page
     * @return continued or completed normalized page
     */
    private Outcome<Realm.Page> advanceParent(
            final Map<Realm.Key, Realm.Resource> resources,
            final Map<Realm.RelationKey, Realm.Relation> relations,
            final CursorState state,
            final ParentPage parent) {
        final int following = parent.index() + 1;
        if (following < parent.items().size()) {
            return output(
                    List.copyOf(resources.values()),
                    List.copyOf(relations.values()),
                    state,
                    Position.group(state.position().start(), parent.items().get(following).id(), Normal._1));
        }
        if (parent.next() > 0) {
            return output(
                    List.copyOf(resources.values()),
                    List.copyOf(relations.values()),
                    state,
                    Position.page(parent.next()));
        }
        return complete(List.copyOf(resources.values()), List.copyOf(relations.values()), state);
    }

    /**
     * Loads, validates, executes with, and closes one administrator-token lease.
     *
     * @param context   invocation context
     * @param timeout   shared timeout
     * @param operation synchronous operation using the open lease
     * @param <T>       successful value type
     * @return asynchronous operation outcome
     */
    private <T> CompletionStage<Outcome<T>> withSecret(
            final Context context,
            final Timeout timeout,
            final Function<SecretLease, Outcome<T>> operation) {
        final CompletionStage<Outcome<SecretLoader.Record>> loaded;
        try {
            loaded = services.secretLoader()
                    .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout);
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "GitHub Enterprise Secret Loader failed before returning a stage"));
        }
        if (loaded == null) {
            return completed(failed(ErrorCode._500, "GitHub Enterprise Secret Loader returned no stage"));
        }
        return loaded.<Outcome<SecretLoader.Record>>handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : failed(ErrorCode._500, "GitHub Enterprise Secret Loader stage failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLoader.Record> success -> execute(success.value(), timeout, operation);
                    case Outcome.Rejected<SecretLoader.Record> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLoader.Record> failed -> completed(Outcome.failed(failed.failure()));
                    default -> completed(
                            failed(ErrorCode._500, "GitHub Enterprise Secret Loader returned an unsupported outcome"));
                });
    }

    /**
     * Parses one loaded record and binds its lease to one terminal asynchronous close point.
     *
     * @param loaded    loaded Secret record
     * @param timeout   shared timeout
     * @param operation synchronous operation using the lease
     * @param <T>       successful value type
     * @return asynchronous closed operation outcome
     */
    private <T> CompletionStage<Outcome<T>> execute(
            final SecretLoader.Record loaded,
            final Timeout timeout,
            final Function<SecretLease, Outcome<T>> operation) {
        final SecretLease raw = loaded == null ? null : loaded.lease();
        final SecretLease secret;
        try {
            secret = services.secretParser().parse(services.entry(), options.credential(), loaded);
        } catch (RuntimeException ignored) {
            if (raw != null) {
                raw.close();
            }
            return completed(failed(ErrorCode._500, "GitHub Enterprise loaded Secret could not be validated"));
        }
        try {
            return CompletableFuture.<Outcome<T>>supplyAsync(() -> {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "GitHub Enterprise operation has no remaining timeout");
                }
                try {
                    return Assert.notNull(operation.apply(secret), "GitHub Enterprise operation returned no outcome");
                } catch (TimeoutException ignored) {
                    return failed(ErrorCode._408, "GitHub Enterprise operation timed out");
                } catch (ValidateException ignored) {
                    return failed(ErrorCode._502, "GitHub Enterprise returned an invalid projection");
                } catch (RuntimeException ignored) {
                    return failed(ErrorCode._503, "GitHub Enterprise transport is unavailable");
                }
            }, services.executor()).whenComplete((ignored, cause) -> secret.close());
        } catch (RuntimeException ignored) {
            secret.close();
            return completed(failed(ErrorCode._500, "GitHub Enterprise operation could not be scheduled"));
        }
    }

    /**
     * Reads one official Link-paged GitHub collection.
     *
     * @param phase     exact collection phase
     * @param parent    Team slug for dependent collections or {@code null}
     * @param page      one-based requested page
     * @param limit     outward page limit
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @param parser    minimal item parser
     * @param <T>       minimal item type
     * @return normalized wire page
     */
    private <T> Outcome<WirePage<T>> page(
            final Phase phase,
            final String parent,
            final int page,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation,
            final Function<JsonValue.ObjectValue, T> parser) {
        final int perPage = Math.min(limit, Normal._100);
        final List<String> segments = phase == Phase.TEAMS ? List.of()
                : List.of(
                        requireText(parent, "GitHub Team slug"),
                        phase == Phase.TEAM_MEMBERS ? "memberships" : "organizations");
        final Url request = url(
                phase == Phase.TEAM_ORGANIZATIONS ? Builder.REALM_ORGANIZATION_ASSIGNMENTS
                        : phase == Phase.TEAM_MEMBERS ? Builder.REALM_GROUP_MEMBERS : Builder.REALM_GROUPS,
                segments,
                Map.of("per_page", Integer.toString(perPage), "page", Integer.toString(page)));
        return request(request, secret, timeout, operation, response -> {
            final List<T> items = new ArrayList<>();
            for (JsonValue value : array(response)) {
                items.add(parser.apply(requiredObject(value, "GitHub collection item")));
            }
            return new WirePage<>(items, nextPage(response.headers(), phase, parent, perPage, page));
        });
    }

    /**
     * Executes one GitHub management GET and decodes a successful object.
     *
     * @param url       manifest-derived URL
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return decoded object or classified failure
     */
    private Outcome<JsonValue.ObjectValue> get(
            final Url url,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        return request(url, secret, timeout, operation, this::object);
    }

    /**
     * Executes one GitHub management GET through an operation-specific bounded response reader.
     *
     * @param url       manifest-derived URL
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @param reader    bounded response reader
     * @param <T>       decoded response type
     * @return decoded value or classified failure
     */
    private <T> Outcome<T> request(
            final Url url,
            final SecretLease secret,
            final Timeout timeout,
            final String operation,
            final Function<Response, T> reader) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "GitHub Enterprise request has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(Protocol.HTTPS, timeout, services.policies()).url(url.toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, GITHUB_JSON)
                    .header(API_VERSION_HEADER, API_VERSION)
                    .header(Http.Header.AUTHORIZATION, "Bearer " + new String(secret.material())).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "GitHub Enterprise request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "GitHub Enterprise endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return httpFailure(response, operation);
            }
            return Outcome.succeeded(reader.apply(response));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "GitHub Enterprise returned an invalid response");
        }
    }

    /**
     * Validates and classifies one official GitHub error response.
     *
     * @param response  owned unsuccessful response
     * @param operation safe operation label
     * @param <T>       expected success type
     * @return classified failure or rejection
     */
    private <T> Outcome<T> httpFailure(final Response response, final String operation) {
        final int status = response.code();
        try {
            requiredString(object(response), "message");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "GitHub Enterprise returned an invalid error envelope");
        }
        final Map<String, JsonValue> details = details(operation, status, response.headers());
        details.put(Builder.ERROR_CODE_FIELD, number(status));
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "GitHub Enterprise rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "GitHub Enterprise rejected the administrator token", details);
        }
        if (status == Http.Status.FORBIDDEN || status == Http.Status.NOT_FOUND) {
            return rejected(ErrorCode._403, "GitHub Enterprise visibility or permission is insufficient", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "GitHub Enterprise is rate limited", details);
        }
        return failed(ErrorCode._502, "GitHub Enterprise returned an upstream error", details);
    }

    /**
     * Extracts and validates the single official next-page Link.
     *
     * @param headers response headers
     * @param phase   owning collection phase
     * @param parent  owning Team slug or {@code null}
     * @param perPage requested page size
     * @param current current requested page
     * @return next one-based page or zero at natural exhaustion
     */
    private int nextPage(
            final FabricX.Headers headers,
            final Phase phase,
            final String parent,
            final int perPage,
            final int current) {
        Integer selected = null;
        for (String header : headers.values(Http.Header.LINK)) {
            for (String element : header.split(",")) {
                final int opening = element.indexOf('<');
                final int closing = element.indexOf('>', opening + 1);
                final String parameters = closing < 0 ? element : element.substring(closing + 1);
                if (!nextRelation(parameters)) {
                    continue;
                }
                if (opening < 0 || closing <= opening) {
                    throw new ValidateException("GitHub response contains a malformed next Link value");
                }
                final int candidate = pagination(element.substring(opening + 1, closing), phase, parent, perPage);
                if (selected != null && selected != candidate) {
                    throw new ValidateException("GitHub response contains conflicting next Link values");
                }
                selected = candidate;
            }
        }
        if (selected != null && selected <= current) {
            throw new ValidateException("GitHub response next Link does not advance pagination");
        }
        return selected == null ? 0 : selected;
    }

    /**
     * Validates an official GitHub continuation URL and returns its page number.
     *
     * @param value   absolute Link target
     * @param phase   owning collection phase
     * @param parent  owning Team slug or {@code null}
     * @param perPage expected page size
     * @return validated positive next page
     */
    private int pagination(final String value, final Phase phase, final String parent, final int perPage) {
        final Url candidate = Url.parse(requireText(value, "GitHub continuation URL"));
        final Url base = target(Builder.REALM_GROUPS).url();
        final String expectedPath = phase == Phase.TEAMS ? base.path()
                : base.path() + "/" + RFC3986.SEGMENT.encode(requireText(parent, "GitHub Team slug"), Charset.UTF_8)
                        + (phase == Phase.TEAM_MEMBERS ? "/memberships" : "/organizations");
        if (!Protocol.HTTPS.getName().equalsIgnoreCase(candidate.scheme()) || candidate.port() != Port._443.getPort()
                || !base.host().equals(candidate.host())
                || candidate.username() != null && !candidate.username().isEmpty()
                || candidate.password() != null && !candidate.password().isEmpty()
                || candidate.fragment() != null && !candidate.fragment().isEmpty()
                || !expectedPath.equals(candidate.path())
                || !candidate.queryParameterNames().equals(Set.of("per_page", "page"))
                || candidate.queryParameterValues("per_page").size() != 1
                || candidate.queryParameterValues("page").size() != 1) {
            throw new ValidateException("GitHub continuation URL violates the selected endpoint boundary");
        }
        final int returnedPerPage = positiveDecimal(candidate.queryParameter("per_page"), "page size");
        final int returnedPage = positiveDecimal(candidate.queryParameter("page"), "page");
        if (returnedPerPage != perPage || returnedPerPage > Normal._100) {
            throw new ValidateException("GitHub continuation page size is inconsistent");
        }
        return returnedPage;
    }

    /**
     * Builds one GitHub URL from a manifest-owned base, encoded segments, and fixed query values.
     *
     * @param targetName exact management target key
     * @param segments   path segments appended to the base
     * @param query      exact query members
     * @return immutable encoded HTTPS URL
     */
    private Url url(final String targetName, final List<String> segments, final Map<String, String> query) {
        final Url base = target(targetName).url();
        final StringBuilder path = new StringBuilder(base.path());
        for (String segment : segments) {
            path.append('/').append(RFC3986.SEGMENT.encode(requireText(segment, "GitHub path segment"), Charset.UTF_8));
        }
        final UrlBuilder builder = Url.builder().scheme(base.scheme()).host(base.host()).path(path.toString());
        query.forEach(builder::query);
        return builder.build();
    }

    /**
     * Resolves one required management endpoint.
     *
     * @param name target key
     * @return resolved endpoint
     */
    private Endpoint target(final String name) {
        final Endpoint endpoint = targets.management().get(name);
        if (endpoint == null) {
            throw new ValidateException("GitHub Enterprise manifest omits a required management target");
        }
        return endpoint;
    }

    /**
     * Emits a page with one canonical continuation cursor.
     *
     * @param resources resource projections accumulated for the current page
     * @param relations relation projections accumulated for the current page
     * @param state     current state
     * @param position  next position
     * @return successful page
     */
    private Outcome<Realm.Page> output(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Position position) {
        return Outcome.succeeded(
                new Realm.Page(resources, relations,
                        Optional.of(encode(new CursorState(state.phase(), state.kinds(), position)))));
    }

    /**
     * Completes one phase and advances to the next requested phase.
     *
     * @param resources resource projections accumulated for the current page
     * @param relations relation projections accumulated for the current page
     * @param state     completed state
     * @return successful completed or continued page
     */
    private Outcome<Realm.Page> complete(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state) {
        Phase next = state.phase().next();
        while (next != null && !state.kinds().contains(next.kind)) {
            next = next.next();
        }
        return Outcome.succeeded(
                new Realm.Page(resources, relations, next == null ? Optional.empty()
                        : Optional.of(encode(new CursorState(next, state.kinds(), Position.initial())))));
    }

    /**
     * Encodes one canonical six-field cursor.
     *
     * @param state cursor state
     * @return opaque cursor
     */
    private Realm.Cursor encode(final CursorState state) {
        final Map<String, JsonValue> envelope = new LinkedHashMap<>();
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(GitHubManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(GitHubManifest.ENTERPRISE.value()));
        envelope.put(Builder.OPERATION_FIELD, number(Realm.Operation.SNAPSHOT.code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code));
        final List<JsonValue> kinds = new ArrayList<>();
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        final Map<String, JsonValue> position = new LinkedHashMap<>();
        position.put(Builder.CURSOR_NEXT_FIELD, new JsonValue.StringValue(Integer.toString(state.position().start())));
        if (state.phase() != Phase.TEAMS) {
            position.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(state.position().parentId()));
            position.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(state.position().relationOffset()));
        }
        envelope.put(Builder.CURSOR_POSITION_FIELD, new JsonValue.ObjectValue(position));
        return new Realm.Cursor(Base64.encodeUrlSafe(JsonKit.writeValue(new JsonValue.ObjectValue(envelope))));
    }

    /**
     * Decodes and canonicalizes one cursor.
     *
     * @param cursor supplied cursor
     * @param kinds  current requested kinds
     * @return validated state
     */
    private CursorState decode(final Realm.Cursor cursor, final Set<Realm.Kind> kinds) {
        try {
            final JsonValue.ObjectValue envelope = requiredObject(
                    JsonKit.readValue(Base64.decode(cursor.value()), Builder.MAXIMUM_REALM_JSON_DEPTH, true),
                    "GitHub cursor");
            exact(
                    envelope,
                    Set.of(
                            Builder.VENDOR_FIELD,
                            Builder.VARIANT_FIELD,
                            Builder.OPERATION_FIELD,
                            Builder.CURSOR_PHASE_FIELD,
                            Builder.CURSOR_KIND_FIELD,
                            Builder.CURSOR_POSITION_FIELD));
            if (!GitHubManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !GitHubManifest.ENTERPRISE.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredInt(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("GitHub cursor context is invalid");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("GitHub cursor kinds do not match the request");
            }
            final JsonValue.ObjectValue raw = requiredObject(envelope, Builder.CURSOR_POSITION_FIELD);
            final Position position;
            if (phase == Phase.TEAMS) {
                exact(raw, Set.of(Builder.CURSOR_NEXT_FIELD));
                position = Position.page(positiveDecimal(requiredString(raw, Builder.CURSOR_NEXT_FIELD)));
            } else {
                exact(
                        raw,
                        Set.of(
                                Builder.CURSOR_NEXT_FIELD,
                                Builder.CURSOR_PARENT_ID_FIELD,
                                Builder.CURSOR_RELATION_OFFSET_FIELD));
                position = Position.group(
                        positiveDecimal(requiredString(raw, Builder.CURSOR_NEXT_FIELD)),
                        nullableString(raw, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                        nonNegativeInt(raw, Builder.CURSOR_RELATION_OFFSET_FIELD));
            }
            final CursorState state = new CursorState(phase, decodedKinds, position);
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("GitHub cursor is not canonical");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("GitHub cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded response object.
     *
     * @param response successful response
     * @return decoded object
     */
    private JsonValue.ObjectValue object(final Response response) {
        return requiredObject(value(response), "GitHub Enterprise response");
    }

    /**
     * Decodes one bounded response array.
     *
     * @param response successful response
     * @return decoded array members
     */
    private List<JsonValue> array(final Response response) {
        final JsonValue value = value(response);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("GitHub Enterprise response must be a JSON array");
        }
        return array.values();
    }

    /**
     * Decodes one bounded GitHub JSON response.
     *
     * @param response successful or error response
     * @return decoded JSON value
     */
    private JsonValue value(final Response response) {
        return JsonKit
                .readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Builder.MAXIMUM_REALM_JSON_DEPTH, true);
    }

    /**
     * Builds safe failure details.
     *
     * @param operation operation label
     * @param status    HTTP status
     * @param headers   response headers
     * @return mutable allow-listed details for optional error code insertion
     */
    private Map<String, JsonValue> details(final String operation, final int status, final FabricX.Headers headers) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        values.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(GitHubManifest.ID.value()));
        values.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(variant.variant().value()));
        values.put(Builder.OPERATION_FIELD, new JsonValue.StringValue(operation));
        values.put(Builder.HTTP_STATUS_FIELD, number(status));
        final String retry = headers.get(Http.Header.RETRY_AFTER);
        if (retry != null) {
            try {
                final long seconds = Long.parseLong(retry);
                if (seconds >= 0L) {
                    values.put(Builder.RETRY_AFTER_SECONDS_FIELD, number(seconds));
                }
            } catch (NumberFormatException ignored) {
                values.remove(Builder.RETRY_AFTER_SECONDS_FIELD);
            }
        }
        return values;
    }

    /**
     * Defines the finite GitHub snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Reads Enterprise Teams as groups.
         */
        TEAMS(1, Realm.Kind.GROUP),

        /**
         * Reads Team members as users and membership relations.
         */
        TEAM_MEMBERS(2, Realm.Kind.USER),

        /**
         * Reads Team organization assignments.
         */
        TEAM_ORGANIZATIONS(3, Realm.Kind.ORGANIZATION);

        /**
         * Stable phase code.
         */
        private final int code;

        /**
         * Resource kind enabling the phase.
         */
        private final Realm.Kind kind;

        /**
         * Creates one phase.
         *
         * @param code stable code
         * @param kind enabling kind
         */
        Phase(final int code, final Realm.Kind kind) {
            this.code = code;
            this.kind = kind;
        }

        /**
         * Resolves one stable phase code.
         *
         * @param code stable code
         * @return phase
         */
        private static Phase from(final int code) {
            for (Phase phase : values()) {
                if (phase.code == code) {
                    return phase;
                }
            }
            throw new ValidateException("GitHub cursor phase is unknown");
        }

        /**
         * Returns the following frozen phase.
         *
         * @return following phase or {@code null}
         */
        private Phase next() {
            return switch (this) {
                case TEAMS -> TEAM_MEMBERS;
                case TEAM_MEMBERS -> TEAM_ORGANIZATIONS;
                case TEAM_ORGANIZATIONS -> null;
            };
        }
    }

    /**
     * Carries one canonical snapshot state.
     *
     * @param phase    snapshot phase represented by this cursor
     * @param kinds    requested kinds
     * @param position recoverable position
     * @author Kimi Liu
     */
    private record CursorState(Phase phase, List<Realm.Kind> kinds, Position position) {

        /**
         * Validates one cursor state.
         */
        private CursorState {
            phase = Assert.notNull(phase, "GitHub cursor phase must not be null");
            kinds = List.copyOf(Assert.notNull(kinds, "GitHub cursor kinds must not be null"));
            position = Assert.notNull(position, "GitHub cursor position must not be null");
            if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds) || !kinds.contains(phase.kind)) {
                throw new ValidateException("GitHub cursor kinds or phase are invalid");
            }
            position.validate(phase);
        }

        /**
         * Creates the first enabled phase.
         *
         * @param kinds requested kinds
         * @return initial state
         */
        private static CursorState initial(final Set<Realm.Kind> kinds) {
            final List<Realm.Kind> ordered = List.copyOf(kinds);
            final Phase initial = ordered.contains(Realm.Kind.GROUP) ? Phase.TEAMS
                    : ordered.contains(Realm.Kind.USER) ? Phase.TEAM_MEMBERS : Phase.TEAM_ORGANIZATIONS;
            return new CursorState(initial, ordered, Position.initial());
        }
    }

    /**
     * Carries one outer Team page and dependent collection page position.
     *
     * @param start          one-based official Team page
     * @param parentId       current replay Team identifier
     * @param relationOffset one-based dependent collection page or zero
     * @author Kimi Liu
     */
    private record Position(int start, Optional<String> parentId, int relationOffset) {

        /**
         * Validates one position.
         */
        private Position {
            if (start <= 0 || relationOffset < 0) {
                throw new ValidateException("GitHub cursor position is out of range");
            }
            parentId = Assert.notNull(parentId, "GitHub cursor parent container must not be null");
            parentId = parentId.isPresent()
                    ? Optional.of(requireText(parentId.getOrNull(), "GitHub cursor parent identifier"))
                    : Optional.empty();
        }

        /**
         * Creates the initial position.
         *
         * @return initial position
         */
        private static Position initial() {
            return page(Normal._1);
        }

        /**
         * Creates a top-level page position.
         *
         * @param start official start index
         * @return page position
         */
        private static Position page(final int start) {
            return new Position(start, Optional.empty(), 0);
        }

        /**
         * Creates a dependent Team collection position.
         *
         * @param start          official Team page
         * @param parentId       replay Team identifier
         * @param relationOffset dependent collection page
         * @return dependent position
         */
        private static Position group(final int start, final String parentId, final int relationOffset) {
            return new Position(start, Optional.ofNullable(parentId), relationOffset);
        }

        /**
         * Validates phase-specific fields.
         *
         * @param phase owning phase
         */
        private void validate(final Phase phase) {
            if (phase == Phase.TEAMS && (parentId.isPresent() || relationOffset != 0)
                    || phase != Phase.TEAMS && parentId.isPresent() != (relationOffset > 0)) {
                throw new ValidateException("GitHub cursor position does not belong to its phase");
            }
        }
    }

    /**
     * Carries one validated GitHub Link-paged collection page.
     *
     * @param items minimal items
     * @param next  next page or zero
     * @param <T>   item type
     * @author Kimi Liu
     */
    private record WirePage<T>(List<T> items, int next) {

        /**
         * Freezes one page.
         */
        private WirePage {
            items = List.copyOf(Assert.notNull(items, "GitHub collection items must not be null"));
            if (next < 0) {
                throw new ValidateException("GitHub collection next page must not be negative");
            }
        }
    }

    /**
     * Carries one replayed outer Team page and selected parent.
     *
     * @param items  Teams on the official outer page
     * @param index  selected Team index or negative at natural exhaustion
     * @param next   following outer page or zero
     * @param parent selected Team or empty at natural exhaustion
     * @author Kimi Liu
     */
    private record ParentPage(List<Group> items, int index, int next, Optional<Group> parent) {

        /**
         * Freezes and validates one replay result.
         */
        private ParentPage {
            items = List.copyOf(Assert.notNull(items, "GitHub replay Teams must not be null"));
            parent = Assert.notNull(parent, "GitHub replay parent container must not be null");
            if (index < -1 || next < 0 || index >= items.size() || parent.isPresent() != (index >= 0)
                    || parent.isPresent() && !parent.getOrNull().equals(items.get(index))) {
                throw new ValidateException("GitHub replay parent state is inconsistent");
            }
        }
    }

    /**
     * Minimal GitHub user projection.
     *
     * @param id          stable identifier
     * @param login       login identifier
     * @param displayName display name
     * @author Kimi Liu
     */
    private record User(String id, String login, String displayName) {

        /**
         * Validates one user.
         */
        private User {
            id = requireText(id, "GitHub user identifier");
            login = requireText(login, "GitHub user login");
            displayName = requireText(displayName, "GitHub user display name");
        }
    }

    /**
     * Minimal GitHub Enterprise Team projection.
     *
     * @param id   stable identifier
     * @param slug stable Team slug
     * @param name display name
     * @author Kimi Liu
     */
    private record Group(String id, String slug, String name) {

        /**
         * Validates one group.
         */
        private Group {
            id = requireText(id, "GitHub group identifier");
            slug = requireText(slug, "GitHub Team slug");
            name = requireText(name, "GitHub Team name");
        }
    }

    /**
     * Minimal GitHub organization projection.
     *
     * @param id    stable numeric identifier
     * @param login stable organization login
     * @author Kimi Liu
     */
    private record Organization(String id, String login) {

        /**
         * Validates one organization.
         */
        private Organization {
            id = requireText(id, "GitHub organization identifier");
            login = requireText(login, "GitHub organization login");
        }
    }

}
