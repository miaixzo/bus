/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.core.net.Protocol;

/**
 * API configuration interface for OAuth and other protocol platforms. Provides specific configurations and provider
 * classes for protocols like OAuth2, SAML, LDAP, etc., to achieve a unified authentication framework. Implementations
 * of this interface should provide the following functionalities:
 * 
 * <pre>
 * 1) {@link #endpoint()}: Returns protocol-specific configurations (e.g., OAuth2 endpoints, LDAP server information, SAML SSO endpoints).
 * 2) {@link #getProtocol()}: Returns the protocol type (e.g., OAUTH2, SAML, LDAP).
 * 3) {@link #getTargetClass()}: Returns the corresponding provider implementation class.
 * </pre>
 * 
 * Note: When extending third-party authorization, this interface must be implemented and registered in the
 * {@link Registry} enum.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Complex {

    /**
     * Retrieves protocol-specific endpoint configurations. The content of the configuration is defined according to the
     * protocol type, for example: - OAuth2: Contains endpoint URLs such as AUTHORIZE, TOKEN, USERINFO. - SAML: Contains
     * ssoEndpoint, metadataUrl, etc. - LDAP: Usually returns an empty map, using {@link Context} for configuration.
     *
     * @return a map of configuration key-value pairs, an empty map by default
     */
    default Map<Endpoint, String> endpoint() {
        return new HashMap<>();
    }

    /**
     * Retrieves the authorization endpoint URL from the protocol-specific configurations.
     *
     * @return the authorization endpoint URL
     */
    default String authorize() {
        return this.endpoint().get(Endpoint.AUTHORIZE);
    }

    /**
     * Retrieves the access token endpoint URL from the protocol-specific configurations.
     *
     * @return the access token endpoint URL
     */
    default String token() {
        return this.endpoint().get(Endpoint.TOKEN);
    }

    /**
     * Retrieves the user info endpoint URL from the protocol-specific configurations.
     *
     * @return the user info endpoint URL
     */
    default String userinfo() {
        return this.endpoint().get(Endpoint.USERINFO);
    }

    /**
     * Retrieves the refresh token endpoint URL from the protocol-specific configurations.
     *
     * @return the refresh token endpoint URL
     */
    default String refresh() {
        return this.endpoint().get(Endpoint.REFRESH);
    }

    /**
     * Retrieves the revoke token endpoint URL from the protocol-specific configurations.
     *
     * @return the revoke token endpoint URL
     */
    default String revoke() {
        return this.endpoint().get(Endpoint.REVOKE);
    }

    /**
     * Retrieves the protocol type. Used to identify the authentication protocol, such as OAUTH2, SAML, LDAP.
     *
     * @return the protocol type
     */
    Protocol getProtocol();

    /**
     * Retrieves the corresponding provider implementation class. The provider class must extend
     * {@link AbstractProvider} and handle protocol-specific authentication logic.
     *
     * @return the provider class
     */
    Class<? extends AbstractProvider> getTargetClass();

    /**
     * Retrieves the string name of the source. Typically the enum name, used to identify the authentication source
     * (e.g., TWITTER, SAML_EXAMPLE). If it's not an enum implementation, it returns the simple class name.
     *
     * @return the source name
     */
    default String getName() {
        if (this instanceof Enum) {
            return String.valueOf(this);
        }
        return this.getClass().getSimpleName();
    }

}
