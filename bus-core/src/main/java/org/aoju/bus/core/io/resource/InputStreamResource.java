package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.IoUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 基于{@link InputStream}的资源获取器<br>
 * 注意：此对象中getUrl方法始终返回null
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InputStreamResource implements Resource {

    private InputStream in;
    private String name;

    /**
     * 构造
     *
     * @param in {@link InputStream}
     */
    public InputStreamResource(InputStream in) {
        this(in, null);
    }

    /**
     * 构造
     *
     * @param in   {@link InputStream}
     * @param name 资源名称
     */
    public InputStreamResource(InputStream in, String name) {
        this.in = in;
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
        return this.in;
    }

    @Override
    public BufferedReader getReader(Charset charset) {
        return IoUtils.getReader(this.in, charset);
    }

    @Override
    public String readStr(Charset charset) throws CommonException {
        BufferedReader reader = null;
        try {
            reader = getReader(charset);
            return IoUtils.read(reader);
        } finally {
            IoUtils.close(reader);
        }
    }

    @Override
    public String readUtf8Str() throws CommonException {
        return readStr(org.aoju.bus.core.consts.Charset.UTF_8);
    }

    @Override
    public byte[] readBytes() throws CommonException {
        InputStream in = null;
        try {
            in = getStream();
            return IoUtils.readBytes(in);
        } finally {
            IoUtils.close(in);
        }
    }

}
