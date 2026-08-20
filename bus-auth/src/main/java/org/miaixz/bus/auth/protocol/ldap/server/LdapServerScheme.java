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

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Scheme.Form;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.ldap.Ldap;
import org.miaixz.bus.auth.protocol.ldap.LdapMessage;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the standards-based LDAPv3 directory server server scheme.
 * <p>
 * Every Registry operation accepts the complete LDAPMessage so the message identifier and Controls survive the
 * transport boundary. Bind establishes connection authentication within the external DirectoryStore; consequently the
 * protocol capabilities are publicly reachable at the framework boundary while directory authorization remains
 * connection-scoped and operation-specific.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LdapServerScheme implements Scheme<LdapServerOptions> {

    /**
     * Stable registration type identifier for LDAP Providers.
     */
    public static final String ID = "ldap-server";
    /**
     * Establishes connection authentication using the standard Bind operation.
     */
    public static final Capability<LdapMessage, LdapMessage> BIND = message(Ldap.BIND);
    /**
     * Terminates an LDAP connection without a protocol response.
     */
    public static final Capability<LdapMessage, Void> UNBIND = empty(Ldap.UNBIND);
    /**
     * Produces ordered Search entry/reference messages followed by SearchResultDone.
     */
    public static final Capability<LdapMessage, List<LdapMessage>> SEARCH = new Capability<>(Ldap.SEARCH,
            LdapMessage.class, messageListType(), Capability.Direction.PROVIDER, Set.of(Capability.Interaction.DIRECT),
            Capability.Security.PUBLIC);
    /**
     * Applies the standard atomic Modify operation.
     */
    public static final Capability<LdapMessage, LdapMessage> MODIFY = message(Ldap.MODIFY);
    /**
     * Applies the standard Add operation.
     */
    public static final Capability<LdapMessage, LdapMessage> ADD = message(Ldap.ADD);
    /**
     * Applies the standard Delete operation.
     */
    public static final Capability<LdapMessage, LdapMessage> DELETE = message(Ldap.DELETE);
    /**
     * Applies the standard Modify DN operation.
     */
    public static final Capability<LdapMessage, LdapMessage> MODIFY_DN = message(Ldap.MODIFY_DN);
    /**
     * Applies the standard Compare operation.
     */
    public static final Capability<LdapMessage, LdapMessage> COMPARE = message(Ldap.COMPARE);
    /**
     * Requests operation cancellation without a protocol response.
     */
    public static final Capability<LdapMessage, Void> ABANDON = empty(Ldap.ABANDON);
    /**
     * Applies an LDAP extended operation, including the standard StartTLS request.
     */
    public static final Capability<LdapMessage, LdapMessage> EXTENDED = message(Ldap.EXTENDED);
    /**
     * Complete ordered LDAPv3 server-role Source capability manifest.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(
            List.of(BIND, UNBIND, SEARCH, MODIFY, ADD, DELETE, MODIFY_DN, COMPARE, ABANDON, EXTENDED));
    /**
     * Formal LDAPv3 specifications implemented by the directory Provider.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.LDAP, new Version(Symbol.THREE),
            Set.of(
                    citation("https://www.rfc-editor.org/rfc/rfc4510", "LDAPv3 Technical Specification Road Map"),
                    citation("https://www.rfc-editor.org/rfc/rfc4511", "LDAP Protocol"),
                    citation("https://www.rfc-editor.org/rfc/rfc4513", "LDAP Authentication Methods and Security")),
            "LDAPv3 Directory Server Provider");
    /**
     * External management form containing only Bind, StartTLS, search, message, and BER limits.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("ldap-directory-provider", "LDAPv3 Directory Provider",
                    List.of(
                            field("anonymous_bind_supported", "Anonymous Bind supported", Form.Type.BOOLEAN, true),
                            field("simple_bind_supported", "Simple Bind supported", Form.Type.BOOLEAN, true),
                            field("sasl_mechanisms", "SASL mechanisms", Form.Type.MULTI_SELECT, true),
                            field("start_tls_supported", "StartTLS supported", Form.Type.BOOLEAN, true),
                            field("maximum_search_entries", "Maximum Search entries", Form.Type.NUMBER, true),
                            field("maximum_search_time_seconds", "Maximum Search time seconds", Form.Type.NUMBER, true),
                            field("maximum_message_bytes", "Maximum LDAP message bytes", Form.Type.NUMBER, true),
                            field("maximum_ber_depth", "Maximum BER depth", Form.Type.NUMBER, true)))));

    /**
     * Creates the stateless LDAP server scheme used to validate and compile server registrations.
     */
    public LdapServerScheme() {
        // No initialization required.
    }

    /**
     * Creates one complete-message request and complete-message response capability.
     *
     * @param key standard LDAP operation key
     * @return immutable direct public server-role capability
     */
    private static Capability<LdapMessage, LdapMessage> message(final Capability.Key key) {
        return new Capability<>(key, LdapMessage.class, LdapMessage.class, Capability.Direction.PROVIDER,
                Set.of(Capability.Interaction.DIRECT), Capability.Security.PUBLIC);
    }

    /**
     * Creates one complete-message request capability whose operation has no protocol response.
     *
     * @param key standard LDAP operation key
     * @return immutable direct public server-role capability
     */
    private static Capability<LdapMessage, Void> empty(final Capability.Key key) {
        return new Capability<>(key, LdapMessage.class, Void.class, Capability.Direction.PROVIDER,
                Set.of(Capability.Interaction.DIRECT), Capability.Security.PUBLIC);
    }

    /**
     * Produces the generic Class token required for an ordered LDAP message list response.
     *
     * @return type-erased List token narrowed at the single declaration boundary
     */
    private static Class<List<LdapMessage>> messageListType() {
        return (Class<List<LdapMessage>>) (Class<?>) List.class;
    }

    /**
     * Creates one formal LDAP standards citation.
     *
     * @param url     official RFC URL
     * @param section implemented subject description
     * @return immutable citation
     */
    private static Conformance.Citation citation(final String url, final String section) {
        return new Conformance.Citation(url, section);
    }

    /**
     * Creates one management field without a deployment default or generic constraint.
     *
     * @param key      stable options key
     * @param label    human-readable field label
     * @param type     management presentation type
     * @param required whether deployment input is mandatory
     * @return immutable field declaration
     */
    private static Form.Field field(
            final String key,
            final String label,
            final Form.Type type,
            final boolean required) {
        return new Form.Field(key, label, type, required, Optional.empty(), List.of());
    }

    /**
     * Returns the stable LDAP server-role Source registration type.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the LDAP directory server Provider category.
     *
     * @return LDAP Provider type
     */
    @Override
    public Protocol protocol() {
        return Protocol.LDAP;
    }

    /**
     * Returns all ten standard LDAPv3 Provider capabilities.
     *
     * @return immutable LDAP Provider manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal LDAPv3 conformance declaration.
     *
     * @return present LDAP conformance
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the external management form for LDAP Provider options.
     *
     * @return immutable LDAP Provider form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no defaults because authentication methods and resource limits are deployment decisions.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<LdapServerOptions> defaults() {
        return Optional.empty();
    }

}
