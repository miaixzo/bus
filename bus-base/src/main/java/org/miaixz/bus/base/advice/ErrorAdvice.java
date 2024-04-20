/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.base.advice;

import org.miaixz.bus.base.service.ErrorService;

import java.util.ServiceLoader;

/**
 * 异常信息处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorAdvice {

    /**
     * 业务处理器处理请求之前被调用,对用户的request进行处理,若返回值为true,
     * 则继续调用后续的拦截器和目标方法；若返回值为false, 则终止请求
     * 这里可以加上登录校验,权限拦截、请求限流等
     *
     * @param ex 对象参数
     * @return 如果执行链应该继续执行, 则为:true 否则:false
     */
    public boolean handler(Exception ex) {
        final ServiceLoader<ErrorService> loader = ServiceLoader.load(ErrorService.class);
        for (ErrorService service : loader) {
            if (service instanceof ErrorService) {
                if (loader.stream().count() > 1) {
                    if (!service.getClass().getName().equals(ErrorService.class.getName())) {
                        service.before(ex);
                        service.after(ex);
                    }
                } else {
                    service.before(ex);
                    service.after(ex);
                }
            }
        }
        return true;
    }

}