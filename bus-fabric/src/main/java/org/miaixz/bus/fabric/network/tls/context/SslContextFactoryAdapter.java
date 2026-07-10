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
package org.miaixz.bus.fabric.network.tls.context;

import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.tls.TlsClientAuth;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.cert.CertificatePolicy;

/**
 * Adapter from SSLContext factories to current TLS context and settings APIs.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface SslContextFactoryAdapter {

    /**
     * Creates an SSL context.
     *
     * @return SSL context
     * @throws Exception when the context cannot be created
     */
    SSLContext create() throws Exception;

    /**
     * Wraps a callable SSL context factory.
     *
     * @param factory factory
     * @return adapter
     */
    static SslContextFactoryAdapter of(final Callable<SSLContext> factory) {
        if (factory == null) {
            throw new ValidateException("SSL context factory must not be null");
        }
        return factory::call;
    }

    /**
     * Wraps an existing SSL context.
     *
     * @param context SSL context
     * @return adapter
     */
    static SslContextFactoryAdapter of(final SSLContext context) {
        if (context == null) {
            throw new ValidateException("SSL context must not be null");
        }
        return () -> context;
    }

    /**
     * Creates a current TLS context.
     *
     * @return TLS context
     */
    default TlsContext tlsContext() {
        return tlsContext(this);
    }

    /**
     * Creates a current TLS context.
     *
     * @param factory factory adapter
     * @return TLS context
     */
    static TlsContext tlsContext(final SslContextFactoryAdapter factory) {
        if (factory == null) {
            throw new ValidateException("SSL context factory must not be null");
        }
        try {
            return TlsContext.of(factory.create());
        } catch (final Exception e) {
            throw new ProtocolException("Unable to create SSL context", e);
        }
    }

    /**
     * Creates TLS settings for a factory-backed context.
     *
     * @param policy     certificate policy
     * @param clientAuth client auth mode
     * @return TLS settings
     */
    static TlsSettings tlsSettings(final CertificatePolicy policy, final TlsClientAuth clientAuth) {
        if (policy == null) {
            throw new ValidateException("Certificate policy must not be null");
        }
        if (clientAuth == null) {
            throw new ValidateException("Client auth mode must not be null");
        }
        return TlsSettings.builder().certificate(policy).clientAuth(clientAuth).build();
    }

}
