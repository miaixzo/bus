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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
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
     * @return SSL context produced for the caller
     * @throws Exception when context creation fails
     */
    SSLContext create() throws Exception;

    /**
     * Wraps a callable SSL context factory.
     *
     * @param factory non-null callable invoked for each {@link #create()} request
     * @return adapter that delegates context creation to the callable
     */
    static SslContextFactoryAdapter of(final Callable<SSLContext> factory) {
        return Assert.notNull(factory, () -> new ValidateException("SSL context factory must not be null"))::call;
    }

    /**
     * Wraps an existing SSL context.
     *
     * @param context non-null SSL context to reuse
     * @return adapter that returns the same context instance for every request
     */
    static SslContextFactoryAdapter of(final SSLContext context) {
        final SSLContext checkedContext = Assert
                .notNull(context, () -> new ValidateException("SSL context must not be null"));
        return () -> checkedContext;
    }

    /**
     * Creates a current TLS context.
     *
     * @return validated fabric TLS context created from this adapter's SSL context
     */
    default TlsContext tlsContext() {
        return tlsContext(this);
    }

    /**
     * Creates a current TLS context.
     *
     * @param factory non-null adapter used to create the underlying SSL context
     * @return validated fabric TLS context
     */
    static TlsContext tlsContext(final SslContextFactoryAdapter factory) {
        try {
            return TlsContext.of(
                    Assert.notNull(factory, () -> new ValidateException("SSL context factory must not be null"))
                            .create());
        } catch (final Exception e) {
            throw new ProtocolException("Unable to create SSL context", e);
        }
    }

    /**
     * Creates TLS settings with explicit certificate and client-authentication policies.
     *
     * @param policy     non-null certificate validation policy
     * @param clientAuth non-null client-authentication mode
     * @return immutable TLS settings containing the supplied policies
     */
    static TlsSettings tlsSettings(final CertificatePolicy policy, final TlsClientAuth clientAuth) {
        return TlsSettings.builder()
                .certificate(Assert.notNull(policy, () -> new ValidateException("Certificate policy must not be null")))
                .clientAuth(
                        Assert.notNull(clientAuth, () -> new ValidateException("Client auth mode must not be null")))
                .build();
    }

}
