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

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Format;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.LicenseProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 许可证校验过滤器。
 * <p>
 * 作为系统中最高优先级的过滤器之一，它在所有请求的最前端强制执行许可证有效性检查。 如果许可证校验失败，请求将被立即中断，不会进入后续的业务逻辑。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 4)
public class LicenseFilter extends AbstractFilter {

    /**
     * 授权提供者，用于处理许可证验证的实际逻辑。
     */
    private final LicenseProvider provider;

    /**
     * 构造器，注入许可证校验提供者。
     *
     * @param provider 授权提供者，负责处理许可证验证。
     */
    public LicenseFilter(LicenseProvider provider) {
        this.provider = provider;
    }

    /**
     * 过滤器的核心执行方法。
     * <p>
     * 此方法从当前请求中提取主机名和端口（Authority），并将其作为验证主体传递给 {@link LicenseProvider}。 {@code provider.validate}
     * 方法若校验失败，应抛出异常，该异常将被框架的全局异常处理器捕获， 从而中断请求并返回统一的错误响应。
     * </p>
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步过滤操作的完成
     * @throws ValidateException 如果许可证校验失败
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        Assets assets = getAssets(context);

        // 获取域名和端口
        Optional<String> authorityOptional = getOriginalAuthority(exchange.getRequest());

        // 使用 Optional 的 API 来处理结果
        String authority = authorityOptional.orElseThrow(() -> {
            // 如果 Optional 为空，说明未找到任何有效的主机信息，构造异常并抛出
            String errorMessage = "Unable to determine the request authority (host:port) for license validation. The request will be ignored.";
            Format.error(exchange, "LICENSE_AUTHORITY_NOT_FOUND", errorMessage);
            throw new ValidateException(ErrorCode._100527);
        });

        Format.info(exchange, "LICENSE_VALIDATING", "Validating license for authority: " + authority);
        if (Consts.TYPE_TWO == assets.getFirewall()) {
            // 调用提供者执行验证。
            // 如果验证失败，provider.validate(authority) 应抛出异常。
            this.provider.validate(authority);
        }

        // 验证通过，请求继续。
        return chain.filter(exchange);
    }

}
