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
package org.miaixz.bus.auth.source.protocol.scim.server;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.source.protocol.ProtocolScheme;
import org.miaixz.bus.auth.source.protocol.scim.*;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the standards-based SCIM 2.0 Service server scheme.
 *
 * @author Kimi Liu
 */
public class ScimServerScheme implements ProtocolScheme<ScimServerOptions> {

    /**
     * Stable Source type identifier for generic SCIM Service Providers.
     */
    public static final String ID = "scim-server";
    /**
     * Creates a standard SCIM resource.
     */
    public static final Capability<Resource, Resource> CREATE = capability(Scim.CREATE, Resource.class, Resource.class);
    /**
     * Retrieves one standard SCIM resource.
     */
    public static final Capability<Resource.Reference, Resource> RETRIEVE = capability(
            Scim.RETRIEVE,
            Resource.Reference.class,
            Resource.class);
    /**
     * Replaces one standard SCIM resource.
     */
    public static final Capability<Resource, Resource> REPLACE = capability(
            Scim.REPLACE,
            Resource.class,
            Resource.class);
    /**
     * Applies one atomic standard SCIM PatchOp.
     */
    public static final Capability<PatchRequest, Resource> PATCH = capability(
            Scim.PATCH,
            PatchRequest.class,
            Resource.class);
    /**
     * Deletes one standard SCIM resource.
     */
    public static final Capability<Resource.Reference, Void> DELETE = capability(
            Scim.DELETE,
            Resource.Reference.class,
            Void.class);
    /**
     * Searches standard SCIM resources.
     */
    public static final Capability<SearchQuery, ListResponse> SEARCH_GET = capability(
            Scim.SEARCH_GET,
            SearchQuery.class,
            ListResponse.class);
    /**
     * Searches standard SCIM resources through POST {@code /.search}.
     */
    public static final Capability<SearchRequest, ListResponse> SEARCH_POST = capability(
            Scim.SEARCH_POST,
            SearchRequest.class,
            ListResponse.class);
    /**
     * Executes a standard ordered SCIM Bulk request.
     */
    public static final Capability<BulkRequest, BulkResponse> BULK = capability(
            Scim.BULK,
            BulkRequest.class,
            BulkResponse.class);
    /**
     * Publishes the standard ServiceProviderConfig discovery resource.
     */
    public static final Capability<Void, ServiceProviderConfig> SERVICE_PROVIDER_CONFIG = capability(
            Scim.SERVICE_PROVIDER_CONFIG,
            Void.class,
            ServiceProviderConfig.class);
    /**
     * Publishes standard ResourceType discovery resources in a ListResponse.
     */
    public static final Capability<Void, ListResponse> RESOURCE_TYPES = capability(
            Scim.RESOURCE_TYPES,
            Void.class,
            ListResponse.class);
    /**
     * Publishes standard Schema discovery resources in a ListResponse.
     */
    public static final Capability<Void, ListResponse> SCHEMAS = capability(
            Scim.SCHEMAS,
            Void.class,
            ListResponse.class);
    /**
     * Complete potential profile manifest before options-based compiler narrowing.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            CREATE,
            RETRIEVE,
            REPLACE,
            PATCH,
            DELETE,
            SEARCH_GET,
            SEARCH_POST,
            BULK,
            SERVICE_PROVIDER_CONFIG,
            RESOURCE_TYPES,
            SCHEMAS));
    /**
     * Formal SCIM specifications implemented by this scheme.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.SCIM, new Version("2.0"),
            Set.of(
                    new Conformance.Citation("https://www.rfc-editor.org/rfc/rfc7643", "SCIM Core Schema"),
                    new Conformance.Citation("https://www.rfc-editor.org/rfc/rfc7644", "SCIM Protocol")),
            "SCIM 2.0 Service Provider");
    /**
     * External management form containing only SCIM deployment options.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("scim-service-provider", "SCIM 2.0 Service Provider",
                    List.of(
                            field("base_uri", "Base URI", Form.Type.URL, true),
                            field("documentation_uri", "Documentation URI", Form.Type.URL, false),
                            field("resource_types", "Resource types", Form.Type.MULTI_SELECT, true),
                            field("authentication_schemes", "Authentication schemes", Form.Type.MULTI_SELECT, true),
                            field("patch_supported", "PATCH supported", Form.Type.BOOLEAN, true),
                            field("bulk_max_operations", "Bulk maximum operations", Form.Type.NUMBER, true),
                            field("bulk_max_payload_size", "Bulk maximum payload size", Form.Type.NUMBER, true),
                            field("filter_max_results", "Filter maximum results", Form.Type.NUMBER, true),
                            field("change_password_supported", "Password change supported", Form.Type.BOOLEAN, true),
                            field("sort_supported", "Sorting supported", Form.Type.BOOLEAN, true),
                            field("etag_supported", "Entity tags supported", Form.Type.BOOLEAN, true),
                            field("maximum_request_bytes", "Maximum request bytes", Form.Type.NUMBER, true),
                            field("maximum_json_depth", "Maximum JSON depth", Form.Type.NUMBER, true)))));

    /**
     * Creates the stateless SCIM server scheme used to compile provisioning Source configurations.
     */
    public ScimServerScheme() {
        // No initialization required.
    }

    /**
     * Creates one direct SCIM Service Provider capability requiring authenticated client access.
     *
     * @param key          direction-neutral SCIM operation key
     * @param requestType  exact standard request class
     * @param responseType exact standard success class
     * @param <Q>          request type
     * @param <S>          success type
     * @return immutable server-role Source capability
     */
    private static <Q, S> Capability<Q, S> capability(
            final Capability.Key key,
            final Class<Q> requestType,
            final Class<S> responseType) {
        return new Capability<>(key, requestType, responseType, Capability.Direction.SERVER,
                Set.of(Capability.Interaction.DIRECT), Capability.Security.CLIENT_AUTHENTICATED);
    }

    /**
     * Creates one management field without a default or generic constraint.
     *
     * @param key      stable options key
     * @param label    human-readable field label
     * @param type     management input presentation type
     * @param required whether management input is mandatory
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
     * Returns the stable SCIM server scheme identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns management presentation metadata for a SCIM Service Provider.
     *
     * @return immutable SCIM server metadata
     */
    @Override
    public Metadata metadata() {
        return new Metadata("SCIM Server", "Exposes standards-based identity provisioning resources through SCIM.",
                "scim");
    }

    /**
     * Returns the SCIM Service Provider category.
     *
     * @return SCIM Service Provider type
     */
    @Override
    public Protocol protocol() {
        return Protocol.SCIM;
    }

    /**
     * Returns every potential SCIM capability before compiler narrowing.
     *
     * @return immutable SCIM capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal SCIM 2.0 conformance declaration.
     *
     * @return present SCIM conformance
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the external management form for SCIM deployment options.
     *
     * @return immutable SCIM Service Provider form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no defaults because endpoint and limits are external deployment decisions.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<ScimServerOptions> defaults() {
        return Optional.empty();
    }

}
