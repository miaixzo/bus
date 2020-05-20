/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.extra.mail;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ApiBoot Mail Request Entity
 *
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
@Data
@Builder
public class AliMailEntity {

    /**
     * 内容类型
     */
    private ContentType contentType;
    /**
     * 邮件内容
     * Limit 28K
     */
    private String content;
    /**
     * 发送邮件地址
     */
    private List<String> toAddress;
    /**
     * 别名
     */
    private String formAlias;
    /**
     * 主题
     */
    private String subject;
    /**
     * 标签名称
     */
    private String tagName;

    public enum ContentType {
        /**
         * html 内容
         */
        HTML,
        /**
         * text 内容
         */
        TEXT
    }

}
