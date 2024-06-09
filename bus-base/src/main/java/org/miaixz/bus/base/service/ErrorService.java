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
package org.miaixz.bus.base.service;

import lombok.NoArgsConstructor;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.logger.Logger;

/**
 * 异常信息处理
 * 此类未找到实现的情况下，采用默认实现
 * 可以根据不同业务需求，继承此类实现对应业务逻辑即可
 * 项目中可通过SPI自定义接入
 * 例：META-INF/services/org.miaixz.bus.base.service.ErrorService
 * <code>
 * org.miaixz.bus.xxx.ErrorService
 * </code>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@NoArgsConstructor
public class ErrorService {

    /**
     * 完成请求处理前调用
     *
     * @param ex 对象参数
     * @return 如果执行链应该继续执行, 则为:true 否则:false
     */
    public boolean before(Exception ex) {
        Logger.error(ExceptionKit.stacktraceToString(ex));
        return true;
    }

    /**
     * 完成请求处理后回调
     *
     * @param ex 对象参数
     * @return 如果执行链应该继续执行, 则为:true 否则:false
     */
    public boolean after(Exception ex) {
        return true;
    }

}