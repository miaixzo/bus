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
package org.miaixz.bus.auth.vendor;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.guard.ReplayKey;
import org.miaixz.bus.auth.guard.UriValidator;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;

/**
 * Stateless validation boundary shared by third-party authentication clients.
 *
 * <p>
 * The utility delegates URI policy to bus-auth/bus-fabric and performs state consumption through the injected
 * tenant-aware atomic store. It owns no cache, clock, transport, or mutable global state.
 * </p>
 *
 * @author Kimi Liu
 */
public final class VendorValidator {

    /**
     * Prevents instantiation of the stateless validation utility.
     */
    private VendorValidator() {
        // No initialization required.
    }

    /**
     * Validates one immutable vendor client registration against its metadata definition.
     *
     * @param registration immutable vendor registration containing only a secret reference
     * @param definition   immutable vendor endpoint and factory definition
     * @throws AuthorizedException  if required client or vendor-specific identifiers are absent
     * @throws ValidateException    if the vendor metadata is invalid
     * @throws NullPointerException if either input is null
     */
    public static void validateRegistration(final VendorRegistration registration, final VendorDefinition definition) {
        final VendorRegistration current = Objects.requireNonNull(registration, "Vendor registration must not be null");
        final VendorDefinition vendor = Objects.requireNonNull(definition, "Vendor definition must not be null");
        final String id = vendor.descriptor().id();
        if (StringKit.isEmpty(current.clientId())) {
            throw new AuthorizedException(VendorErrors._110010);
        }
        if (StringKit.isEmpty(current.secretId())) {
            throw new AuthorizedException(VendorErrors._110011);
        }
        if (("alipay".equals(id) || "stack_overflow".equals(id) || "wechat_ee".equals(id))
                && StringKit.isEmpty(current.unionId())) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        if (("coding".equals(id) || "okta".equals(id)) && StringKit.isEmpty(current.prefix())) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        if ("ximalaya".equals(id)) {
            if (StringKit.isEmpty(current.deviceId()) || StringKit.isEmpty(current.type())
                    || !Symbol.THREE.equals(current.type()) && StringKit.isEmpty(current.unionId())) {
                throw new AuthorizedException(VendorErrors._110004);
            }
        }
    }

    /**
     * Validates the context's local callback endpoint against the injected Fabric address policy.
     *
     * @param context immutable authentication operation context
     * @param policy  closed Fabric address scheme, port, and network policy
     * @throws AuthorizedException                                  if the context has no local callback endpoint
     * @throws org.miaixz.bus.core.lang.exception.ProtocolException if the URI violates the address policy
     * @throws NullPointerException                                 if an argument is null
     */
    public static void validateRedirect(final Context context, final AddressPolicy policy) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final AddressPolicy rules = Objects.requireNonNull(policy, "Address policy must not be null");
        UriValidator.transport(
                current.localEndpoint().orElseThrow(() -> new AuthorizedException(VendorErrors._110003)).address()
                        .toUri(),
                rules);
    }

    /**
     * Validates a root inbound callback for the selected vendor without exposing callback values in diagnostics.
     *
     * @param callback   immutable inbound callback
     * @param definition selected vendor definition
     * @throws AuthorizedException  if the vendor reported a problem or the required authorization code is absent
     * @throws ValidateException    if callback parameter cardinality is invalid
     * @throws NullPointerException if an argument is null
     */
    public static void validateCallback(final Callback.Inbound callback, final VendorDefinition definition) {
        final Callback.Inbound current = Objects.requireNonNull(callback, "Callback must not be null");
        final VendorDefinition vendor = Objects.requireNonNull(definition, "Vendor definition must not be null");
        final String id = vendor.descriptor().id();
        if (current.problem() != null) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        if ("twitter".equals(id)) {
            return;
        }
        final String parameter = "alipay".equals(id) ? "auth_code"
                : "huawei".equals(id) ? "authorization_code" : "code";
        final String code = current.value(parameter).orElse(null);
        if (StringKit.isEmpty(code)) {
            throw new AuthorizedException(VendorErrors._110005);
        }
    }

    /**
     * Atomically consumes one tenant-isolated OAuth state value.
     *
     * <p>
     * The raw state never becomes a store key or diagnostic value. The returned stage completes exceptionally with the
     * registered illegal-state error when the state is missing, forged, expired, or already consumed.
     * </p>
     *
     * @param context immutable authentication operation context supplying the tenant
     * @param state   opaque callback state value
     * @param store   injected tenant-aware atomic state store
     * @return non-null asynchronous completion after one successful atomic consumption
     * @throws AuthorizedException  if {@code state} is null or blank
     * @throws NullPointerException if {@code context}, {@code store}, or the returned store stage is null
     */
    public static CompletionStage<Void> consumeState(
            final Context context,
            final String state,
            final StateStore store) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final StateStore states = Objects.requireNonNull(store, "State store must not be null");
        if (StringKit.isEmpty(state)) {
            throw new AuthorizedException(VendorErrors._110006);
        }
        final String key = ReplayKey.derive(current.tenantId(), "oauth2", "state", state);
        return Objects.requireNonNull(states.take(current, key), "State-store take stage must not be null")
                .thenApply(value -> {
                    if (value == null || value.isEmpty()) {
                        throw new AuthorizedException(VendorErrors._110006);
                    }
                    return null;
                });
    }

}
