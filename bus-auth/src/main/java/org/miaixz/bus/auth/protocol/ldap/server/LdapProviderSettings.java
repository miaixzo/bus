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
package org.miaixz.bus.auth.protocol.ldap.server;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.miaixz.bus.auth.protocol.ldap.SaslCredentials;
import org.miaixz.bus.auth.provider.ProviderSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds immutable deployment limits and advertised authentication mechanisms for one LDAPv3 Provider.
 * <p>
 * The settings declare which Bind choices and the standard StartTLS extended operation are available. They contain
 * neither directory data nor connection authentication state; the external {@link DirectoryStore} owns both at runtime.
 * The Provider driver additionally requires the message limit not to exceed the shared LDAP security baseline.
 * </p>
 *
 * @param anonymousBindSupported   whether an anonymous LDAPv3 Bind is accepted
 * @param simpleBindSupported      whether password-based simple Bind is accepted over a TLS-protected connection
 * @param saslMechanisms           supported RFC 4422 SASL mechanism names
 * @param startTlsSupported        whether the standard LDAP StartTLS extended operation is enabled
 * @param maximumSearchEntries     positive maximum entries produced by one Search operation
 * @param maximumSearchTimeSeconds positive maximum Search execution time in seconds
 * @param maximumMessageBytes      positive maximum encoded LDAP message size
 * @param maximumBerDepth          positive maximum nested BER depth
 * @author Kimi Liu
 */
public record LdapProviderSettings(boolean anonymousBindSupported, boolean simpleBindSupported,
        Set<String> saslMechanisms, boolean startTlsSupported, int maximumSearchEntries, int maximumSearchTimeSeconds,
        long maximumMessageBytes, int maximumBerDepth) implements ProviderSettings {

    /**
     * Freezes mechanism names and verifies that the Provider has a usable Bind and positive resource limits.
     *
     * @throws IllegalArgumentException if the mechanism set or one of its members is {@code null}
     * @throws ValidateException        if no Bind choice is enabled or a numeric limit is not positive
     */
    public LdapProviderSettings {
        Assert.notNull(saslMechanisms, "LDAP Provider SASL mechanisms must not be null");
        final LinkedHashSet<String> mechanisms = new LinkedHashSet<>(saslMechanisms.size());
        for (String mechanism : saslMechanisms) {
            final String current = Assert.notBlank(mechanism, "LDAP Provider SASL mechanism must not be blank");
            new SaslCredentials(current, Optional.empty());
            mechanisms.add(current);
        }
        saslMechanisms = Collections.unmodifiableSet(mechanisms);
        if (!anonymousBindSupported && !simpleBindSupported && saslMechanisms.isEmpty()) {
            throw new ValidateException("LDAP Provider must enable at least one Bind authentication choice");
        }
        if (maximumSearchEntries <= 0 || maximumSearchTimeSeconds <= 0 || maximumMessageBytes <= 0
                || maximumBerDepth <= 0) {
            throw new ValidateException("LDAP Provider search, message, and BER depth limits must be positive");
        }
    }

}
