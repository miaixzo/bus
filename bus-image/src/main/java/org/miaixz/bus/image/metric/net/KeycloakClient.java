/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.net;

import org.miaixz.bus.image.Device;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class KeycloakClient {

    private Device device;
    private String keycloakClientID;
    private String keycloakServerURL;
    private String keycloakRealm;
    private String keycloakClientSecret;
    private String userID;
    private String password;
    private GrantType keycloakGrantType = GrantType.client_credentials;
    private boolean tlsAllowAnyHostname;
    private boolean tlsDisableTrustManager;

    public KeycloakClient() {
    }

    public KeycloakClient(String keycloakClientID) {
        setKeycloakClientID(keycloakClientID);
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        if (device != null) {
            if (this.device != null)
                throw new IllegalStateException("already owned by " + this.device.getDeviceName());
        }
        this.device = device;
    }

    public String getKeycloakClientID() {
        return keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public String getKeycloakServerURL() {
        return keycloakServerURL;
    }

    public void setKeycloakServerURL(String keycloakServerURL) {
        this.keycloakServerURL = keycloakServerURL;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public void setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
    }

    public GrantType getKeycloakGrantType() {
        return keycloakGrantType;
    }

    public void setKeycloakGrantType(GrantType keycloakGrantType) {
        this.keycloakGrantType = keycloakGrantType;
    }

    public String getKeycloakClientSecret() {
        return keycloakClientSecret;
    }

    public void setKeycloakClientSecret(String keycloakClientSecret) {
        this.keycloakClientSecret = keycloakClientSecret;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTLSAllowAnyHostname() {
        return tlsAllowAnyHostname;
    }

    public void setTLSAllowAnyHostname(boolean tlsAllowAnyHostname) {
        this.tlsAllowAnyHostname = tlsAllowAnyHostname;
    }

    public boolean isTLSDisableTrustManager() {
        return tlsDisableTrustManager;
    }

    public void setTLSDisableTrustManager(boolean tlsDisableTrustManager) {
        this.tlsDisableTrustManager = tlsDisableTrustManager;
    }

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

    @Override
    public String toString() {
        return "KeycloakClient[keycloakClientID=" + keycloakClientID + ",keycloakServerURL=" + keycloakServerURL
                + ",keycloakRealm=" + keycloakRealm + ",keycloakGrantType=" + keycloakGrantType
                + ",keycloakClientSecret=" + keycloakClientSecret + ",userID=" + userID + ",password=" + password
                + ",tlsAllowAnyHostname=" + tlsAllowAnyHostname + ",tlsDisableTrustManager=" + tlsDisableTrustManager
                + ']';
    }

    public enum GrantType {
        client_credentials, password
    }

}
