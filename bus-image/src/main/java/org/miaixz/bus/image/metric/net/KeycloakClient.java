/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
                throw new IllegalStateException("already owned by " +
                        this.device.getDeviceName());
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
        return "KeycloakClient[keycloakClientID=" + keycloakClientID
                + ",keycloakServerURL=" + keycloakServerURL
                + ",keycloakRealm=" + keycloakRealm
                + ",keycloakGrantType=" + keycloakGrantType
                + ",keycloakClientSecret=" + keycloakClientSecret
                + ",userID=" + userID
                + ",password=" + password
                + ",tlsAllowAnyHostname=" + tlsAllowAnyHostname
                + ",tlsDisableTrustManager=" + tlsDisableTrustManager
                + ']';
    }

    public enum GrantType {
        client_credentials, password
    }

}
