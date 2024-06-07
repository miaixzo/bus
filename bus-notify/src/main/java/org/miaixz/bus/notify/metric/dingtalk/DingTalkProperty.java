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
package org.miaixz.bus.notify.metric.dingtalk;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.notify.magic.Property;

/**
 * 钉钉通知模版
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
public class DingTalkProperty extends Property {

    /**
     * 应用agentId
     */
    private String agentId;
    /**
     * 接收者的用户userId列表，最大列表长度：100
     */
    private String userIdList;
    /**
     * 接收者的部门id列表，最大列表长度：20,  接收者是部门id下(包括子部门下)的所有用户
     */
    private String deptIdList;
    /**
     * 是否发送给企业全部用户 true,false
     */
    private boolean toAllUser;
    /**
     * 白名单列表
     */
    private String whiteList;
    /**
     * json字符串
     */
    private String msg;

    /**
     * 钉钉token
     */
    private String token;

}
