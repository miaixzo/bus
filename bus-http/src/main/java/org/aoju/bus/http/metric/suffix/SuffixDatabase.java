/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.metric.suffix;

import org.aoju.bus.core.io.BufferSource;
import org.aoju.bus.core.io.GzipSource;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.IDN;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 提供的公共后缀数据库
 * <a href="https://publicsuffix.org/">publicsuffix.org</a>.
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public final class SuffixDatabase {

    public static final String PUBLIC_SUFFIX_RESOURCE = Normal.META_DATA_INF + "/suffixes/suffixes.gz";

    private static final byte[] WILDCARD_LABEL = new byte[]{Symbol.C_STAR};
    private static final String[] EMPTY_RULE = Normal.EMPTY_STRING_ARRAY;
    private static final String[] PREVAILING_RULE = new String[]{Symbol.STAR};
    private static final byte EXCEPTION_MARKER = Symbol.C_NOT;
    private static final SuffixDatabase instance = new SuffixDatabase();
    private final AtomicBoolean listRead = new AtomicBoolean(false);
    private final CountDownLatch readCompleteLatch = new CountDownLatch(1);

    private byte[] publicSuffixListBytes;
    private byte[] publicSuffixExceptionListBytes;

    public static SuffixDatabase get() {
        return instance;
    }

    private static String binarySearchBytes(byte[] bytesToSearch, byte[][] labels, int labelIndex) {
        int low = 0;
        int high = bytesToSearch.length;
        String match = null;
        while (low < high) {
            int mid = (low + high) / 2;
            // Search for a '\n' that marks the start of a value. Don't go back past the start of the
            // array.
            while (mid > -1 && bytesToSearch[mid] != Symbol.C_LF) {
                mid--;
            }
            mid++;

            int end = 1;
            while (bytesToSearch[mid + end] != Symbol.C_LF) {
                end++;
            }
            int publicSuffixLength = (mid + end) - mid;

            // Compare the bytes. Note that the file stores UTF-8 encoded bytes, so we must compare the
            // unsigned bytes.
            int compareResult;
            int currentLabelIndex = labelIndex;
            int currentLabelByteIndex = 0;
            int publicSuffixByteIndex = 0;

            boolean expectDot = false;
            while (true) {
                int byte0;
                if (expectDot) {
                    byte0 = Symbol.C_DOT;
                    expectDot = false;
                } else {
                    byte0 = labels[currentLabelIndex][currentLabelByteIndex] & 0xff;
                }

                int byte1 = bytesToSearch[mid + publicSuffixByteIndex] & 0xff;

                compareResult = byte0 - byte1;
                if (compareResult != 0) break;

                publicSuffixByteIndex++;
                currentLabelByteIndex++;
                if (publicSuffixByteIndex == publicSuffixLength) break;

                if (labels[currentLabelIndex].length == currentLabelByteIndex) {
                    // We've exhausted our current label. Either there are more labels to compare, in which
                    // case we expect a dot as the next character. Otherwise, we've checked all our labels.
                    if (currentLabelIndex == labels.length - 1) {
                        break;
                    } else {
                        currentLabelIndex++;
                        currentLabelByteIndex = -1;
                        expectDot = true;
                    }
                }
            }

            if (compareResult < 0) {
                high = mid - 1;
            } else if (compareResult > 0) {
                low = mid + end + 1;
            } else {
                // We found a match, but are the lengths equal?
                int publicSuffixBytesLeft = publicSuffixLength - publicSuffixByteIndex;
                int labelBytesLeft = labels[currentLabelIndex].length - currentLabelByteIndex;
                for (int i = currentLabelIndex + 1; i < labels.length; i++) {
                    labelBytesLeft += labels[i].length;
                }

                if (labelBytesLeft < publicSuffixBytesLeft) {
                    high = mid - 1;
                } else if (labelBytesLeft > publicSuffixBytesLeft) {
                    low = mid + end + 1;
                } else {
                    // Found a match.
                    match = new String(bytesToSearch, mid, publicSuffixLength, Charset.UTF_8);
                    break;
                }
            }
        }
        return match;
    }

    public String getEffectiveTldPlusOne(String domain) {
        if (domain == null) throw new NullPointerException("domain == null");

        String unicodeDomain = IDN.toUnicode(domain);
        String[] domainLabels = unicodeDomain.split("\\.");
        String[] rule = findMatchingRule(domainLabels);
        if (domainLabels.length == rule.length && rule[0].charAt(0) != EXCEPTION_MARKER) {
            return null;
        }

        int firstLabelOffset;
        if (rule[0].charAt(0) == EXCEPTION_MARKER) {
            firstLabelOffset = domainLabels.length - rule.length;
        } else {
            firstLabelOffset = domainLabels.length - (rule.length + 1);
        }

        StringBuilder effectiveTldPlusOne = new StringBuilder();
        String[] punycodeLabels = domain.split("\\.");
        for (int i = firstLabelOffset; i < punycodeLabels.length; i++) {
            effectiveTldPlusOne.append(punycodeLabels[i]).append(Symbol.C_DOT);
        }
        effectiveTldPlusOne.deleteCharAt(effectiveTldPlusOne.length() - 1);

        return effectiveTldPlusOne.toString();
    }

    private String[] findMatchingRule(String[] domainLabels) {
        if (!listRead.get() && listRead.compareAndSet(false, true)) {
            readTheListUninterruptibly();
        } else {
            try {
                readCompleteLatch.await();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (this) {
            if (publicSuffixListBytes == null) {
                throw new IllegalStateException("Unable to load " + PUBLIC_SUFFIX_RESOURCE + " resource "
                        + "from the classpath.");
            }
        }

        byte[][] domainLabelsUtf8Bytes = new byte[domainLabels.length][];
        for (int i = 0; i < domainLabels.length; i++) {
            domainLabelsUtf8Bytes[i] = domainLabels[i].getBytes(Charset.UTF_8);
        }

        String exactMatch = null;
        for (int i = 0; i < domainLabelsUtf8Bytes.length; i++) {
            String rule = binarySearchBytes(publicSuffixListBytes, domainLabelsUtf8Bytes, i);
            if (rule != null) {
                exactMatch = rule;
                break;
            }
        }

        String wildcardMatch = null;
        if (domainLabelsUtf8Bytes.length > 1) {
            byte[][] labelsWithWildcard = domainLabelsUtf8Bytes.clone();
            for (int labelIndex = 0; labelIndex < labelsWithWildcard.length - 1; labelIndex++) {
                labelsWithWildcard[labelIndex] = WILDCARD_LABEL;
                String rule = binarySearchBytes(publicSuffixListBytes, labelsWithWildcard, labelIndex);
                if (rule != null) {
                    wildcardMatch = rule;
                    break;
                }
            }
        }

        String exception = null;
        if (wildcardMatch != null) {
            for (int labelIndex = 0; labelIndex < domainLabelsUtf8Bytes.length - 1; labelIndex++) {
                String rule = binarySearchBytes(
                        publicSuffixExceptionListBytes, domainLabelsUtf8Bytes, labelIndex);
                if (rule != null) {
                    exception = rule;
                    break;
                }
            }
        }

        if (exception != null) {
            exception = Symbol.NOT + exception;
            return exception.split("\\.");
        } else if (exactMatch == null && wildcardMatch == null) {
            return PREVAILING_RULE;
        }

        String[] exactRuleLabels = exactMatch != null
                ? exactMatch.split("\\.")
                : EMPTY_RULE;

        String[] wildcardRuleLabels = wildcardMatch != null
                ? wildcardMatch.split("\\.")
                : EMPTY_RULE;

        return exactRuleLabels.length > wildcardRuleLabels.length
                ? exactRuleLabels
                : wildcardRuleLabels;
    }

    private void readTheListUninterruptibly() {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    readTheList();
                    return;
                } catch (InterruptedIOException e) {
                    Thread.interrupted();
                    interrupted = true;
                } catch (IOException e) {
                    Logger.warn("Failed to read public suffix list", e);
                    return;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void readTheList() throws IOException {
        byte[] publicSuffixListBytes;
        byte[] publicSuffixExceptionListBytes;

        InputStream resource = SuffixDatabase.class.getResourceAsStream(PUBLIC_SUFFIX_RESOURCE);
        if (resource == null) return;

        BufferSource bufferedSource = IoUtils.buffer(new GzipSource(IoUtils.source(resource)));
        try {
            int totalBytes = bufferedSource.readInt();
            publicSuffixListBytes = new byte[totalBytes];
            bufferedSource.readFully(publicSuffixListBytes);

            int totalExceptionBytes = bufferedSource.readInt();
            publicSuffixExceptionListBytes = new byte[totalExceptionBytes];
            bufferedSource.readFully(publicSuffixExceptionListBytes);
        } finally {
            IoUtils.close(bufferedSource);
        }

        synchronized (this) {
            this.publicSuffixListBytes = publicSuffixListBytes;
            this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
        }

        readCompleteLatch.countDown();
    }

    void setListBytes(byte[] publicSuffixListBytes, byte[] publicSuffixExceptionListBytes) {
        this.publicSuffixListBytes = publicSuffixListBytes;
        this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
        listRead.set(true);
        readCompleteLatch.countDown();
    }

}
