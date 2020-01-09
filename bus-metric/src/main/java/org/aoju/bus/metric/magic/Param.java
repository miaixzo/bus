/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.magic;

import java.io.Serializable;

/**
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public interface Param extends Serializable {

    /**
     * 获取接口名
     *
     * @return 返回接口名
     */
    String fatchName();

    /**
     * 获取版本号
     *
     * @return 返回版本号
     */
    String fatchVersion();

    /**
     * 获取appKey
     *
     * @return 返回appKey
     */
    String fatchAppKey();

    /**
     * 获取业务参数
     *
     * @return 返回业务参数
     */
    String fatchData();

    /**
     * 获取时间戳
     *
     * @return 返回时间戳
     */
    String fatchTimestamp();

    /**
     * 获取签名串
     *
     * @return 返回签名串
     */
    String fatchSign();

    /**
     * 获取格式化类型
     *
     * @return 返回格式化类型
     */
    String fatchFormat();

    /**
     * 获取accessToken
     *
     * @return 返回accessToken
     */
    String fatchAccessToken();

    /**
     * 获取签名方式
     *
     * @return 返回签名方式
     */
    String fatchSignMethod();

}