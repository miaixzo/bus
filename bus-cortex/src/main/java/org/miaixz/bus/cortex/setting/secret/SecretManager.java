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
package org.miaixz.bus.cortex.setting.secret;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Authorizes read-only Secret access and binds backend reads to connection generations. Cache storage, capacity and TTL
 * belong to Bus MemoryCache; fixed resource guards coordinate reads and invalidation. An injected cache must be empty
 * and exclusively owned by this manager. Closing drains admitted guarded reads and erases cached materials.
 */
public final class SecretManager implements AutoCloseable {

    /**
     * Upper character count admitted per cache entry. Larger valid backend values are returned without caching. Total
     * entry capacity remains the supplied MemoryCache's responsibility.
     */
    private static final int MAX_CACHED_CHARACTERS = 8192;

    /**
     * Insert-only tenant connection bindings used after policy authorization.
     */
    private final Map<ConnectionKey, Binding> bindings = new ConcurrentHashMap<>();
    /**
     * Mandatory application authorization policy evaluated for every operation.
     */
    private final AccessPolicy policy;
    /**
     * Dedicated Bus memory cache of complete owned values; null disables caching. This manager owns its lifecycle.
     */
    private final MemoryCache<String, SecretMaterial> cache;
    /**
     * Maximum configured cache lifetime before refresh or reload.
     */
    private final Duration ttl;
    /**
     * Lead time before a cached value must be refreshed synchronously.
     */
    private final Duration refreshBefore;
    /**
     * Margin subtracted from a known backend expiration before cache admission.
     */
    private final Duration expirySafetyMargin;
    /**
     * Fixed resource-lock stripes; avoid retaining an unbounded lock per user-supplied reference.
     */
    private final ReentrantLock[] guards = IntStream.range(0, 64).mapToObj(index -> new ReentrantLock())
            .toArray(ReentrantLock[]::new);
    /**
     * Terminal manager lifecycle flag, visible across caller threads.
     */
    private volatile boolean closed;

    /**
     * Creates SecretManager with explicitly bound collaborators. Dependencies are not discovered implicitly.
     *
     * @param policy mandatory policy evaluated before each operation, including cache hits
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public SecretManager(AccessPolicy policy) {
        this(policy, null);
    }

    /**
     * Creates SecretManager with explicitly bound collaborators. Dependencies are not discovered implicitly.
     *
     * @param policy mandatory policy evaluated before each operation, including cache hits
     * @param cache  empty, dedicated Bus memory cache owned and closed by this manager; null disables caching
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public SecretManager(AccessPolicy policy, MemoryCache<String, SecretMaterial> cache) {
        this(policy, cache, Duration.ofSeconds(60), Duration.ofSeconds(10), Duration.ofSeconds(5));
    }

    /**
     * Creates SecretManager with explicitly bound collaborators. Dependencies are not discovered implicitly.
     *
     * @param policy             mandatory policy evaluated before each operation, including cache hits
     * @param cache              empty, dedicated Bus memory cache owned and closed by this manager; null disables
     *                           caching
     * @param ttl                positive upper bound on the cache lifetime, at least one millisecond
     * @param refreshBefore      non-negative refresh lead time, strictly shorter than ttl
     * @param expirySafetyMargin non-negative margin subtracted from the backend expiration
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public SecretManager(AccessPolicy policy, MemoryCache<String, SecretMaterial> cache, Duration ttl,
            Duration refreshBefore, Duration expirySafetyMargin) {
        this.policy = Objects.requireNonNull(policy);
        this.cache = cache;
        this.ttl = Objects.requireNonNull(ttl);
        this.refreshBefore = Objects.requireNonNull(refreshBefore);
        this.expirySafetyMargin = Objects.requireNonNull(expirySafetyMargin);
        if (ttl.toMillis() < 1 || refreshBefore.isNegative() || refreshBefore.compareTo(ttl) >= 0
                || expirySafetyMargin.isNegative()) {
            throw new IllegalArgumentException("Invalid secret cache timing");
        }
        if (cache != null) {
            if (cache.estimatedSize() != 0) {
                throw new IllegalArgumentException("Secret cache must be empty and exclusively owned");
            }
            cache.setRemovalListener((key, value) -> value.close());
        }
    }

    /**
     * Builds a collision-free identity/resource cache prefix using encoded components. It includes no secret payload
     * and isolates tenant, application and principal.
     *
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return resource-scope cache prefix
     */
    private static String scope(SecretReference ref, SecretContext context) {
        return Stream.of(context.tenantId(), context.appId(), context.principal(), ref.connectionId(), ref.resource())
                .map(UrlEncoder::encodeAll).collect(Collectors.joining("/", "", "/"));
    }

    /**
     * Checks the trusted deadline and current access policy, then resolves an enabled tenant connection and verifies
     * its resource prefixes. Denial also invalidates the current cache scope.
     *
     * @param action  operation to authorize, such as read, describe, refresh, or invalidate
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return authorized connection/provider binding
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private Binding binding(String action, SecretReference ref, SecretContext context) {
        Objects.requireNonNull(ref);
        Objects.requireNonNull(context).checkDeadline();
        ensureOpen();
        try {
            policy.requireAllowed(action, ref, context);
        } catch (RuntimeException failure) {
            discardAfterFailure(ref, context);
            throw (ExceptionKit.getThrowableList(failure).stream().anyMatch(ForbiddenException.class::isInstance)
                    ? new ForbiddenException("Secret access denied")
                    : new CryptoException("Secret operation failed"));
        }
        var binding = requireBinding(context.tenantId(), ref.connectionId());
        if (!binding.connection().allows(ref.resource())) {
            throw new ForbiddenException("Secret access denied");
        }
        return binding;
    }

    /**
     * Authorizes metadata inspection and returns only advertised capabilities, without contacting the backend or
     * exposing resource paths.
     *
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return immutable copy of the backend read capabilities
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    public Set<SecretProvider.Capability> describe(SecretReference ref, SecretContext context) {
        var binding = binding("describe", ref, context);
        var capabilities = capabilities(binding);
        context.checkDeadline();
        ensureOpen();
        return capabilities;
    }

    /**
     * Resolves an authorized backend value and, when requested, extracts one top-level JSON string field. Authorization
     * is performed on every call. The returned material is independent and must be closed by the caller.
     *
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return caller-owned material containing the requested value
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    public SecretMaterial resolve(SecretReference ref, SecretContext context) {
        var binding = binding("read", ref, context);
        ReentrantLock lock = guard(scope(ref, context), context);
        try {
            return resolveValue(binding, ref, context);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resolves and projects one authorized value while the caller holds its resource guard. Keeping the guard through
     * extraction makes close wait for admitted reads even when caching is disabled.
     *
     * @param binding authorized immutable connection/provider binding
     * @param ref     requested backend value or JSON field
     * @param context trusted identity and absolute deadline
     * @return independent caller-owned material
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if the backend denies access
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if a deadline, lifecycle or payload check fails
     */
    private SecretMaterial resolveValue(Binding binding, SecretReference ref, SecretContext context) {
        var needed = ref.version() != null ? SecretProvider.Capability.READ_VERSION
                : SecretProvider.Capability.READ_CURRENT;
        if (!capabilities(binding).contains(needed)) {
            throw new CryptoException("Secret operation or payload is not supported");
        }
        SecretReference whole = new SecretReference(ref.connectionId(), ref.resource(), ref.version(), null,
                ref.expectedType());
        try (SecretMaterial value = cache == null ? read(binding, whole, context) : cached(binding, whole, context)) {
            context.checkDeadline();
            char[] copy = value.copy();
            try {
                if (ref.field() == null) {
                    context.checkDeadline();
                    ensureOpen();
                    return new SecretMaterial(copy, value.resolvedVersion(), value.fetchedAt(), value.expiresAt(),
                            value.connectionGeneration());
                }
                byte[] json = new String(copy).getBytes(StandardCharsets.UTF_8);
                Object parsed;
                try {
                    parsed = JsonKit.toJavaValue(JsonKit.readValue(json, 32, true));
                } finally {
                    Arrays.fill(json, (byte) 0);
                }
                if (!(parsed instanceof Map<?, ?> fields)) {
                    throw new CryptoException("Invalid secret reference");
                }
                Object selected = fields.get(ref.field());
                if (!(selected instanceof String text)) {
                    throw new CryptoException("Invalid secret reference");
                }
                char[] extracted = text.toCharArray();
                try {
                    context.checkDeadline();
                    ensureOpen();
                    return new SecretMaterial(extracted, value.resolvedVersion(), value.fetchedAt(), value.expiresAt(),
                            value.connectionGeneration());
                } finally {
                    Arrays.fill(extracted, '\0');
                }
            } finally {
                Arrays.fill(copy, '\0');
            }
        } catch (UncheckedException failure) {
            discardAfterFailure(ref, context);
            throw (ExceptionKit.getThrowableList(failure).stream().anyMatch(ForbiddenException.class::isInstance)
                    ? new ForbiddenException("Secret access denied")
                    : new CryptoException("Secret operation failed"));
        } catch (RuntimeException failure) {
            discardAfterFailure(ref, context);
            throw (ExceptionKit.getThrowableList(failure).stream().anyMatch(ForbiddenException.class::isInstance)
                    ? new ForbiddenException("Secret access denied")
                    : new CryptoException("Secret operation failed"));
        }
    }

    /**
     * Copies advertised capabilities through the same sanitized boundary as other provider calls. A malformed custom
     * provider must not expose arbitrary diagnostics through describe or pre-read capability checks.
     *
     * @param binding authorized connection/provider binding
     * @return immutable non-null capability set
     * @throws org.miaixz.bus.core.lang.exception.UncheckedException carrying only only a fixed, non-sensitive message
     */
    private Set<SecretProvider.Capability> capabilities(Binding binding) {
        try {
            return Set.copyOf(binding.provider().capabilities());
        } catch (RuntimeException failure) {
            throw (ExceptionKit.getThrowableList(failure).stream().anyMatch(ForbiddenException.class::isInstance)
                    ? new ForbiddenException("Secret access denied")
                    : new CryptoException("Secret operation failed"));
        }
    }

    /**
     * Reads the complete backend payload using fresh credentials per HTTP attempt. Validates the actual version and
     * lifetime before returning; field extraction belongs to the manager.
     *
     * @param binding authorized connection and provider with matching configuration generation
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return caller-owned material with the actual backend version
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private SecretMaterial read(Binding binding, SecretReference ref, SecretContext context) {
        context.checkDeadline();
        ensureOpen();
        try (SecretMaterial value = binding.provider().read(ref, context)) {
            context.checkDeadline();
            if (value.connectionGeneration() != binding.connection().generation()) {
                throw new CryptoException("Conflicting secret configuration or lifecycle");
            }
            if (ref.version() != null && !ref.version().equals(value.resolvedVersion())) {
                throw new CryptoException("Requested secret version was not returned");
            }
            char[] chars = value.copy();
            try {
                return new SecretMaterial(chars, value.resolvedVersion(), value.fetchedAt(), value.expiresAt(),
                        value.connectionGeneration());
            } finally {
                Arrays.fill(chars, '\0');
            }
        } catch (UncheckedException failure) {
            throw (ExceptionKit.getThrowableList(failure).stream().anyMatch(ForbiddenException.class::isInstance)
                    ? new ForbiddenException("Secret access denied")
                    : new CryptoException("Secret operation failed"));
        } catch (RuntimeException failure) {
            throw (ExceptionKit.getThrowableList(failure).stream().anyMatch(ForbiddenException.class::isInstance)
                    ? new ForbiddenException("Secret access denied")
                    : new CryptoException("Secret operation failed"));
        }
    }

    /**
     * Authorizes refresh, invalidates this identity/resource scope, and resolves a fresh value through the same
     * authorization and version checks as an ordinary read.
     *
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return fresh caller-owned material, which must be closed
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    public SecretMaterial refresh(SecretReference ref, SecretContext context) {
        binding("refresh", ref, context);
        if (cache != null) {
            invalidateCached(ref, context);
        }
        return resolve(ref, context);
    }

    /**
     * Authorizes invalidation and discards all cached versions of this resource for the current tenant, application and
     * principal.
     *
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    public void invalidate(SecretReference ref, SecretContext context) {
        binding("invalidate", ref, context);
        if (cache != null) {
            invalidateCached(ref, context);
        }
    }

    /**
     * Rejects new operations after the manager lifecycle has been closed.
     *
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the manager has been closed
     */
    private void ensureOpen() {
        if (closed) {
            throw new CryptoException("Secret resource is closed");
        }
    }

    /**
     * Acquires the fixed stripe for one identity/resource scope within the caller deadline. The caller must unlock the
     * returned guard; interruption restores the thread flag.
     *
     * @param scope   encoded tenant/application/principal/resource prefix selecting the fixed guard stripe
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return locked resource coordination guard
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private ReentrantLock guard(String scope, SecretContext context) {
        context.checkDeadline();
        ReentrantLock lock = guards[Math.floorMod(scope.hashCode(), guards.length)];
        try {
            long remaining = Duration.between(Instant.now(), context.deadline()).toNanos();
            if (remaining <= 0 || !lock.tryLock(remaining, TimeUnit.NANOSECONDS)) {
                throw new CryptoException("Secret operation timed out");
            }
            ensureOpen();
            return lock;
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new CryptoException("Secret operation timed out");
        } catch (RuntimeException failure) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            throw failure;
        }
    }

    /**
     * Resolves through the dedicated Bus MemoryCache while coordinating same-resource reads and invalidation. Refresh
     * checks precede hits; near-expiry values are never used as stale fallback. Oversized values bypass cache storage.
     * The enclosing resolution holds the resource guard through this method and subsequent field extraction.
     *
     * @param binding authorized connection and provider with matching configuration generation
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return independent caller-owned material
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private SecretMaterial cached(Binding binding, SecretReference ref, SecretContext context) {
        String scope = scope(ref, context);
        String key = scope + binding.connection().generation() + "/"
                + UrlEncoder.encodeAll(ref.version() == null ? "" : ref.version());
        SecretMaterial hit = cache.read(key);
        if (hit != null) {
            Instant refreshAt = hit.fetchedAt().plus(ttl).minus(refreshBefore);
            if (hit.expiresAt() != null) {
                Instant actualRefresh = hit.expiresAt().minus(expirySafetyMargin).minus(refreshBefore);
                if (actualRefresh.isBefore(refreshAt)) {
                    refreshAt = actualRefresh;
                }
            }
            long jitterWindow = Math.max(1, ttl.toMillis() / 20);
            refreshAt = refreshAt.minusMillis(Math.floorMod(key.hashCode(), jitterWindow));
            if (Instant.now().isBefore(refreshAt)) {
                try {
                    char[] chars = hit.copy();
                    try {
                        return new SecretMaterial(chars, hit.resolvedVersion(), hit.fetchedAt(), hit.expiresAt(),
                                hit.connectionGeneration());
                    } finally {
                        Arrays.fill(chars, '\0');
                    }
                } catch (UncheckedException ignored) {
                    // Expiration or concurrent capacity eviction requires a fresh backend read.
                }
            }
            cache.remove(key);
        }
        try (SecretMaterial loaded = read(binding, ref, context)) {
            ensureOpen();
            context.checkDeadline();
            Instant expiry = Instant.now().plus(ttl);
            if (loaded.expiresAt() != null) {
                Instant actual = loaded.expiresAt().minus(expirySafetyMargin);
                if (actual.isBefore(expiry)) {
                    expiry = actual;
                }
            }
            long millis = Duration.between(Instant.now(), expiry).toMillis();
            if (millis < 1) {
                throw new CryptoException("Secret value has expired");
            }
            char[] chars = loaded.copy();
            try {
                SecretMaterial result = new SecretMaterial(chars, loaded.resolvedVersion(), loaded.fetchedAt(),
                        loaded.expiresAt(), loaded.connectionGeneration());
                // Keep large payloads out of the shared cache; normal resolution still returns their full value.
                if (chars.length <= MAX_CACHED_CHARACTERS) {
                    SecretMaterial stored = new SecretMaterial(chars, loaded.resolvedVersion(), loaded.fetchedAt(),
                            loaded.expiresAt(), loaded.connectionGeneration());
                    try {
                        cache.write(key, stored, millis);
                    } catch (RuntimeException failure) {
                        stored.close();
                        result.close();
                        throw failure;
                    }
                }
                return result;
            } finally {
                Arrays.fill(chars, '\0');
            }
        }
    }

    /**
     * Attempts scoped cache cleanup without replacing the original failure. A denied request must remain denied even if
     * its deadline expires while another reader holds the resource guard. Every future read still reauthorizes.
     *
     * @param ref     failed backend reference
     * @param context original trusted identity and deadline
     */
    private void discardAfterFailure(SecretReference ref, SecretContext context) {
        if (cache == null || closed) {
            return;
        }
        String prefix = scope(ref, context);
        ReentrantLock lock = guards[Math.floorMod(prefix.hashCode(), guards.length)];
        if (!lock.tryLock()) {
            return;
        }
        try {
            removeScope(prefix);
        } catch (RuntimeException ignored) {
            // Preserve the original failure. Busy entries may remain until TTL, but every read reauthorizes.
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes all cached selectors and generations in one identity/resource scope under its guard. In-flight reads
     * finish before invalidation returns, preventing later publication of their old value.
     *
     * @param ref     backend reference within the authenticated caller scope
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     */
    private void invalidateCached(SecretReference ref, SecretContext context) {
        String prefix = scope(ref, context);
        ReentrantLock lock = guard(prefix, context);
        try {
            removeScope(prefix);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes one scope from the native Bus cache after its caller has acquired the matching resource guard. Removal
     * callbacks erase each owned value; the trailing separator in the prefix prevents matching adjacent resources.
     *
     * @param prefix encoded identity/resource prefix, including its terminal separator
     */
    private void removeScope(String prefix) {
        for (String key : cache.getNativeCache().keySet()) {
            if (key.startsWith(prefix)) {
                cache.remove(key);
            }
        }
    }

    /**
     * Marks the manager closed, drains admitted resource operations, and closes its exclusively owned Bus memory cache.
     * Cache removal callbacks erase retained materials. Repeated calls are safe.
     */
    @Override
    public void close() {
        synchronized (bindings) {
            closed = true;
        }
        for (ReentrantLock lock : guards) {
            lock.lock();
        }
        try {
            if (cache != null) {
                cache.close();
            }
        } finally {
            for (int index = guards.length - 1; index >= 0; index--) {
                guards[index].unlock();
            }
        }
    }

    /**
     * Registers one tenant/connection binding atomically. A duplicate is rejected; replacing a connection requires a
     * new manager lifecycle. Publication is serialized with the close transition, so a closing manager cannot accept a
     * late binding after validation.
     *
     * @param connection immutable routing and resource-prefix restrictions
     * @param provider   provider instance whose type must match the connection
     * @throws CryptoException if connection registration conflicts with existing configuration or lifecycle
     */
    public void register(SecretConnection connection, SecretProvider provider) {
        Binding binding = new Binding(connection, provider);
        synchronized (bindings) {
            ensureOpen();
            if (bindings.putIfAbsent(new ConnectionKey(connection.tenantId(), connection.id()), binding) != null) {
                throw new CryptoException("Conflicting secret configuration or lifecycle");
            }
        }
    }

    /**
     * Looks up an enabled connection within one tenant. Missing and disabled connections both throw ForbiddenException
     * to avoid revealing their existence.
     *
     * @param tenantId authenticated tenant identifier
     * @param id       connection identifier scoped to the tenant
     * @return enabled immutable connection/provider binding
     * @throws ForbiddenException if no enabled binding exists for the tenant and connection
     */
    private Binding requireBinding(String tenantId, String id) {
        Binding binding = bindings.get(new ConnectionKey(tenantId, id));
        if (binding == null || !binding.connection().enabled()) {
            throw new ForbiddenException("Secret access denied");
        }
        return binding;
    }

    /**
     * Mandatory authorization boundary for Secret operations. The trusted identity context is independent of persisted
     * references. Policy evaluation precedes connection lookup and occurs even when a backend value is already cached.
     */
    @FunctionalInterface
    public interface AccessPolicy {

        /**
         * Authorizes the requested operation before connection lookup or a cached value is returned. Implementations
         * must throw ForbiddenException when access is denied; returning normally grants access.
         *
         * @param action    operation to authorize, such as read, describe, refresh, or invalidate
         * @param reference backend reference; field extraction is performed by the manager
         * @param context   authenticated caller identity and absolute request deadline; never populated from an
         *                  untrusted reference
         * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
         *                                                               representation is not permitted
         * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
         *                                                               checks fail; inspect the exception type;
         *                                                               diagnostics are deliberately sanitized
         */
        void requireAllowed(String action, SecretReference reference, SecretContext context);
    }

    /**
     * Immutable tenant/connection lookup key. It deliberately excludes caller-controlled backend details.
     *
     * @param tenantId authenticated tenant identifier
     * @param id       connection identifier scoped to the tenant
     */
    private record ConnectionKey(String tenantId, String id) {
    }

    /**
     * Immutable validated pairing of routing restrictions and a provider of the matching backend type.
     *
     * @param connection immutable routing configuration and prefix restrictions
     * @param provider   provider instance whose type must match the connection
     */
    private record Binding(SecretConnection connection, SecretProvider provider) {

        /**
         * Validates the record components before publishing this immutable value.
         *
         * @param connection immutable routing configuration and prefix restrictions
         * @param provider   provider instance whose type must match the connection
         * @throws IllegalArgumentException if component values violate this record's constraints
         * @throws NullPointerException     if a required component or collection is null
         */
        public Binding {
            Objects.requireNonNull(connection);
            Objects.requireNonNull(provider);
            if (!connection.provider().equals(provider.type())) {
                throw new IllegalArgumentException("Secret provider type mismatch");
            }
        }
    }

}
