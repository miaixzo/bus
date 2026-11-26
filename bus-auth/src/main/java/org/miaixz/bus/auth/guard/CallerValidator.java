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
package org.miaixz.bus.auth.guard;

import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.guard.GuardEvaluator.Verified;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Selects client credentials and applies caller constraints to verified service identities.
 */
public final class CallerValidator {

    private CallerValidator() {
        // No initialization required.
    }

    /**
     * Selects the first enabled client credential present in the request. Bearer takes precedence over API key.
     *
     * @param headers       client request headers
     * @param bearerEnabled whether to read the Bearer channel
     * @param apiKeyEnabled whether to read the API key channel
     * @return selected credential, or null when neither enabled channel provides one
     */
    public static Http.Auth.Credential credential(
            Map<String, ?> headers,
            boolean bearerEnabled,
            boolean apiKeyEnabled) {
        Http.Auth.Credential bearer = bearerEnabled ? Http.Auth.bearerCredential(headers) : null;
        return bearer == null && apiKeyEnabled ? Http.Auth.apiKeyCredential(headers) : bearer;
    }

    /**
     * Prefers a credential already selected by the caller to the original Bearer header.
     *
     * @param selected credential already attached to the request, if present
     * @param headers  original client headers
     * @return value available to use as signing material, or null when none was provided
     */
    public static String signingCredential(String selected, Map<String, ?> headers) {
        return StringKit.isNotBlank(selected) ? selected : Http.Auth.bearerToken(headers);
    }

    /**
     * Checks a required identity against the selected subject and forwarding constraints.
     *
     * @param caller       verified caller, or null when verification supplied no identity
     * @param subjects     permitted immediate subjects, or an empty set for no additional restriction
     * @param origins      permitted original callers, or an empty set for no additional restriction
     * @param requireAsset whether a verified asset claim is required
     * @return whether the verified caller meets every supplied constraint
     */
    public static boolean matches(Verified caller, Set<String> subjects, Set<String> origins, boolean requireAsset) {
        return caller != null && caller.subject() != null && !caller.subject().isBlank()
                && (subjects.isEmpty() || subjects.contains(caller.subject()))
                && (origins.isEmpty() || caller.origin() != null && origins.contains(caller.origin()))
                && (!requireAsset || caller.asset() != null && !caller.asset().isBlank());
    }

    /**
     * Checks whether the verified original caller matches the expected application code.
     *
     * @param caller         verified caller, or null when no identity was established
     * @param expectedOrigin trusted application code selected by the operation
     * @return whether the verified origin equals the expected code
     */
    public static boolean matchesOrigin(Verified caller, String expectedOrigin) {
        return caller != null && expectedOrigin != null && !expectedOrigin.isBlank()
                && expectedOrigin.equals(caller.origin());
    }

    /**
     * Checks the subject and audience of a direct service call.
     *
     * @param caller  verified service identity
     * @param service expected subject and audience
     * @return whether the caller is that service without a forwarded origin
     */
    public static boolean matchesDirect(Verified caller, String service) {
        return caller != null && service != null && service.equals(caller.subject())
                && service.equals(caller.audience()) && caller.origin() == null;
    }

}
