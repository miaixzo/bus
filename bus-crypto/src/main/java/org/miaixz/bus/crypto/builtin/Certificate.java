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
 * 证书相关套件
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

    private static final long serialVersionUID = -1L;

    /**
     * 证书编号
     */
    private String serial;

    /**
     * 证书文件
     */
    private String fileName;

    /**
     * 证书版本
     */
    private String version;

    /**
     * P12证书对应的密码
     */
    private String password;

    /**
     * 证书公钥
     */
    private String publicKey;

    /**
     * 证书颁发者名称
     */
    private String issuerCN;

    /**
     * 证书颁发者组织
     */
    private String issuerO;

    /**
     * 证书颁发者
     */
    private Principal issuer;

    /**
     * 此证书主体
     */
    private Principal subject;

    /**
     * 此证书主体名称
     */
    private String subjectCN;

    /**
     * 此证书主体组织
     */
    private String subjectO;

    /**
     * 有效起始日期
     */
    private Date notBefore;

    /**
     * 有效终止日期
     */
    private Date notAfter;

    /**
     * 证书本身
     */
    private X509Certificate self;

    /**
     * CA证书签发
     *
     * @return the {@link X509Certificate}
     */
    public X509Certificate build() {
        // 创建书颁发者
        X500NameBuilder issuer = new X500NameBuilder(BCStyle.INSTANCE);
        issuer.addRDN(BCStyle.CN, this.issuerCN);
        issuer.addRDN(BCStyle.O, this.issuerO);

        // 创建证书主体
        X500NameBuilder subject = new X500NameBuilder(BCStyle.INSTANCE);
        subject.addRDN(BCStyle.CN, this.subjectCN);
        subject.addRDN(BCStyle.O, this.subjectO);

        // 创建证书的公钥/私钥对
        KeyPair keyPair = Keeper.generateKeyPair("RSA");
        // 与证书关联的公钥
        PublicKey publicKey = keyPair.getPublic();
        // 与证书关联的私钥
        PrivateKey privateKey = keyPair.getPrivate();
        // 主体密钥标识符
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
