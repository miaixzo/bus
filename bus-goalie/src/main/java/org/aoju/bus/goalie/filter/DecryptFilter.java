/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.goalie.filter;

import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.crypto.Mode;
import org.aoju.bus.crypto.Padding;
import org.aoju.bus.crypto.symmetric.AES;
import org.aoju.bus.crypto.symmetric.Crypto;
import org.aoju.bus.goalie.Config;
import org.aoju.bus.goalie.Context;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 数据解密
 *
 * @author Justubborn
 * @version 6.3.5
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DecryptFilter implements WebFilter {

    private Config.Decrypt decrypt;
    private Crypto crypto;

    public DecryptFilter(Config.Decrypt decrypt) {
        this.decrypt = decrypt;
    }

    @PostConstruct
    public void init() {
        if (Algorithm.AES.getValue().equals(decrypt.getType())) {
            crypto = new AES(Mode.CBC, Padding.PKCS7Padding, decrypt.getKey().getBytes(), decrypt.getOffset().getBytes());
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchange.Builder builder = exchange.mutate();
        if (decrypt.isEnabled() && Context.get(exchange).isNeedDecrypt()) {
            doDecrypt(Context.get(exchange).getRequestMap());
        }
        return chain.filter(builder.build());
    }

    /**
     * 解密
     *
     * @param map 参数
     */
    private void doDecrypt(Map<String, String> map) {
        if (null == crypto) {
            return;
        }
        map.forEach((k, v) -> {
            if (StringKit.isNotBlank(v)) {
                map.put(k, crypto.decryptStr(v.replaceAll(Symbol.SPACE, Symbol.PLUS), Charset.UTF_8));
            }
        });
    }

}
