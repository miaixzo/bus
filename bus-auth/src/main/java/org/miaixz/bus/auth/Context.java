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
package org.miaixz.bus.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * Context configuration class, supporting protocols such as OAuth2, SAML, and LDAP. * This class holds various
 * configuration parameters required for authentication processes.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    /**
     * Corresponds to the key of various platforms (OAuth2: Client ID).
     */
    private String clientId;

    /**
     * Corresponds to the secret of various platforms (OAuth2: Client Secret).
     */
    private String clientSecret;

    /**
     * *
     * <p>
     * Specific identifier for different platforms:
     * </p>
     * *
     * <ul>
     * *
     * <li>1. Alipay publicKey</li> *
     * <li>2. WeChat Work, agentId of the authorized web application</li> *
     * <li>3. OktaScope Authorization server ID, defaults to 'default'</li> *
     * <li>4. MicrosoftScope Tenant ID in Entra ID (formerly Microsoft AAD)</li> *
     * <li>5. Ximalaya client package name, required if {@link Context#type} is 1 or 2.</li> *
     * <ul>
     * *
     * <li>1 - package name for Android clients</li> *
     * <li>2 - Bundle ID for iOS clients</li> *
     * </ul>
     * *
     * </ul>
     */
    private String unionId;

    /**
     * Extended ID.
     */
    private String extId;

    /**
     * Device ID, a unique identifier for the device.
     */
    private String deviceId;

    /**
     * *
     * <p>
     * Type identifier for different platforms:
     * </p>
     * *
     * <ul>
     * *
     * <li>1. WeChat Work third-party authorization user type: member | admin</li> *
     * <li>2. Ximalaya client operating system: 1-iOS, 2-Android, 3-Web</li> *
     * </ul>
     */
    private String type;

    /**
     * Currently only for QQ login. Indicates a specific flag for QQ authentication.
     */
    private boolean flag;

    /**
     * 
     * PKCE (Proof Key for Code Exchange) mode for OAuth2.
     */
    private boolean pkce;

    /**
     * Domain prefix (for CodingScope, OktaScope).
     */
    private String prefix;

    /**
     * Callback address (for OAuth2).
     */
    private String redirectUri;

    /**
     * Custom authorization platform scope content (for OAuth2).
     */
    private List<String> scopes;

    /**
     * Flag to ignore {@code state} validation.
     */
    private boolean ignoreState;

    /**
     * Flag to ignore {@code redirectUri} validation.
     */
    private boolean ignoreRedirectUri;

    /**
     * Key identifier in Apple Developer account.
     */
    private String kid;

    /**
     * Team ID in Apple Developer account.
     */
    private String teamId;

    /**
     * Parameter for the new version of WeChat Work web login.
     */
    private String loginType = "CorpApp";

    /**
     * Language code for WeChat Work platform.
     */
    private String lang = "zh";

    /**
     * Extension properties.
     */
    private String extension;

    /**
     * Dynamic endpoints for OAuth2 providers.
     */
    private Map<Endpoint, String> endpoint;

}
