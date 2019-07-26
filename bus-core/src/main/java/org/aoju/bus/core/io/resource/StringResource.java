package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.IoUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 字符串资源，字符串做为资源
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class StringResource implements Resource {

    private String data;
    private String name;
    private Charset charset;

    /**
     * 构造，使用UTF8编码
     *
     * @param data 资源数据
     */
    public StringResource(String data) {
        this(data, null);
    }

    /**
     * 构造，使用UTF8编码
     *
     * @param data 资源数据
     * @param name 资源名称
     */
    public StringResource(String data, String name) {
        this(data, name, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param data    资源数据
     * @param name    资源名称
     * @param charset 编码
     */
    public StringResource(String data, String name, Charset charset) {
        this.data = data;
        this.name = name;
        this.charset = charset;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public InputStream getStream() {
        return new ByteArrayInputStream(readBytes());
    }

    @Override
    public BufferedReader getReader(Charset charset) {
        return IoUtils.getReader(new StringReader(this.data));
    }

    @Override
    public String readStr(Charset charset) throws CommonException {
        return this.data;
    }

    @Override
    public String readUtf8Str() throws CommonException {
        return this.data;
    }

    @Override
    public byte[] readBytes() throws CommonException {
        return this.data.getBytes(this.charset);
    }

}
