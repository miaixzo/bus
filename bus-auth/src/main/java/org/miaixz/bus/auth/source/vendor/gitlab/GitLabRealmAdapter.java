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
package org.miaixz.bus.auth.source.vendor.gitlab;

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
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.SourceServices;
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
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the implementation-neutral GitLab Enterprise realm surface.
 * <p>
 * Snapshot pagination advances through the configured top-level group, descendant groups, direct members, and all
 * visible members. Groups normalize only to organizations; direct and inherited memberships are emitted once with their
 * official access level. Every invocation uses one freshly loaded read-only Token lease, preserves GitLab's official
 * page headers, and closes the lease at the single terminal stage boundary without caching it.
 * </p>
 *
 * @author Kimi Liu
 */
public class GitLabRealmAdapter implements VendorAdapter {

    /**
     * GitLab pagination response header carrying the next page number.
     */
    private static final String NEXT_PAGE_HEADER = "X-Next-Page";

    /**
     * GitLab administrator Token request header.
     */
    private static final String PRIVATE_TOKEN_HEADER = "PRIVATE-TOKEN";

    /**
     * Empty immutable JSON attributes used by projections without declared extensions.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Exact resource categories exposed by GitLab group management.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set.of(Realm.Kind.USER, Realm.Kind.ORGANIZATION);

    /**
     * Ordered management-target closure required from the GitLab Enterprise manifest.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.REALM_USERS,
            Builder.REALM_USER,
            Builder.REALM_ORGANIZATIONS,
            Builder.REALM_ORGANIZATION,
            Builder.REALM_ORGANIZATION_USERS);

    /**
     * Selected immutable GitLab Enterprise Variant.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated GitLab Enterprise deployment options.
     */
    private final GitLabOptions options;

    /**
     * Caller-owned execution services.
     */
    private final SourceServices services;

    /**
     * Resolved official GitLab Enterprise resource targets.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Creates one Source-isolated GitLab Enterprise realm adapter.
     *
     * @param spaceId  Source space used for isolation
     * @param sourceId Source identifier
     * @param manifest exact GitLab manifest
     * @param variant  selected Enterprise Variant
     * @param options  validated Enterprise options
     * @param services capability-limited Source services
     * @throws IllegalArgumentException if an identifier is blank or collaborator is {@code null}
     * @throws ValidateException        if the manifest, Variant, options, protocol, or targets are inconsistent
     */
    public GitLabRealmAdapter(final String spaceId, final String sourceId, final GitLabManifest manifest,
            final VendorManifest.Variant variant, final GitLabOptions options, final SourceServices services) {
        Assert.notBlank(spaceId, "GitLab Enterprise space id must not be blank");
        Assert.notBlank(sourceId, "GitLab Enterprise Source id must not be blank");
        final GitLabManifest selectedManifest = Assert.notNull(manifest, "GitLab manifest must not be null");
        this.variant = Assert.notNull(variant, "GitLab Enterprise Variant must not be null");
        this.options = Assert.notNull(options, "GitLab Enterprise options must not be null");
        this.services = Assert.notNull(services, "GitLab Enterprise services must not be null");
        if (!GitLabManifest.ID.equals(selectedManifest.vendor())
                || !GitLabManifest.ENTERPRISE.equals(this.variant.variant())
                || !selectedManifest.variant(this.variant.variant()).equals(this.variant)
                || this.variant.protocol() != Protocol.HTTPS || !GitLabManifest.ID.equals(this.options.vendor())
                || !GitLabManifest.ENTERPRISE.equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || !this.options.scopes().isEmpty()
                || this.options.credential().type() != org.miaixz.bus.auth.Credential.Type.SHARED_SECRET) {
            throw new ValidateException("GitLab realm adapter requires the frozen management Variant");
        }
        this.services.policies().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (!List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("GitLab Enterprise manifest has an invalid management target set");
        }
    }

    /**
     * Creates one exact GitLab page query.
     *
     * @param page  one-based page
     * @param limit outward page limit
     * @return immutable query map
     */
    private static Map<String, String> query(final int page, final int limit) {
        return Map.of("per_page", Integer.toString(Math.min(limit, Normal._100)), "page", Integer.toString(page));
    }

    /**
     * Extracts and validates the official X-Next-Page value.
     *
     * @param headers response headers
     * @param current current requested page
     * @return next one-based page or zero at natural exhaustion
     */
    private static int nextPage(final FabricX.Headers headers, final int current) {
        Integer selected = null;
        for (String header : headers.values(NEXT_PAGE_HEADER)) {
            final String normalized = StringKit.trim(header);
            if (normalized.isEmpty()) {
                continue;
            }
            final int candidate = positiveDecimal(normalized, "next page");
            if (selected != null && selected != candidate) {
                throw new ValidateException("GitLab response contains conflicting next page values");
            }
            selected = candidate;
        }
        if (selected != null && selected <= current) {
            throw new ValidateException("GitLab response next page does not advance pagination");
        }
        return selected == null ? 0 : selected;
    }

    /**
     * Parses one minimal GitLab user.
     *
     * @param value decoded object
     * @return validated user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        return new User(requiredIdentifier(value, "id"), requiredString(value, "username"),
                requiredString(value, "name"), userState(requiredString(value, "state")));
    }

    /**
     * Parses one minimal GitLab group-backed organization.
     *
     * @param value decoded object
     * @return validated group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        return new Group(requiredIdentifier(value, "id"), requiredString(value, "full_path"),
                fallback(value, "full_name", () -> requiredString(value, "name")),
                optionalIdentifier(value, "parent_id"));
    }

    /**
     * Parses one minimal GitLab group-member projection.
     *
     * @param value decoded object
     * @return validated member projection
     */
    private static Member member(final JsonValue.ObjectValue value) {
        return new Member(user(value), nonNegativeInt(value, "access_level"));
    }

    /**
     * Converts one user projection to a normalized resource.
     *
     * @param user       user projection
     * @param observedAt observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()),
                orderedIdentifier("username", user.username()), user.name(), user.state(), EMPTY_ATTRIBUTES,
                observedAt);
    }

    /**
     * Converts one GitLab group projection to a normalized organization resource.
     *
     * @param group      group projection
     * @param observedAt observation instant
     * @return immutable organization resource
     */
    private static Realm.Resource organizationResource(final Group group, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.ORGANIZATION, group.id()),
                orderedIdentifier("full_path", group.fullPath()), group.name(), Realm.State.UNKNOWN, EMPTY_ATTRIBUTES,
                observedAt);
    }

    /**
     * Creates one child-to-parent organization relation.
     *
     * @param group      child group projection
     * @param observedAt observation instant
     * @return immutable hierarchy relation
     */
    private static Realm.Relation parentRelation(final Group group, final Instant observedAt) {
        return new Realm.Relation(
                new Realm.RelationKey(Realm.RelationKind.PARENT, new Realm.Key(Realm.Kind.ORGANIZATION, group.id()),
                        new Realm.Key(Realm.Kind.ORGANIZATION, group.parentId().getOrNull())),
                EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one normalized user-to-organization membership relation.
     *
     * @param member     validated member projection
     * @param groupId    stable group identifier
     * @param inherited  whether membership is inherited
     * @param observedAt observation instant
     * @return immutable membership relation
     */
    private static Realm.Relation memberRelation(
            final Member member,
            final String groupId,
            final boolean inherited,
            final Instant observedAt) {
        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        attributes.put("access_level", number(member.accessLevel()));
        attributes.put("inherited", new JsonValue.BooleanValue(inherited));
        return new Realm.Relation(
                new Realm.RelationKey(Realm.RelationKind.MEMBER, new Realm.Key(Realm.Kind.USER, member.user().id()),
                        new Realm.Key(Realm.Kind.ORGANIZATION, groupId)),
                new JsonValue.ObjectValue(attributes), observedAt);
    }

    /**
     * Maps one exact GitLab user state to the normalized state closure.
     *
     * @param value exact upstream state
     * @return normalized state
     */
    private static Realm.State userState(final String value) {
        return switch (value) {
            case "active" -> Realm.State.ACTIVE;
            case "blocked", "banned", "deactivated", "ldap_blocked", "blocked_pending_approval" -> Realm.State.INACTIVE;
            default -> Realm.State.UNKNOWN;
        };
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
     * Reads one positive stable GitLab numeric identifier without precision loss.
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
     * Reads an optional stable GitLab numeric identifier.
     *
     * @param object parent object
     * @param name   member name
     * @return canonical identifier when present
     */
    private static Optional<String> optionalIdentifier(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        return Optional.of(requiredIdentifier(object, name));
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
                throw new ValidateException("GitLab " + label + " must be canonical and positive");
            }
            return result;
        } catch (NumberFormatException cause) {
            throw new ValidateException("GitLab " + label + " is invalid", cause);
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
            throw new ValidateException("GitLab cursor contains an invalid member set");
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
                throw new ValidateException("GitLab cursor kind must be numeric");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("GitLab cursor kind is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !result.add(kind)) {
                throw new ValidateException("GitLab cursor kinds are not canonical");
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
        throw new ValidateException("GitLab cursor contains an unsupported kind");
    }

    /**
     * Validates the requested kind closure.
     *
     * @param kinds requested kinds
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("GitLab snapshot contains an unsupported kind");
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
            default -> failed(ErrorCode._500, "GitLab delegated outcome is unsupported");
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
            default -> failed(ErrorCode._500, "GitLab internal outcome cannot be propagated");
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
     * Routes one exact GitLab Enterprise management capability.
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
        Assert.notNull(capability, "GitLab Realm capability must not be null");
        Assert.notNull(context, "GitLab Realm context must not be null");
        Assert.notNull(timeout, "GitLab Realm timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected(ErrorCode._400, "GitLab Realm capability is not declared"));
        }
        if (capability.equals(Realm.DESCRIBE) && request instanceof Realm.Describe) {
            return completed(Outcome.succeeded(capability.responseType().cast(GitLabManifest.realmDescription())));
        }
        if (capability.equals(Realm.SNAPSHOT) && request instanceof Realm.Snapshot value) {
            return narrow(snapshot(value, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.RETRIEVE) && request instanceof Realm.Retrieve value) {
            return narrow(retrieve(value, context, timeout), capability.responseType());
        }
        return completed(rejected(ErrorCode._400, "GitLab Realm request does not match the capability contract"));
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
            return completed(rejected(ErrorCode._400, "GitLab Enterprise snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "GitLab Enterprise snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
        return withSecret(context, timeout, secret -> switch (state.phase()) {
            case ROOT -> root(secret, state, observedAt, timeout);
            case DESCENDANTS -> descendants(secret, request, state, observedAt, timeout);
            case DIRECT_MEMBERS -> members(secret, request, state, observedAt, timeout, false);
            case ALL_MEMBERS -> members(secret, request, state, observedAt, timeout, true);
        });
    }

    /**
     * Reads the configured top-level group as the snapshot root organization.
     *
     * @param secret     open administrator-token lease
     * @param state      root cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized root organization page
     */
    private Outcome<Realm.Page> root(
            final SecretLease secret,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Group> fetched = root(secret, timeout, "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<Group> success)) {
            return propagate(fetched);
        }
        return complete(List.of(organizationResource(success.value(), observedAt)), List.of(), state);
    }

    /**
     * Reads one official descendant-group page and emits child-to-parent relations.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      descendant cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized organizations and hierarchy relations
     */
    private Outcome<Realm.Page> descendants(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Group> root = root(secret, timeout, "snapshot");
        if (!(root instanceof Outcome.Succeeded<Group> rootSuccess)) {
            return propagate(root);
        }
        final Outcome<WirePage<Group>> fetched = descendants(
                rootSuccess.value().id(),
                state.position().start(),
                request.limit(),
                secret,
                timeout,
                "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        final Map<Realm.RelationKey, Realm.Relation> relations = new LinkedHashMap<>();
        for (Group group : success.value().items()) {
            putResource(resources, organizationResource(group, observedAt), "GitLab descendant page");
            if (group.parentId().isPresent()) {
                putRelation(relations, parentRelation(group, observedAt), "GitLab descendant page");
            }
        }
        if (success.value().next() > 0) {
            return output(
                    List.copyOf(resources.values()),
                    List.copyOf(relations.values()),
                    state,
                    Position.page(success.value().next()));
        }
        return complete(List.copyOf(resources.values()), List.copyOf(relations.values()), state);
    }

    /**
     * Reads one direct or inherited group-member page under deterministic group replay.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      membership cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @param inherited  whether the phase emits only inherited members
     * @return normalized users and membership relations
     */
    private Outcome<Realm.Page> members(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout,
            final boolean inherited) {
        final Outcome<ParentPage> replayed = parent(state, request.limit(), secret, timeout, "snapshot");
        if (!(replayed instanceof Outcome.Succeeded<ParentPage> replaySuccess)) {
            return propagate(replayed);
        }
        if (replaySuccess.value().parent().isEmpty()) {
            return complete(List.of(), List.of(), state);
        }
        final Group group = replaySuccess.value().parent().getOrNull();
        final int innerPage = state.position().relationOffset() == 0 ? Normal._1 : state.position().relationOffset();
        final Outcome<Set<String>> direct = inherited ? directIdentifiers(group.id(), secret, timeout, "snapshot")
                : Outcome.succeeded(Set.of());
        if (!(direct instanceof Outcome.Succeeded<Set<String>> directSuccess)) {
            return propagate(direct);
        }
        final Outcome<WirePage<Member>> fetched = memberPage(
                group.id(),
                inherited,
                innerPage,
                request.limit(),
                secret,
                timeout,
                "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Member>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        final Map<Realm.RelationKey, Realm.Relation> relations = new LinkedHashMap<>();
        for (Member member : success.value().items()) {
            if (inherited && directSuccess.value().contains(member.user().id())) {
                continue;
            }
            putResource(resources, userResource(member.user(), observedAt), "GitLab member page");
            putRelation(relations, memberRelation(member, group.id(), inherited, observedAt), "GitLab member page");
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
            return completed(rejected(ErrorCode._400, "GitLab Enterprise retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "GitLab Enterprise retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
        return withSecret(context, timeout, secret -> retrieve(secret, request.key(), observedAt, timeout));
    }

    /**
     * Retrieves one official User or group-backed Organization resource.
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
                : organizationResource(group(success.value()), observedAt);
        if (!key.equals(resource.key())) {
            return failed(ErrorCode._502, "GitLab Enterprise retrieval returned a different stable identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(resource)));
    }

    /**
     * Replays the root and one official descendant page to select the cursor-owned group.
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
        final Outcome<Group> root = root(secret, timeout, operation);
        if (!(root instanceof Outcome.Succeeded<Group> rootSuccess)) {
            return propagate(root);
        }
        final Outcome<WirePage<Group>> descendants = descendants(
                rootSuccess.value().id(),
                state.position().start(),
                limit,
                secret,
                timeout,
                operation);
        if (!(descendants instanceof Outcome.Succeeded<WirePage<Group>> descendantSuccess)) {
            return propagate(descendants);
        }
        final List<Group> groups = new ArrayList<>();
        if (state.position().start() == Normal._1) {
            groups.add(rootSuccess.value());
        }
        groups.addAll(descendantSuccess.value().items());
        if (groups.isEmpty()) {
            if (state.position().parentId().isPresent() || descendantSuccess.value().next() > 0) {
                return failed(ErrorCode._502, "GitLab group replay page is inconsistent");
            }
            return Outcome.succeeded(new ParentPage(groups, -1, descendantSuccess.value().next(), Optional.empty()));
        }
        if (state.position().parentId().isEmpty()) {
            return Outcome.succeeded(
                    new ParentPage(groups, 0, descendantSuccess.value().next(), Optional.of(groups.getFirst())));
        }
        for (int index = 0; index < groups.size(); index++) {
            if (state.position().parentId().getOrNull().equals(groups.get(index).id())) {
                return Outcome.succeeded(
                        new ParentPage(groups, index, descendantSuccess.value().next(),
                                Optional.of(groups.get(index))));
            }
        }
        return failed(ErrorCode._502, "GitLab replay group no longer exists on its official page");
    }

    /**
     * Advances a membership phase to the following group or descendant page.
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
            return completed(failed(ErrorCode._500, "GitLab Enterprise Secret Loader failed before returning a stage"));
        }
        if (loaded == null) {
            return completed(failed(ErrorCode._500, "GitLab Enterprise Secret Loader returned no stage"));
        }
        return loaded.<Outcome<SecretLoader.Record>>handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : failed(ErrorCode._500, "GitLab Enterprise Secret Loader stage failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLoader.Record> success -> execute(success.value(), timeout, operation);
                    case Outcome.Rejected<SecretLoader.Record> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLoader.Record> failed -> completed(Outcome.failed(failed.failure()));
                    default -> completed(
                            failed(ErrorCode._500, "GitLab Enterprise Secret Loader returned an unsupported outcome"));
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
            return completed(failed(ErrorCode._500, "GitLab Enterprise loaded Secret could not be validated"));
        }
        try {
            return CompletableFuture.<Outcome<T>>supplyAsync(() -> {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "GitLab Enterprise operation has no remaining timeout");
                }
                try {
                    return Assert.notNull(operation.apply(secret), "GitLab Enterprise operation returned no outcome");
                } catch (TimeoutException ignored) {
                    return failed(ErrorCode._408, "GitLab Enterprise operation timed out");
                } catch (ValidateException ignored) {
                    return failed(ErrorCode._502, "GitLab Enterprise returned an invalid projection");
                } catch (RuntimeException ignored) {
                    return failed(ErrorCode._503, "GitLab Enterprise transport is unavailable");
                }
            }, services.executor()).whenComplete((ignored, cause) -> secret.close());
        } catch (RuntimeException ignored) {
            secret.close();
            return completed(failed(ErrorCode._500, "GitLab Enterprise operation could not be scheduled"));
        }
    }

    /**
     * Reads the configured top-level GitLab group.
     *
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return validated root group
     */
    private Outcome<Group> root(final SecretLease secret, final Timeout timeout, final String operation) {
        final Outcome<JsonValue.ObjectValue> fetched = get(
                target(Builder.REALM_ORGANIZATIONS).url(),
                secret,
                timeout,
                operation);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        try {
            return Outcome.succeeded(group(success.value()));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "GitLab returned an invalid root group");
        }
    }

    /**
     * Reads one official descendant-group page.
     *
     * @param rootId    stable root group identifier
     * @param page      one-based page
     * @param limit     outward page limit
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return validated descendant page
     */
    private Outcome<WirePage<Group>> descendants(
            final String rootId,
            final int page,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final Url request = url(Builder.REALM_ORGANIZATION, List.of(rootId, "descendant_groups"), query(page, limit));
        return collection(request, page, secret, timeout, operation, GitLabRealmAdapter::group);
    }

    /**
     * Reads one direct or all-members page for a stable group.
     *
     * @param groupId   stable group identifier
     * @param all       whether to use the inherited-inclusive members endpoint
     * @param page      one-based page
     * @param limit     outward page limit
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return validated membership page
     */
    private Outcome<WirePage<Member>> memberPage(
            final String groupId,
            final boolean all,
            final int page,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final List<String> segments = all ? List.of(groupId, "members", "all") : List.of(groupId, "members");
        return collection(
                url(Builder.REALM_ORGANIZATION_USERS, segments, query(page, limit)),
                page,
                secret,
                timeout,
                operation,
                GitLabRealmAdapter::member);
    }

    /**
     * Reads all direct member identifiers required to classify an all-members page.
     *
     * @param groupId   stable group identifier
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return immutable direct member identifier set
     */
    private Outcome<Set<String>> directIdentifiers(
            final String groupId,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final Set<String> identifiers = new LinkedHashSet<>();
        int page = Normal._1;
        while (true) {
            final Outcome<WirePage<Member>> fetched = memberPage(
                    groupId,
                    false,
                    page,
                    Normal._100,
                    secret,
                    timeout,
                    operation);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Member>> success)) {
                return propagate(fetched);
            }
            for (Member member : success.value().items()) {
                identifiers.add(member.user().id());
            }
            if (success.value().next() == 0) {
                return Outcome.succeeded(Set.copyOf(identifiers));
            }
            page = success.value().next();
        }
    }

    /**
     * Reads one JSON-array collection and validates its X-Next-Page continuation.
     *
     * @param url       exact collection URL
     * @param page      current one-based page
     * @param secret    open administrator-token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @param parser    minimal item parser
     * @param <T>       minimal item type
     * @return validated collection page
     */
    private <T> Outcome<WirePage<T>> collection(
            final Url url,
            final int page,
            final SecretLease secret,
            final Timeout timeout,
            final String operation,
            final Function<JsonValue.ObjectValue, T> parser) {
        return request(url, secret, timeout, operation, response -> {
            final List<T> items = new ArrayList<>();
            for (JsonValue value : array(response)) {
                items.add(parser.apply(requiredObject(value, "GitLab collection item")));
            }
            return new WirePage<>(items, nextPage(response.headers(), page));
        });
    }

    /**
     * Executes one GitLab management GET and decodes a successful object.
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
     * Executes one GitLab management GET through an operation-specific bounded response reader.
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
            return failed(ErrorCode._408, "GitLab Enterprise request has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(Protocol.HTTPS, timeout, services.policies()).url(url.toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(PRIVATE_TOKEN_HEADER, new String(secret.material())).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "GitLab Enterprise request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "GitLab Enterprise endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return httpFailure(response, operation);
            }
            return Outcome.succeeded(reader.apply(response));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "GitLab Enterprise returned an invalid response");
        }
    }

    /**
     * Validates and classifies one official GitLab error response.
     *
     * @param response  owned unsuccessful response
     * @param operation safe operation label
     * @param <T>       expected success type
     * @return classified failure or rejection
     */
    private <T> Outcome<T> httpFailure(final Response response, final String operation) {
        final int status = response.code();
        try {
            final JsonValue.ObjectValue error = object(response);
            final JsonValue message = error.values().get("message");
            if (message == null || message instanceof JsonValue.NullValue) {
                throw new ValidateException("GitLab error response omits message");
            }
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "GitLab Enterprise returned an invalid error envelope");
        }
        final Map<String, JsonValue> details = details(operation, status, response.headers());
        details.put(Builder.ERROR_CODE_FIELD, number(status));
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "GitLab Enterprise rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "GitLab Enterprise rejected the administrator token", details);
        }
        if (status == Http.Status.FORBIDDEN || status == Http.Status.NOT_FOUND) {
            return rejected(ErrorCode._403, "GitLab Enterprise visibility or permission is insufficient", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "GitLab Enterprise is rate limited", details);
        }
        return failed(ErrorCode._502, "GitLab Enterprise returned an upstream error", details);
    }

    /**
     * Builds one GitLab URL from a manifest-owned base, encoded segments, and fixed query values.
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
            path.append('/').append(RFC3986.SEGMENT.encode(requireText(segment, "GitLab path segment"), Charset.UTF_8));
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
            throw new ValidateException("GitLab Enterprise manifest omits a required management target");
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
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(GitLabManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(GitLabManifest.ENTERPRISE.value()));
        envelope.put(Builder.OPERATION_FIELD, number(Realm.Operation.SNAPSHOT.code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code));
        final List<JsonValue> kinds = new ArrayList<>();
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        final Map<String, JsonValue> position = new LinkedHashMap<>();
        position.put(Builder.CURSOR_NEXT_FIELD, new JsonValue.StringValue(Integer.toString(state.position().start())));
        if (state.phase() == Phase.DIRECT_MEMBERS || state.phase() == Phase.ALL_MEMBERS) {
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
                    "GitLab cursor");
            exact(
                    envelope,
                    Set.of(
                            Builder.VENDOR_FIELD,
                            Builder.VARIANT_FIELD,
                            Builder.OPERATION_FIELD,
                            Builder.CURSOR_PHASE_FIELD,
                            Builder.CURSOR_KIND_FIELD,
                            Builder.CURSOR_POSITION_FIELD));
            if (!GitLabManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !GitLabManifest.ENTERPRISE.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredInt(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("GitLab cursor context is invalid");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("GitLab cursor kinds do not match the request");
            }
            final JsonValue.ObjectValue raw = requiredObject(envelope, Builder.CURSOR_POSITION_FIELD);
            final Position position;
            if (phase == Phase.ROOT || phase == Phase.DESCENDANTS) {
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
                throw new ValidateException("GitLab cursor is not canonical");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("GitLab cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded response object.
     *
     * @param response successful response
     * @return decoded object
     */
    private JsonValue.ObjectValue object(final Response response) {
        return requiredObject(value(response), "GitLab Enterprise response");
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
            throw new ValidateException("GitLab Enterprise response must be a JSON array");
        }
        return array.values();
    }

    /**
     * Decodes one bounded GitLab JSON response.
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
        values.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(GitLabManifest.ID.value()));
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
     * Defines the finite GitLab snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Reads the configured top-level group.
         */
        ROOT(1, Realm.Kind.ORGANIZATION),

        /**
         * Reads descendant groups.
         */
        DESCENDANTS(2, Realm.Kind.ORGANIZATION),

        /**
         * Reads direct group members.
         */
        DIRECT_MEMBERS(3, Realm.Kind.USER),

        /**
         * Reads inherited-inclusive group members and emits only inherited entries.
         */
        ALL_MEMBERS(4, Realm.Kind.USER);

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
            throw new ValidateException("GitLab cursor phase is unknown");
        }

        /**
         * Returns the following frozen phase.
         *
         * @return following phase or {@code null}
         */
        private Phase next() {
            return switch (this) {
                case ROOT -> DESCENDANTS;
                case DESCENDANTS -> DIRECT_MEMBERS;
                case DIRECT_MEMBERS -> ALL_MEMBERS;
                case ALL_MEMBERS -> null;
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
            phase = Assert.notNull(phase, "GitLab cursor phase must not be null");
            kinds = List.copyOf(Assert.notNull(kinds, "GitLab cursor kinds must not be null"));
            position = Assert.notNull(position, "GitLab cursor position must not be null");
            if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds) || !kinds.contains(phase.kind)) {
                throw new ValidateException("GitLab cursor kinds or phase are invalid");
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
            final Phase initial = ordered.contains(Realm.Kind.ORGANIZATION) ? Phase.ROOT : Phase.DIRECT_MEMBERS;
            return new CursorState(initial, ordered, Position.initial());
        }
    }

    /**
     * Carries one descendant-group page and dependent member-page position.
     *
     * @param start          one-based descendant page
     * @param parentId       current replay group identifier
     * @param relationOffset one-based member page or zero
     * @author Kimi Liu
     */
    private record Position(int start, Optional<String> parentId, int relationOffset) {

        /**
         * Validates one position.
         */
        private Position {
            if (start <= 0 || relationOffset < 0) {
                throw new ValidateException("GitLab cursor position is out of range");
            }
            parentId = Assert.notNull(parentId, "GitLab cursor parent container must not be null");
            parentId = parentId.isPresent()
                    ? Optional.of(requireText(parentId.getOrNull(), "GitLab cursor parent identifier"))
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
         * Creates a dependent group-member position.
         *
         * @param start          official descendant page
         * @param parentId       replay group identifier
         * @param relationOffset member collection page
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
            final boolean memberPhase = phase == Phase.DIRECT_MEMBERS || phase == Phase.ALL_MEMBERS;
            if (!memberPhase && (parentId.isPresent() || relationOffset != 0)
                    || memberPhase && parentId.isPresent() != (relationOffset > 0)) {
                throw new ValidateException("GitLab cursor position does not belong to its phase");
            }
        }
    }

    /**
     * Carries one validated GitLab header-paged collection page.
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
            items = List.copyOf(Assert.notNull(items, "GitLab collection items must not be null"));
            if (next < 0) {
                throw new ValidateException("GitLab collection next page must not be negative");
            }
        }
    }

    /**
     * Carries one replayed group page and selected membership parent.
     *
     * @param items  groups on the official replay page
     * @param index  selected group index or negative at natural exhaustion
     * @param next   following outer page or zero
     * @param parent selected group or empty at natural exhaustion
     * @author Kimi Liu
     */
    private record ParentPage(List<Group> items, int index, int next, Optional<Group> parent) {

        /**
         * Freezes and validates one replay result.
         */
        private ParentPage {
            items = List.copyOf(Assert.notNull(items, "GitLab replay groups must not be null"));
            parent = Assert.notNull(parent, "GitLab replay parent container must not be null");
            if (index < -1 || next < 0 || index >= items.size() || parent.isPresent() != (index >= 0)
                    || parent.isPresent() && !parent.getOrNull().equals(items.get(index))) {
                throw new ValidateException("GitLab replay parent state is inconsistent");
            }
        }
    }

    /**
     * Minimal GitLab user projection.
     *
     * @param id       stable identifier
     * @param username login identifier
     * @param name     display name
     * @param state    normalized state
     * @author Kimi Liu
     */
    private record User(String id, String username, String name, Realm.State state) {

        /**
         * Validates one user.
         */
        private User {
            id = requireText(id, "GitLab user identifier");
            username = requireText(username, "GitLab user name");
            name = requireText(name, "GitLab user display name");
            state = Assert.notNull(state, "GitLab user state must not be null");
        }
    }

    /**
     * Minimal GitLab group projection.
     *
     * @param id       stable identifier
     * @param fullPath stable full path
     * @param name     display name
     * @param parentId optional stable parent identifier
     * @author Kimi Liu
     */
    private record Group(String id, String fullPath, String name, Optional<String> parentId) {

        /**
         * Validates one group.
         */
        private Group {
            id = requireText(id, "GitLab group identifier");
            fullPath = requireText(fullPath, "GitLab group full path");
            name = requireText(name, "GitLab group name");
            parentId = Assert.notNull(parentId, "GitLab group parent container must not be null");
        }
    }

    /**
     * Minimal GitLab membership projection.
     *
     * @param user        stable user projection
     * @param accessLevel official access level
     * @author Kimi Liu
     */
    private record Member(User user, int accessLevel) {

        /**
         * Validates one membership.
         */
        private Member {
            user = Assert.notNull(user, "GitLab member user must not be null");
            if (accessLevel < 0) {
                throw new ValidateException("GitLab member access level must not be negative");
            }
        }
    }

}
