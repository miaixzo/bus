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
package org.miaixz.bus.auth.protocol.ldap.client;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.*;

import org.miaixz.bus.auth.Evidence;
import org.miaixz.bus.auth.protocol.ldap.LdapAttribute;
import org.miaixz.bus.auth.protocol.ldap.SearchResultEntry;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.IdentityMapper;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Clock;

/**
 * Maps one uniquely selected and password-verified LDAP Search result entry to an external identity.
 * <p>
 * The caller invokes this mapper only after a successful user Bind on the same LDAP connection used for the Search. The
 * entry distinguished name is therefore the stable Source-local subject. Only explicitly configured textual attributes
 * are projected, every value is decoded with strict UTF-8, and no directory access or account linking is performed
 * here.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LdapIdentityParser implements IdentityMapper<SearchResultEntry> {

    /**
     * Immutable LDAP Source mapping options.
     */
    private final LdapClientOptions options;

    /**
     * Shared Fabric time source for verified evidence provenance.
     */
    private final Clock clock;

    /**
     * Creates a pure LDAP entry mapper.
     *
     * @param options immutable attribute projection options
     * @param clock   shared verification time source
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public LdapIdentityParser(final LdapClientOptions options, final Clock clock) {
        this.options = Assert.notNull(options, "LDAP identity options must not be null");
        this.clock = Assert.notNull(clock, "LDAP identity clock must not be null");
    }

    /**
     * Decodes one configured textual LDAP attribute value with malformed-input rejection.
     *
     * @param value exact LDAP attribute value octets
     * @return decoded Unicode value
     * @throws ValidateException if the value is not valid UTF-8
     */
    private static String utf8(final byte[] value) {
        try {
            return Charset.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(value)).toString();
        } catch (CharacterCodingException exception) {
            throw new ValidateException("LDAP identity attribute is not valid UTF-8", exception);
        }
    }

    /**
     * Maps one validated LDAP entry to a password-authenticated external identity.
     *
     * @param sourceId registered LDAP Source identifier
     * @param input    uniquely selected entry whose DN was successfully rebound
     * @return immutable protocol-neutral external identity
     * @throws IllegalArgumentException if an argument is invalid
     * @throws ValidateException        if a configured attribute value is not valid UTF-8
     */
    @Override
    public ExternalIdentity map(final String sourceId, final SearchResultEntry input) {
        Assert.notBlank(sourceId, "LDAP identity Source id must not be blank");
        Assert.notNull(input, "LDAP identity Search result entry must not be null");

        final Map<String, String> configuredNames = configuredNames();
        final Map<String, List<JsonValue>> projectedValues = new LinkedHashMap<>();
        options.attributes().forEach(name -> projectedValues.put(name, new ArrayList<>()));
        for (LdapAttribute attribute : input.attributes()) {
            final String configured = configuredNames.get(attribute.type().toLowerCase(Locale.ROOT));
            if (configured == null) {
                continue;
            }
            final List<JsonValue> values = projectedValues.get(configured);
            for (LdapAttribute.AttributeValue value : attribute.values()) {
                values.add(new JsonValue.StringValue(utf8(value.value())));
            }
        }

        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        projectedValues.forEach((name, values) -> {
            if (!values.isEmpty()) {
                attributes.put(name, new JsonValue.ArrayValue(values));
            }
        });
        final Evidence evidence = new Evidence(Evidence.Type.PASSWORD, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim("ldap_bind", new JsonValue.BooleanValue(true), sourceId, clock.now()));
        return new ExternalIdentity(sourceId, input.objectName().value(), new JsonValue.ObjectValue(attributes),
                List.of(evidence));
    }

    /**
     * Indexes configured output names by LDAP's case-insensitive attribute-description comparison form.
     *
     * @return canonical-name to configured-name index in configuration order
     */
    private Map<String, String> configuredNames() {
        final Map<String, String> names = new LinkedHashMap<>(options.attributes().size());
        options.attributes().forEach(name -> names.put(name.toLowerCase(Locale.ROOT), name));
        return names;
    }

}
