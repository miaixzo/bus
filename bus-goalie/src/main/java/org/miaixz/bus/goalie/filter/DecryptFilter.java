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
package org.miaixz.bus.goalie.filter;

import java.util.Map;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.center.AES;
import org.miaixz.bus.goalie.Config;
import org.miaixz.bus.goalie.Context;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

/**
 * 数据解密过滤器，负责对请求参数进行解密处理
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DecryptFilter implements WebFilter {

    /**
     * 解密配置，包含是否启用、密钥、算法类型和偏移量
     */
    private final Config.Decrypt decrypt;

    /**
     * 对称加密实例，用于执行解密操作
     */
    private Crypto crypto;

    /**
     * 构造器，初始化解密配置
     *
     * @param decrypt 解密配置对象
     */
    public DecryptFilter(Config.Decrypt decrypt) {
        this.decrypt = decrypt;
    }

    /**
     * 初始化方法，在 bean 创建后执行，配置 AES 加密实例
     */
    @PostConstruct
    public void init() {
        if (Algorithm.AES.getValue().equals(decrypt.getType())) {
            // 使用 AES 算法，CBC 模式，PKCS7 填充
            crypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, decrypt.getKey().getBytes(),
                    decrypt.getOffset().getBytes());
        }
    }

    /**
     * 过滤器主逻辑，检查是否需要解密并执行解密操作
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 创建可变的 exchange
        ServerWebExchange.Builder builder = exchange.mutate();

        // 检查是否启用解密且上下文要求解密
        if (decrypt.isEnabled() && Context.get(exchange).isNeedDecrypt()) {
            doDecrypt(Context.get(exchange).getRequestMap());
        }

        // 继续执行过滤器链
        return chain.filter(builder.build());
    }

    /**
     * 执行解密操作，遍历参数并解密非空值
     *
     * @param map 请求参数映射
     */
    private void doDecrypt(Map<String, String> map) {
        if (null == crypto) {
            return; // 未初始化加密实例，直接返回
        }
        map.forEach((k, v) -> {
            if (StringKit.isNotBlank(v)) {
                // 替换空格为加号（处理 URL 编码），解密为 UTF-8 字符串
                map.put(k, crypto.decryptString(v.replaceAll(Symbol.SPACE, Symbol.PLUS), Charset.UTF_8));
            }
        });
    }

}