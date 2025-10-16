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
package org.miaixz.bus.crypto.center;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * ASN.1 (Abstract Syntax Notation One) utility class. ASN.1 describes a data format for representing, encoding,
 * transmitting, and decoding data. Its encoding formats include DER (Distinguished Encoding Rules), BER (Basic Encoding
 * Rules), and DL (Definite Length).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ASN1 {

    /**
     * Encodes ASN.1 elements into DER (Distinguished Encoding Rules) format.
     *
     * @param elements The ASN.1 elements to encode.
     * @return The encoded bytes in DER format.
     * @throws InternalException if an I/O error occurs during encoding.
     */
    public static byte[] encodeDer(final ASN1Encodable... elements) {
        return encode(ASN1Encoding.DER, elements);
    }

    /**
     * Encodes ASN.1 elements into a specified ASN.1 encoding format.
     *
     * @param asn1Encoding The encoding format, such as DER, BER, or DL (see {@link ASN1Encoding}).
     * @param elements     The ASN.1 elements to encode.
     * @return The encoded bytes in the specified ASN.1 format.
     * @throws CryptoException   if an unsupported ASN.1 encoding is provided.
     * @throws InternalException if an I/O error occurs during encoding.
     */
    public static byte[] encode(final String asn1Encoding, final ASN1Encodable... elements) {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        encodeTo(asn1Encoding, out, elements);
        return out.toByteArray();
    }

    /**
     * Encodes ASN.1 elements into a specified ASN.1 encoding format and writes them to an output stream.
     *
     * @param asn1Encoding The encoding format, such as DER, BER, or DL (see {@link ASN1Encoding}).
     * @param out          The {@link OutputStream} to write the encoded data to.
     * @param elements     The ASN.1 elements to encode.
     * @throws CryptoException   if an unsupported ASN.1 encoding is provided.
     * @throws InternalException if an I/O error occurs during encoding.
     */
    public static void encodeTo(final String asn1Encoding, final OutputStream out, final ASN1Encodable... elements) {
        final ASN1Sequence sequence;
        switch (asn1Encoding) {
            case ASN1Encoding.DER:
                sequence = new DERSequence(elements);
                break;

            case ASN1Encoding.BER:
                sequence = new BERSequence(elements);
                break;

            case ASN1Encoding.DL:
                sequence = new DLSequence(elements);
                break;

            default:
                throw new CryptoException("Unsupported ASN1 encoding: {}", asn1Encoding);
        }
        try {
            sequence.encodeTo(out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reads an ASN.1 data stream and decodes it into an {@link ASN1Object}.
     *
     * @param in The {@link InputStream} containing the ASN.1 data.
     * @return The decoded {@link ASN1Object}.
     * @throws InternalException if an I/O error occurs during decoding.
     */
    public static ASN1Object decode(final InputStream in) {
        final ASN1InputStream asn1In = new ASN1InputStream(in);
        try {
            return asn1In.readObject();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves a string representation of the ASN.1 format, typically used for debugging.
     *
     * @param in The {@link InputStream} containing the ASN.1 data.
     * @return The string representation of the {@link ASN1Object}.
     * @see ASN1Dump#dumpAsString(Object)
     * @throws InternalException if an I/O error occurs during decoding.
     */
    public static String getDumpString(final InputStream in) {
        return ASN1Dump.dumpAsString(decode(in));
    }

    /**
     * Generates an X.500 distinguished name (X500Name) from provided components.
     *
     * @param C  Country Name (e.g., CN).
     * @param ST State or Province Name (e.g., Beijing).
     * @param L  Locality Name (e.g., Beijing).
     * @param O  Organization Name (e.g., company name).
     * @param OU Organizational Unit Name (e.g., department name).
     * @param CN Common Name (e.g., server IP or domain name, like 192.168.30.71 or www.baidu.com).
     * @return The constructed {@link X500Name}.
     */
    public static X500Name createX500Name(
            final String C,
            final String ST,
            final String L,
            final String O,
            final String OU,
            final String CN) {
        return new X500NameBuilder().addRDN(BCStyle.C, C).addRDN(BCStyle.ST, ST).addRDN(BCStyle.L, L)
                .addRDN(BCStyle.O, O).addRDN(BCStyle.OU, OU).addRDN(BCStyle.CN, CN).build();
    }

}
