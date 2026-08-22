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
package org.miaixz.bus.auth.vendor.feishu;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the frozen Feishu v3 OAuth browser and enterprise Contact API Vendor variants.
 * <p>
 * The authorization request remains a standard OAuth operation with mandatory S256 PKCE. Feishu's v3 JSON token and
 * refresh requests and its code/msg/data profile envelope remain private Source-authentication behavior. The separate
 * enterprise Variant exposes only provider-neutral directory capabilities over official Contact v3 endpoints.
 * </p>
 *
 * @author Kimi Liu
 */
public class FeishuManifest implements VariantManifest<FeishuOptions> {

    /**
     * Stable Feishu platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("feishu");

    /**
     * Stable identifier of the sole Feishu OAuth variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Stable identifier of the Feishu enterprise Contact API variant.
     */
    public static final Vendor.Variant ENTERPRISE = new Vendor.Variant("enterprise");

    /**
     * Feishu v3 JSON client-id/client-secret authentication method.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_JSON = new Endpoint.Authentication("client_secret_json");

    /**
     * Exact Source and standard OAuth capabilities of the default variant.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Exact provider-neutral capabilities implemented by the enterprise Contact API adapter.
     */
    private static final Capability.Manifest ENTERPRISE_CAPABILITIES = new Capability.Manifest(
            List.of(Realm.describe(ID), Realm.snapshot(ID), Realm.retrieve(ID)));

    /**
     * Feishu-specific limitation stating that functional roles cannot be completely enumerated.
     */
    private static final String FUNCTIONAL_ROLES_NOT_ENUMERATED = "functional-roles-not-enumerated";

    /**
     * Exact enterprise coverage description shared with the compiled enterprise adapter.
     */
    private static final Realm.Description ENTERPRISE_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.GROUP),
            Set.of(Realm.RelationKind.PARENT, Realm.RelationKind.MEMBER, Realm.RelationKind.MANAGER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.PARTIAL, Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE,
            List.of(
                    Builder.ENTERPRISE_LIMITATION_APPLICATION_VISIBLE_CONTACT_SCOPE,
                    Builder.ENTERPRISE_LIMITATION_HIERARCHY_PAGES_REPLAY_FROM_ROOT,
                    FUNCTIONAL_ROLES_NOT_ENUMERATED,
                    Builder.ENTERPRISE_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Exact Feishu v3 wire differences retained only inside Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.JSON,
                    OAuth2.Parameters.CLIENT_SECRET,
                    OAuth2.Parameters.CLIENT_SECRET,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "code",
                    OAuth2.Parameters.ERROR,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "data",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true));

    /**
     * Complete immutable endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VariantManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET, List.of(),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://accounts.feishu.cn/open-apis/authen/v1/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed("https://accounts.feishu.cn/oauth/v3/token", Http.Method.POST, CLIENT_SECRET_JSON)),
                    Optional.of(
                            fixed(
                                    "https://open.feishu.cn/open-apis/authen/v1/user_info",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed("https://accounts.feishu.cn/oauth/v3/token", Http.Method.POST, CLIENT_SECRET_JSON)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Complete immutable Contact v3 enterprise Variant declaration.
     */
    private static final VariantManifest.Variant ENTERPRISE_VARIANT = new VariantManifest.Variant(ID, ENTERPRISE,
            Protocol.HTTPS, VariantManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of(),
            new VendorTargets(Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal",
                                    Http.Method.POST,
                                    CLIENT_SECRET_JSON)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), managementTargets()),
            ENTERPRISE_CAPABILITIES, List.of());

    /**
     * Creates the stateless Feishu manifest used by Vendor directory assembly.
     */
    public FeishuManifest() {
    }

    /**
     * Returns the frozen Feishu enterprise coverage description.
     *
     * @return immutable enterprise description
     */
    static Realm.Description enterpriseDescription() {
        return ENTERPRISE_DESCRIPTION;
    }

    /**
     * Creates the ordered official Contact v3 management target map.
     *
     * @return immutable-by-construction management target declarations
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        final VendorTargets.Target departmentUsers = fixed(
                "https://open.feishu.cn/open-apis/contact/v3/users/find_by_department",
                Http.Method.GET,
                Endpoint.Authentication.BEARER);
        targets.put(Builder.ENTERPRISE_USERS, departmentUsers);
        targets.put(
                Builder.ENTERPRISE_USER,
                fixed(
                        "https://open.feishu.cn/open-apis/contact/v3/users",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_ORGANIZATIONS,
                fixed(
                        "https://open.feishu.cn/open-apis/contact/v3/departments",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(Builder.ENTERPRISE_ORGANIZATION_USERS, departmentUsers);
        targets.put(
                Builder.ENTERPRISE_GROUPS,
                fixed(
                        "https://open.feishu.cn/open-apis/contact/v3/group/simplelist",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_GROUP_MEMBERS,
                fixed(
                        "https://open.feishu.cn/open-apis/contact/v3/group",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        return targets;
    }

    /**
     * Creates one fixed credential-free HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
     * @param authentication endpoint authentication declaration
     * @return immutable fixed endpoint target
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable registered Feishu wire deviation.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact Feishu field or envelope name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Feishu wraps the response
     * @return immutable deviation declaration
     */
    private static VendorDeviation deviation(
            final String operation,
            final VendorDeviation.Location location,
            final String vendorName,
            final String standardName,
            final Optional<MediaType> mediaType,
            final Http.Method method,
            final boolean enveloped) {
        return new VendorDeviation(operation, location, vendorName, Optional.ofNullable(standardName), mediaType,
                method, enveloped);
    }

    /**
     * Returns the stable Feishu routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Feishu presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Feishu", "Feishu OAuth login and enterprise directory access", "feishu");
    }

    /**
     * Returns the supported Feishu variants in stable declaration order.
     *
     * @return immutable default and enterprise manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT, ENTERPRISE_VARIANT);
    }

    /**
     * Returns the exact requested Feishu manifest Variant.
     *
     * @param variant requested variant
     * @return exact immutable manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return VARIANT;
        }
        if (ENTERPRISE.equals(variant)) {
            return ENTERPRISE_VARIANT;
        }
        throw new ValidateException("Feishu Vendor variant is not supported");
    }

}
