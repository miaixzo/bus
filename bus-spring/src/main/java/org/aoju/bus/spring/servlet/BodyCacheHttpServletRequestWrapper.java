package org.aoju.bus.spring.servlet;

import org.aoju.bus.core.utils.IoUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * @author aoju.org
 * @version 3.0.1
 * @Group 839128
 * @since JDK 1.8
 */
public class BodyCacheHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final static byte[] DEFAULT_BYTE = new byte[0];
    private byte[] body;
    private ServletInputStreamWrapper inputStreamWrapper;

    BodyCacheHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = IoUtils.readBytes(request.getInputStream());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.body != null ? this.body : DEFAULT_BYTE);
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

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
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