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
package org.miaixz.bus.auth.protocol.radius.server;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.radius.*;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the RADIUS Access and Accounting server server scheme implemented by the Provider driver.
 * <p>
 * Both capabilities retain complete binary packet models. The Registry security boundary is public because RADIUS
 * performs client and packet authentication inside the protocol operation after the trusted transport adapter has
 * supplied its network context.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RadiusServerScheme implements Scheme<RadiusServerOptions> {

    /**
     * Stable registration type identifier for RADIUS Providers.
     */
    public static final String ID = "radius-server";

    /**
     * Processes one standard Access-Request into Accept, Reject, or Challenge.
     */
    public static final Capability<AccessRequest, RadiusPacket> ACCESS = new Capability<>(Radius.ACCESS,
            AccessRequest.class, RadiusPacket.class, Capability.Direction.PROVIDER,
            Set.of(Capability.Interaction.DIRECT), Capability.Security.PUBLIC);

    /**
     * Processes one standard Accounting-Request into Accounting-Response.
     */
    public static final Capability<AccountingRequest, AccountingResponse> ACCOUNTING = new Capability<>(
            Radius.ACCOUNTING, AccountingRequest.class, AccountingResponse.class, Capability.Direction.PROVIDER,
            Set.of(Capability.Interaction.DIRECT), Capability.Security.PUBLIC);

    /**
     * Complete ordered RADIUS server-role Source capability manifest.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(ACCESS, ACCOUNTING));

    /**
     * Formal standards implemented by the RADIUS Provider.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.RADIUS, new Version("1.1"),
            Set.of(
                    citation("https://www.rfc-editor.org/rfc/rfc2865", "RADIUS Access and packet format"),
                    citation("https://www.rfc-editor.org/rfc/rfc2866", "RADIUS Accounting"),
                    citation("https://www.rfc-editor.org/rfc/rfc2869", "RADIUS extensions"),
                    citation("https://www.rfc-editor.org/rfc/rfc3579", "RADIUS EAP support"),
                    citation("https://www.rfc-editor.org/rfc/rfc5080", "RADIUS implementation issues"),
                    citation("https://www.rfc-editor.org/rfc/rfc6614", "Historic RADIUS over TLS"),
                    citation("https://www.rfc-editor.org/rfc/rfc9765", "Experimental RADIUS/1.1 transport profile")),
            "RADIUS Access and Accounting with experimental RADIUS/1.1 transport profile");

    /**
     * External management form corresponding exactly to RadiusServerOptions.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("radius-provider", "RADIUS Provider",
                    List.of(
                            field("versions", "RADIUS versions", Form.Type.MULTI_SELECT),
                            field("eap_supported", "EAP supported", Form.Type.BOOLEAN),
                            field("require_message_authenticator", "Require Message-Authenticator", Form.Type.BOOLEAN),
                            field("maximum_packet_bytes", "Maximum packet bytes", Form.Type.NUMBER)))));

    /**
     * Creates the stateless RADIUS server scheme.
     */
    public RadiusServerScheme() {
        // No initialization required.
    }

    /**
     * Creates one official specification citation.
     *
     * @param url     official RFC URL
     * @param section implemented subject description
     * @return immutable citation
     */
    private static Conformance.Citation citation(final String url, final String section) {
        return new Conformance.Citation(url, section);
    }

    /**
     * Creates one required non-secret management field.
     *
     * @param key   stable options key
     * @param label human-readable field label
     * @param type  management presentation type
     * @return immutable field declaration
     */
    private static Form.Field field(final String key, final String label, final Form.Type type) {
        return new Form.Field(key, label, type, true, Optional.empty(), List.of());
    }

    /**
     * Returns the stable RADIUS server-role Source registration type.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the RADIUS server Provider category.
     *
     * @return RADIUS Provider type
     */
    @Override
    public Protocol protocol() {
        return Protocol.RADIUS;
    }

    /**
     * Returns the Access and Accounting capability manifest.
     *
     * @return immutable two-operation manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal RADIUS conformance declaration.
     *
     * @return present RADIUS conformance
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the exact external management form.
     *
     * @return immutable RADIUS options form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no defaults because accepted versions and security policy are deployment decisions.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<RadiusServerOptions> defaults() {
        return Optional.empty();
    }

}
