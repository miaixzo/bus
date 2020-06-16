/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.notify;

import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.magic.Template;

import java.util.Map;

/**
 * 通知器,用于发送通知,如: 短信,邮件,语音,微信等s
 *
 * @author Justubborn
 * @version 6.0.0
 * @since JDK1.8+
 */
public interface Provider<T extends Template> {

    /**
     * 指定模版ID进行发送.
     * 发送失败或者模版不存在将返回
     *
     * @param templateId 模版id
     * @param context    内容
     * @return 结果
     */
    Message send(String templateId, Map<String, String> context);

    /**
     * 指定模版{@link Template}并发送.
     * <p>
     * 注意:不同等服务商使用的模版实现不同.
     *
     * @param template 模版
     * @return 结果
     */
    Message send(T template);

}
