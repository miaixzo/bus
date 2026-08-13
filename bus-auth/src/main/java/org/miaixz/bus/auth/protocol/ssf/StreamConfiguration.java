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
package org.miaixz.bus.auth.protocol.ssf;

import java.net.URI;
import java.time.Duration;

import org.miaixz.bus.auth.guard.UriValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;

/**
 * Immutable push or poll stream configuration.
 *
 * @param mode              delivery mode
 * @param endpoint          HTTPS push endpoint or poll collection endpoint
 * @param policy            Fabric-only delivery policy
 * @param maximumAttempts   total delivery attempts
 * @param maximumRetryAfter maximum accepted Retry-After delay
 * @author Kimi Liu
 */
public record StreamConfiguration(Mode mode, URI endpoint, DeliveryPolicy policy, int maximumAttempts,
        Duration maximumRetryAfter) {

    /**
     * Validates stream configuration.
     *
     * @param mode              delivery mode
     * @param endpoint          HTTPS endpoint
     * @param policy            Fabric-only delivery policy
     * @param maximumAttempts   total delivery attempts
     * @param maximumRetryAfter maximum accepted Retry-After delay
     * @throws ValidateException if a required value, HTTPS route, retry count, or Retry-After bound is invalid
     * @throws ProtocolException if the endpoint violates the direct Fabric address policy
     */
    public StreamConfiguration {
        mode = Assert.notNull(mode, () -> new ValidateException("SSF stream mode must not be null"));
        policy = Assert.notNull(policy, () -> new ValidateException("SSF transport policy must not be null"));
        endpoint = UriValidator.https(UriValidator.transport(endpoint, policy.addressPolicy()));
        Assert.isTrue(
                maximumAttempts > Normal._0 && maximumAttempts <= Normal._16,
                () -> new ValidateException("SSF maximum attempts is invalid"));
        maximumRetryAfter = Assert
                .notNull(maximumRetryAfter, () -> new ValidateException("SSF maximum Retry-After must not be null"));
        Assert.isTrue(
                !maximumRetryAfter.isNegative() && !maximumRetryAfter.isZero()
                        && maximumRetryAfter.compareTo(Duration.ofHours(1)) <= 0,
                () -> new ValidateException("SSF maximum Retry-After is invalid"));
    }

    /**
     * Delivery mode.
     *
     * @author Kimi Liu
     */
    public enum Mode {
        /**
         * HTTP push delivery.
         */
        PUSH,
        /**
         * Product-owned polling storage.
         */
        POLL
    }

    /**
     * Immutable Fabric route and timeout policy used for SSF delivery. Credential material is deliberately excluded and
     * remains owned by the installed Fabric context; event contents remain owned by {@link SSF.Event}.
     *
     * @param addressPolicy HTTPS destination policy
     * @param timeout       bounded Fabric operation timeouts
     * @author Kimi Liu
     */
    public record DeliveryPolicy(AddressPolicy addressPolicy, Timeout timeout) {

        /**
         * Validates the direct Fabric policy and restricts SSF delivery to HTTPS.
         *
         * @throws ValidateException if a policy is null or allows a non-HTTPS scheme
         */
        public DeliveryPolicy {
            addressPolicy = Assert
                    .notNull(addressPolicy, () -> new ValidateException("SSF address policy must not be null"));
            timeout = Assert.notNull(timeout, () -> new ValidateException("SSF timeout must not be null"));
            Assert.isTrue(
                    addressPolicy.allowedSchemes().equals(java.util.Set.of(Protocol.HTTPS)),
                    () -> new ValidateException("SSF delivery policy must allow only HTTPS"));
        }
    }

}
