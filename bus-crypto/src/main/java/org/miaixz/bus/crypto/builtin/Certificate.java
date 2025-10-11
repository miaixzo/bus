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
package org.miaixz.bus.crypto.builtin;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.crypto.Keeper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Certificate related toolkit. This class provides utilities for handling and building X.509 certificates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852288292676L;

    /**
     * The serial number of the certificate.
     */
    private String serial;

    /**
     * The file name associated with the certificate.
     */
    private String fileName;

    /**
     * The version of the certificate.
     */
    private String version;

    /**
     * The password for the P12 certificate.
     */
    private String password;

    /**
     * The public key of the certificate.
     */
    private String publicKey;

    /**
     * The Common Name (CN) of the certificate issuer.
     */
    private String issuerCN;

    /**
     * The Organization (O) of the certificate issuer.
     */
    private String issuerO;

    /**
     * The issuer of this certificate.
     */
    private Principal issuer;

    /**
     * The subject of this certificate.
     */
    private Principal subject;

    /**
     * The Common Name (CN) of the certificate subject.
     */
    private String subjectCN;

    /**
     * The Organization (O) of the certificate subject.
     */
    private String subjectO;

    /**
     * The date from which the certificate is valid.
     */
    private Date notBefore;

    /**
     * The date until which the certificate is valid.
     */
    private Date notAfter;

    /**
     * The X.509 certificate itself.
     */
    private X509Certificate self;

    /**
     * Builds and signs an X.509 certificate, typically for a Certificate Authority (CA). This method generates a
     * self-signed certificate or a certificate signed by a CA if issuer and subject are different.
     *
     * @return The generated {@link X509Certificate}.
     * @throws InternalException if any cryptographic operation or certificate building fails.
     */
    public X509Certificate build() {
        // Create certificate issuer
        X500NameBuilder issuer = new X500NameBuilder(BCStyle.INSTANCE);
        issuer.addRDN(BCStyle.CN, this.issuerCN);
        issuer.addRDN(BCStyle.O, this.issuerO);

        // Create certificate subject
        X500NameBuilder subject = new X500NameBuilder(BCStyle.INSTANCE);
        subject.addRDN(BCStyle.CN, this.subjectCN);
        subject.addRDN(BCStyle.O, this.subjectO);

        // Generate public/private key pair for the certificate
        KeyPair keyPair = Keeper.generateKeyPair("RSA");
        // Public key associated with the certificate
        PublicKey publicKey = keyPair.getPublic();
        // Private key associated with the certificate
        PrivateKey privateKey = keyPair.getPrivate();
        // Subject Key Identifier
        SubjectKeyIdentifier subjectKeyIdentifier = new SubjectKeyIdentifier(publicKey.getEncoded());
        KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment
                | KeyUsage.dataEncipherment | KeyUsage.cRLSign);
        ASN1EncodableVector purposes = new ASN1EncodableVector();
        purposes.add(KeyPurposeId.id_kp_serverAuth);
        purposes.add(KeyPurposeId.id_kp_clientAuth);
        purposes.add(KeyPurposeId.anyExtendedKeyUsage);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer.build(),
                BigInteger.valueOf(Long.parseLong(serial)), this.notBefore, this.notAfter, subject.build(), publicKey);
        try {
            certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
            certBuilder.addExtension(Extension.subjectKeyIdentifier, false, subjectKeyIdentifier);
            certBuilder.addExtension(Extension.keyUsage, false, usage);
            certBuilder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));
            return new JcaX509CertificateConverter().getCertificate(
                    certBuilder.build(new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(privateKey)));
        } catch (CertIOException | OperatorCreationException | CertificateException e) {
            throw new InternalException(e);
        }
    }

}
