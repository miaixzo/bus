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
package org.aoju.bus.metric.manual;

/**
 * 默认的结果封装类
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8++
 */
public class ApiResult implements Result {

    private String code = Errors.SUCCESS.getCode();
    private String msg;
    private Object data;

    public ApiResult() {
        super();
    }

    public ApiResult(Object data) {
        super();
        this.data = data;
    }

    public static ApiResult success(Object data) {
        ApiResult result = new ApiResult();
        result.setData(data);
        return result;
    }

    public static ApiResult error(ErrorMeta errorMeta) {
        ApiResult result = new ApiResult();
        result.setCode(errorMeta.getCode());
        result.setMsg(errorMeta.getMsg());
        return result;
    }

    public String getCode() {
        return code;
    }

    @Override
    public void setCode(Object code) {
        this.code = String.valueOf(code);
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

}
