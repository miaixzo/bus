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
package org.miaixz.bus.pay.metric.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.AntCertificationUtil;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.Builder;

import java.util.*;

/**
 * 支付宝配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AliPayBuilder {

    /**
     * 生成签名结果
     *
     * @param params   要签名的数组
     * @param key      签名密钥
     * @param signType 签名类型
     * @return 签名结果字符串
     */
    public static String buildRequestMySign(Map<String, String> params, String key, String signType)
            throws AlipayApiException {
        // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String preStr = createLinkString(params);
        if (Algorithm.MD5.getValue().equals(signType)) {
            return Builder.md5(preStr.concat(key));
        } else if (Algorithm.RSA2.getValue().equals(signType)) {
            return AlipaySignature.rsa256Sign(preStr, key, AlipayConstants.CHARSET_UTF8);
        } else if (Algorithm.RSA.getValue().equals(signType)) {
            return AlipaySignature.rsaSign(preStr, key, AlipayConstants.CHARSET_UTF8);
        }
        return null;
    }

    /**
     * 生成要请求给支付宝的参数数组
     *
     * @param params   请求前的参数数组
     * @param key      商户的私钥
     * @param signType 签名类型
     * @return 要请求的参数数组
     */
    public static Map<String, String> buildRequestPara(Map<String, String> params, String key, String signType) {
        // 除去数组中的空值和签名参数
        Map<String, String> tempMap = paraFilter(params);
        // 生成签名结果
        String mySign;
        try {
            mySign = buildRequestMySign(params, key, signType);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
        // 签名结果与签名方式加入请求提交参数组中
        tempMap.put("sign", mySign);
        tempMap.put("sign_type", signType);
        return tempMap;
    }

    /**
     * 除去数组中的空值和签名参数
     *
     * @param params 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>(params.size());
        if (params.size() <= 0) {
            return result;
        }
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null || "".equals(value) || "sign".equalsIgnoreCase(key)
                    || "sign_type".equalsIgnoreCase(key)) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * 把数组所有元素排序
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder content = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            // 拼接时，不包括最后一个&字符
            content.append(key).append("=").append(value).append("&");
        }
        if (content.lastIndexOf("&") == content.length() - 1) {
            content.deleteCharAt(content.length() - 1);
        }
        return content.toString();
    }

    /**
     * 从证书内容验签
     *
     * @param params                  待验签的从支付宝接收到的参数Map
     * @param alipayPublicCertContent 支付宝公钥证书内容
     * @param charset                 参数内容编码集
     * @param signType                指定采用的签名方式，RSA或RSA2
     * @return true：验签通过；false：验签不通过
     * @throws AlipayApiException
     */
    public static boolean rsaCertCheckV1ByContent(Map<String, String> params, String alipayPublicCertContent,
            String charset, String signType) throws AlipayApiException {

        String publicKey = Base64
                .encode(AntCertificationUtil.getCertFromContent(alipayPublicCertContent).getPublicKey().getEncoded());
        return AlipaySignature.rsaCheckV1(params, publicKey, charset, signType);
    }

}
