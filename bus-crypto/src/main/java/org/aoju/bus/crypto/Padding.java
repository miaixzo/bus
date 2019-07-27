package org.aoju.bus.crypto;

/**
 * 补码方式
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum Padding {
    /**
     * 无补码
     */
    NoPadding,
    ISO10126Padding,
    OAEPPadding,
    PKCS1Padding,
    PKCS5Padding,
    SSL3Padding
}
