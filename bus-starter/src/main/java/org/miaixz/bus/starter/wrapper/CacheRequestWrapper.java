/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.starter.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.EscapeKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

import java.io.*;

/**
 * 跨站攻击/请求包装器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;
    private ServletInputStreamWrapper inputStreamWrapper;

    CacheRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 从ParameterMap获取参数，并保存以便多次获取
        Logger.info(Symbol.DELIM, JsonKit.toJsonString(request.getParameterMap()));
        // 从InputStream获取参数，并保存以便多次获取
        this.body = IoKit.readBytes(request.getInputStream());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(null != this.body ? this.body : Normal.EMPTY_BYTE_ARRAY);
        // 初始 ServletInputStreamWrapper
        this.inputStreamWrapper = new ServletInputStreamWrapper(byteArrayInputStream);
        // 设置 InputStream 到我们自己的包装类中
        this.inputStreamWrapper.setInputStream(byteArrayInputStream);
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public ServletInputStream getInputStream() {
        return this.inputStreamWrapper;
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.inputStreamWrapper));
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (null == values || values.length <= 0) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = values[i];
            if (!JsonKit.isJson(values[i])) {
                encodedValues[i] = EscapeKit.escapeHtml4(values[i]);
            }
        }
        return encodedValues;
    }

    @Override
    public String getParameter(String name) {
        String content = super.getParameter(name);
        if (!JsonKit.isJson(content)) {
            content = EscapeKit.escapeHtml4(content);
        }
        return content;
    }

    @Override
    public String getHeader(String name) {
        String content = super.getHeader(name);
        if (!JsonKit.isJson(content)) {
            content = EscapeKit.escapeHtml4(content);
        }
        return content;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static class ServletInputStreamWrapper extends ServletInputStream {

        private InputStream inputStream;

        @Override
        public boolean isFinished() {
            return true;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }

        @Override
        public int read() throws IOException {
            return this.inputStream.read();
        }
    }

}
