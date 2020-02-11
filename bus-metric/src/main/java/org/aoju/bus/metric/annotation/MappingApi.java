/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.metric.annotation;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.metric.ApiContext;

import java.lang.annotation.*;

/**
 * 作用在service类的方法上，service类被@ApiService标记
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8++
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingApi {

    String value() default Normal.EMPTY;

    /**
     * @return 接口名，建议命名规则: <strong>业务.模块.名字.动词</strong>
     * 支付业务订单状态修改：pay.order.status.update
     * 充值业务用户余额充值：charge.user.money.recharge
     * 充值业务用户余额查询：charge.user.money.search
     */
    String name();

    /**
     * @return 接口版本号，默认""，建议命名规则：x.y，如1.0，1.1
     */
    String version() default Normal.EMPTY;

    /**
     * @return 忽略验证签名，默认false。为true接口不执行验签操作，但其它验证会执行。
     */
    boolean ignoreSign() default false;

    /**
     * @return 忽略所有验证，默认false。为true接口都不执行任何验证操作。
     */
    boolean ignoreValidate() default false;

    /**
     * @return 是否对返回结果进行包装，如果设置成false，则直接返回业务方法结果。
     */
    boolean wrapResult() default true;

    /**
     * @return 设置true，不会输出json到客户端，需要调用 {@link ApiContext#getResponse()} 手动返回结果。
     */
    boolean noReturn() default false;

    /**
     * @return 设置true，此接口将忽略jwt认证（不管有没有jwt）。默认false
     */
    boolean ignoreJWT() default false;

    /**
     * @return 是否忽略token，true忽略。默认false
     */
    boolean isIgnoreToken() default false;

}
