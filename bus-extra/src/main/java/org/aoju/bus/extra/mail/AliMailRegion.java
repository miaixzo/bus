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
package org.aoju.bus.extra.mail;

import lombok.Getter;

/**
 * 区域信息
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
@Getter
public enum AliMailRegion {
    /**
     * 华北1 - 青岛
     */
    qingdao("cn-qingdao"),
    /**
     * 华北2 - 北京
     */
    beijing("cn-beijing"),
    /**
     * 华北3 - 张家口
     */
    zhangjiakou("cn-zhangjiakou"),
    /**
     * 华北5 - 呼和浩特
     */
    huhehaote("cn-huhehaote"),
    /**
     * 华东1 - 杭州
     */
    hangzhou("cn-hangzhou"),
    /**
     * 华东2 - 上海
     */
    shanghai("cn-shanghai"),
    /**
     * 华南1 - 深圳
     */
    shenzhen("cn-shenzhen"),
    /**
     * 香港
     */
    hongkong("cn-hongkong"),
    /**
     * 亚太东南 1 - 新加坡
     */
    apsoutheast1("ap-southeast-1"),
    /**
     * 亚太东南 2 - 悉尼
     */
    apsoutheast2("ap-southeast-2"),
    /**
     * 亚太东南 3 - 吉隆坡
     */
    apsoutheast3("ap-southeast-3"),
    /**
     * 亚太东南 5 - 雅加达
     */
    apsoutheast5("ap-southeast-5"),
    /**
     * 亚太南部 1 - 孟买
     */
    apsouth1("ap-south-1"),
    /**
     * 亚太东北 1 - 东京
     */
    apnortheast1("ap-northeast-1"),
    /**
     * 美国西部 1 - 硅谷
     */
    uswest1("us-west-1"),
    /**
     * 美国东部 1 - 弗吉尼亚
     */
    useast1("us-east-1"),
    /**
     * 欧洲中部 1 - 法兰克福
     */
    eucentral1("eu-central-1"),
    /**
     * 英国（伦敦）
     */
    euwest1("eu-west-1"),
    /**
     * 中东东部 1 - 迪拜
     */
    meeast1("me-east-1");

    private String value;

    AliMailRegion(String value) {
        this.value = value;
    }
}
