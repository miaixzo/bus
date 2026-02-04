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
package org.miaixz.bus.http.socket;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.secure.CipherSuite;

/**
 * A record of a TLS handshake. This class holds information about the TLS version, cipher suite, and the certificates
 * of both the peer and the local party for an HTTPS connection.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Handshake {

    /**
     * The TLS version used for this handshake.
     */
    private final TlsVersion tlsVersion;
    /**
     * The cipher suite used for this handshake.
     */
    private final CipherSuite cipherSuite;
    /**
     * The list of certificates presented by the peer.
     */
    private final List<Certificate> peerCertificates;
    /**
     * The list of certificates presented by the local party.
     */
    private final List<Certificate> localCertificates;

    /**
     * Constructs a new Handshake instance.
     *
     * @param tlsVersion        The TLS version.
     * @param cipherSuite       The cipher suite.
     * @param peerCertificates  The list of peer certificates.
     * @param localCertificates The list of local certificates.
     */
    private Handshake(TlsVersion tlsVersion, CipherSuite cipherSuite, List<Certificate> peerCertificates,
            List<Certificate> localCertificates) {
        this.tlsVersion = tlsVersion;
        this.cipherSuite = cipherSuite;
        this.peerCertificates = peerCertificates;
        this.localCertificates = localCertificates;
    }

    /**
     * Creates a Handshake instance from an SSL session.
     *
     * @param session The SSL session.
     * @return A new Handshake instance.
     * @throws IOException if the session is invalid or parsing fails.
     */
    public static Handshake get(SSLSession session) throws IOException {
        String cipherSuiteString = session.getCipherSuite();
        if (null == cipherSuiteString) {
            throw new IllegalStateException("cipherSuite == null");
        }
        if ("SSL_NULL_WITH_NULL_NULL".equals(cipherSuiteString)) {
            throw new IOException("cipherSuite == SSL_NULL_WITH_NULL_NULL");
        }
        CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);

        String tlsVersionString = session.getProtocol();
        if (null == tlsVersionString) {
            throw new IllegalStateException("tlsVersion == null");
        }
        if ("NONE".equals(tlsVersionString))
            throw new IOException("tlsVersion == NONE");
        TlsVersion tlsVersion = TlsVersion.forJavaName(tlsVersionString);

        Certificate[] peerCertificates;
        try {
            peerCertificates = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException ignored) {
            peerCertificates = null;
        }
        List<Certificate> peerCertificatesList = null != peerCertificates ? Builder.immutableList(peerCertificates)
                : Collections.emptyList();

        Certificate[] localCertificates = session.getLocalCertificates();
        List<Certificate> localCertificatesList = null != localCertificates ? Builder.immutableList(localCertificates)
                : Collections.emptyList();

        return new Handshake(tlsVersion, cipherSuite, peerCertificatesList, localCertificatesList);
    }

    /**
     * Creates a Handshake instance from its components.
     *
     * @param tlsVersion        The TLS version.
     * @param cipherSuite       The cipher suite.
     * @param peerCertificates  The list of peer certificates.
     * @param localCertificates The list of local certificates.
     * @return A new Handshake instance.
     * @throws NullPointerException if tlsVersion or cipherSuite is null.
     */
    public static Handshake get(
            TlsVersion tlsVersion,
            CipherSuite cipherSuite,
            List<Certificate> peerCertificates,
            List<Certificate> localCertificates) {
        if (tlsVersion == null)
            throw new NullPointerException("tlsVersion == null");
        if (cipherSuite == null)
            throw new NullPointerException("cipherSuite == null");
        return new Handshake(tlsVersion, cipherSuite, Builder.immutableList(peerCertificates),
                Builder.immutableList(localCertificates));
    }

    /**
     * Returns the TLS version of the connection.
     *
     * @return The {@link TlsVersion}.
     */
    public TlsVersion tlsVersion() {
        return tlsVersion;
    }

    /**
     * Returns the cipher suite of the connection.
     *
     * @return The {@link CipherSuite}.
     */
    public CipherSuite cipherSuite() {
        return cipherSuite;
    }

    /**
     * Returns a list of certificates that identify the remote peer.
     *
     * @return An immutable list of certificates, which may be empty.
     */
    public List<Certificate> peerCertificates() {
        return peerCertificates;
    }

    /**
     * Returns the principal that identifies the remote peer.
     *
     * @return The peer's principal, or null if the peer is anonymous.
     */
    public Principal peerPrincipal() {
        return !peerCertificates.isEmpty() ? ((X509Certificate) peerCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    /**
     * Returns a list of certificates that identify this side of the connection.
     *
     * @return An immutable list of certificates, which may be empty.
     */
    public List<Certificate> localCertificates() {
        return localCertificates;
    }

    /**
     * Returns the principal that identifies this side of the connection.
     *
     * @return The local principal, or null if this side is anonymous.
     */
    public Principal localPrincipal() {
        return !localCertificates.isEmpty() ? ((X509Certificate) localCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    /**
     * Compares this Handshake object with another for equality.
     *
     * @param other The other object to compare against.
     * @return true if the two Handshake objects are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Handshake))
            return false;
        Handshake that = (Handshake) other;
        return tlsVersion.equals(that.tlsVersion) && cipherSuite.equals(that.cipherSuite)
                && peerCertificates.equals(that.peerCertificates) && localCertificates.equals(that.localCertificates);
    }

    /**
     * Computes the hash code for this Handshake object.
     *
     * @return The hash code value.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + tlsVersion.hashCode();
        result = 31 * result + cipherSuite.hashCode();
        result = 31 * result + peerCertificates.hashCode();
        result = 31 * result + localCertificates.hashCode();
        return result;
    }

    /**
     * Returns a string representation of this Handshake.
     *
     * @return A string containing TLS version, cipher suite, and certificate information.
     */
    @Override
    public String toString() {
        return "Handshake{" + "tlsVersion=" + tlsVersion + " cipherSuite=" + cipherSuite + " peerCertificates="
                + names(peerCertificates) + " localCertificates=" + names(localCertificates) + '}';
    }

    /**
     * Converts a list of certificates to a list of their subject distinguished names.
     *
     * @param certificates The list of certificates.
     * @return A list of subject DN strings.
     */
    private List<String> names(List<Certificate> certificates) {
        List<String> strings = new ArrayList<>();

        for (Certificate cert : certificates) {
            if (cert instanceof X509Certificate) {
                strings.add(String.valueOf(((X509Certificate) cert).getSubjectDN()));
            } else {
                strings.add(cert.getType());
            }
        }

        return strings;
    }

}
