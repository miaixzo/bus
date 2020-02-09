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

import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.metric.ApiConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证服务，需要自己实现
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8++
 */
public interface Oauth2Manager {

    /**
     * 添加 auth code
     *
     * @param authCode code值
     * @param authUser 用户
     */
    void addAuthCode(String authCode, OpenUser authUser);

    /**
     * 添加accessToken
     *
     * @param accessToken  token值
     * @param refreshToken refreshToken
     * @param authUser     用户
     * @param expiresIn    时长,秒
     */
    void addAccessToken(String accessToken, String refreshToken, OpenUser authUser, long expiresIn);


    /**
     * 删除这个accessToken
     *
     * @param accessToken 令牌
     */
    void removeAccessToken(String accessToken);

    /**
     * 删除这个refreshToken
     *
     * @param refreshToken 令牌
     */
    void removeRefreshToken(String refreshToken);

    /**
     * 获取RefreshToken
     *
     * @param refreshToken 令牌
     * @return 返回Token信息
     */
    RefreshToken getRefreshToken(String refreshToken);

    /**
     * 验证auth code是否有效
     *
     * @param authCode 授权码
     * @return 无效返回false
     */
    boolean checkAuthCode(String authCode);

    /**
     * 根据auth code获取用户
     *
     * @param authCode 授权码
     * @return 返回用户
     */
    OpenUser getUserByAuthCode(String authCode);

    /**
     * 根据access token获取用户名
     *
     * @param accessToken token值
     * @return 返回用户
     */
    OpenUser getUserByAccessToken(String accessToken);

    /**
     * 返回accessToken中追加的参数
     *
     * @param user 用户
     * @return 返回追加的参数
     */
    Map<String, String> getParam(OpenUser user);

    /**
     * 获取auth code / access token 过期时间
     *
     * @param config 配置
     * @return 返回过期时间，单位秒
     */
    long getExpireIn(ApiConfig config);

    /**
     * 用户登录，需判断是否已经登录
     *
     * @param request 请求
     * @return 返回用户对象
     * @throws AuthorizedException 登录失败异常
     */
    OpenUser login(HttpServletRequest request) throws AuthorizedException;

}