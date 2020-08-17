/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.metric.manual;

import org.aoju.bus.metric.ApiContext;

import java.util.*;

/**
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public class DefaultVerifier implements Verifier {

    @Override
    public boolean verify(ApiParam apiParam, String secret) {
        boolean isSame = false;
        String signCode = apiParam.fatchSignAndRemove();

        if (signCode != null) {
            String clientSign = buildSign(apiParam, secret);
            isSame = signCode.equals(clientSign);
        }

        return isSame;
    }

    public String buildSign(Map<String, ?> paramsMap, String secret) {
        Set<String> keySet = paramsMap.keySet();
        List<String> paramNames = new ArrayList<>(keySet);

        Collections.sort(paramNames);

        StringBuilder paramNameValue = new StringBuilder();

        for (String paramName : paramNames) {
            paramNameValue.append(paramName).append(paramsMap.get(paramName));
        }

        String source = secret + paramNameValue.toString() + secret;

        return encrypt(source);
    }

    /**
     * 生成md5,全部大写。
     *
     * @param source 数据
     * @return 返回MD5全部大写
     */
    public String encrypt(String source) {
        Safety safety = ApiContext.getConfig().getSafety();
        return safety.md5(source).toUpperCase();
    }

}
