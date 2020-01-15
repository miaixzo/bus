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
package org.aoju.bus.metric.secure;

import org.aoju.bus.core.key.ObjectID;
import org.aoju.bus.metric.ApiConfig;

/**
 * @author Kimi Liu
 * @version 5.5.3
 * @since JDK 1.8++
 */
public interface Oauth2Service {

    /**
     * oauth2授权,获取code.
     * <pre>
     * 1. 首先访问授权页面；
     * 2. 该控制器首先检查clientId是否正确；如果错误将返回相应的错误信息；
     * 3. 然后判断用户是否登录了，如果没有登录首先到登录页面登录；
     * 4. 登录成功后生成相应的code即授权码，然后重定向到客户端地址
     * </pre>
     *
     * @param config 配置信息
     * @return 返回响应内容
     */
    default String authorize(ApiConfig config) {
        return ObjectID.id();
    }

    /**
     * 通过code获取accessToken.
     * <pre>
     * 1、首先通过如http://localhost:8080/api/accessToken，POST提交如下数据访问:
     *  <code>
     *  code: 6d250650831fea227749f49a5b49ccad
     *  client_id: test
     *  client_secret: 123456
     *  grant_type: authorization_code
     *  redirect_uri: http://localhost:8080/api/authorize
     * </code>
     * 2、该控制器会验证client_id、client_secret、auth code的正确性，如果错误会返回相应的错误；
     * 3、如果验证通过会生成并返回相应的访问令牌accessToken。
     * </pre>
     *
     * @param config 配置项
     * @return 返回响应内容
     */
    default String accessToken(ApiConfig config) {
        return ObjectID.id();
    }

    /**
     * 设置Oauth2业务管理类
     *
     * @param oauth2Manager Oauth2Manager
     */
    void setOauth2Manager(Oauth2Manager oauth2Manager);

}
