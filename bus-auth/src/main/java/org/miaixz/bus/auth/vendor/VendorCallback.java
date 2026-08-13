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

import java.net.URI;
import java.util.Optional;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Package-private vendor client view of the root inbound callback. Authorization codes and state values are never
 * rendered, and this view never appears in the public {@link VendorProvider} contract.
 *
 * @param source     immutable root callback
 * @param codeValue  optional authorization code
 * @param stateValue optional anti-forgery state
 * @param problem    optional remote authorization problem
 * @author Kimi Liu
 */
record VendorCallback(Callback.Inbound source, String codeValue, String stateValue, Callback.Problem problem) {

    /**
     * Validates the required root callback.
     *
     * @throws ValidateException if {@code source} is null
     */
    VendorCallback {
        if (source == null) {
            throw new ValidateException("Vendor callback source must not be null");
        }
    }

    /**
     * Extracts canonical code, state, and problem values from one root callback.
     *
     * @param source immutable inbound callback
     * @return package-private vendor callback view
     * @throws ValidateException if the callback is null, has conflicting success and error values, duplicates a
     *                           parameter, or contains an invalid error URI
     */
    static VendorCallback from(final Callback.Inbound source) {
        if (source == null) {
            throw new ValidateException("Vendor callback source must not be null");
        }
        final String code = source.parameters().unique("code", "auth_code", "authorization_code").orElse(null);
        final String error = source.parameters().single("error").orElse(null);
        if (code != null && (error != null || source.problem() != null)) {
            throw new ValidateException("Vendor callback cannot contain both a code and problem");
        }
        Callback.Problem problem = source.problem();
        if (problem == null && error != null) {
            try {
                problem = new Callback.Problem(error, source.parameters().single("error_description").orElse(""),
                        source.parameters().single("error_uri").map(URI::create).orElse(null));
            } catch (final IllegalArgumentException failure) {
                throw new ValidateException("Vendor callback error URI is invalid", failure);
            }
        }
        return new VendorCallback(source, code, source.parameters().single("state").orElse(null), problem);
    }

    /**
     * Returns the optional authorization code.
     *
     * @return optional sensitive code
     */
    Optional<String> code() {
        return Optional.ofNullable(codeValue);
    }

    /**
     * Returns the optional anti-forgery state.
     *
     * @return optional sensitive state
     */
    Optional<String> state() {
        return Optional.ofNullable(stateValue);
    }

    /**
     * Returns one optional unique root callback parameter.
     *
     * @param name parameter name
     * @return optional parameter value
     * @throws ValidateException if the name is invalid or the parameter is duplicated
     */
    Optional<String> value(final String name) {
        return source.parameters().single(name);
    }

    /**
     * Returns a fixed representation that excludes codes, state, parameters, and problem details.
     *
     * @return redacted representation
     */
    @Override
    public String toString() {
        return "VendorCallback[REDACTED]";
    }

}
