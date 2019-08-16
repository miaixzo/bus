package org.aoju.bus.core.consts;

public class ModeType {

    /**
     * 默认的AES加密方式：AES/CBC/PKCS5Padding
     */
    public static final String AES = "AES";
    public static final String ARCFOUR = "ARCFOUR";
    public static final String Blowfish = "Blowfish";
    /**
     * 默认的DES加密方式：DES/ECB/PKCS5Padding
     */
    public static final String DES = "DES";
    /**
     * 3DES算法，默认实现为：DESede/CBC/PKCS5Padding
     */
    public static final String DESede = "DESede";
    public static final String RC2 = "RC2";
    /**
     * RSA算法
     */
    public static final String RSA = "RSA";
    /**
     * RSA算法，此算法用了默认补位方式为RSA/ECB/PKCS1Padding
     */
    public static final String RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding";
    /**
     * RSA算法，此算法用了RSA/None/NoPadding
     */
    public static final String RSA_None = "RSA/None/NoPadding";
    /**
     * EC算法
     */
    public static final String EC = "EC";
    public static final String PBEWithMD5AndDES = "PBEWithMD5AndDES";
    public static final String PBEWithSHA1AndDESede = "PBEWithSHA1AndDESede";
    public static final String PBEWithSHA1AndRC2_40 = "PBEWithSHA1AndRC2_40";
    // The RSA signature algorithm
    public static final String NONEwithRSA = "NONEwithRSA";
    // The MD2/MD5 with RSA Encryption signature algorithm
    public static final String MD2withRSA = "MD2withRSA";
    public static final String MD5withRSA = "MD5withRSA";
    // The signature algorithm with SHA-* and the RSA
    public static final String SHA1withRSA = "SHA1withRSA";
    public static final String SHA256withRSA = "SHA256withRSA";
    public static final String SHA384withRSA = "SHA384withRSA";
    public static final String SHA512withRSA = "SHA512withRSA";
    // The Digital Signature Algorithm
    public static final String NONEwithDSA = "NONEwithDSA";
    // The DSA with SHA-1 signature algorithm
    public static final String SHA1withDSA = "SHA1withDSA";
    // The ECDSA signature algorithms
    public static final String NONEwithECDSA = "NONEwithECDSA";
    public static final String SHA1withECDSA = "SHA1withECDSA";
    public static final String SHA256withECDSA = "SHA256withECDSA";
    public static final String SHA384withECDSA = "SHA384withECDSA";
    public static final String SHA512withECDSA = "SHA512withECDSA";
    public static final String HmacMD5 = "HmacMD5";
    public static final String HmacSHA1 = "HmacSHA1";
    public static final String HmacSHA256 = "HmacSHA256";
    public static final String HmacSHA384 = "HmacSHA384";
    public static final String HmacSHA512 = "HmacSHA512";
    public static final String MD2 = "MD2";
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";
    public static final String SHA384 = "SHA-384";
    public static final String SHA512 = "SHA-512";
    public static final String SHAPRNG = "SHA1PRNG";

}
