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
package org.miaixz.bus.core.net.tls;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;

/**
 * A {@link HostnameVerifier} that trusts all hostnames.
 * <p>
 * <strong>Warning:</strong> This class should be used with caution, as it effectively disables hostname verification,
 * which can expose your application to man-in-the-middle attacks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnyHostnameVerifier implements HostnameVerifier {

    /**
     * Singleton instance of {@code AnyHostnameVerifier}.
     */
    public static final AnyHostnameVerifier INSTANCE = new AnyHostnameVerifier();

    /**
     * See {@link X509Certificate#getSubjectAlternativeNames()}. DNS alternative name type.
     */
    private static final int ALT_DNS_NAME = 2;
    /**
     * See {@link X509Certificate#getSubjectAlternativeNames()}. IP address alternative name type.
     */
    private static final int ALT_IPA_NAME = 7;

    /**
     * Private constructor for singleton pattern.
     */
    private AnyHostnameVerifier() {
    }

    /**
     * Returns a list of all alternative names from the given certificate.
     *
     * @param certificate The {@link X509Certificate}.
     * @return A list of all alternative names.
     */
    public static List<String> allSubjectAltNames(X509Certificate certificate) {
        List<String> altIpaNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
        List<String> altDnsNames = getSubjectAltNames(certificate, ALT_DNS_NAME);
        List<String> result = new ArrayList<>(altIpaNames.size() + altDnsNames.size());
        result.addAll(altIpaNames);
        result.addAll(altDnsNames);
        return result;
    }

    /**
     * Gets the subject alternative names of a given type from the certificate.
     *
     * @param certificate The {@link X509Certificate}.
     * @param type        The type of the alternative name.
     * @return A list of subject alternative names.
     */
    private static List<String> getSubjectAltNames(X509Certificate certificate, int type) {
        List<String> result = new ArrayList<>();
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (null == subjectAltNames) {
                return Collections.emptyList();
            }
            for (Object subjectAltName : subjectAltNames) {
                List<?> entry = (List<?>) subjectAltName;
                if (null == entry || entry.size() < 2) {
                    continue;
                }
                Integer altNameType = (Integer) entry.get(0);
                if (null == altNameType) {
                    continue;
                }
                if (altNameType == type) {
                    String altName = (String) entry.get(1);
                    if (null != altName) {
                        result.add(altName);
                    }
                }
            }
            return result;
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Verifies that the host name is an acceptable match with the server's authentication scheme.
     *
     * @param host    the host name.
     * @param session the SSL session.
     * @return {@code true} if the host name is acceptable, {@code false} otherwise.
     */
    @Override
    public boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certificates = session.getPeerCertificates();
            return verify(host, (X509Certificate) certificates[0]);
        } catch (SSLException e) {
            return false;
        }
    }

    /**
     * Verifies the host against the given certificate.
     *
     * @param host        The host to verify.
     * @param certificate The {@link X509Certificate}.
     * @return {@code true} if the host is valid for the given certificate, {@code false} otherwise.
     */
    public boolean verify(String host, X509Certificate certificate) {
        return Pattern.IP_ADDRESS_PATTERN.matcher(host).matches() ? verifyIpAddress(host, certificate)
                : verifyHostname(host, certificate);
    }

    /**
     * Returns true if {@code certificate} matches {@code ipAddress}.
     *
     * @param ipAddress   The IP address to verify.
     * @param certificate The {@link X509Certificate}.
     * @return {@code true} if the IP address is valid for the given certificate, {@code false} otherwise.
     */
    private boolean verifyIpAddress(String ipAddress, X509Certificate certificate) {
        List<String> altNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
        for (int i = 0, size = altNames.size(); i < size; i++) {
            if (ipAddress.equalsIgnoreCase(altNames.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if {@code certificate} matches {@code hostname}.
     *
     * @param hostname    The hostname to verify.
     * @param certificate The {@link X509Certificate}.
     * @return {@code true} if the hostname is valid for the given certificate, {@code false} otherwise.
     */
    private boolean verifyHostname(String hostname, X509Certificate certificate) {
        hostname = hostname.toLowerCase(Locale.US);
        List<String> altNames = getSubjectAltNames(certificate, ALT_DNS_NAME);
        for (String altName : altNames) {
            if (verifyHostname(hostname, altName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code hostname} matches the domain name {@code pattern}.
     *
     * @param hostname The hostname in lowercase.
     * @param pattern  The domain name pattern from the certificate. It may be a wildcard pattern like
     *                 {@code *.android.com}.
     * @return {@code true} if the hostname matches the pattern, {@code false} otherwise.
     */
    public boolean verifyHostname(String hostname, String pattern) {
        // Basic health checks
        if ((null == hostname) || (hostname.length() == 0) || (hostname.startsWith(Symbol.DOT))
                || (hostname.endsWith(Symbol.DOUBLE_DOT))) {
            return false;
        }
        if ((null == pattern) || (pattern.length() == 0) || (pattern.startsWith(Symbol.DOT))
                || (pattern.endsWith(Symbol.DOUBLE_DOT))) {
            return false;
        }

        if (!hostname.endsWith(Symbol.DOT)) {
            hostname += Symbol.C_DOT;
        }
        if (!pattern.endsWith(Symbol.DOT)) {
            pattern += Symbol.C_DOT;
        }

        pattern = pattern.toLowerCase(Locale.US);
        // Hostnames and patterns are now in lowercase -- domain names are case-insensitive.

        if (!pattern.contains(Symbol.STAR)) {
            // Not a wildcard pattern -- hostname and pattern must match exactly.
            return hostname.equals(pattern);
        }

        // Wildcard pattern logic
        if ((!pattern.startsWith("*.")) || (pattern.indexOf(Symbol.C_STAR, 1) != -1)) {
            // Wildcard must be at the beginning, and only one wildcard is allowed.
            return false;
        }

        // Optimization: check whether hostname is too short to match the pattern. hostName must be at
        // least as long as the pattern because asterisk must match the whole left-most label and
        // hostname starts with a non-empty label. Thus, asterisk has to match one or more characters.
        if (hostname.length() < pattern.length()) {
            // hostname too short to match the pattern.
            return false;
        }

        if ("*.".equals(pattern)) {
            // Wildcard pattern for single-label domain name -- not permitted.
            return false;
        }

        // hostname must end with the region of pattern following the asterisk.
        String suffix = pattern.substring(1);
        if (!hostname.endsWith(suffix)) {
            // hostname does not end with the suffix
            return false;
        }

        // Check that asterisk did not match across domain name labels.
        int suffixStartIndexInHostname = hostname.length() - suffix.length();
        if ((suffixStartIndexInHostname > 0)
                && (hostname.lastIndexOf(Symbol.C_DOT, suffixStartIndexInHostname - 1) != -1)) {
            // Asterisk matched across a label boundary.
            return false;
        }

        return true;
    }

}
