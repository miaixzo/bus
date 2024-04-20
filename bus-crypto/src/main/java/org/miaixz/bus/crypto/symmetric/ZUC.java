package org.miaixz.bus.crypto.symmetric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.RandomKit;
import org.miaixz.bus.crypto.Builder;

import javax.crypto.spec.IvParameterSpec;

/**
 * 祖冲之算法集（ZUC算法）实现，基于BouncyCastle实现。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZUC extends Crypto {

    private static final long serialVersionUID = 1L;

    /**
     * 构造
     *
     * @param algorithm ZUC算法枚举，包括128位和256位两种
     * @param key       密钥
     * @param iv        加盐，128位加盐是16bytes，256位是25bytes，{@code null}是随机加盐
     */
    public ZUC(ZUCAlgorithm algorithm, byte[] key, byte[] iv) {
        super(algorithm.value,
                Builder.generateKey(algorithm.value, key),
                generateIvParam(algorithm, iv));
    }

    /**
     * 生成ZUC算法密钥
     *
     * @param algorithm ZUC算法
     * @return 密钥
     * @see Builder#generateKey(String)
     */
    public static byte[] generateKey(ZUCAlgorithm algorithm) {
        return Builder.generateKey(algorithm.value).getEncoded();
    }

    /**
     * 生成加盐参数
     *
     * @param algorithm ZUC算法
     * @param iv        加盐，128位加盐是16bytes，256位是25bytes，{@code null}是随机加盐
     * @return {@link IvParameterSpec}
     */
    private static IvParameterSpec generateIvParam(ZUCAlgorithm algorithm, byte[] iv) {
        if (null == iv) {
            switch (algorithm) {
                case ZUC_128:
                    iv = RandomKit.randomBytes(Normal._16);
                    break;
                case ZUC_256:
                    iv = RandomKit.randomBytes(25);
                    break;
            }
        }
        return new IvParameterSpec(iv);
    }

    /**
     * ZUC类型，包括128位和256位
     */
    public enum ZUCAlgorithm {
        ZUC_128("ZUC-128"),
        ZUC_256("ZUC-256");

        private final String value;

        /**
         * 构造
         *
         * @param value 算法的字符串表示，区分大小写
         */
        ZUCAlgorithm(String value) {
            this.value = value;
        }

        /**
         * 获得算法的字符串表示形式
         *
         * @return 算法字符串
         */
        public String getValue() {
            return this.value;
        }
    }

}
