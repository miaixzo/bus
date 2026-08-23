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
package org.miaixz.bus.auth.source.protocol.ldap;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.protocol.ldap.client.LdapClientOptions;
import org.miaixz.bus.auth.source.protocol.ldap.server.LdapServerOptions;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Protocol;

/**
 * Exposes the standard LDAPv3 protocol-operation keys and explicit server and client driver factories.
 * <p>
 * The keys correspond one-to-one with the LDAP protocol operations defined by RFC 4511. StartTLS is represented by
 * {@link #EXTENDED}, because RFC 4511 defines it as an ExtendedRequest rather than a separate protocol operation.
 * </p>
 *
 * @author Kimi Liu
 */
public class Ldap {

    /**
     * LDAP protocol version number required by LDAPv3 Bind requests.
     */
    public static final int VERSION_3 = Normal._3;

    /**
     * RFC 4511 selector requesting every user attribute.
     */
    public static final String ALL_USER_ATTRIBUTES = "*";

    /**
     * RFC 4511 selector requesting no attributes.
     */
    public static final String NO_ATTRIBUTES = "1.1";

    /**
     * RFC 4511 StartTLS extended-operation object identifier.
     */
    public static final String START_TLS_OID = "1.3.6.1.4.1.1466.20037";

    /**
     * RFC 4511 Notice of Disconnection extended-response object identifier.
     */
    public static final String NOTICE_OF_DISCONNECTION_OID = "1.3.6.1.4.1.1466.20036";

    /**
     * BER universal BOOLEAN tag used by LDAP ASN.1 values.
     */
    public static final int BER_BOOLEAN = 0x01;

    /**
     * BER universal INTEGER tag used by LDAP ASN.1 values.
     */
    public static final int BER_INTEGER = 0x02;

    /**
     * BER universal OCTET STRING tag used by LDAP ASN.1 values.
     */
    public static final int BER_OCTET_STRING = 0x04;

    /**
     * BER universal ENUMERATED tag used by LDAP ASN.1 values.
     */
    public static final int BER_ENUMERATED = 0x0a;

    /**
     * BER constructed SEQUENCE tag used by LDAP ASN.1 values.
     */
    public static final int BER_SEQUENCE = 0x30;

    /**
     * BER constructed SET tag used by LDAP ASN.1 values.
     */
    public static final int BER_SET = 0x31;

    /**
     * Context-specific constructed tag enclosing LDAPMessage controls.
     */
    public static final int CONTROLS_TAG = 0xa0;

    /**
     * APPLICATION 0 constructed BindRequest tag.
     */
    public static final int BIND_REQUEST_TAG = 0x60;

    /**
     * APPLICATION 1 constructed BindResponse tag.
     */
    public static final int BIND_RESPONSE_TAG = 0x61;

    /**
     * APPLICATION 2 primitive UnbindRequest tag.
     */
    public static final int UNBIND_REQUEST_TAG = 0x42;

    /**
     * APPLICATION 3 constructed SearchRequest tag.
     */
    public static final int SEARCH_REQUEST_TAG = 0x63;

    /**
     * APPLICATION 4 constructed SearchResultEntry tag.
     */
    public static final int SEARCH_RESULT_ENTRY_TAG = 0x64;

    /**
     * APPLICATION 5 constructed SearchResultDone tag.
     */
    public static final int SEARCH_RESULT_DONE_TAG = 0x65;

    /**
     * APPLICATION 6 constructed ModifyRequest tag.
     */
    public static final int MODIFY_REQUEST_TAG = 0x66;

    /**
     * APPLICATION 7 constructed ModifyResponse tag.
     */
    public static final int MODIFY_RESPONSE_TAG = 0x67;

    /**
     * APPLICATION 8 constructed AddRequest tag.
     */
    public static final int ADD_REQUEST_TAG = 0x68;

    /**
     * APPLICATION 9 constructed AddResponse tag.
     */
    public static final int ADD_RESPONSE_TAG = 0x69;

    /**
     * APPLICATION 10 primitive DelRequest tag.
     */
    public static final int DELETE_REQUEST_TAG = 0x4a;

    /**
     * APPLICATION 11 constructed DelResponse tag.
     */
    public static final int DELETE_RESPONSE_TAG = 0x6b;

    /**
     * APPLICATION 12 constructed ModifyDNRequest tag.
     */
    public static final int MODIFY_DN_REQUEST_TAG = 0x6c;

    /**
     * APPLICATION 13 constructed ModifyDNResponse tag.
     */
    public static final int MODIFY_DN_RESPONSE_TAG = 0x6d;

    /**
     * APPLICATION 14 constructed CompareRequest tag.
     */
    public static final int COMPARE_REQUEST_TAG = 0x6e;

    /**
     * APPLICATION 15 constructed CompareResponse tag.
     */
    public static final int COMPARE_RESPONSE_TAG = 0x6f;

    /**
     * APPLICATION 16 primitive AbandonRequest tag.
     */
    public static final int ABANDON_REQUEST_TAG = 0x50;

    /**
     * APPLICATION 19 constructed SearchResultReference tag.
     */
    public static final int SEARCH_RESULT_REFERENCE_TAG = 0x73;

    /**
     * APPLICATION 23 constructed ExtendedRequest tag.
     */
    public static final int EXTENDED_REQUEST_TAG = 0x77;

    /**
     * APPLICATION 24 constructed ExtendedResponse tag.
     */
    public static final int EXTENDED_RESPONSE_TAG = 0x78;

    /**
     * APPLICATION 25 constructed IntermediateResponse tag.
     */
    public static final int INTERMEDIATE_RESPONSE_TAG = 0x79;

    /**
     * Context-specific primitive simple Bind authentication tag.
     */
    public static final int SIMPLE_AUTHENTICATION_TAG = 0x80;

    /**
     * Context-specific constructed SASL Bind authentication tag.
     */
    public static final int SASL_AUTHENTICATION_TAG = 0xa3;

    /**
     * Context-specific primitive BindResponse server SASL credentials tag.
     */
    public static final int SERVER_SASL_CREDENTIALS_TAG = 0x87;

    /**
     * Context-specific constructed AND filter tag.
     */
    public static final int FILTER_AND_TAG = 0xa0;

    /**
     * Context-specific constructed OR filter tag.
     */
    public static final int FILTER_OR_TAG = 0xa1;

    /**
     * Context-specific constructed NOT filter tag.
     */
    public static final int FILTER_NOT_TAG = 0xa2;

    /**
     * Context-specific constructed equalityMatch filter tag.
     */
    public static final int FILTER_EQUALITY_TAG = 0xa3;

    /**
     * Context-specific constructed substrings filter tag.
     */
    public static final int FILTER_SUBSTRINGS_TAG = 0xa4;

    /**
     * Context-specific constructed greaterOrEqual filter tag.
     */
    public static final int FILTER_GREATER_OR_EQUAL_TAG = 0xa5;

    /**
     * Context-specific constructed lessOrEqual filter tag.
     */
    public static final int FILTER_LESS_OR_EQUAL_TAG = 0xa6;

    /**
     * Context-specific primitive present filter tag.
     */
    public static final int FILTER_PRESENT_TAG = 0x87;

    /**
     * Context-specific constructed approxMatch filter tag.
     */
    public static final int FILTER_APPROXIMATE_TAG = 0xa8;

    /**
     * Context-specific constructed extensibleMatch filter tag.
     */
    public static final int FILTER_EXTENSIBLE_TAG = 0xa9;

    /**
     * Context-specific primitive initial substring tag.
     */
    public static final int SUBSTRING_INITIAL_TAG = 0x80;

    /**
     * Context-specific primitive any substring tag.
     */
    public static final int SUBSTRING_ANY_TAG = 0x81;

    /**
     * Context-specific primitive final substring tag.
     */
    public static final int SUBSTRING_FINAL_TAG = 0x82;

    /**
     * Context-specific primitive extensible-match matchingRule tag.
     */
    public static final int MATCHING_RULE_TAG = 0x81;

    /**
     * Context-specific primitive extensible-match type tag.
     */
    public static final int MATCHING_TYPE_TAG = 0x82;

    /**
     * Context-specific primitive extensible-match matchValue tag.
     */
    public static final int MATCH_VALUE_TAG = 0x83;

    /**
     * Context-specific primitive extensible-match dnAttributes tag.
     */
    public static final int DN_ATTRIBUTES_TAG = 0x84;

    /**
     * Context-specific primitive ModifyDN newSuperior tag.
     */
    public static final int NEW_SUPERIOR_TAG = 0x80;

    /**
     * Context-specific primitive ExtendedRequest requestName tag.
     */
    public static final int EXTENDED_REQUEST_NAME_TAG = 0x80;

    /**
     * Context-specific primitive ExtendedRequest requestValue tag.
     */
    public static final int EXTENDED_REQUEST_VALUE_TAG = 0x81;

    /**
     * Context-specific primitive ExtendedResponse responseName tag.
     */
    public static final int EXTENDED_RESPONSE_NAME_TAG = 0x8a;

    /**
     * Context-specific primitive ExtendedResponse responseValue tag.
     */
    public static final int EXTENDED_RESPONSE_VALUE_TAG = 0x8b;

    /**
     * Context-specific primitive IntermediateResponse responseName tag.
     */
    public static final int INTERMEDIATE_RESPONSE_NAME_TAG = 0x80;

    /**
     * Context-specific primitive IntermediateResponse responseValue tag.
     */
    public static final int INTERMEDIATE_RESPONSE_VALUE_TAG = 0x81;

    /**
     * Context-specific constructed LDAPResult referral tag.
     */
    public static final int RESULT_REFERRAL_TAG = 0xa3;

    /**
     * LDAP Bind operation key for establishing an authentication state on a connection.
     */
    public static final Capability.Key BIND = Capability.Key.standard(Protocol.LDAP, "bind");

    /**
     * LDAP Unbind operation key for terminating a protocol session without a response.
     */
    public static final Capability.Key UNBIND = Capability.Key.standard(Protocol.LDAP, "unbind");

    /**
     * LDAP Search operation key for streaming entries, references, and a final result.
     */
    public static final Capability.Key SEARCH = Capability.Key.standard(Protocol.LDAP, "search");

    /**
     * LDAP Modify operation key for changing attributes of an existing entry.
     */
    public static final Capability.Key MODIFY = Capability.Key.standard(Protocol.LDAP, "modify");

    /**
     * LDAP Add operation key for creating a directory entry.
     */
    public static final Capability.Key ADD = Capability.Key.standard(Protocol.LDAP, "add");

    /**
     * LDAP Delete operation key for removing a directory entry.
     */
    public static final Capability.Key DELETE = Capability.Key.standard(Protocol.LDAP, "delete");

    /**
     * LDAP Modify DN operation key for renaming or moving a directory entry.
     */
    public static final Capability.Key MODIFY_DN = Capability.Key.standard(Protocol.LDAP, "modify_dn");

    /**
     * LDAP Compare operation key for testing one attribute-value assertion.
     */
    public static final Capability.Key COMPARE = Capability.Key.standard(Protocol.LDAP, "compare");

    /**
     * LDAP Abandon operation key for requesting cancellation without a response.
     */
    public static final Capability.Key ABANDON = Capability.Key.standard(Protocol.LDAP, "abandon");

    /**
     * LDAP Extended operation key, including the standard StartTLS operation.
     */
    public static final Capability.Key EXTENDED = Capability.Key.standard(Protocol.LDAP, "extended");

    /**
     * Creates an LDAP protocol operation constant holder with no retained state.
     */
    public Ldap() {
        // No initialization required.
    }

    /**
     * Creates the server-side LDAPv3 driver.
     *
     * @return new LDAP Server driver
     */
    public static SourceDriver<LdapServerOptions> server() {
        return new LdapServerDriver();
    }

    /**
     * Creates the client-side LDAPv3 driver.
     *
     * @return new LDAP Client driver
     */
    public static SourceDriver<LdapClientOptions> client() {
        return new LdapClientDriver();
    }

}
