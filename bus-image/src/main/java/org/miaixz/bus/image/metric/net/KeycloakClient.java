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
package org.miaixz.bus.image.metric.net;

import org.miaixz.bus.image.Device;

/**
 * Represents the KeycloakClient type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class KeycloakClient {

    /**
     * The device value.
     */
    private Device device;

    /**
     * The keycloak client id value.
     */
    private String keycloakClientID;

    /**
     * The keycloak server url value.
     */
    private String keycloakServerURL;

    /**
     * The keycloak realm value.
     */
    private String keycloakRealm;

    /**
     * The keycloak client secret value.
     */
    private String keycloakClientSecret;

    /**
     * The user id value.
     */
    private String userID;

    /**
     * The password value.
     */
    private String password;

    /**
     * The keycloak grant type value.
     */
    private GrantType keycloakGrantType = GrantType.client_credentials;

    /**
     * The tls allow any hostname value.
     */
    private boolean tlsAllowAnyHostname;

    /**
     * The tls disable trust manager value.
     */
    private boolean tlsDisableTrustManager;

    /**
     * Creates a new instance.
     */
    public KeycloakClient() {
        // No initialization required.
    }

    /**
     * Creates a new instance.
     *
     * @param keycloakClientID the keycloak client id.
     */
    public KeycloakClient(String keycloakClientID) {
        setKeycloakClientID(keycloakClientID);
    }

    /**
     * Gets the device.
     *
     * @return the device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the device.
     *
     * @param device the device.
     */
    public void setDevice(Device device) {
        if (device != null) {
            if (this.device != null)
                throw new IllegalStateException("already owned by " + this.device.getDeviceName());
        }
        this.device = device;
    }

    /**
     * Gets the keycloak client id.
     *
     * @return the keycloak client id.
     */
    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    /**
     * Sets the keycloak client id.
     *
     * @param keycloakClientID the keycloak client id.
     */
    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    /**
     * Gets the keycloak server url.
     *
     * @return the keycloak server url.
     */
    public String getKeycloakServerURL() {
        return keycloakServerURL;
    }

    /**
     * Sets the keycloak server url.
     *
     * @param keycloakServerURL the keycloak server url.
     */
    public void setKeycloakServerURL(String keycloakServerURL) {
        this.keycloakServerURL = keycloakServerURL;
    }

    /**
     * Gets the keycloak realm.
     *
     * @return the keycloak realm.
     */
    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    /**
     * Sets the keycloak realm.
     *
     * @param keycloakRealm the keycloak realm.
     */
    public void setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
    }

    /**
     * Gets the keycloak grant type.
     *
     * @return the keycloak grant type.
     */
    public GrantType getKeycloakGrantType() {
        return keycloakGrantType;
    }

    /**
     * Sets the keycloak grant type.
     *
     * @param keycloakGrantType the keycloak grant type.
     */
    public void setKeycloakGrantType(GrantType keycloakGrantType) {
        this.keycloakGrantType = keycloakGrantType;
    }

    /**
     * Gets the keycloak client secret.
     *
     * @return the keycloak client secret.
     */
    public String getKeycloakClientSecret() {
        return keycloakClientSecret;
    }

    /**
     * Sets the keycloak client secret.
     *
     * @param keycloakClientSecret the keycloak client secret.
     */
    public void setKeycloakClientSecret(String keycloakClientSecret) {
        this.keycloakClientSecret = keycloakClientSecret;
    }

    /**
     * Gets the user id.
     *
     * @return the user id.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets the user id.
     *
     * @param userID the user id.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Gets the password.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Determines whether tls allow any hostname.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isTLSAllowAnyHostname() {
        return tlsAllowAnyHostname;
    }

    /**
     * Sets the tls allow any hostname.
     *
     * @param tlsAllowAnyHostname the tls allow any hostname.
     */
    public void setTLSAllowAnyHostname(boolean tlsAllowAnyHostname) {
        this.tlsAllowAnyHostname = tlsAllowAnyHostname;
    }

    /**
     * Determines whether tls disable trust manager.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isTLSDisableTrustManager() {
        return tlsDisableTrustManager;
    }

    /**
     * Sets the tls disable trust manager.
     *
     * @param tlsDisableTrustManager the tls disable trust manager.
     */
    public void setTLSDisableTrustManager(boolean tlsDisableTrustManager) {
        this.tlsDisableTrustManager = tlsDisableTrustManager;
    }

    /**
     * Executes the clone operation.
     *
     * @return the operation result.
     */
    public KeycloakClient clone() {
        KeycloakClient clone = new KeycloakClient();
        clone.device = device;
        clone.keycloakClientID = keycloakClientID;
        clone.keycloakServerURL = keycloakServerURL;
        clone.keycloakRealm = keycloakRealm;
        clone.keycloakClientSecret = keycloakClientSecret;
        clone.userID = userID;
        clone.password = password;
        clone.keycloakGrantType = keycloakGrantType;
        clone.tlsAllowAnyHostname = tlsAllowAnyHostname;
        clone.tlsDisableTrustManager = tlsDisableTrustManager;
        return clone;
    }

    /**
     * Executes the reconfigure operation.
     *
     * @param src the src.
     */
    public void reconfigure(KeycloakClient src) {
        keycloakServerURL = src.keycloakServerURL;
        keycloakRealm = src.keycloakRealm;
        keycloakGrantType = src.keycloakGrantType;
        keycloakClientSecret = src.keycloakClientSecret;
        userID = src.userID;
        password = src.password;
        tlsAllowAnyHostname = src.tlsAllowAnyHostname;
        tlsDisableTrustManager = src.tlsDisableTrustManager;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "KeycloakClient[keycloakClientID=" + keycloakClientID + ",keycloakServerURL=" + keycloakServerURL
                + ",keycloakRealm=" + keycloakRealm + ",keycloakGrantType=" + keycloakGrantType
                + ",keycloakClientSecret=" + keycloakClientSecret + ",userID=" + userID + ",password=" + password
                + ",tlsAllowAnyHostname=" + tlsAllowAnyHostname + ",tlsDisableTrustManager=" + tlsDisableTrustManager
                + ']';
    }

    /**
     * Defines the GrantType values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum GrantType {
        /**
         * Constant for the client credentials value.
         */
        client_credentials,
        /**
         * Constant for the password value.
         */
        password

    }

}
