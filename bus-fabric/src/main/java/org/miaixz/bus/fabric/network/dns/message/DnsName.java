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
package org.miaixz.bus.fabric.network.dns.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * DNS domain-name normalization and wire codec utilities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsName {

    /**
     * Root domain in canonical textual form.
     */
    public static final String ROOT = Symbol.DOT;

    /**
     * Wildcard owner label in canonical textual form.
     */
    public static final String WILDCARD = Symbol.STAR + ROOT;

    /**
     * Regular expression that splits DNS labels on the root separator dot.
     */
    private static final String LABEL_SEPARATOR_REGEX = "\\.";

    /**
     * Maximum DNS name length including the root terminator.
     */
    private static final int MAX_NAME_LENGTH = 255;

    /**
     * Maximum DNS label length.
     */
    private static final int MAX_LABEL_LENGTH = 63;

    /**
     * Maximum compression-pointer hops accepted while decoding one name.
     */
    private static final int MAX_POINTER_HOPS = 32;

    /**
     * Prevents instantiation of this utility class.
     */
    private DnsName() {
    }

    /**
     * Normalizes a textual DNS name into lowercase absolute form.
     *
     * @param name textual DNS name
     * @return lowercase absolute DNS name ending with a dot
     * @throws ValidateException if the name is blank, too long, or contains an invalid label
     */
    public static String normalize(final String name) {
        if (name == null) {
            throw new ValidateException("DNS name must not be null");
        }
        final String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new ValidateException("DNS name must not be blank");
        }
        if (ROOT.equals(trimmed)) {
            return ROOT;
        }
        final String withoutRoot = trimmed.endsWith(ROOT) ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        final String[] labels = withoutRoot.split(LABEL_SEPARATOR_REGEX, -1);
        final ArrayList<String> normalized = new ArrayList<>(labels.length);
        int length = 1;
        for (final String label : labels) {
            if (label.isEmpty()) {
                throw new ValidateException("DNS name contains an empty label");
            }
            final String ascii = IDN.toASCII(label).toLowerCase(Locale.ROOT);
            final int bytes = ascii.getBytes(StandardCharsets.US_ASCII).length;
            if (bytes == 0 || bytes > MAX_LABEL_LENGTH) {
                throw new ValidateException("DNS label length is out of range");
            }
            length += bytes + 1;
            normalized.add(ascii);
        }
        if (length > MAX_NAME_LENGTH) {
            throw new ValidateException("DNS name length exceeds 255 bytes");
        }
        return String.join(ROOT, normalized) + ROOT;
    }

    /**
     * Returns whether a candidate name lies at or below an origin.
     *
     * @param name   candidate DNS name
     * @param origin origin DNS name
     * @return true when the normalized candidate equals the origin or ends below it
     */
    public static boolean inZone(final String name, final String origin) {
        final String normalizedName = normalize(name);
        final String normalizedOrigin = normalize(origin);
        return normalizedName.equals(normalizedOrigin) || normalizedName.endsWith(ROOT + normalizedOrigin);
    }

    /**
     * Returns whether a candidate name is below an origin, excluding the origin itself.
     *
     * @param name   candidate DNS name
     * @param origin origin DNS name
     * @return true when the normalized candidate is a descendant of the normalized origin
     */
    public static boolean descendantOf(final String name, final String origin) {
        final String normalizedName = normalize(name);
        final String normalizedOrigin = normalize(origin);
        return !normalizedName.equals(normalizedOrigin) && normalizedName.endsWith(ROOT + normalizedOrigin);
    }

    /**
     * Creates a canonical absolute DNS name from a label slice.
     *
     * @param labels        source labels without the root label
     * @param fromInclusive first label index
     * @param toExclusive   end label index
     * @return canonical DNS name ending with the root dot
     */
    public static String fromLabels(final String[] labels, final int fromInclusive, final int toExclusive) {
        final String[] slice = labelSlice(labels, fromInclusive, toExclusive);
        if (slice.length == Normal._0) {
            return ROOT;
        }
        return normalize(String.join(ROOT, slice) + ROOT);
    }

    /**
     * Creates a canonical wildcard DNS owner from a label slice.
     *
     * @param labels        source labels without the root label
     * @param fromInclusive first suffix label index
     * @param toExclusive   end label index
     * @return canonical wildcard DNS owner
     */
    public static String wildcardFromLabels(final String[] labels, final int fromInclusive, final int toExclusive) {
        final String suffix = fromLabels(labels, fromInclusive, toExclusive);
        return ROOT.equals(suffix) ? WILDCARD : normalize(Symbol.STAR + ROOT + suffix);
    }

    /**
     * Splits a canonical DNS name into labels without the root label.
     *
     * @param name textual DNS name
     * @return labels excluding the root label
     */
    public static String[] labels(final String name) {
        final String normalized = normalize(name);
        if (ROOT.equals(normalized)) {
            return Normal.EMPTY_STRING_ARRAY;
        }
        return normalized.substring(Normal._0, normalized.length() - Normal._1).split(LABEL_SEPARATOR_REGEX);
    }

    /**
     * Copies and validates a label slice.
     *
     * @param labels        source labels
     * @param fromInclusive first label index
     * @param toExclusive   end label index
     * @return copied label slice
     */
    private static String[] labelSlice(final String[] labels, final int fromInclusive, final int toExclusive) {
        if (labels == null) {
            throw new ValidateException("DNS labels must not be null");
        }
        if (fromInclusive < Normal._0 || toExclusive < fromInclusive || toExclusive > labels.length) {
            throw new ValidateException("DNS label slice is out of range");
        }
        return Arrays.copyOfRange(labels, fromInclusive, toExclusive);
    }

    /**
     * Encodes a textual DNS name without compression.
     *
     * @param output target output stream
     * @param name   textual DNS name
     * @throws ProtocolException if the output stream rejects the encoded name
     */
    public static void write(final OutputStream output, final String name) {
        final String normalized = normalize(name);
        try {
            if (ROOT.equals(normalized)) {
                output.write(0);
                return;
            }
            final String[] labels = labels(normalized);
            for (final String label : labels) {
                final byte[] bytes = label.getBytes(StandardCharsets.US_ASCII);
                output.write(bytes.length);
                output.write(bytes);
            }
            output.write(0);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode DNS name", e);
        }
    }

    /**
     * Encodes a textual DNS name into a byte array.
     *
     * @param name textual DNS name
     * @return DNS wire-format name bytes
     */
    public static byte[] wire(final String name) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(output, name);
        return output.toByteArray();
    }

    /**
     * Reads a possibly compressed DNS name from a complete DNS message.
     *
     * @param message complete DNS message
     * @param offset  byte offset at which the name starts
     * @return decoded name and next offset for the caller's linear parser
     * @throws ProtocolException if the name is truncated, malformed, or uses a pointer loop
     */
    public static ReadResult read(final byte[] message, final int offset) {
        if (message == null) {
            throw new ProtocolException("DNS message must not be null");
        }
        if (offset < 0 || offset >= message.length) {
            throw new ProtocolException("DNS name offset is out of range");
        }
        final ArrayList<String> labels = new ArrayList<>();
        int cursor = offset;
        int nextOffset = -1;
        int hops = 0;
        int length = 1;
        while (true) {
            if (cursor >= message.length) {
                throw new ProtocolException("DNS name exceeds message length");
            }
            final int lengthOctet = message[cursor] & 0xff;
            if ((lengthOctet & 0xc0) == 0xc0) {
                if (cursor + 1 >= message.length) {
                    throw new ProtocolException("DNS compression pointer is truncated");
                }
                if (++hops > MAX_POINTER_HOPS) {
                    throw new ProtocolException("DNS compression pointer depth exceeded");
                }
                final int pointer = ((lengthOctet & 0x3f) << 8) | (message[cursor + 1] & 0xff);
                if (pointer >= message.length) {
                    throw new ProtocolException("DNS compression pointer is out of range");
                }
                if (nextOffset < 0) {
                    nextOffset = cursor + 2;
                }
                cursor = pointer;
                continue;
            }
            if ((lengthOctet & 0xc0) != 0) {
                throw new ProtocolException("DNS label uses an unsupported length prefix");
            }
            cursor++;
            if (lengthOctet == 0) {
                final int endOffset = nextOffset < 0 ? cursor : nextOffset;
                final String name = labels.isEmpty() ? ROOT : String.join(ROOT, labels) + ROOT;
                return new ReadResult(normalize(name), endOffset);
            }
            if (lengthOctet > MAX_LABEL_LENGTH || cursor + lengthOctet > message.length) {
                throw new ProtocolException("DNS label length is invalid");
            }
            final String label = new String(message, cursor, lengthOctet, StandardCharsets.US_ASCII);
            labels.add(label);
            length += lengthOctet + 1;
            if (length > MAX_NAME_LENGTH) {
                throw new ProtocolException("DNS name length exceeds 255 bytes");
            }
            cursor += lengthOctet;
        }
    }

    /**
     * Immutable result produced by DNS name decoding.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class ReadResult {

        /**
         * Decoded canonical DNS name.
         */
        private final String name;

        /**
         * Next offset for the caller's non-compressed parser.
         */
        private final int nextOffset;

        /**
         * Creates a read result.
         *
         * @param name       decoded canonical DNS name
         * @param nextOffset next linear parser offset
         */
        private ReadResult(final String name, final int nextOffset) {
            this.name = normalize(name);
            this.nextOffset = nextOffset;
        }

        /**
         * Returns the decoded DNS name.
         *
         * @return canonical DNS name ending with a dot
         */
        public String name() {
            return name;
        }

        /**
         * Returns the next parser offset.
         *
         * @return next byte offset after the encoded name
         */
        public int nextOffset() {
            return nextOffset;
        }

    }

}
