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
package org.miaixz.bus.auth.protocol.radius;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.protocol.radius.server.RadiusServerOptions;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.core.net.Protocol;

/**
 * Exposes the standard RADIUS Access and Accounting operation keys and the explicit Provider-driver factory.
 * <p>
 * The namespace deliberately excludes Dynamic Authorization because the runtime implements RFC 2865 Access, RFC 2866
 * Accounting, and RFC 3579 EAP pass-through operations only.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Radius {

    /**
     * Fixed number of octets in either supported RADIUS packet header.
     */
    public static final int HEADER_BYTES = 20;

    /**
     * Maximum packet size for Access and explicitly enabled RADIUS/1.1 packets.
     */
    public static final int MAXIMUM_PACKET_BYTES = 4096;

    /**
     * Maximum historic RFC 2866 Accounting packet size.
     */
    public static final int HISTORIC_ACCOUNTING_MAXIMUM_BYTES = 4095;

    /**
     * Number of octets in a historic Request or Response Authenticator.
     */
    public static final int AUTHENTICATOR_BYTES = 16;

    /**
     * Number of octets in an RFC 9765 RADIUS/1.1 correlation token.
     */
    public static final int TOKEN_BYTES = 4;

    /**
     * Maximum complete Attribute size including Type and Length.
     */
    public static final int MAXIMUM_ATTRIBUTE_BYTES = 255;

    /**
     * Maximum Attribute Value size after Type and Length.
     */
    public static final int MAXIMUM_ATTRIBUTE_VALUE_BYTES = 253;

    /**
     * Registered UDP authentication service port.
     */
    public static final int AUTHENTICATION_PORT = 1812;

    /**
     * Registered UDP accounting service port.
     */
    public static final int ACCOUNTING_PORT = 1813;

    /**
     * Standard RADIUS Access operation key.
     */
    public static final Capability.Key ACCESS = Capability.Key.standard(Protocol.RADIUS, "access");

    /**
     * Standard RADIUS Accounting operation key.
     */
    public static final Capability.Key ACCOUNTING = Capability.Key.standard(Protocol.RADIUS, "accounting");

    /**
     * Prevents construction of the RADIUS protocol-operation namespace.
     */
    private Radius() {
        // No initialization required.
    }

    /**
     * Creates the server-side RADIUS driver.
     *
     * @return new RADIUS Provider driver
     */
    public static SourceDriver<RadiusServerOptions> provider() {
        return new RadiusProviderDriver();
    }

    /**
     * Defines the packet Codes implemented by the Access and Accounting profile.
     */
    public static final class Codes {

        /**
         * Access-Request Code.
         */
        public static final int ACCESS_REQUEST = 1;
        /**
         * Access-Accept Code.
         */
        public static final int ACCESS_ACCEPT = 2;
        /**
         * Access-Reject Code.
         */
        public static final int ACCESS_REJECT = 3;
        /**
         * Accounting-Request Code.
         */
        public static final int ACCOUNTING_REQUEST = 4;
        /**
         * Accounting-Response Code.
         */
        public static final int ACCOUNTING_RESPONSE = 5;
        /**
         * Access-Challenge Code.
         */
        public static final int ACCESS_CHALLENGE = 11;

        /**
         * Prevents construction of the packet Code namespace.
         */
        private Codes() {
            // No initialization required.
        }

    }

    /**
     * Defines the registered Attribute Types used by the implemented profile.
     */
    public static final class Attributes {

        /**
         * User-Name Attribute Type.
         */
        public static final int USER_NAME = 1;
        /**
         * User-Password Attribute Type.
         */
        public static final int USER_PASSWORD = 2;
        /**
         * CHAP-Password Attribute Type.
         */
        public static final int CHAP_PASSWORD = 3;
        /**
         * NAS-IP-Address Attribute Type.
         */
        public static final int NAS_IP_ADDRESS = 4;
        /**
         * Reply-Message Attribute Type.
         */
        public static final int REPLY_MESSAGE = 18;
        /**
         * State Attribute Type.
         */
        public static final int STATE = 24;
        /**
         * Vendor-Specific Attribute Type.
         */
        public static final int VENDOR_SPECIFIC = 26;
        /**
         * NAS-Identifier Attribute Type.
         */
        public static final int NAS_IDENTIFIER = 32;
        /**
         * Proxy-State Attribute Type.
         */
        public static final int PROXY_STATE = 33;
        /**
         * Acct-Status-Type Attribute Type.
         */
        public static final int ACCT_STATUS_TYPE = 40;
        /**
         * CHAP-Challenge Attribute Type.
         */
        public static final int CHAP_CHALLENGE = 60;
        /**
         * EAP-Message Attribute Type.
         */
        public static final int EAP_MESSAGE = 79;
        /**
         * Message-Authenticator Attribute Type.
         */
        public static final int MESSAGE_AUTHENTICATOR = 80;
        /**
         * Error-Cause Attribute Type.
         */
        public static final int ERROR_CAUSE = 101;

        /**
         * Prevents construction of the Attribute Type namespace.
         */
        private Attributes() {
            // No initialization required.
        }

    }

}
