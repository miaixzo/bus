/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.secure;

import java.util.*;

/**
 * Defines the TLS cipher suites that are supported. Not all platforms support all cipher suites. This class omits
 * cipher suites that are unavailable on popular platforms for simplicity.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class CipherSuite {

    /** A map of all known cipher suites, indexed by their Java name. */
    private static final Map<String, CipherSuite> INSTANCES = new LinkedHashMap<>();

    /**
     * A {@code CipherSuite} with RSA key exchange, NULL encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_NULL_MD5 = init("SSL_RSA_WITH_NULL_MD5", 0x0001);
    /**
     * A {@code CipherSuite} with RSA key exchange, NULL encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_NULL_SHA = init("SSL_RSA_WITH_NULL_SHA", 0x0002);
    /**
     * A legacy {@code CipherSuite} with RSA Export key exchange, 40-bit RC4 encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_RSA_EXPORT_WITH_RC4_40_MD5 = init("SSL_RSA_EXPORT_WITH_RC4_40_MD5", 0x0003);
    /**
     * A {@code CipherSuite} with RSA key exchange, 128-bit RC4 encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_RC4_128_MD5 = init("SSL_RSA_WITH_RC4_128_MD5", 0x0004);
    /**
     * A {@code CipherSuite} with RSA key exchange, 128-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_RC4_128_SHA = init("SSL_RSA_WITH_RC4_128_SHA", 0x0005);
    /**
     * A legacy {@code CipherSuite} with RSA Export key exchange, 40-bit DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = init(
            "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
            0x0008);
    /**
     * A {@code CipherSuite} with RSA key exchange, DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_DES_CBC_SHA = init("SSL_RSA_WITH_DES_CBC_SHA", 0x0009);
    /**
     * A {@code CipherSuite} with RSA key exchange, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_3DES_EDE_CBC_SHA = init("SSL_RSA_WITH_3DES_EDE_CBC_SHA", 0x000a);
    /**
     * A legacy {@code CipherSuite} with DHE DSS Export key exchange, 40-bit DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = init(
            "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
            0x0011);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_DES_CBC_SHA = init("SSL_DHE_DSS_WITH_DES_CBC_SHA", 0x0012);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = init(
            "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
            0x0013);
    /**
     * A legacy {@code CipherSuite} with DHE RSA Export key exchange, 40-bit DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = init(
            "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
            0x0014);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_DES_CBC_SHA = init("SSL_DHE_RSA_WITH_DES_CBC_SHA", 0x0015);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = init(
            "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
            0x0016);
    /**
     * A legacy anonymous {@code CipherSuite} with Diffie-Hellman Export key exchange, 40-bit RC4 encryption, and MD5
     * MAC.
     */
    public static final CipherSuite TLS_DH_anon_EXPORT_WITH_RC4_40_MD5 = init(
            "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
            0x0017);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 128-bit RC4 encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_RC4_128_MD5 = init("SSL_DH_anon_WITH_RC4_128_MD5", 0x0018);
    /**
     * A legacy anonymous {@code CipherSuite} with Diffie-Hellman Export key exchange, 40-bit DES encryption, and SHA-1
     * MAC.
     */
    public static final CipherSuite TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA = init(
            "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA",
            0x0019);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_DES_CBC_SHA = init("SSL_DH_anon_WITH_DES_CBC_SHA", 0x001a);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_3DES_EDE_CBC_SHA = init(
            "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
            0x001b);
    /**
     * A {@code CipherSuite} with Kerberos key exchange, DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_KRB5_WITH_DES_CBC_SHA = init("TLS_KRB5_WITH_DES_CBC_SHA", 0x001e);
    /**
     * A {@code CipherSuite} with Kerberos key exchange, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_KRB5_WITH_3DES_EDE_CBC_SHA = init("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", 0x001f);
    /**
     * A {@code CipherSuite} with Kerberos key exchange, 128-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_KRB5_WITH_RC4_128_SHA = init("TLS_KRB5_WITH_RC4_128_SHA", 0x0020);
    /**
     * A {@code CipherSuite} with Kerberos key exchange, DES encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_KRB5_WITH_DES_CBC_MD5 = init("TLS_KRB5_WITH_DES_CBC_MD5", 0x0022);
    /**
     * A {@code CipherSuite} with Kerberos key exchange, 3DES encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_KRB5_WITH_3DES_EDE_CBC_MD5 = init("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", 0x0023);
    /**
     * A {@code CipherSuite} with Kerberos key exchange, 128-bit RC4 encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_KRB5_WITH_RC4_128_MD5 = init("TLS_KRB5_WITH_RC4_128_MD5", 0x0024);
    /**
     * A legacy {@code CipherSuite} with Kerberos Export key exchange, 40-bit DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA = init(
            "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA",
            0x0026);
    /**
     * A legacy {@code CipherSuite} with Kerberos Export key exchange, 40-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_RC4_40_SHA = init("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", 0x0028);
    /**
     * A legacy {@code CipherSuite} with Kerberos Export key exchange, 40-bit DES encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5 = init(
            "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5",
            0x0029);
    /**
     * A legacy {@code CipherSuite} with Kerberos Export key exchange, 40-bit RC4 encryption, and MD5 MAC.
     */
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_RC4_40_MD5 = init("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", 0x002b);
    /**
     * A {@code CipherSuite} with RSA key exchange, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA = init("TLS_RSA_WITH_AES_128_CBC_SHA", 0x002f);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA = init("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", 0x0032);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA = init("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", 0x0033);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA = init("TLS_DH_anon_WITH_AES_128_CBC_SHA", 0x0034);
    /**
     * A {@code CipherSuite} with RSA key exchange, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA = init("TLS_RSA_WITH_AES_256_CBC_SHA", 0x0035);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA = init("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", 0x0038);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA = init("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", 0x0039);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA = init("TLS_DH_anon_WITH_AES_256_CBC_SHA", 0x003a);
    /**
     * A {@code CipherSuite} with RSA key exchange, NULL encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_NULL_SHA256 = init("TLS_RSA_WITH_NULL_SHA256", 0x003b);
    /**
     * A {@code CipherSuite} with RSA key exchange, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA256 = init("TLS_RSA_WITH_AES_128_CBC_SHA256", 0x003c);
    /**
     * A {@code CipherSuite} with RSA key exchange, 256-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA256 = init("TLS_RSA_WITH_AES_256_CBC_SHA256", 0x003d);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA256 = init(
            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
            0x0040);
    /**
     * A {@code CipherSuite} with RSA key exchange, 128-bit Camellia encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_CAMELLIA_128_CBC_SHA = init(
            "TLS_RSA_WITH_CAMELLIA_128_CBC_SHA",
            0x0041);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 128-bit Camellia encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA = init(
            "TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA",
            0x0044);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 128-bit Camellia encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA = init(
            "TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA",
            0x0045);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA256 = init(
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            0x0067);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 256-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 = init(
            "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
            0x006a);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 256-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 = init(
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            0x006b);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA256 = init(
            "TLS_DH_anon_WITH_AES_128_CBC_SHA256",
            0x006c);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 256-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA256 = init(
            "TLS_DH_anon_WITH_AES_256_CBC_SHA256",
            0x006d);
    /**
     * A {@code CipherSuite} with RSA key exchange, 256-bit Camellia encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_CAMELLIA_256_CBC_SHA = init(
            "TLS_RSA_WITH_CAMELLIA_256_CBC_SHA",
            0x0084);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 256-bit Camellia encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA = init(
            "TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA",
            0x0087);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 256-bit Camellia encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA = init(
            "TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA",
            0x0088);
    /**
     * A {@code CipherSuite} with PSK key exchange, 128-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_PSK_WITH_RC4_128_SHA = init("TLS_PSK_WITH_RC4_128_SHA", 0x008a);
    /**
     * A {@code CipherSuite} with PSK key exchange, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_PSK_WITH_3DES_EDE_CBC_SHA = init("TLS_PSK_WITH_3DES_EDE_CBC_SHA", 0x008b);
    /**
     * A {@code CipherSuite} with PSK key exchange, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_PSK_WITH_AES_128_CBC_SHA = init("TLS_PSK_WITH_AES_128_CBC_SHA", 0x008c);
    /**
     * A {@code CipherSuite} with PSK key exchange, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_PSK_WITH_AES_256_CBC_SHA = init("TLS_PSK_WITH_AES_256_CBC_SHA", 0x008d);
    /**
     * A {@code CipherSuite} with RSA key exchange, SEED encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_SEED_CBC_SHA = init("TLS_RSA_WITH_SEED_CBC_SHA", 0x0096);
    /**
     * A {@code CipherSuite} with RSA key exchange, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_AES_128_GCM_SHA256 = init("TLS_RSA_WITH_AES_128_GCM_SHA256", 0x009c);
    /**
     * A {@code CipherSuite} with RSA key exchange, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_RSA_WITH_AES_256_GCM_SHA384 = init("TLS_RSA_WITH_AES_256_GCM_SHA384", 0x009d);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 = init(
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            0x009e);
    /**
     * A {@code CipherSuite} with DHE RSA key exchange, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 = init(
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            0x009f);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_GCM_SHA256 = init(
            "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
            0x00a2);
    /**
     * A {@code CipherSuite} with DHE DSS key exchange, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_GCM_SHA384 = init(
            "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
            0x00a3);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_GCM_SHA256 = init(
            "TLS_DH_anon_WITH_AES_128_GCM_SHA256",
            0x00a6);
    /**
     * An anonymous {@code CipherSuite} with Diffie-Hellman key exchange, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_GCM_SHA384 = init(
            "TLS_DH_anon_WITH_AES_256_GCM_SHA384",
            0x00a7);
    /**
     * A signaling {@code CipherSuite} to indicate support for secure renegotiation.
     */
    public static final CipherSuite TLS_EMPTY_RENEGOTIATION_INFO_SCSV = init(
            "TLS_EMPTY_RENEGOTIATION_INFO_SCSV",
            0x00ff);
    /**
     * A signaling {@code CipherSuite} to prevent TLS downgrade attacks.
     */
    public static final CipherSuite TLS_FALLBACK_SCSV = init("TLS_FALLBACK_SCSV", 0x5600);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, NULL encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_NULL_SHA = init("TLS_ECDH_ECDSA_WITH_NULL_SHA", 0xc001);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 128-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_RC4_128_SHA = init("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", 0xc002);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = init(
            "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
            0xc003);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = init(
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
            0xc004);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = init(
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
            0xc005);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, NULL encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_NULL_SHA = init("TLS_ECDHE_ECDSA_WITH_NULL_SHA", 0xc006);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 128-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = init("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", 0xc007);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = init(
            "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
            0xc008);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = init(
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
            0xc009);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = init(
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
            0xc00a);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, NULL encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_NULL_SHA = init("TLS_ECDH_RSA_WITH_NULL_SHA", 0xc00b);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 128-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_RC4_128_SHA = init("TLS_ECDH_RSA_WITH_RC4_128_SHA", 0xc00c);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = init(
            "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
            0xc00d);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = init(
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
            0xc00e);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = init(
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
            0xc00f);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, NULL encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_NULL_SHA = init("TLS_ECDHE_RSA_WITH_NULL_SHA", 0xc010);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 128-bit RC4 encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_RC4_128_SHA = init("TLS_ECDHE_RSA_WITH_RC4_128_SHA", 0xc011);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 3DES encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = init(
            "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
            0xc012);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = init(
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            0xc013);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = init(
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            0xc014);
    /**
     * An anonymous ECDH {@code CipherSuite} with NULL encryption and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_anon_WITH_NULL_SHA = init("TLS_ECDH_anon_WITH_NULL_SHA", 0xc015);
    /**
     * An anonymous ECDH {@code CipherSuite} with 128-bit RC4 encryption and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_anon_WITH_RC4_128_SHA = init("TLS_ECDH_anon_WITH_RC4_128_SHA", 0xc016);
    /**
     * An anonymous ECDH {@code CipherSuite} with 3DES encryption and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = init(
            "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA",
            0xc017);
    /**
     * An anonymous ECDH {@code CipherSuite} with 128-bit AES-CBC encryption and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_anon_WITH_AES_128_CBC_SHA = init(
            "TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
            0xc018);
    /**
     * An anonymous ECDH {@code CipherSuite} with 256-bit AES-CBC encryption and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDH_anon_WITH_AES_256_CBC_SHA = init(
            "TLS_ECDH_anon_WITH_AES_256_CBC_SHA",
            0xc019);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = init(
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            0xc023);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 256-bit AES-CBC encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = init(
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            0xc024);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256 = init(
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
            0xc025);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 256-bit AES-CBC encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384 = init(
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
            0xc026);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = init(
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            0xc027);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 256-bit AES-CBC encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = init(
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            0xc028);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 128-bit AES-CBC encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256 = init(
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
            0xc029);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 256-bit AES-CBC encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384 = init(
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
            0xc02a);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = init(
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            0xc02b);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = init(
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            0xc02c);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256 = init(
            "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
            0xc02d);
    /**
     * An ECDH {@code CipherSuite} with ECDSA authentication, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384 = init(
            "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
            0xc02e);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = init(
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            0xc02f);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = init(
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            0xc030);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 128-bit AES-GCM encryption, and SHA-256 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256 = init(
            "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
            0xc031);
    /**
     * An ECDH {@code CipherSuite} with RSA authentication, 256-bit AES-GCM encryption, and SHA-384 MAC.
     */
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384 = init(
            "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
            0xc032);
    /**
     * An ECDHE {@code CipherSuite} with PSK key exchange, 128-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA = init(
            "TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA",
            0xc035);
    /**
     * An ECDHE {@code CipherSuite} with PSK key exchange, 256-bit AES-CBC encryption, and SHA-1 MAC.
     */
    public static final CipherSuite TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA = init(
            "TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA",
            0xc036);
    /**
     * An ECDHE {@code CipherSuite} with RSA authentication and ChaCha20-Poly1305 encryption.
     */
    public static final CipherSuite TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 = init(
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            0xcca8);
    /**
     * An ECDHE {@code CipherSuite} with ECDSA authentication and ChaCha20-Poly1305 encryption.
     */
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 = init(
            "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
            0xcca9);
    /**
     * A DHE {@code CipherSuite} with RSA authentication and ChaCha20-Poly1305 encryption.
     */
    public static final CipherSuite TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256 = init(
            "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            0xccaa);
    /**
     * An ECDHE {@code CipherSuite} with PSK key exchange and ChaCha20-Poly1305 encryption.
     */
    public static final CipherSuite TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256 = init(
            "TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256",
            0xccac);

    /**
     * A TLS 1.3 {@code CipherSuite} with 128-bit AES-GCM encryption.
     */
    public static final CipherSuite TLS_AES_128_GCM_SHA256 = init("TLS_AES_128_GCM_SHA256", 0x1301);
    /**
     * A TLS 1.3 {@code CipherSuite} with 256-bit AES-GCM encryption.
     */
    public static final CipherSuite TLS_AES_256_GCM_SHA384 = init("TLS_AES_256_GCM_SHA384", 0x1302);
    /**
     * A TLS 1.3 {@code CipherSuite} with ChaCha20-Poly1305 encryption.
     */
    public static final CipherSuite TLS_CHACHA20_POLY1305_SHA256 = init("TLS_CHACHA20_POLY1305_SHA256", 0x1303);
    /**
     * A TLS 1.3 {@code CipherSuite} with 128-bit AES-CCM encryption.
     */
    public static final CipherSuite TLS_AES_128_CCM_SHA256 = init("TLS_AES_128_CCM_SHA256", 0x1304);
    /**
     * A TLS 1.3 {@code CipherSuite} with 128-bit AES-CCM (8-byte tag) encryption.
     */
    public static final CipherSuite TLS_AES_128_CCM_8_SHA256 = init("TLS_AES_128_CCM_8_SHA256", 0x1305);
    /**
     * A comparator that sorts cipher suite names by ignoring the {@code TLS_} or {@code SSL_} prefix. This is necessary
     * for cross-platform consistency because some JVMs (like IBM's) use {@code SSL_} prefixes where Oracle's uses
     * {@code TLS_}.
     */
    public static final Comparator<String> ORDER_BY_NAME = (a, b) -> {
        for (int i = 4, limit = Math.min(a.length(), b.length()); i < limit; i++) {
            char charA = a.charAt(i);
            char charB = b.charAt(i);
            if (charA != charB)
                return charA < charB ? -1 : 1;
        }
        int lengthA = a.length();
        int lengthB = b.length();
        if (lengthA != lengthB)
            return lengthA < lengthB ? -1 : 1;
        return 0;
    };

    /**
     * The Java name of this cipher suite, e.g., {@code SSL_RSA_WITH_RC4_128_MD5}.
     */
    public final String javaName;

    private CipherSuite(String javaName) {
        if (null == javaName) {
            throw new NullPointerException();
        }
        this.javaName = javaName;
    }

    /**
     * Returns the {@link CipherSuite} for a given Java name.
     *
     * @param javaName The name used by the Java APIs for this cipher suite. This may differ from the IANA standard name
     *                 for older suites (e.g., using "SSL_" instead of "TLS_").
     * @return The corresponding {@link CipherSuite} instance.
     */
    public static synchronized CipherSuite forJavaName(String javaName) {
        CipherSuite result = INSTANCES.get(javaName);
        if (null == result) {
            // Check for an alternative name (e.g., "SSL_" vs "TLS_").
            result = INSTANCES.get(secondaryName(javaName));

            if (null == result) {
                // If still not found, create a new instance for this unknown cipher suite.
                result = new CipherSuite(javaName);
            }

            // Store the original name for future lookups.
            INSTANCES.put(javaName, result);
        }
        return result;
    }

    /**
     * Generates a secondary name for a cipher suite by swapping the "TLS_" and "SSL_" prefixes.
     * 
     * @param javaName The original Java name.
     * @return The alternative name.
     */
    private static String secondaryName(String javaName) {
        if (javaName.startsWith("TLS_")) {
            return "SSL_" + javaName.substring(4);
        } else if (javaName.startsWith("SSL_")) {
            return "TLS_" + javaName.substring(4);
        } else {
            return javaName;
        }
    }

    /**
     * Converts an array of Java cipher suite names into a list of {@link CipherSuite} instances.
     *
     * @param cipherSuites An array of Java cipher suite names.
     * @return An unmodifiable list of {@link CipherSuite} instances.
     */
    public static List<CipherSuite> forJavaNames(String... cipherSuites) {
        List<CipherSuite> result = new ArrayList<>(cipherSuites.length);
        for (String cipherSuite : cipherSuites) {
            result.add(forJavaName(cipherSuite));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Initializes a known cipher suite and adds it to the instances map.
     *
     * @param javaName The name used by the Java APIs.
     * @param value    The integer identifier for this cipher suite (for documentation).
     * @return The new {@link CipherSuite} instance.
     */
    private static CipherSuite init(String javaName, int value) {
        CipherSuite suite = new CipherSuite(javaName);
        INSTANCES.put(javaName, suite);
        return suite;
    }

    /**
     * Returns the Java name of this cipher suite. For some older cipher suites the Java name has the prefix
     * {@code SSL_}, causing the Java name to be different from the instance name which is always prefixed {@code TLS_}.
     * For example, {@code TLS_RSA_EXPORT_WITH_RC4_40_MD5.javaName()} is {@code "SSL_RSA_EXPORT_WITH_RC4_40_MD5"}.
     *
     * @return The Java name string.
     */
    public String javaName() {
        return javaName;
    }

    @Override
    public String toString() {
        return javaName;
    }

}
