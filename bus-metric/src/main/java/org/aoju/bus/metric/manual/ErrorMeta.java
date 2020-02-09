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
package org.aoju.bus.metric.manual;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.metric.ApiContext;

import java.util.Locale;

/**
 * 错误对象
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8++
 */
public class ErrorMeta implements Error<String> {

    private String isvModule;
    private String code;
    private String msg;

    public ErrorMeta(String isvModule, String code, String msg) {
        super();
        this.isvModule = isvModule;
        this.code = code;
        this.msg = msg;
    }

    public ErrorMeta(String code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIsvModule() {
        return isvModule;
    }

    public void setIsvModule(String isvModule) {
        this.isvModule = isvModule;
    }

    /**
     * @param params i18n属性文件参数。顺序对应文件中的占位符{0},{1}
     * @return 返回exception
     */
    public InstrumentException getException(Object... params) {
        return this.getException(ApiContext.getLocal(), params);
    }

    /**
     * 返回exception，并且附带数据
     *
     * @param data   数据
     * @param params i18n属性文件参数。顺序对应文件中的占位符{0},{1}
     * @return 返回exception
     */
    public InstrumentException getExceptionData(Object data, Object... params) {
        return this.getException(ApiContext.getLocal(), params);
    }

    public InstrumentException getException(Locale locale, Object... params) {
        Error<String> error = ErrorFactory.getError(this, locale, params);
        return new InstrumentException(error.getCode(), error.getMsg());
    }

}
