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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.lang.exception.LicenseException;

/**
 * 许可证校验提供者接口。
 * <p>
 * 定义了校验许可证有效性的核心功能。实现此接口的服务应包含具体的许可证校验逻辑， 例如检查有效期、绑定的硬件信息、域名等。
 * </p>
 */
public interface LicenseProvider {

    /**
     * 执行许可证验证操作。
     * <p>
     * <b>实现约定:</b>
     * <ul>
     * <li>如果许可证对给定的验证主体有效，此方法应正常返回，不执行任何操作。</li>
     * <li>如果许可证无效（如过期、主体不匹配、签名错误等），此方法应抛出 {@link LicenseException} 或其他运行时异常来中断操作。</li>
     * </ul>
     *
     * @param principal 用于验证许可证的实体标识，例如域名 (e.g., "example.com:443") * 或公司名称 (e.g., "Acme Corporation")。
     * @throws LicenseException 如果许可证校验失败。
     */
    default boolean validate(String principal) {
        // 默认实现为空，允许在某些环境中禁用许可证检查。
        // 具体的校验逻辑应由实现类提供。
        return true;
    }

}
