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
package org.miaixz.bus.auth.source.protocol.ldap.client;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme.Conformance;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.ProtocolScheme;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the generic LDAPv3 directory-client scheme.
 * <p>
 * The Source enters through the framework's application-level authentication initiation capability and accepts only
 * direct credential requests at runtime. Its wire behavior remains standard LDAPv3 Bind, Search, StartTLS, and Unbind;
 * no LDAP protocol operation is renamed or exposed as a Vendor operation.
 * </p>
 *
 * @author Kimi Liu
 */
public class LdapClientScheme implements ProtocolScheme<LdapClientOptions> {

    /**
     * Stable Source type identifier for generic LDAP Sources.
     */
    public static final String ID = "ldap";
    /**
     * Complete application-level capability manifest implemented by an LDAP Source runtime.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(SourceWorkflow.INITIATE));
    /**
     * Formal LDAPv3 specifications implemented by the directory Source.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.LDAP, new Version(Symbol.THREE),
            Set.of(
                    citation("https://www.rfc-editor.org/rfc/rfc4510", "LDAPv3 Technical Specification Road Map"),
                    citation("https://www.rfc-editor.org/rfc/rfc4511", "LDAP Protocol"),
                    citation("https://www.rfc-editor.org/rfc/rfc4513", "LDAP Authentication Methods and Security")),
            "LDAPv3 Directory Client Source");
    /**
     * External management form containing only LDAP connection, search, credential-reference, and limit options.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("ldap-directory-source", "LDAPv3 Directory Source",
                    List.of(
                            field("host", "LDAP server host", Form.Type.TEXT, true),
                            field("port", "LDAP server port", Form.Type.NUMBER, true),
                            field("security_mode", "Security mode", Form.Type.SELECT, true),
                            field("search_base", "Search base DN", Form.Type.TEXT, true),
                            field("username_attribute", "Username attribute", Form.Type.TEXT, true),
                            field("attributes", "Identity attributes", Form.Type.MULTI_SELECT, true),
                            field("bind_dn", "Service account bind DN", Form.Type.TEXT, false),
                            field("bind_credential", "Service account password reference", Form.Type.SECRET, false),
                            field("time_limit_seconds", "Search time limit seconds", Form.Type.NUMBER, true),
                            field("maximum_message_bytes", "Maximum LDAP message bytes", Form.Type.NUMBER, true),
                            field("maximum_ber_depth", "Maximum BER depth", Form.Type.NUMBER, true)))));

    /**
     * Creates the stateless LDAP client scheme used to validate and compile directory registrations.
     */
    public LdapClientScheme() {
        // No initialization required.
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
     * Creates one management field without a plaintext default or generic constraint.
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
     * Returns the stable LDAP Source configuration type.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns management presentation metadata for a generic LDAP client Source.
     *
     * @return immutable LDAP client metadata
     */
    @Override
    public Metadata metadata() {
        return new Metadata("LDAP Client", "Connects to an external LDAP directory for identity operations.", "ldap");
    }

    /**
     * Returns the generic LDAP directory Source category.
     *
     * @return LDAP Source type
     */
    @Override
    public Protocol protocol() {
        return Protocol.LDAP;
    }

    /**
     * Returns the single application-level initiation capability.
     *
     * @return immutable LDAP Source manifest
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
     * Returns the external management form for LDAP Source deployment options.
     *
     * @return immutable LDAP Source form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no defaults because the remote directory and security limits are deployment input.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<LdapClientOptions> defaults() {
        return Optional.empty();
    }

}
