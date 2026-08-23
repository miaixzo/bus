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
package org.miaixz.bus.auth.source.protocol.saml;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.protocol.saml.client.SamlClientOptions;
import org.miaixz.bus.auth.source.protocol.saml.server.SamlServerOptions;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Protocol;

/**
 * Exposes direction-neutral SAML 2.0 operation keys and explicit server and client driver factories.
 *
 * @author Kimi Liu
 */
public class Saml {

    /**
     * Required SAML protocol version lexical value.
     */
    public static final String VERSION_2_0 = "2.0";

    /**
     * Maximum UTF-8 octet length of RelayState in HTTP Bindings.
     */
    public static final int MAXIMUM_RELAY_STATE_BYTES = Normal._80;
    /**
     * SAML Web Browser SSO profile operation backed by a SingleSignOnService endpoint.
     */
    public static final Capability.Key SINGLE_SIGN_ON = Capability.Key.standard(Protocol.SAML, "single_sign_on");
    /**
     * SAML Single Logout profile operation backed by a SingleLogoutService endpoint.
     */
    public static final Capability.Key SINGLE_LOGOUT = Capability.Key.standard(Protocol.SAML, "single_logout");
    /**
     * SAML Metadata document publication or retrieval operation.
     */
    public static final Capability.Key METADATA = Capability.Key.standard(Protocol.SAML, "metadata");

    /**
     * Creates a SAML protocol operation namespace instance with no retained state.
     */
    public Saml() {
        // No initialization required.
    }

    /**
     * Creates the server-side SAML driver.
     *
     * @return new SAML Server driver
     */
    public static SourceDriver<SamlServerOptions> server() {
        return new SamlServerDriver();
    }

    /**
     * Creates the client-side SAML driver.
     *
     * @return new SAML Client driver
     */
    public static SourceDriver<SamlClientOptions> client() {
        return new SamlClientDriver();
    }

    /**
     * Defines namespaces used by the SAML protocol, assertion, metadata, signature, and encryption vocabularies.
     */
    public static class Namespaces {

        /**
         * SAML 2.0 protocol namespace.
         */
        public static final String PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
        /**
         * SAML 2.0 assertion namespace.
         */
        public static final String ASSERTION = "urn:oasis:names:tc:SAML:2.0:assertion";
        /**
         * SAML 2.0 metadata namespace.
         */
        public static final String METADATA = "urn:oasis:names:tc:SAML:2.0:metadata";
        /**
         * XML Signature namespace.
         */
        public static final String SIGNATURE = "http://www.w3.org/2000/09/xmldsig#";
        /**
         * XML Encryption 1.0 namespace.
         */
        public static final String ENCRYPTION = "http://www.w3.org/2001/04/xmlenc#";
        /**
         * XML Encryption 1.1 namespace.
         */
        public static final String ENCRYPTION_11 = "http://www.w3.org/2009/xmlenc11#";

        /**
         * Creates an XML namespace registry instance.
         */
        public Namespaces() {
            // No initialization required.
        }

    }

    /**
     * Defines SAML 2.0 binding identifiers implemented by this framework.
     */
    public static class Bindings {

        /**
         * HTTP-POST Binding identifier.
         */
        public static final String HTTP_POST = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
        /**
         * HTTP-Redirect Binding identifier.
         */
        public static final String HTTP_REDIRECT = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";

        /**
         * Creates a SAML binding registry instance.
         */
        public Bindings() {
            // No initialization required.
        }

    }

    /**
     * Defines standard top-level and commonly emitted SAML StatusCode values.
     */
    public static class Statuses {

        /**
         * Successful processing status.
         */
        public static final String SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
        /**
         * Requester-caused failure status.
         */
        public static final String REQUESTER = "urn:oasis:names:tc:SAML:2.0:status:Requester";
        /**
         * Responder-caused failure status.
         */
        public static final String RESPONDER = "urn:oasis:names:tc:SAML:2.0:status:Responder";
        /**
         * Protocol version mismatch status.
         */
        public static final String VERSION_MISMATCH = "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch";
        /**
         * Policy-denied request status.
         */
        public static final String REQUEST_DENIED = "urn:oasis:names:tc:SAML:2.0:status:RequestDenied";
        /**
         * Unknown principal status.
         */
        public static final String UNKNOWN_PRINCIPAL = "urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal";
        /**
         * Invalid attribute name or value status.
         */
        public static final String INVALID_ATTRIBUTE = "urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue";
        /**
         * Invalid NameID policy status.
         */
        public static final String INVALID_NAME_ID_POLICY = "urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy";
        /**
         * Unavailable requested authentication context status.
         */
        public static final String NO_AUTHN_CONTEXT = "urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext";
        /**
         * Unsupported request status.
         */
        public static final String REQUEST_UNSUPPORTED = "urn:oasis:names:tc:SAML:2.0:status:RequestUnsupported";

        /**
         * Creates a SAML status registry instance.
         */
        public Statuses() {
            // No initialization required.
        }

    }

    /**
     * Defines standard SAML NameID format identifiers used by the implemented profiles.
     */
    public static class NameIdFormats {

        /**
         * Unspecified NameID format.
         */
        public static final String UNSPECIFIED = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
        /**
         * Email address NameID format.
         */
        public static final String EMAIL = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
        /**
         * Persistent pseudonymous NameID format.
         */
        public static final String PERSISTENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
        /**
         * Transient pseudonymous NameID format.
         */
        public static final String TRANSIENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
        /**
         * Entity identifier NameID format.
         */
        public static final String ENTITY = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

        /**
         * Creates a NameID format registry instance.
         */
        public NameIdFormats() {
            // No initialization required.
        }

    }

    /**
     * Defines standard SubjectConfirmation method identifiers used by the profiles.
     */
    public static class ConfirmationMethods {

        /**
         * Bearer SubjectConfirmation method.
         */
        public static final String BEARER = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
        /**
         * Holder-of-key SubjectConfirmation method.
         */
        public static final String HOLDER_OF_KEY = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
        /**
         * Sender-vouches SubjectConfirmation method.
         */
        public static final String SENDER_VOUCHES = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";

        /**
         * Creates a subject-confirmation method registry instance.
         */
        public ConfirmationMethods() {
            // No initialization required.
        }

    }

    /**
     * Defines HTTP Binding form and query parameter names.
     */
    public static class Parameters {

        /**
         * SAML request message parameter.
         */
        public static final String REQUEST = "SAMLRequest";
        /**
         * SAML response message parameter.
         */
        public static final String RESPONSE = "SAMLResponse";
        /**
         * Relay state parameter.
         */
        public static final String RELAY_STATE = "RelayState";
        /**
         * Redirect Binding signature algorithm parameter.
         */
        public static final String SIGNATURE_ALGORITHM = "SigAlg";
        /**
         * Redirect Binding signature parameter.
         */
        public static final String SIGNATURE = "Signature";
        /**
         * External consumer metadata field carrying its Single Logout Service URL.
         */
        public static final String SINGLE_LOGOUT_SERVICE_URL = "single_logout_service_url";

        /**
         * Creates a SAML HTTP parameter registry instance.
         */
        public Parameters() {
            // No initialization required.
        }

    }

    /**
     * Defines XML local names used by the implemented SAML protocol and assertion model.
     */
    public static class Xml {

        /**
         *
         * SAML Address attribute local name.
         *
         */
        public static final String ADDRESS = "Address";
        /**
         * SAML Advice element local name.
         */
        public static final String ADVICE = "Advice";
        /**
         * NameIDPolicy AllowCreate attribute local name.
         */
        public static final String ALLOW_CREATE = "AllowCreate";
        /**
         * Assertion element local name.
         */
        public static final String ASSERTION = "Assertion";
        /**
         * AssertionConsumerServiceIndex attribute local name.
         */
        public static final String ASSERTION_CONSUMER_SERVICE_INDEX = "AssertionConsumerServiceIndex";
        /**
         * AssertionConsumerServiceURL attribute local name.
         */
        public static final String ASSERTION_CONSUMER_SERVICE_URL = "AssertionConsumerServiceURL";
        /**
         * Attribute element local name.
         */
        public static final String ATTRIBUTE = "Attribute";
        /**
         * AttributeConsumingServiceIndex attribute local name.
         */
        public static final String ATTRIBUTE_CONSUMING_SERVICE_INDEX = "AttributeConsumingServiceIndex";
        /**
         * AttributeStatement element local name.
         */
        public static final String ATTRIBUTE_STATEMENT = "AttributeStatement";
        /**
         * AttributeValue element local name.
         */
        public static final String ATTRIBUTE_VALUE = "AttributeValue";
        /**
         * Audience element local name.
         */
        public static final String AUDIENCE = "Audience";
        /**
         * AudienceRestriction element local name.
         */
        public static final String AUDIENCE_RESTRICTION = "AudienceRestriction";
        /**
         * AuthenticatingAuthority element local name.
         */
        public static final String AUTHENTICATING_AUTHORITY = "AuthenticatingAuthority";
        /**
         * AuthnContext element local name.
         */
        public static final String AUTHN_CONTEXT = "AuthnContext";
        /**
         * AuthnContextClassRef element local name.
         */
        public static final String AUTHN_CONTEXT_CLASS_REF = "AuthnContextClassRef";
        /**
         * AuthnContextDecl element local name.
         */
        public static final String AUTHN_CONTEXT_DECL = "AuthnContextDecl";
        /**
         * AuthnContextDeclRef element local name.
         */
        public static final String AUTHN_CONTEXT_DECL_REF = "AuthnContextDeclRef";
        /**
         * AuthnInstant attribute local name.
         */
        public static final String AUTHN_INSTANT = "AuthnInstant";
        /**
         * AuthnRequest element local name.
         */
        public static final String AUTHN_REQUEST = "AuthnRequest";
        /**
         * AuthnStatement element local name.
         */
        public static final String AUTHN_STATEMENT = "AuthnStatement";
        /**
         * AuthzDecisionStatement element local name.
         */
        public static final String AUTHZ_DECISION_STATEMENT = "AuthzDecisionStatement";
        /**
         * BaseID element local name.
         */
        public static final String BASE_ID = "BaseID";
        /**
         * RequestedAuthnContext Comparison attribute local name.
         */
        public static final String COMPARISON = "Comparison";
        /**
         * Conditions element local name.
         */
        public static final String CONDITIONS = "Conditions";
        /**
         * Consent attribute local name.
         */
        public static final String CONSENT = "Consent";
        /**
         * ProxyRestriction Count attribute local name.
         */
        public static final String COUNT = "Count";
        /**
         * SubjectLocality DNSName attribute local name.
         */
        public static final String DNS_NAME = "DNSName";
        /**
         * Destination attribute local name.
         */
        public static final String DESTINATION = "Destination";
        /**
         * EncryptedAssertion element local name.
         */
        public static final String ENCRYPTED_ASSERTION = "EncryptedAssertion";
        /**
         * EncryptedAttribute element local name.
         */
        public static final String ENCRYPTED_ATTRIBUTE = "EncryptedAttribute";
        /**
         * EncryptedID element local name.
         */
        public static final String ENCRYPTED_ID = "EncryptedID";
        /**
         * Extensions element local name.
         */
        public static final String EXTENSIONS = "Extensions";
        /**
         * ForceAuthn attribute local name.
         */
        public static final String FORCE_AUTHN = "ForceAuthn";
        /**
         * NameID Format attribute local name.
         */
        public static final String FORMAT = "Format";
        /**
         * Attribute FriendlyName attribute local name.
         */
        public static final String FRIENDLY_NAME = "FriendlyName";
        /**
         * XML ID attribute local name used by SAML messages.
         */
        public static final String ID = "ID";
        /**
         * Metadata schema Id attribute local name.
         */
        public static final String METADATA_ID = "Id";
        /**
         * InResponseTo correlation attribute local name.
         */
        public static final String IN_RESPONSE_TO = "InResponseTo";
        /**
         * IsPassive attribute local name.
         */
        public static final String IS_PASSIVE = "IsPassive";
        /**
         * IssueInstant attribute local name.
         */
        public static final String ISSUE_INSTANT = "IssueInstant";
        /**
         * Issuer element local name.
         */
        public static final String ISSUER = "Issuer";
        /**
         * License element local name used by extension content.
         */
        public static final String LICENSE = "License";
        /**
         * LogoutRequest element local name.
         */
        public static final String LOGOUT_REQUEST = "LogoutRequest";
        /**
         * LogoutResponse element local name.
         */
        public static final String LOGOUT_RESPONSE = "LogoutResponse";
        /**
         * SubjectConfirmation Method attribute local name.
         */
        public static final String METHOD = "Method";
        /**
         * Attribute Name attribute local name.
         */
        public static final String NAME = "Name";
        /**
         * Attribute NameFormat attribute local name.
         */
        public static final String NAME_FORMAT = "NameFormat";
        /**
         * NameID element local name.
         */
        public static final String NAME_ID = "NameID";
        /**
         * NameIDPolicy element local name.
         */
        public static final String NAME_ID_POLICY = "NameIDPolicy";
        /**
         * NameQualifier attribute local name.
         */
        public static final String NAME_QUALIFIER = "NameQualifier";
        /**
         * NotBefore condition attribute local name.
         */
        public static final String NOT_BEFORE = "NotBefore";
        /**
         * NotOnOrAfter condition attribute local name.
         */
        public static final String NOT_ON_OR_AFTER = "NotOnOrAfter";
        /**
         * OneTimeUse condition element local name.
         */
        public static final String ONE_TIME_USE = "OneTimeUse";
        /**
         * ProtocolBinding attribute local name.
         */
        public static final String PROTOCOL_BINDING = "ProtocolBinding";
        /**
         * ProviderName attribute local name.
         */
        public static final String PROVIDER_NAME = "ProviderName";
        /**
         * ProxyRestriction condition element local name.
         */
        public static final String PROXY_RESTRICTION = "ProxyRestriction";
        /**
         * Logout Reason attribute local name.
         */
        public static final String REASON = "Reason";
        /**
         * SubjectConfirmationData Recipient attribute local name.
         */
        public static final String RECIPIENT = "Recipient";
        /**
         * RequestedAuthnContext element local name.
         */
        public static final String REQUESTED_AUTHN_CONTEXT = "RequestedAuthnContext";
        /**
         * Response element local name.
         */
        public static final String RESPONSE = "Response";
        /**
         * SPNameQualifier attribute local name.
         */
        public static final String SP_NAME_QUALIFIER = "SPNameQualifier";
        /**
         * SPProvidedID attribute local name.
         */
        public static final String SP_PROVIDED_ID = "SPProvidedID";
        /**
         * Scoping element local name.
         */
        public static final String SCOPING = "Scoping";
        /**
         * SessionIndex element or attribute local name.
         */
        public static final String SESSION_INDEX = "SessionIndex";
        /**
         * SessionNotOnOrAfter attribute local name.
         */
        public static final String SESSION_NOT_ON_OR_AFTER = "SessionNotOnOrAfter";
        /**
         * XML Signature element local name.
         */
        public static final String SIGNATURE = "Signature";
        /**
         * Abstract Statement element local name.
         */
        public static final String STATEMENT = "Statement";
        /**
         * Status element local name.
         */
        public static final String STATUS = "Status";
        /**
         * StatusCode element local name.
         */
        public static final String STATUS_CODE = "StatusCode";
        /**
         * StatusDetail element local name.
         */
        public static final String STATUS_DETAIL = "StatusDetail";
        /**
         * StatusMessage element local name.
         */
        public static final String STATUS_MESSAGE = "StatusMessage";
        /**
         * Subject element local name.
         */
        public static final String SUBJECT = "Subject";
        /**
         * SubjectConfirmation element local name.
         */
        public static final String SUBJECT_CONFIRMATION = "SubjectConfirmation";
        /**
         * SubjectConfirmationData element local name.
         */
        public static final String SUBJECT_CONFIRMATION_DATA = "SubjectConfirmationData";
        /**
         * SubjectLocality element local name.
         */
        public static final String SUBJECT_LOCALITY = "SubjectLocality";
        /**
         * StatusCode Value attribute local name.
         */
        public static final String VALUE = "Value";
        /**
         * SAML Version attribute local name.
         */
        public static final String VERSION = "Version";
        /**
         * Lowercase XML id attribute local name accepted for extension content.
         */
        public static final String ID_LOWER = "id";
        /**
         * XML namespace declaration qualified name for the xsi prefix.
         */
        public static final String XMLNS_XSI = "xmlns:xsi";
        /**
         * XML namespace declaration qualified name for the xs prefix.
         */
        public static final String XMLNS_XS = "xmlns:xs";
        /**
         * XML Schema instance type qualified name.
         */
        public static final String XSI_TYPE = "xsi:type";
        /**
         * XML Schema instance nil qualified name.
         */
        public static final String XSI_NIL = "xsi:nil";
        /**
         * XML Schema string type qualified value.
         */
        public static final String XS_STRING = "xs:string";
        /**
         * XML Schema decimal type qualified value.
         */
        public static final String XS_DECIMAL = "xs:decimal";
        /**
         * XML Schema boolean type qualified value.
         */
        public static final String XS_BOOLEAN = "xs:boolean";
        /**
         * Qualified SAML assertion element prefix.
         */
        public static final String ASSERTION_PREFIX = "saml:";
        /**
         * Qualified SAML protocol element prefix.
         */
        public static final String PROTOCOL_PREFIX = "samlp:";

        /**
         * Creates an XML local-name registry instance.
         */
        public Xml() {
            // No initialization required.
        }

    }

}
