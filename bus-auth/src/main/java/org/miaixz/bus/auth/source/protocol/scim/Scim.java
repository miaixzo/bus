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
package org.miaixz.bus.auth.source.protocol.scim;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.protocol.scim.server.ScimServerOptions;
import org.miaixz.bus.core.net.Protocol;

/**
 * Exposes standard SCIM 2.0 Service Provider operation keys, schema identifiers, and the Server-driver factory.
 *
 * @author Kimi Liu
 */
public class Scim {

    /**
     * SCIM resource creation operation key.
     */
    public static final Capability.Key CREATE = Capability.Key.standard(Protocol.SCIM, "create");

    /**
     * SCIM resource retrieval operation key.
     */
    public static final Capability.Key RETRIEVE = Capability.Key.standard(Protocol.SCIM, "retrieve");

    /**
     * SCIM resource replacement operation key.
     */
    public static final Capability.Key REPLACE = Capability.Key.standard(Protocol.SCIM, "replace");

    /**
     * SCIM resource patch operation key.
     */
    public static final Capability.Key PATCH = Capability.Key.standard(Protocol.SCIM, "patch");

    /**
     * SCIM resource deletion operation key.
     */
    public static final Capability.Key DELETE = Capability.Key.standard(Protocol.SCIM, "delete");

    /**
     * SCIM resource search operation key.
     */
    public static final Capability.Key SEARCH_GET = Capability.Key.standard(Protocol.SCIM, "scim.search.get");

    /**
     * SCIM POST {@code /.search} routing operation key.
     */
    public static final Capability.Key SEARCH_POST = Capability.Key.standard(Protocol.SCIM, "scim.search.post");

    /**
     * SCIM bulk operation key.
     */
    public static final Capability.Key BULK = Capability.Key.standard(Protocol.SCIM, "bulk");

    /**
     * SCIM ServiceProviderConfig discovery operation key.
     */
    public static final Capability.Key SERVICE_PROVIDER_CONFIG = Capability.Key
            .standard(Protocol.SCIM, "service_provider_config");

    /**
     * SCIM ResourceTypes discovery operation key.
     */
    public static final Capability.Key RESOURCE_TYPES = Capability.Key.standard(Protocol.SCIM, "resource_types");

    /**
     * SCIM Schemas discovery operation key.
     */
    public static final Capability.Key SCHEMAS = Capability.Key.standard(Protocol.SCIM, "schemas");

    /**
     * RFC 7643 core User schema identifier.
     */
    public static final String USER_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:User";

    /**
     * RFC 7643 core Group schema identifier.
     */
    public static final String GROUP_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Group";

    /**
     * RFC 7644 ListResponse message schema identifier.
     */
    public static final String LIST_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

    /**
     * RFC 7644 PatchOp message schema identifier.
     */
    public static final String PATCH_OP_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    /**
     * RFC 7644 BulkRequest message schema identifier.
     */
    public static final String BULK_REQUEST_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";

    /**
     * RFC 7644 BulkResponse message schema identifier.
     */
    public static final String BULK_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

    /**
     * RFC 7644 Error message schema identifier.
     */
    public static final String ERROR_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:Error";

    /**
     * RFC 7643 Schema resource schema identifier.
     */
    public static final String SCHEMA_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Schema";

    /**
     * RFC 7643 ResourceType resource schema identifier.
     */
    public static final String RESOURCE_TYPE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

    /**
     * RFC 7643 ServiceProviderConfig resource schema identifier.
     */
    public static final String SERVICE_PROVIDER_CONFIG_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";

    /**
     * RFC 7644 SearchRequest message schema identifier.
     */
    public static final String SEARCH_REQUEST_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:SearchRequest";

    /**
     * Creates a SCIM protocol constant holder with no retained state.
     */
    public Scim() {
        // No initialization required.
    }

    /**
     * Creates a fresh SCIM 2.0 Service Provider runtime registration.
     *
     * @return new SCIM Server driver
     */
    public static SourceDriver<ScimServerOptions> server() {
        return new ScimServerDriver();
    }

    /**
     * Defines standard relative endpoint paths used by SCIM HTTP bindings.
     */
    public static class Paths {

        /**
         * User resource collection path.
         */
        public static final String USERS = "/Users";
        /**
         * Group resource collection path.
         */
        public static final String GROUPS = "/Groups";
        /**
         * Bulk request endpoint path.
         */
        public static final String BULK = "/Bulk";
        /**
         * Search request endpoint suffix.
         */
        public static final String SEARCH = "/.search";
        /**
         * ServiceProviderConfig discovery path.
         */
        public static final String SERVICE_PROVIDER_CONFIG = "/ServiceProviderConfig";
        /**
         * ResourceTypes discovery collection path.
         */
        public static final String RESOURCE_TYPES = "/ResourceTypes";
        /**
         * Schemas discovery collection path.
         */
        public static final String SCHEMAS = "/Schemas";

        /**
         * Creates an endpoint path registry instance.
         */
        public Paths() {
            // No initialization required.
        }

    }

    /**
     * Defines standard resource type names used by routing and membership values.
     */
    public static class ResourceTypes {

        /**
         * User resource type name.
         */
        public static final String USER = "User";
        /**
         * Group resource type name.
         */
        public static final String GROUP = "Group";

        /**
         * Creates a resource type registry instance.
         */
        public ResourceTypes() {
            // No initialization required.
        }

    }

    /**
     * Defines standard PatchOp operation values.
     */
    public static class Operations {

        /**
         * Add operation value.
         */
        public static final String ADD = "add";
        /**
         * Remove operation value.
         */
        public static final String REMOVE = "remove";
        /**
         * Replace operation value.
         */
        public static final String REPLACE = "replace";

        /**
         * Creates a PatchOp operation registry instance.
         */
        public Operations() {
            // No initialization required.
        }

    }

    /**
     * Defines standard SCIM JSON attribute and message member names.
     */
    public static class Attributes {

        /**
         *
         * Standard schemas member name.
         *
         */
        public static final String SCHEMAS = "schemas";
        /**
         * Standard resource id member name.
         */
        public static final String ID = "id";
        /**
         * Standard external identifier member name.
         */
        public static final String EXTERNAL_ID = "externalId";
        /**
         * Standard metadata member name.
         */
        public static final String META = "meta";
        /**
         * Standard resource type member name.
         */
        public static final String RESOURCE_TYPE = "resourceType";
        /**
         * Standard creation timestamp member name.
         */
        public static final String CREATED = "created";
        /**
         * Standard modification timestamp member name.
         */
        public static final String LAST_MODIFIED = "lastModified";
        /**
         * Standard resource version member name.
         */
        public static final String VERSION = "version";
        /**
         * Standard resource location member name.
         */
        public static final String LOCATION = "location";
        /**
         * Standard user-name member name.
         */
        public static final String USER_NAME = "userName";
        /**
         * Standard structured name member name.
         */
        public static final String NAME = "name";
        /**
         * Standard display-name member name.
         */
        public static final String DISPLAY_NAME = "displayName";
        /**
         * Standard nickname member name.
         */
        public static final String NICK_NAME = "nickName";
        /**
         * Standard profile URL member name.
         */
        public static final String PROFILE_URL = "profileUrl";
        /**
         * Standard title member name.
         */
        public static final String TITLE = "title";
        /**
         * Standard user-type member name.
         */
        public static final String USER_TYPE = "userType";
        /**
         * Standard preferred-language member name.
         */
        public static final String PREFERRED_LANGUAGE = "preferredLanguage";
        /**
         * Standard locale member name.
         */
        public static final String LOCALE = "locale";
        /**
         * Standard timezone member name.
         */
        public static final String TIMEZONE = "timezone";
        /**
         * Standard active-state member name.
         */
        public static final String ACTIVE = "active";
        /**
         * Standard password member name.
         */
        public static final String PASSWORD = "password";
        /**
         * Standard email collection member name.
         */
        public static final String EMAILS = "emails";
        /**
         * Standard telephone collection member name.
         */
        public static final String PHONE_NUMBERS = "phoneNumbers";
        /**
         * Standard instant-messaging collection member name.
         */
        public static final String IMS = "ims";
        /**
         * Standard photo collection member name.
         */
        public static final String PHOTOS = "photos";
        /**
         * Standard address collection member name.
         */
        public static final String ADDRESSES = "addresses";
        /**
         * Standard group membership member name.
         */
        public static final String GROUPS = "groups";
        /**
         * Standard entitlement collection member name.
         */
        public static final String ENTITLEMENTS = "entitlements";
        /**
         * Standard role collection member name.
         */
        public static final String ROLES = "roles";
        /**
         * Standard certificate collection member name.
         */
        public static final String X509_CERTIFICATES = "x509Certificates";
        /**
         * Standard group members member name.
         */
        public static final String MEMBERS = "members";
        /**
         * Standard multi-valued item value member name.
         */
        public static final String VALUE = "value";
        /**
         * Standard multi-valued item display member name.
         */
        public static final String DISPLAY = "display";
        /**
         * Standard multi-valued item type member name.
         */
        public static final String TYPE = "type";
        /**
         * Standard list resources member name.
         */
        public static final String RESOURCES = "Resources";
        /**
         * Standard total result count member name.
         */
        public static final String TOTAL_RESULTS = "totalResults";
        /**
         * Standard start index member name.
         */
        public static final String START_INDEX = "startIndex";
        /**
         * Standard page item count member name.
         */
        public static final String ITEMS_PER_PAGE = "itemsPerPage";
        /**
         * Standard requested attribute list member name.
         */
        public static final String ATTRIBUTES = "attributes";
        /**
         * Standard excluded attribute list member name.
         */
        public static final String EXCLUDED_ATTRIBUTES = "excludedAttributes";
        /**
         * Standard filter member name.
         */
        public static final String FILTER = "filter";
        /**
         * Standard sort attribute member name.
         */
        public static final String SORT_BY = "sortBy";
        /**
         * Standard sort direction member name.
         */
        public static final String SORT_ORDER = "sortOrder";
        /**
         * Standard requested result count member name.
         */
        public static final String COUNT = "count";
        /**
         * Standard PatchOp Operations member name.
         */
        public static final String OPERATIONS = "Operations";
        /**
         * Standard patch operation member name.
         */
        public static final String OP = "op";
        /**
         * Standard patch attribute path member name.
         */
        public static final String PATH = "path";
        /**
         * Standard bulk fail-on-errors member name.
         */
        public static final String FAIL_ON_ERRORS = "failOnErrors";
        /**
         * Standard bulk identifier member name.
         */
        public static final String BULK_ID = "bulkId";
        /**
         * Standard bulk HTTP method member name.
         */
        public static final String METHOD = "method";
        /**
         * Standard bulk operation payload member name.
         */
        public static final String DATA = "data";
        /**
         * Standard HTTP status member name.
         */
        public static final String STATUS = "status";
        /**
         * Standard SCIM error type member name.
         */
        public static final String SCIM_TYPE = "scimType";
        /**
         * Standard SCIM error detail member name.
         */
        public static final String DETAIL = "detail";
        /**
         * Standard discovery supported flag member name.
         */
        public static final String SUPPORTED = "supported";
        /**
         * Standard discovery endpoint member name.
         */
        public static final String ENDPOINT = "endpoint";
        /**
         * Standard discovery schema member name.
         */
        public static final String SCHEMA = "schema";
        /**
         * Standard schema extension list member name.
         */
        public static final String SCHEMA_EXTENSIONS = "schemaExtensions";
        /**
         * Standard description member name.
         */
        public static final String DESCRIPTION = "description";
        /**
         * Standard embedded response member name.
         */
        public static final String RESPONSE = "response";
        /**
         * Standard discovery resource name member.
         */
        public static final String RESOURCE_NAME = "name";
        /**
         * Standard required flag member name.
         */
        public static final String REQUIRED = "required";
        /**
         * Standard multi-valued declaration member name.
         */
        public static final String MULTI_VALUED = "multiValued";
        /**
         * Standard case-exact declaration member name.
         */
        public static final String CASE_EXACT = "caseExact";
        /**
         * Standard mutability declaration member name.
         */
        public static final String MUTABILITY = "mutability";
        /**
         * Standard returned declaration member name.
         */
        public static final String RETURNED = "returned";
        /**
         * Standard uniqueness declaration member name.
         */
        public static final String UNIQUENESS = "uniqueness";
        /**
         * Standard canonical-values declaration member name.
         */
        public static final String CANONICAL_VALUES = "canonicalValues";
        /**
         * Standard reference-types declaration member name.
         */
        public static final String REFERENCE_TYPES = "referenceTypes";
        /**
         * Standard sub-attributes declaration member name.
         */
        public static final String SUB_ATTRIBUTES = "subAttributes";
        /**
         * Standard documentation URI member name.
         */
        public static final String DOCUMENTATION_URI = "documentationUri";
        /**
         * Standard patch-support member name.
         */
        public static final String PATCH_SUPPORTED = "patch";
        /**
         * Standard bulk-support member name.
         */
        public static final String BULK_SUPPORTED = "bulk";
        /**
         * Standard filter-support member name.
         */
        public static final String FILTER_SUPPORTED = "filter";
        /**
         * Standard password-change support member name.
         */
        public static final String CHANGE_PASSWORD = "changePassword";
        /**
         * Standard sort-support member name.
         */
        public static final String SORT_SUPPORTED = "sort";
        /**
         * Standard entity-tag support member name.
         */
        public static final String ETAG_SUPPORTED = "etag";
        /**
         * Standard authentication-schemes member name.
         */
        public static final String AUTHENTICATION_SCHEMES = "authenticationSchemes";
        /**
         * Standard specification URI member name.
         */
        public static final String SPEC_URI = "specUri";
        /**
         * Standard maximum operation count member name.
         */
        public static final String MAX_OPERATIONS = "maxOperations";
        /**
         * Standard maximum payload size member name.
         */
        public static final String MAX_PAYLOAD_SIZE = "maxPayloadSize";
        /**
         * Standard maximum result count member name.
         */
        public static final String MAX_RESULTS = "maxResults";
        /**
         * Standard URI reference sub-attribute name.
         */
        public static final String REFERENCE = "$ref";
        /**
         * Standard primary-value marker member name.
         */
        public static final String PRIMARY = "primary";
        /**
         * Standard formatted-value member name.
         */
        public static final String FORMATTED = "formatted";
        /**
         * Standard family-name member name.
         */
        public static final String FAMILY_NAME = "familyName";
        /**
         * Standard given-name member name.
         */
        public static final String GIVEN_NAME = "givenName";
        /**
         * Standard middle-name member name.
         */
        public static final String MIDDLE_NAME = "middleName";
        /**
         * Standard honorific-prefix member name.
         */
        public static final String HONORIFIC_PREFIX = "honorificPrefix";
        /**
         * Standard honorific-suffix member name.
         */
        public static final String HONORIFIC_SUFFIX = "honorificSuffix";
        /**
         * Standard street-address member name.
         */
        public static final String STREET_ADDRESS = "streetAddress";
        /**
         * Standard locality member name.
         */
        public static final String LOCALITY = "locality";
        /**
         * Standard region member name.
         */
        public static final String REGION = "region";
        /**
         * Standard postal-code member name.
         */
        public static final String POSTAL_CODE = "postalCode";
        /**
         * Standard country member name.
         */
        public static final String COUNTRY = "country";

        /**
         * Creates an attribute-name registry instance.
         */
        public Attributes() {
            // No initialization required.
        }

    }

}
