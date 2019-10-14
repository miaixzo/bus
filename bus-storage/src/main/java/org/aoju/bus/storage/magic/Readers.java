/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.storage.magic;


import org.aoju.bus.core.utils.StringUtils;

/**
 * 响应结果
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public class Readers {

    /**
     * 请求返回码,正确为 0
     */
    public String errcode;

    /**
     * 请求返回消息
     */
    public String errmsg;

    /**
     * 请求返回数据
     */
    public Object data;

    public Readers(String data) {
        this("0", data, null);
    }

    public Readers(Object data) {
        this("0", "", data);
    }

    public Readers(String errcode, String errmsg) {
        this(errcode, StringUtils.isEmpty(errcode) ? "-1" : errmsg, null);
    }

    public Readers(String errcode, String errmsg, Object data) {
        this.errmsg = errmsg;
        this.errcode = errcode;
        this.data = data;
    }

}
