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
package org.miaixz.bus.fabric.network.dns.suffix;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.IDN;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.GzipSource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Builder;

/**
 * Public suffix list backed domain boundary checks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class PublicSuffix {

    /**
     * Wildcard label marker.
     */
    private static final byte[] WILDCARD_LABEL = new byte[] { Symbol.C_STAR };

    /**
     * Empty rule value.
     */
    private static final String[] EMPTY_RULE = Normal.EMPTY_STRING_ARRAY;

    /**
     * Fallback wildcard rule.
     */
    private static final String[] PREVAILING_RULE = new String[] { Symbol.STAR };

    /**
     * True after the first load attempt.
     */
    private final AtomicBoolean listRead = new AtomicBoolean(false);

    /**
     * Coordinates concurrent first readers.
     */
    private final CountDownLatch readCompleteLatch = new CountDownLatch(Normal._1);

    /**
     * Public suffix rules encoded as UTF-8 bytes.
     */
    private byte[] publicSuffixListBytes;

    /**
     * Public suffix exception rules encoded as UTF-8 bytes.
     */
    private byte[] publicSuffixExceptionListBytes;

    /**
     * Prevents utility construction.
     */
    private PublicSuffix() {
        // No initialization required.
    }

    /**
     * Returns whether a domain is itself a public suffix.
     *
     * @param domain domain
     * @return true when the domain is not registrable as an owner domain
     */
    public static boolean isPublic(final String domain) {
        return effectiveTldPlusOne(domain) == null;
    }

    /**
     * Returns the effective top-level domain plus one.
     *
     * @param domain domain
     * @return effective TLD plus one, or null when the input is itself a public suffix or an address literal
     */
    public static String effectiveTldPlusOne(final String domain) {
        final String normalized = normalize(domain);
        if (isAddressLiteral(normalized)) {
            return null;
        }
        return instance().getEffectiveTldPlusOne(normalized);
    }

    /**
     * Returns the matching public suffix rule for a domain.
     *
     * @param domain domain
     * @return matching rule, or null for address literals
     */
    public static String match(final String domain) {
        final String normalized = normalize(domain);
        if (isAddressLiteral(normalized)) {
            return null;
        }
        return String.join(Symbol.DOT, instance().findMatchingRule(IDN.toUnicode(normalized).split("\\.")));
    }

    /**
     * Returns the shared public suffix list.
     *
     * @return public suffix list
     */
    private static PublicSuffix instance() {
        return Instances.get(PublicSuffix.class.getName(), PublicSuffix::new);
    }

    /**
     * Returns the effective top-level domain plus one by referencing the public suffix list.
     *
     * @param domain normalized punycode domain
     * @return effective TLD plus one, or null
     */
    private String getEffectiveTldPlusOne(final String domain) {
        final String unicodeDomain = IDN.toUnicode(domain);
        final String[] domainLabels = unicodeDomain.split("\\.");
        final String[] rule = findMatchingRule(domainLabels);
        if (domainLabels.length == rule.length && rule[Normal._0].charAt(Normal._0) != Symbol.C_NOT) {
            return null;
        }

        final int firstLabelOffset;
        if (rule[Normal._0].charAt(Normal._0) == Symbol.C_NOT) {
            firstLabelOffset = domainLabels.length - rule.length;
        } else {
            firstLabelOffset = domainLabels.length - (rule.length + Normal._1);
        }

        final StringBuilder effectiveTldPlusOne = new StringBuilder();
        final String[] punycodeLabels = domain.split("\\.");
        for (int i = firstLabelOffset; i < punycodeLabels.length; i++) {
            effectiveTldPlusOne.append(punycodeLabels[i]).append(Symbol.C_DOT);
        }
        effectiveTldPlusOne.deleteCharAt(effectiveTldPlusOne.length() - Normal._1);
        return effectiveTldPlusOne.toString();
    }

    /**
     * Finds the matching public suffix rule.
     *
     * @param domainLabels domain labels
     * @return matching rule
     */
    private String[] findMatchingRule(final String[] domainLabels) {
        if (!listRead.get() && listRead.compareAndSet(false, true)) {
            readTheListUninterruptibly();
        } else {
            try {
                readCompleteLatch.await();
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (this) {
            if (publicSuffixListBytes == null || publicSuffixExceptionListBytes == null) {
                throw new IllegalStateException("Unable to load " + Builder.PUBLIC_SUFFIX_PUBLIC_SUFFIX_RESOURCE
                        + " resource from the classpath.");
            }
        }

        final byte[][] domainLabelsUtf8Bytes = new byte[domainLabels.length][];
        for (int i = Normal._0; i < domainLabels.length; i++) {
            domainLabelsUtf8Bytes[i] = ByteString.encodeString(domainLabels[i], Charset.UTF_8).toByteArray();
        }

        String exactMatch = null;
        for (int i = Normal._0; i < domainLabelsUtf8Bytes.length; i++) {
            final String rule = binarySearchBytes(publicSuffixListBytes, domainLabelsUtf8Bytes, i);
            if (rule != null) {
                exactMatch = rule;
                break;
            }
        }

        String wildcardMatch = null;
        if (domainLabelsUtf8Bytes.length > Normal._1) {
            final byte[][] labelsWithWildcard = domainLabelsUtf8Bytes.clone();
            for (int labelIndex = Normal._0; labelIndex < labelsWithWildcard.length - Normal._1; labelIndex++) {
                labelsWithWildcard[labelIndex] = WILDCARD_LABEL;
                final String rule = binarySearchBytes(publicSuffixListBytes, labelsWithWildcard, labelIndex);
                if (rule != null) {
                    wildcardMatch = rule;
                    break;
                }
            }
        }

        String exception = null;
        if (wildcardMatch != null) {
            for (int labelIndex = Normal._0; labelIndex < domainLabelsUtf8Bytes.length - Normal._1; labelIndex++) {
                final String rule = binarySearchBytes(
                        publicSuffixExceptionListBytes,
                        domainLabelsUtf8Bytes,
                        labelIndex);
                if (rule != null) {
                    exception = rule;
                    break;
                }
            }
        }

        if (exception != null) {
            return (Symbol.NOT + exception).split("\\.");
        }
        if (exactMatch == null && wildcardMatch == null) {
            return PREVAILING_RULE;
        }

        final String[] exactRuleLabels = exactMatch != null ? exactMatch.split("\\.") : EMPTY_RULE;
        final String[] wildcardRuleLabels = wildcardMatch != null ? wildcardMatch.split("\\.") : EMPTY_RULE;
        return exactRuleLabels.length > wildcardRuleLabels.length ? exactRuleLabels : wildcardRuleLabels;
    }

    /**
     * Reads the list while preserving interrupted status.
     */
    private void readTheListUninterruptibly() {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    readTheList();
                    return;
                } catch (final InterruptedIOException e) {
                    Thread.interrupted();
                    interrupted = true;
                } catch (final IOException e) {
                    return;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            readCompleteLatch.countDown();
        }
    }

    /**
     * Reads the compressed public suffix resource.
     *
     * @throws IOException when the resource cannot be read
     */
    private void readTheList() throws IOException {
        final InputStream resource = PublicSuffix.class
                .getResourceAsStream(Builder.PUBLIC_SUFFIX_PUBLIC_SUFFIX_RESOURCE);
        if (resource == null) {
            return;
        }

        final byte[] publicSuffixBytes;
        final byte[] publicSuffixExceptionBytes;
        try (BufferSource bufferSource = IoKit.buffer(new GzipSource(IoKit.source(resource)))) {
            final int totalBytes = bufferSource.readInt();
            publicSuffixBytes = new byte[totalBytes];
            bufferSource.readFully(publicSuffixBytes);

            final int totalExceptionBytes = bufferSource.readInt();
            publicSuffixExceptionBytes = new byte[totalExceptionBytes];
            bufferSource.readFully(publicSuffixExceptionBytes);
        }

        synchronized (this) {
            publicSuffixListBytes = publicSuffixBytes;
            publicSuffixExceptionListBytes = publicSuffixExceptionBytes;
        }
    }

    /**
     * Normalizes a domain for suffix checks.
     *
     * @param domain domain
     * @return normalized punycode domain
     */
    private static String normalize(final String domain) {
        String normalized = domain;
        while (normalized != null && normalized.startsWith(Symbol.DOT)) {
            normalized = normalized.substring(Normal._1);
        }
        return NetKit.normalizeHost(normalized, "Public suffix domain");
    }

    /**
     * Returns whether a domain is an address literal.
     *
     * @param domain normalized domain
     * @return true when address literal
     */
    private static boolean isAddressLiteral(final String domain) {
        return domain.indexOf(Symbol.C_COLON) >= Normal._0 || Pattern.IPV4_PATTERN.matcher(domain).matches();
    }

    /**
     * Searches sorted suffix bytes for a matching rule.
     *
     * @param bytesToSearch suffix bytes
     * @param labels        domain labels
     * @param labelIndex    starting label index
     * @return matching rule, or null
     */
    private static String binarySearchBytes(final byte[] bytesToSearch, final byte[][] labels, final int labelIndex) {
        int low = Normal._0;
        int high = bytesToSearch.length;
        String match = null;
        while (low < high) {
            int mid = (low + high) / Normal._2;
            while (mid > Normal.__1 && bytesToSearch[mid] != Symbol.C_LF) {
                mid--;
            }
            mid++;

            int end = Normal._1;
            while (bytesToSearch[mid + end] != Symbol.C_LF) {
                end++;
            }
            final int publicSuffixLength = (mid + end) - mid;

            int compareResult;
            int currentLabelIndex = labelIndex;
            int currentLabelByteIndex = Normal._0;
            int publicSuffixByteIndex = Normal._0;
            boolean expectDot = false;
            while (true) {
                final int byte0;
                if (expectDot) {
                    byte0 = Symbol.C_DOT;
                    expectDot = false;
                } else {
                    byte0 = labels[currentLabelIndex][currentLabelByteIndex] & Builder.UNSIGNED_BYTE_MASK;
                }

                final int byte1 = bytesToSearch[mid + publicSuffixByteIndex] & Builder.UNSIGNED_BYTE_MASK;
                compareResult = byte0 - byte1;
                if (compareResult != Normal._0) {
                    break;
                }

                publicSuffixByteIndex++;
                currentLabelByteIndex++;
                if (publicSuffixByteIndex == publicSuffixLength) {
                    break;
                }

                if (labels[currentLabelIndex].length == currentLabelByteIndex) {
                    if (currentLabelIndex == labels.length - Normal._1) {
                        break;
                    }
                    currentLabelIndex++;
                    currentLabelByteIndex = Normal.__1;
                    expectDot = true;
                }
            }

            if (compareResult < Normal._0) {
                high = mid - Normal._1;
            } else if (compareResult > Normal._0) {
                low = mid + end + Normal._1;
            } else {
                final int publicSuffixBytesLeft = publicSuffixLength - publicSuffixByteIndex;
                int labelBytesLeft = labels[currentLabelIndex].length - currentLabelByteIndex;
                for (int i = currentLabelIndex + Normal._1; i < labels.length; i++) {
                    labelBytesLeft += labels[i].length;
                }

                if (labelBytesLeft < publicSuffixBytesLeft) {
                    high = mid - Normal._1;
                } else if (labelBytesLeft > publicSuffixBytesLeft) {
                    low = mid + end + Normal._1;
                } else {
                    match = new String(bytesToSearch, mid, publicSuffixLength, Charset.UTF_8);
                    break;
                }
            }
        }
        return match;
    }

}
