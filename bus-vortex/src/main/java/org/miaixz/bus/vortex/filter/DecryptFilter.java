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
package org.miaixz.bus.vortex.filter;

import java.util.Map;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.center.AES;
import org.miaixz.bus.vortex.Config;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

/**
 * 数据解密过滤器，负责对请求参数进行解密处理
 * <p>
 * 该过滤器在过滤器链中位于较高优先级位置（Ordered.HIGHEST_PRECEDENCE + 1）， 主要用于对加密的请求参数进行解密操作。当解密功能启用且上下文标记需要解密时，
 * 过滤器会遍历所有请求参数，对非空参数值进行解密处理。
 * </p>
 * <p>
 * 目前支持AES-CBC-PKCS7Padding解密方式，通过配置文件可以设置解密密钥和偏移量。 解密后的参数将替换原始加密参数，继续传递给后续的过滤器链。
 * </p>
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DecryptFilter extends AbstractFilter {

    /**
     * 解密配置对象，包含解密相关的配置信息
     * <p>
     * 该对象存储了解密功能是否启用、解密算法类型、解密密钥和偏移量等配置信息。 这些配置通常从应用程序的配置文件中加载，用于初始化解密实例。
     * </p>
     */
    private final Config.Decrypt decrypt;

    /**
     * 加密/解密工具实例，用于执行实际的解密操作
     * <p>
     * 该实例在初始化阶段根据配置创建，目前支持AES-CBC-PKCS7Padding解密方式。 如果解密功能未启用或配置不正确，该实例可能为null。
     * </p>
     */
    private Crypto crypto;

    /**
     * 构造器，初始化解密配置
     *
     * @param decrypt 解密配置对象，包含解密相关的配置信息
     */
    public DecryptFilter(Config.Decrypt decrypt) {
        this.decrypt = decrypt;
    }

    /**
     * 初始化方法，在 bean 创建后执行，配置 AES 加密实例
     * <p>
     * 该方法使用@PostConstruct注解，在bean实例化并完成依赖注入后由容器自动调用。 它根据解密配置创建相应的加密/解密实例，目前仅支持AES算法。
     * 如果配置中指定的是AES算法，将使用CBC模式、PKCS7Padding填充方式， 以及配置中提供的密钥和偏移量来初始化AES实例。
     * </p>
     */
    @PostConstruct
    public void init() {
        if (Algorithm.AES.getValue().equals(decrypt.getType())) {
            crypto = new AES(Algorithm.Mode.CBC, Padding.PKCS7Padding, decrypt.getKey().getBytes(),
                    decrypt.getOffset().getBytes());
        }
    }

    /**
     * 内部过滤方法，执行解密逻辑
     * <p>
     * 该方法是过滤器的核心实现，负责执行请求参数的解密操作。 它首先检查解密功能是否启用以及上下文是否标记需要解密， 如果满足条件，则对请求参数进行解密处理。
     * </p>
     * <p>
     * 解密操作完成后，会记录相应的日志信息，然后继续执行过滤器链。 如果解密功能未启用或上下文不需要解密，则直接跳过解密步骤。
     * </p>
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应信息
     * @param chain    过滤器链，用于将请求传递给下一个过滤器
     * @param context  请求上下文，包含请求相关的状态信息
     * @return {@link Mono<Void>} 表示异步处理完成，当解密操作完成后继续执行过滤器链
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        ServerWebExchange.Builder builder = exchange.mutate();
        if (decrypt.isEnabled() && context.isNeedDecrypt()) {
            doDecrypt(exchange, getRequestMap(context));
            Format.info(exchange, "DECRYPT_PERFORMED", "Path: " + exchange.getRequest().getURI().getPath());
        }
        return chain.filter(builder.build());
    }

    /**
     * 执行解密操作，遍历参数并解密非空值
     * <p>
     * 该方法遍历请求参数映射中的所有键值对，对非空参数值进行解密处理。 在解密前，会先检查加密/解密实例是否已正确初始化，如果未初始化则跳过解密操作。
     * </p>
     * <p>
     * 解密过程中，会先将参数值中的空格替换为加号（这是URL编码的常见处理）， 然后使用UTF-8字符集进行解密，解密后的值将替换原始加密值。
     * </p>
     *
     * @param exchange ServerWebExchange 对象，包含请求和响应信息
     * @param map      请求参数映射，包含所有需要解密的参数
     */
    private void doDecrypt(ServerWebExchange exchange, Map<String, String> map) {
        if (null == crypto) {
            Format.warn(exchange, "DECRYPT_SKIPPED", "Crypto instance not initialized");
            return;
        }
        map.forEach((k, v) -> {
            if (StringKit.isNotBlank(v)) {
                map.put(k, crypto.decryptString(v.replaceAll(Symbol.SPACE, Symbol.PLUS), Charset.UTF_8));
            }
        });
    }

}