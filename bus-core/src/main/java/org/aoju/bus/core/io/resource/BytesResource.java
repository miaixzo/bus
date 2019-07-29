package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 基于byte[]的资源获取器<br>
 * 注意：此对象中getUrl方法始终返回null
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BytesResource implements Resource {

    private byte[] bytes;
    private String name;

    /**
     * 构造
     *
     * @param bytes 字节数组
     */
    public BytesResource(byte[] bytes) {
        this(bytes, null);
    }

    /**
     * 构造
     *
     * @param bytes 字节数组
     * @param name  资源名称
     */
    public BytesResource(byte[] bytes, String name) {
        this.bytes = bytes;
        this.name = name;
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
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public BufferedReader getReader(Charset charset) {
        return new BufferedReader(new StringReader(readStr(charset)));
    }

    @Override
    public String readStr(Charset charset) throws InstrumentException {
        return StringUtils.str(this.bytes, charset);
    }

    @Override
    public String readUtf8Str() throws InstrumentException {
        return readStr(org.aoju.bus.core.consts.Charset.UTF_8);
    }

    @Override
    public byte[] readBytes() throws InstrumentException {
        return this.bytes;
    }

}
