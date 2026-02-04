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
package org.miaixz.bus.http;

import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.http.plugin.httpz.*;

import javax.net.ssl.X509TrustManager;

/**
 * A utility class for sending HTTP requests with a convenient, chainable API. It supports various request methods like
 * GET, POST, PUT, HEAD, and DELETE.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpz {

    /**
     * The static client instance used for managing HTTP requests.
     */
    private static Client client = new Client();

    /**
     * Creates a new {@link HttpBuilder} instance using the default client.
     *
     * @return a new {@link HttpBuilder} instance.
     */
    public static HttpBuilder newBuilder() {
        return new HttpBuilder(client.getHttpd());
    }

    /**
     * Creates a new {@link HttpBuilder} instance using the specified client.
     *
     * @param client The {@link Httpd} client to use.
     * @return a new {@link HttpBuilder} instance.
     */
    public static HttpBuilder newBuilder(Httpd client) {
        return new HttpBuilder(client);
    }

    /**
     * Creates a new {@link GetBuilder} for constructing GET requests.
     *
     * @return a new {@link GetBuilder} instance.
     */
    public static GetBuilder get() {
        return client.get();
    }

    /**
     * Creates a new {@link PostBuilder} for constructing POST requests.
     *
     * @return a new {@link PostBuilder} instance.
     */
    public static PostBuilder post() {
        return client.post();
    }

    /**
     * Creates a new {@link PutBuilder} for constructing PUT requests.
     *
     * @return a new {@link PutBuilder} instance.
     */
    public static PutBuilder put() {
        return client.put();
    }

    /**
     * Creates a new {@link HeadBuilder} for constructing HEAD requests.
     *
     * @return a new {@link HeadBuilder} instance.
     */
    public static HeadBuilder head() {
        return client.head();
    }

    /**
     * Creates a new {@link DeleteBuilder} for constructing DELETE requests.
     *
     * @return a new {@link DeleteBuilder} instance.
     */
    public static DeleteBuilder delete() {
        return client.delete();
    }

    /**
     * Gets the currently used client instance.
     *
     * @return the {@link Client} instance.
     */
    public static Client getClient() {
        return client;
    }

    /**
     * Sets a custom client instance.
     *
     * @param httpClient The custom {@link Client} instance.
     */
    public static void setClient(Client httpClient) {
        Httpz.client = httpClient;
    }

    /**
     * The inner client class that manages the execution and cancellation of HTTP requests.
     */
    public static class Client {

        /**
         * The core HTTP client.
         */
        private Httpd httpd;

        /**
         * Default constructor that initializes the {@link Httpd} client with SSL configuration.
         */
        public Client() {
            final X509TrustManager trustManager = SSLContextBuilder.newTrustManager();
            this.httpd = new Httpd().newBuilder()
                    .sslSocketFactory(SSLContextBuilder.newSslSocketFactory(trustManager), trustManager)
                    .hostnameVerifier((hostname, session) -> true) // Trust all hostnames
                    .build();
        }

        /**
         * Initializes with a specified {@link Httpd} client.
         *
         * @param httpd The {@link Httpd} client.
         */
        public Client(Httpd httpd) {
            this.httpd = httpd;
        }

        /**
         * Cancels all queued or running requests using the default client.
         */
        public static void cancelAll() {
            cancelAll(client.getHttpd());
        }

        /**
         * Cancels all queued or running requests for a specific client.
         *
         * @param httpd The {@link Httpd} client.
         */
        public static void cancelAll(final Httpd httpd) {
            if (httpd != null) {
                // Cancel queued calls
                for (NewCall call : httpd.dispatcher().queuedCalls()) {
                    call.cancel();
                }
                // Cancel running calls
                for (NewCall call : httpd.dispatcher().runningCalls()) {
                    call.cancel();
                }
            }
        }

        /**
         * Cancels requests with a specific tag using the default client.
         *
         * @param tag The request tag.
         */
        public static void cancel(final Object tag) {
            cancel(client.getHttpd(), tag);
        }

        /**
         * Cancels requests with a specific tag for a specific client.
         *
         * @param httpd The {@link Httpd} client.
         * @param tag   The request tag.
         */
        public static void cancel(final Httpd httpd, final Object tag) {
            if (httpd != null && tag != null) {
                // Cancel queued calls with matching tag
                for (NewCall call : httpd.dispatcher().queuedCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
                // Cancel running calls with matching tag
                for (NewCall call : httpd.dispatcher().runningCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
        }

        /**
         * Creates a {@link GetBuilder} for constructing GET requests.
         *
         * @return a new {@link GetBuilder} instance.
         */
        public GetBuilder get() {
            return new GetBuilder(httpd);
        }

        /**
         * Creates a {@link PostBuilder} for constructing POST requests.
         *
         * @return a new {@link PostBuilder} instance.
         */
        public PostBuilder post() {
            return new PostBuilder(httpd);
        }

        /**
         * Creates a {@link PutBuilder} for constructing PUT requests.
         *
         * @return a new {@link PutBuilder} instance.
         */
        public PutBuilder put() {
            return new PutBuilder(httpd);
        }

        /**
         * Creates a {@link HeadBuilder} for constructing HEAD requests.
         *
         * @return a new {@link HeadBuilder} instance.
         */
        public HeadBuilder head() {
            return new HeadBuilder(httpd);
        }

        /**
         * Creates a {@link DeleteBuilder} for constructing DELETE requests.
         *
         * @return a new {@link DeleteBuilder} instance.
         */
        public DeleteBuilder delete() {
            return new DeleteBuilder(httpd);
        }

        /**
         * Gets the current {@link Httpd} client.
         *
         * @return the {@link Httpd} instance.
         */
        public Httpd getHttpd() {
            return httpd;
        }

        /**
         * Sets the {@link Httpd} client.
         *
         * @param httpd the {@link Httpd} instance.
         */
        public void setHttpd(Httpd httpd) {
            this.httpd = httpd;
        }
    }

}
