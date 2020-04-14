/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.http.secure;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.Builder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * 一个与RFC 2818一致的HostnameVerifier
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
public final class OkHostnameVerifier implements HostnameVerifier {

    public static final OkHostnameVerifier INSTANCE = new OkHostnameVerifier();

    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;

    private OkHostnameVerifier() {
    }

    public static List<String> allSubjectAltNames(X509Certificate certificate) {
        List<String> altIpaNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
        List<String> altDnsNames = getSubjectAltNames(certificate, ALT_DNS_NAME);
        List<String> result = new ArrayList<>(altIpaNames.size() + altDnsNames.size());
        result.addAll(altIpaNames);
        result.addAll(altDnsNames);
        return result;
    }

    private static List<String> getSubjectAltNames(X509Certificate certificate, int type) {
        List<String> result = new ArrayList<>();
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                return Collections.emptyList();
            }
            for (Object subjectAltName : subjectAltNames) {
                List<?> entry = (List<?>) subjectAltName;
                if (entry == null || entry.size() < 2) {
                    continue;
                }
                Integer altNameType = (Integer) entry.get(0);
                if (altNameType == null) {
                    continue;
                }
                if (altNameType == type) {
                    String altName = (String) entry.get(1);
                    if (altName != null) {
                        result.add(altName);
                    }
                }
            }
            return result;
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certificates = session.getPeerCertificates();
            return verify(host, (X509Certificate) certificates[0]);
        } catch (SSLException e) {
            return false;
        }
    }

    public boolean verify(String host, X509Certificate certificate) {
        return Builder.verifyAsIpAddress(host)
                ? verifyIpAddress(host, certificate)
                : verifyHostname(host, certificate);
    }

    private boolean verifyIpAddress(String ipAddress, X509Certificate certificate) {
        List<String> altNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
        for (int i = 0, size = altNames.size(); i < size; i++) {
            if (ipAddress.equalsIgnoreCase(altNames.get(i))) {
                return true;
            }
        }
        return false;
    }

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
     * 返回{@code true} iff {@code hostname}匹配域名{@code pattern}.
     *
     * @param hostname 小写字母的主机名.
     * @param pattern  从证书的域名模式。可能是一个通配符模式，如{@code *.android.com}
     * @return the true/false
     */
    public boolean verifyHostname(String hostname, String pattern) {
        // 基本健康检查
        if ((hostname == null) || (hostname.length() == 0) || (hostname.startsWith(Symbol.DOT))
                || (hostname.endsWith(Symbol.DOUBLE_DOT))) {
            return false;
        }
        if ((pattern == null) || (pattern.length() == 0) || (pattern.startsWith(Symbol.DOT))
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
        // 主机名和模式现在是小写的——域名不区分大小写.

        if (!pattern.contains(Symbol.STAR)) {
            // 不是通配符模式——主机名和模式必须完全匹配.
            return hostname.equals(pattern);
        }

        if ((!pattern.startsWith("*.")) || (pattern.indexOf(Symbol.C_STAR, 1) != -1)) {
            return false;
        }

        if (hostname.length() < pattern.length()) {
            return false;
        }

        if ("*.".equals(pattern)) {
            return false;
        }

        String suffix = pattern.substring(1);
        if (!hostname.endsWith(suffix)) {
            return false;
        }

        int suffixStartIndexInHostname = hostname.length() - suffix.length();
        if ((suffixStartIndexInHostname > 0)
                && (hostname.lastIndexOf(Symbol.C_DOT, suffixStartIndexInHostname - 1) != -1)) {
            return false;
        }

        return true;
    }

}
