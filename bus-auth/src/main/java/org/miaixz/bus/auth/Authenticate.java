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
package org.miaixz.bus.auth;

import java.util.Arrays;
import java.util.function.Function;

import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 授权模块构建器，用于快速构造认证提供者。 通过建造者模式配置认证来源、上下文、缓存和协议配置，动态创建对应的认证提供者实例。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Authenticate {

    /**
     * 认证来源（如 TWITTER、SAML）
     */
    private String source;
    /**
     * 上下文配置，包含协议特定参数
     */
    private Context context;
    /**
     * 缓存实现，用于存储状态等临时数据
     */
    private ExtendCache cache;
    /**
     * 自定义协议配置数组
     */
    private Complex[] complex;

    /**
     * 私有构造函数，防止直接实例化。
     */
    private Authenticate() {

    }

    /**
     * 创建 Authorize 构建器实例。
     *
     * @return 新创建的 Authorize 实例
     */
    public static Authenticate builder() {
        return new Authenticate();
    }

    /**
     * 设置认证来源。
     *
     * @param source 认证来源（如 TWITTER、SAML_EXAMPLE）
     * @return 当前 Authorize 实例
     */
    public Authenticate source(String source) {
        this.source = source;
        return this;
    }

    /**
     * 设置上下文配置。
     *
     * @param context 上下文配置对象
     * @return 当前 Authorize 实例
     */
    public Authenticate context(Context context) {
        this.context = context;
        return this;
    }

    /**
     * 使用函数动态设置上下文配置。
     *
     * @param context 函数，根据 source 生成上下文配置
     * @return 当前 Authorize 实例
     */
    public Authenticate context(Function<String, Context> context) {
        this.context = context.apply(this.source);
        return this;
    }

    /**
     * 设置缓存实现。
     *
     * @param cache 缓存对象
     * @return 当前 Authorize 实例
     */
    public Authenticate cache(ExtendCache cache) {
        this.cache = cache;
        return this;
    }

    /**
     * 设置自定义协议配置。
     *
     * @param complex 协议配置数组
     * @return 当前 Authorize 实例
     */
    public Authenticate complex(Complex... complex) {
        this.complex = complex;
        return this;
    }

    /**
     * 构建认证提供者实例。 根据配置的 source 查找匹配的 Complex，动态创建对应的提供者实例。
     *
     * @return 认证提供者实例
     * @throws AuthorizedException 如果 source 或 context 未设置，或未找到匹配的 Complex
     */
    public Provider build() {
        // 验证 source 和 context 是否已设置
        if (StringKit.isEmpty(this.source) || null == this.context) {
            throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
        }

        // 合并默认的 Registry 和自定义 Complex
        Complex[] complexes = this.concat(Registry.values(), this.complex);

        // 筛选符合 source 的 Complex
        Complex complex = Arrays.stream(complexes).distinct()
                .filter(authSource -> authSource.getName().equalsIgnoreCase(this.source)).findAny()
                .orElseThrow(() -> new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode()));

        // 获取提供者类
        Class<? extends AbstractProvider> targetClass = complex.getTargetClass();
        if (null == targetClass) {
            throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
        }

        // 动态创建提供者实例
        try {
            if (this.cache == null) {
                return targetClass.getDeclaredConstructor(Context.class).newInstance(this.context);
            } else {
                return targetClass.getDeclaredConstructor(Context.class, ExtendCache.class).newInstance(this.context,
                        this.cache);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthorizedException(ErrorCode.NOT_IMPLEMENTED.getCode());
        }
    }

    /**
     * 合并两个 Complex 数组。
     *
     * @param first  第一个 Complex 数组（通常为默认配置）
     * @param second 第二个 Complex 数组（自定义配置）
     * @return 合并后的 Complex 数组
     */
    private Complex[] concat(Complex[] first, Complex[] second) {
        if (null == second || second.length == 0) {
            return first;
        }
        Complex[] result = new Complex[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}