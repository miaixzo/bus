package org.miaixz.bus.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines the standard endpoints for various authentication protocols. These endpoints are used to configure the URLs
 * for authorization, token exchange, user information retrieval, token refreshing, and token revocation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum Endpoint {

    /**
     * Configuration key for the authorization endpoint.
     */
    AUTHORIZE,
    /**
     * Configuration key for the access token endpoint.
     */
    ACCESS_TOKEN,
    /**
     * Configuration key for the user information endpoint.
     */
    USERINFO,
    /**
     * Configuration key for the refresh token endpoint.
     */
    REFRESH,
    /**
     * Configuration key for the revoke token endpoint.
     */
    REVOKE
}
