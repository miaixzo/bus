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
package org.miaixz.bus.http.secure;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.Route;
import org.miaixz.bus.http.UnoUrl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;

/**
 * An authenticator that bridges Httpd's authentication mechanism with Java's built-in {@link java.net.Authenticator}.
 * This allows the client to use credentials configured globally in the JVM.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NetAuthenticator implements Authenticator {

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        List<Challenge> challenges = response.challenges();
        Request request = response.request();
        UnoUrl url = request.url();
        boolean proxyAuthorization = response.code() == 407;
        Proxy proxy = route.proxy();

        for (int i = 0, size = challenges.size(); i < size; i++) {
            Challenge challenge = challenges.get(i);
            if (!"Basic".equalsIgnoreCase(challenge.scheme())) {
                continue;
            }

            PasswordAuthentication auth;
            if (proxyAuthorization) {
                InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
                auth = java.net.Authenticator.requestPasswordAuthentication(
                        proxyAddress.getHostName(),
                        getConnectToInetAddress(proxy, url),
                        proxyAddress.getPort(),
                        url.scheme(),
                        challenge.realm(),
                        challenge.scheme(),
                        url.url(),
                        java.net.Authenticator.RequestorType.PROXY);
            } else {
                auth = java.net.Authenticator.requestPasswordAuthentication(
                        url.host(),
                        getConnectToInetAddress(proxy, url),
                        url.port(),
                        url.scheme(),
                        challenge.realm(),
                        challenge.scheme(),
                        url.url(),
                        java.net.Authenticator.RequestorType.SERVER);
            }

            if (null != auth) {
                String credential = Credentials
                        .basic(auth.getUserName(), new String(auth.getPassword()), challenge.charset());
                return request.newBuilder()
                        .header(proxyAuthorization ? HTTP.PROXY_AUTHORIZATION : HTTP.AUTHORIZATION, credential).build();
            }
        }

        return null; // No credentials found.
    }

    /**
     * Determines the correct InetAddress to use for the authentication request based on the proxy configuration.
     *
     * @param proxy The proxy being used.
     * @param url   The request URL.
     * @return The InetAddress of the host to connect to.
     * @throws IOException if the host cannot be resolved.
     */
    private InetAddress getConnectToInetAddress(Proxy proxy, UnoUrl url) throws IOException {
        return (null != proxy && proxy.type() != Proxy.Type.DIRECT) ? ((InetSocketAddress) proxy.address()).getAddress()
                : InetAddress.getByName(url.host());
    }

}
