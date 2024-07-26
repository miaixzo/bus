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
package org.miaixz.bus.core.net.tls;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * https 域名校验，信任所有域名 注意此类慎用，信任全部可能会有中间人攻击风险
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TrustAnyHostnameVerifier implements HostnameVerifier {

    /**
     * 单例对象
     */
    public static final TrustAnyHostnameVerifier INSTANCE = new TrustAnyHostnameVerifier();

    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;

    private TrustAnyHostnameVerifier() {
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
        return Pattern.IP_ADDRESS_PATTERN.matcher(host).matches() ? verifyIpAddress(host, certificate)
                : verifyHostname(host, certificate);
    }

    /**
     * Returns true if {@code certificate} matches {@code ipAddress}.
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
     * 返回{@code true} iff {@code hostname}匹配域名{@code pattern}.
     *
     * @param hostname 小写字母的主机名.
     * @param pattern  从证书的域名模式。可能是一个通配符模式，如{@code *.android.com}
     * @return the true/false
     */
    public boolean verifyHostname(String hostname, String pattern) {
        // 基本健康检查
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
        // 主机名和模式现在是小写的——域名不区分大小写.

        if (!pattern.contains(Symbol.STAR)) {
            // 不是通配符模式——主机名和模式必须完全匹配.
            return hostname.equals(pattern);
        }

        if ((!pattern.startsWith("*.")) || (pattern.indexOf(Symbol.C_STAR, 1) != -1)) {
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
            return false;
        }

        return true;
    }

}
